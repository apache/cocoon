/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.language.markup.xsp.XSPModuleHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.helpers.VariableConfiguration;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/** 
 * Rewrites URIs in links to a value determined by an InputModule.
 * The URI scheme identifies the InputModule to use, and the rest of the URI is
 * used as the attribute name.
 * <h3>Example</h3>
 * For instance, if we had an {@link
 * org.apache.cocoon.components.modules.input.XMLFileModule}, configured to
 * read values from an XML file:
 * <pre>
 * &lt;site>
 *   &lt;faq>
 *     &lt;how_to_boil_eggs href="faq/eggs.html"/>
 *   &lt;/faq>
 * &lt;/site>
 * </pre>
 * mapped to the prefix 'site:', then &lt;link
 * href="site:/site/faq/how_to_boil_eggs/@href"> would be replaced with
 * &lt;link href="faq/eggs.html"&gt;
 * <p>
 * InputModules are configured twice; first statically in
 * <code>cocoon.xconf</code>, and then dynamically at runtime, with dynamic
 * configuration (if any) taking precedence.  LinkRewriterTransformer allows
 * you to pass a dynamic configuration to used InputModules as follows.
 * <p>
 * First, a template Configuration is specified in the static
 * &lt;map:components> block of the sitemap:
 * <pre>
 *  &lt;map:transformer name="linkrewriter"
 *    src="org.apache.cocoon.transformation.LinkRewriterTransformer">
 *    &lt;link-attrs>href src&lt;/link-attrs>
 *    &lt;schemes>site ext&lt;/schemes>
 *    &lt;input-module name="site">
 *      &lt;file src="cocoon://samples/link/linkmap" reloadable="true"/>
 *    &lt;/input-module>
 *    &lt;input-module name="mapper">
 *      &lt;input-module name="site">
 *        &lt;file src="{src}" reloadable="true"/>
 *      &lt;/input-module>
 *      &lt;prefix>/site/&lt;/prefix>
 *      &lt;suffix>/@href&lt;/suffix>
 *    &lt;/input-module>
 *  &lt;/map:transformer>
 * </pre>
 * Here, we have first configured which attributes to examine, and which URL
 * schemes to consider rewriting.  In this example, &lt;a href="site:index"> would
 * be processed.  See below for more configuration options.
 * Then, we have established dynamic configuration templates for two modules,
 * 'site' (an {@link org.apache.cocoon.components.modules.input.XMLFileModule}
 * and 'mapper' (A {@link
 * org.apache.cocoon.components.modules.input.SimpleMappingMetaModule}.  All
 * other InputModules will use their static configs.  Note that, when
 * configuring a Meta InputModule like 'mapper', we need to also configure the
 * 'inner' module (here, 'site') with a nested &lt;input-module>.
 * <p>
 * There is one further twist; to have <em>really</em> dynamic configuration,
 * we need information available only when the transformer actually runs.  This
 * is why the above config was called a "template" Configuration; it needs to
 * be 'instantiated' and provided extra info, namely:
 * <ul>
 *  <li>The {src} string will be replaced with the map:transform @src attribute value.
 *  <li>Any other {variables} will be replaced with map:parameter values
 * </ul>
 * With the above config template, we can have a matcher like:
 *
 * <pre>
 *    &lt;map:match pattern="**welcome">
 *      &lt;map:generate src="index.xml"/>
 *      &lt;map:transform type="linkrewriter" src="cocoon:/{1}linkmap"/>
 *      &lt;map:serialize type="xml"/>
 *    &lt;/map:match>
 * </pre>
 *
 * Which would cause the 'mapper' XMLFileModule to be configured with a
 * different XML file, depending on the request.
 * <p>
 * Similarly, we could use a dynamic prefix:
 * <pre>
 *      &lt;prefix>{prefix}&lt;/prefix>
 * </pre>
 * in the template config, and:
 * <pre>
 *   &lt;map:parameter name="prefix" value="/site/"/>
 * </pre>
 * in the map:transform
 * <p>
 * A live example of LinkRewriterTransformer can be found in the <a
 * href="http://xml.apache.org/forrest/">Apache Forrest</a> sitemap.
 * <p>
 *
 * <h3>Configuration</h3>
 * <p>
 * The following map:parameter's and map:transformer parameters are recognised:
 * <dl>
 *  <dt>link-attrs</dt>
 *  <dd>Space-separated list of attributes to consider links (to be
 *  transformed). Defaults to 'href'.</dd>
 *  <dt>schemes</dt>
 *  <dd>Space-separated list of URI schemes to explicitly include.  If specified, all URIs with unlisted schemes will not be converted.</dd>
 *  <dt>exclude-schemes</dt>
 *  <dd>Space-separated list of URI schemes to explicitly exclude. Defaults to 'http https ftp news mailto'.</dd>
 *  <dt>bad-link-str</dt>
 *  <dd>String to use for links with a correct InputModule prefix, but no value
 *  therein.  Defaults to the original URI.</dd>
 * </dl>
 *
 * <p>
 * Note that currently, only links in the default ("") namespace are converted.
 *
 * @author <a href="mailto:jefft@apache.org">Jeff Turner</a>
 * @version CVS $Id: LinkRewriterTransformer.java,v 1.8 2003/09/24 22:04:40 cziegeler Exp $
 */
public class LinkRewriterTransformer
    extends AbstractSAXTransformer implements Initializable, Configurable
{

    private static String NAMESPACE="";

    /** A list of attributes considered 'links' */
    private Set linkAttrs;

    /** List containing schemes (protocols) of links to log */
    private Set inSchemes;
    private Set outSchemes;

    /** Configuration passed to the component once through configure(). */
    private Configuration origConf; 

    /** Derivation of origConf with variables obtained from setup() parameters.
     * Recreated once per invocation. */
    private Configuration conf; 

    private XSPModuleHelper modHelper;

    private String badLinkStr;

    /**
     * Configure this component from the map:transformer block.  Called before
     * initialization and setup.
     */
    public void configure(Configuration conf)
        throws ConfigurationException {
        if (conf == null) throw new NullPointerException("No static configuration passed to LinkRewriter");
        this.origConf = conf;
    }
 
    /**
     * Initiate resources prior to this component becoming active.
     */
    public void initialize() throws Exception {
        this.namespaceURI = NAMESPACE;
        this.modHelper = new XSPModuleHelper();
        modHelper.setup(this.manager);
    }

    /**
     * Setup this component to handle a map:transform instance.
     */
    public void setup(SourceResolver resolver, Map objectModel,
            String src, Parameters parameters)
        throws ProcessingException, SAXException, IOException
    {
        super.setup(resolver, objectModel, src, parameters);
        this.badLinkStr = parameters.getParameter("bad-link-str",    // per-request config
                origConf.getChild("bad-link-str"). // else fall back to per-instance config
                getValue(null)                     // else use hardcoded default
                );
        this.linkAttrs = split(parameters.getParameter("link-attrs",
                    origConf.getChild("link-attrs").
                    getValue("href")
                    ), " ");
        this.inSchemes = split(parameters.getParameter("schemes",
                    origConf.getChild("schemes").
                    getValue("")
                    ), " ");
        this.outSchemes = split(parameters.getParameter("exclude-schemes",
                    origConf.getChild("exclude-schemes").
                    getValue("http https ftp news mailto")
                    ), " ");
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("bad-link-str = "+badLinkStr);
            getLogger().debug("link-attrs = "+linkAttrs);
            getLogger().debug("schemes = "+inSchemes);
            getLogger().debug("exclude-schemes = "+outSchemes);
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Will ignore the following schemes: " + outSchemes);
        }
        // Generate conf
        VariableConfiguration varConf = new VariableConfiguration(this.origConf);
        varConf.addVariable("src", src);
        varConf.addVariables(parameters);
        try {
            this.conf = varConf.getConfiguration();
        } catch (ConfigurationException ce) {
            throw new ProcessingException("Couldn't create dynamic config ", ce);
        }
    }

    /** Recycle this component for use in another map:transform. */
    public void recycle() {
        super.recycle();
        this.resolver = null;
        this.linkAttrs = null;
        this.inSchemes = null;
        this.outSchemes = null;
        this.conf = null;
        // Note: configure() and initialize() are not called after every
        //recycle, so don't null origConf
    }

    /** Split a string into a Set of strings.
     * @param str String to split
     * @param delim Delimiter character
     * @return A Set of strings in 'str'
     */
    private Set split(String str, String delim) {
        if (str == null) return null;
        Set schemes = new HashSet();
        StringTokenizer st = new StringTokenizer(str, delim);
        while (st.hasMoreTokens()) {
            String pfx = st.nextToken();
            schemes.add(pfx);
        }
        return schemes;
    }


    /**
     * Start processing elements of our namespace.
     * This hook is invoked for each sax event with our namespace.
     * @param uri The namespace of the element.
     * @param name The local name of the element.
     * @param raw The qualified name of the element.
     * @param attr The attributes of the element.
     */
    public void startTransformingElement(String uri,
            String name,
            String raw,
            Attributes attr)
        throws ProcessingException, IOException, SAXException 
    {
        Attributes newAttrs = null;
        boolean matched = false;

        Iterator iter = linkAttrs.iterator();
        while (iter.hasNext()) {
            int attrIdx = attr.getIndex((String)iter.next());
            if (attrIdx != -1) {
                String oldAttr = attr.getValue(attrIdx);
                int i = oldAttr.indexOf(":");
                if (i != -1) {
                    String scheme = oldAttr.substring(0, i);
                    String addr = oldAttr.substring(i+1);
                    if (outSchemes.contains(scheme)) {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("Ignoring link '"+scheme+":"+addr+"'");
                        }
                    } else if (inSchemes.contains(scheme)) {
                        matched = true;
                        newAttrs = getLinkAttr(attr, attrIdx, scheme, addr);
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("Converted link '"+oldAttr+"' to '"+newAttrs.getValue(attrIdx)+"'");
                        }
                    } else {
                        if (inSchemes.size() == 0) {
                            // If the link wasn't deliberately excluded from a
                            // list of 'good' links, then include it.
                            matched = true;
                            newAttrs = getLinkAttr(attr, attrIdx, scheme, addr);
                            getLogger().debug("Converted link '"+oldAttr+"' to '"+newAttrs.getValue(attrIdx)+"'");
                        }
                    }
                }
            }
        }
        if (matched) {
            super.startTransformingElement(uri, name, raw, newAttrs);
        } else {
            super.startTransformingElement(uri, name, raw, attr);
        }
    }

    /**
     * Rewrite link in a set of attributes.
     *
     * @param oldAttrs Attributes containing unconverted link.
     * @param linkIndex index of link to convert
     * @param scheme URI scheme (indicating InputModule) of link
     * @param addr URI scheme of link
     * @return an Attributes based on <code>oldAttrs</code>, but with one attribute rewritten.
     */
    private Attributes getLinkAttr(Attributes oldAttrs, int linkIndex, String scheme, String addr) {
        AttributesImpl newAttrs = new AttributesImpl(oldAttrs);
        try {
            String modValue = (String)modHelper.getAttribute(this.objectModel, getConf(scheme), scheme, addr, (badLinkStr!=null?badLinkStr:scheme+":"+addr));
            newAttrs.setValue(linkIndex, modValue);
        } catch (org.apache.avalon.framework.CascadingRuntimeException e) {
            // Rethrow Configuration errors
            if (e.getCause() instanceof ConfigurationException) throw e;

            // Swallow IM errors, usually prefixes like 'telnet' that aren't
            // bound to an InputModule. These should really be declared in
            // 'exclude-schemes', hence the 'error' classification of this log.
            getLogger().error("Error rewriting link '"+scheme+":"+addr+"': "+e.getMessage());
        }
        return newAttrs;
    }

    /**
     * Retrieve a dynamic Configuration for a specific InputModule.
     * @param scheme InputModule name
     * @return Configuration for specified scheme, from the map:transformer block.
     */
    private Configuration getConf(String scheme) {
        Configuration[] schemeConfs = this.conf.getChildren("input-module");
        for (int i=0; i<schemeConfs.length; i++) {
            if (scheme.equals(schemeConfs[i].getAttribute("name", null))) {
                return schemeConfs[i];
            }
        }
        return null;
    }

}
