/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.language.markup.xsp.XSPModuleHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.helpers.VariableConfiguration;
import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;
import org.apache.regexp.RESyntaxException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Rewrites URIs in links to a value determined by an InputModule.
 * The URI scheme identifies the InputModule to use, and the rest of the URI is
 * used as the attribute name.
 *
 * <h3>Example</h3>
 * <p>For instance, if we had an {@link
 * org.apache.cocoon.components.modules.input.XMLFileModule}, configured to
 * read values from an XML file:
 * <pre>
 * &lt;site&gt;
 *   &lt;faq&gt;
 *     &lt;how_to_boil_eggs href="faq/eggs.html"/&gt;
 *   &lt;/faq&gt;
 * &lt;/site&gt;
 * </pre>
 *
 * mapped to the prefix 'site:', then <code>&lt;link
 * href="site:/site/faq/how_to_boil_eggs/@href"&gt;</code> would be replaced
 * with <code>&lt;link href="faq/eggs.html"&gt;</code>
 *
 * <h3>InputModule Configuration</h3>
 * <p>InputModules are configured twice; first statically in
 * <code>cocoon.xconf</code>, and then dynamically at runtime, with dynamic
 * configuration (if any) taking precedence. Transformer allows
 * you to pass a dynamic configuration to used InputModules as follows.
 *
 * <p>First, a template Configuration is specified in the static
 * &lt;map:components&gt; block of the sitemap within &lt;input-module&gt; tags:
 * <pre>
 *  &lt;map:transformer name="linkrewriter"
 *      src="org.apache.cocoon.transformation.LinkRewriterTransformer"&gt;
 *    &lt;link-attrs&gt;href src&lt;/link-attrs&gt;
 *    &lt;schemes&gt;site ext&lt;/schemes&gt;
 *    &lt;input-module name="site"&gt;
 *      &lt;file src="cocoon://samples/link/linkmap" reloadable="true"/&gt;
 *    &lt;/input-module&gt;
 *    &lt;input-module name="mapper"&gt;
 *      &lt;input-module name="site"&gt;
 *        &lt;file src="{src}" reloadable="true"/&gt;
 *      &lt;/input-module&gt;
 *      &lt;prefix&gt;/site/&lt;/prefix&gt;
 *      &lt;suffix&gt;/@href&lt;/suffix&gt;
 *    &lt;/input-module&gt;
 *  &lt;/map:transformer&gt;
 * </pre>
 *
 * Here, we have first configured which attributes to examine, and which URL
 * schemes to consider rewriting. In this example, &lt;a href="site:index"&gt;
 * would be processed. See below for more configuration options.
 *
 * <p>Then, we have established dynamic configuration templates for two modules,
 * 'site' (an {@link org.apache.cocoon.components.modules.input.XMLFileModule}
 * and 'mapper' (A {@link
 * org.apache.cocoon.components.modules.input.SimpleMappingMetaModule}.  All
 * other InputModules will use their static configs. Note that, when
 * configuring a meta InputModule like 'mapper', we need to also configure the
 * 'inner' module (here, 'site') with a nested &lt;input-module&gt;.
 *
 * <p>There is one further twist; to have <em>really</em> dynamic configuration,
 * we need information available only when the transformer actually runs. This
 * is why the above config was called a "template" configuration; it needs to
 * be 'instantiated' and provided extra info, namely:
 * <ul>
 *  <li>The {src} string will be replaced with the map:transform @src attribute value.
 *  <li>Any other {variables} will be replaced with map:parameter values
 * </ul>
 *
 * With the above config template, we can have a matcher like:
 *
 * <pre>
 *    &lt;map:match pattern="**welcome"&gt;
 *      &lt;map:generate src="index.xml"/&gt;
 *      &lt;map:transform type="linkrewriter" src="cocoon:/{1}linkmap"/&gt;
 *      &lt;map:serialize type="xml"/&gt;
 *    &lt;/map:match&gt;
 * </pre>
 *
 * Which would cause the 'mapper' XMLFileModule to be configured with a
 * different XML file, depending on the request.
 *
 * <p>Similarly, we could use a dynamic prefix:
 * <pre>
 *   &lt;prefix&gt;{prefix}&lt;/prefix&gt;
 * </pre>
 * in the template config, and:
 * <pre>
 *   &lt;map:parameter name="prefix" value="/site/"/&gt;
 * </pre>
 * in the map:transform
 *
 * <p>A live example of LinkRewriterTransformer can be found in the <a
 * href="http://xml.apache.org/forrest/">Apache Forrest</a> sitemap.
 *
 * <h3>Transformer Configuration</h3>
 * <p>
 * The following configuration entries in map:transformer block are recognised:
 * <dl>
 *   <dt>link-attrs</dt>
 *   <dd>Space-separated list of attributes to consider links (to be
 *   transformed). The whole value of the attribute is considered link and
 *   transformed.</dd>
 *
 *   <dt>link-attr</dt>
 *   <dd>0..n of these elements each specify an attribute containing link(s)
 *   (to be transformed) and optionally a regular expression to locate
 *   substring(s) of the attribute value considered link(s). Has two
 *   attributes:
 *     <dl>
 *       <dt>name</dt>
 *       <dd>(required) name of the attribute whose value contains link(s).</dd>
 *       <dt>pattern</dt>
 *       <dd>(optional) regular expression such that when matched against the
 *       attribute value, all parenthesized expressions (except number 0) will
 *       be considered links that should be transformed. If absent, the whole value
 *       of the attribute is considered to be a link, as if the attribute was
 *       included in 'link-attrs'.</dd>
 *     </dl>
 *   </dd>
 *
 *   <dt>schemes</dt>
 *   <dd>Space-separated list of URI schemes to explicitly include.
 *   If specified, all URIs with unlisted schemes will <i>not</i> be converted.</dd>
 *
 *   <dt>exclude-schemes</dt>
 *   <dd>Space-separated list of URI schemes to explicitly exclude.
 *   Defaults to 'http https ftp news mailto'.</dd>
 *
 *   <dt>bad-link-str</dt>
 *   <dd>String to use for links with a correct InputModule prefix, but no value
 *   therein.  Defaults to the original URI.</dd>
 * </dl>
 *
 * <p>
 * The attributes considered to contain links are a <em>set</em> of the attributes
 * specified in 'link-attrs' element and all 'link-attr' elements. Each attribute
 * should be specified only once either in 'link-attrs' or 'link-attr'; i.e. an
 * attribute can have at most 1 regular expression associated with it. If neither
 * 'link-attrs' nor 'link-attr' configuration is present, defaults to 'href'.
 *
 * <p>Below is an example of regular expression usage that will transform links
 * <code>x1</code> and <code>x2</code> in
 * <code>&lt;action target="foo url(x1) bar url(x2)"/&gt;</code>:
 *
 * <pre>
 *   &lt;map:transformer name="linkrewriter"
 *       src="org.apache.cocoon.transformation.LinkRewriterTransformer"&gt;
 *     &lt;link-attr name="target" pattern="(?:url\((.*?)\).*?){1,2}$"/&gt;
 *     &lt;!-- additional configuration ... --&gt;
 *   &lt;/map:transformer&gt;
 * </pre>
 *
 * <p>
 * When matched against the value of <code>target</code> attribute above,
 * the parenthesized expressions are:<br/>
 * <samp>
 *   $0 = url(x1) bar url(x2)<br/>
 *   $1 = x1<br/>
 *   $2 = x2<br/>
 * </samp>
 *
 * <p>
 * Expression number 0 is always discarded by the transformer and the rest
 * are considered links and re-written.
 *
 * <p>If present, map:parameter's from the map:transform block override the
 * corresponding configuration entries from map:transformer. As an exception,
 * 'link-attr' parameters are not recognised; 'link-attrs' parameter overrides
 * both 'link-attrs' and 'link-attr' configuration.
 *
 * <p>
 * <b>NOTE:</b> Currently, only links in the default ("") namespace are converted.
 *
 * @version CVS $Id: LinkRewriterTransformer.java,v 1.12 2004/04/22 12:15:48 vgritsenko Exp $
 */
public class LinkRewriterTransformer
    extends AbstractSAXTransformer
    implements Initializable, Disposable {

    private final static String NAMESPACE = "";

    /**
     * A guardian object denoting absense of regexp pattern for a given
     * attribute. Used as value in linkAttrs and origLinkAttrs maps.
     */
    private final static Object NO_REGEXP = new Object();

    //
    // Configure()'d parameters
    //

    /** Configuration passed to the component once through configure(). */
    private Configuration origConf;

    private String origBadLinkStr;
    private String origInSchemes;
    private String origOutSchemes;

    /**
     * A map where keys are those attributes which are considered 'links'.
     * Obtained from configuration passed to the component once through
     * the configure() method.
     *
     * <p>Map contains NO_REGEXP object for attributes whose whole values are
     * considered links, or compiled RE expressions for attributes whose values
     * might contain a link.
     */
    private Map origLinkAttrs;

    //
    // Setup()'d parameters
    //

    /**
     * Derivation of origConf with variables obtained from setup() parameters.
     * Recreated once per invocation.
     */
    private Configuration conf;

    /**
     * String to use for links with a correct InputModule prefix, but no value
     * therein.
     */
    private String badLinkStr;

    /** Set containing schemes (protocols) of links to process */
    private Set inSchemes;

    /** Set containing schemes (protocols) of links to exclude from processing */
    private Set outSchemes;

    /**
     * A map of attributes considered 'links' and corresponding RE expression
     * or NO_REGEXP object. Recreated once per invocation or copied from
     * origLinkAttrs based on setup() method parameters.
     */
    private Map linkAttrs;

    private XSPModuleHelper modHelper;


    /**
     * Configure this component from the map:transformer block.  Called before
     * initialization and setup.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);

        this.origConf = conf;
        this.origBadLinkStr = conf.getChild("bad-link-str").getValue(null);
        this.origInSchemes = conf.getChild("schemes").getValue("");
        this.origOutSchemes = conf.getChild("exclude-schemes").getValue("http https ftp news mailto");

        /*
         * Setup origLinkAttrs map from the original Configuration:
         * 1. Parse link-attrs Configuration
         * 2. Process link-attr Children, warn if overwriting
         * 3. If no link-attrs, and no link-attr are available, defaults to "href"
         */

        String linkAttrsValue = conf.getChild("link-attrs").getValue("");
        this.origLinkAttrs = split(linkAttrsValue, " ", NO_REGEXP);

        Configuration[] attrConfs = conf.getChildren("link-attr");
        if (attrConfs.length > 0) {
            RECompiler compiler = new RECompiler();
            for (int i = 0; i < attrConfs.length; i++) {
                String attr = attrConfs[i].getAttribute("name");
                if (getLogger().isWarnEnabled() && origLinkAttrs.containsKey(attr)) {
                    getLogger().warn("Duplicate configuration entry found for attribute '" +
                                     attr + "', overwriting previous configuration");
                }

                String pattern = attrConfs[i].getAttribute("pattern", null);
                if (pattern == null) {
                    this.origLinkAttrs.put(attr, NO_REGEXP);
                } else {
                    try {
                        this.origLinkAttrs.put(attr, compiler.compile(pattern));
                    } catch (RESyntaxException e) {
                        String msg = "Invalid regexp pattern '" + pattern + "' specified for attribute '" + attr + "'";
                        throw new ConfigurationException(msg, attrConfs[i], e);
                    }
                }
            }
        }

        // If nothing configured, default to href attribute
        if (this.origLinkAttrs.size() == 0) {
            this.origLinkAttrs.put("href", NO_REGEXP);
        }
    }

    /**
     * Initiate resources prior to this component becoming active.
     */
    public void initialize() throws Exception {
        this.namespaceURI = NAMESPACE;
        this.modHelper = new XSPModuleHelper();
        this.modHelper.setup(this.manager);
    }

    /**
     * Setup this component to handle a map:transform instance.
     */
    public void setup(SourceResolver resolver,
                      Map objectModel,
                      String src,
                      Parameters parameters)
            throws ProcessingException, SAXException, IOException {
        super.setup(resolver, objectModel, src, parameters);

        this.badLinkStr = parameters.getParameter("bad-link-str",       // per-request config
                                                  this.origBadLinkStr); // else fall back to per-instance config

        this.inSchemes = split(parameters.getParameter("schemes", this.origInSchemes), " ");
        this.outSchemes = split(parameters.getParameter("exclude-schemes", this.origOutSchemes), " ");

        this.linkAttrs = this.origLinkAttrs;
        if (parameters.isParameter("link-attrs")) {
            try {
                this.linkAttrs = split(parameters.getParameter("link-attrs"), " ", NO_REGEXP);
            } catch (ParameterException ex) {
                // shouldn't happen
            }
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("bad-link-str = " + badLinkStr);
            getLogger().debug("link-attrs = " + linkAttrs);
            getLogger().debug("schemes = " + inSchemes);
            getLogger().debug("exclude-schemes = " + outSchemes);
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

        // Note: configure() and initialize() are not called after every
        //       recycle, so don't null origConf, origLinkAttrs, etc.
        this.conf = null;
        this.badLinkStr = null;
        this.linkAttrs = null;
        this.inSchemes = null;
        this.outSchemes = null;
    }

    /**
     * Split a string into a Set of strings.
     *
     * @param str String to split
     * @param delim Delimiter character
     * @return A Set of strings in 'str'
     */
    private Set split(String str, String delim) {
        if (str == null) {
            return null;
        }

        Set tokens = new HashSet();
        StringTokenizer st = new StringTokenizer(str, delim);
        while (st.hasMoreTokens()) {
            tokens.add(st.nextToken());
        }
        return tokens;
    }

    /**
     * Split a string and create a Map where keys are the tokens from the string.
     *
     * @param str String to split
     * @param delim Delimiter character
     * @param valueObj Object to insert in the Map (may be null)
     * @return A Map of strings in 'str'
     */
    private Map split(String str, String delim, Object valueObj) {
        if (str == null) {
            return null;
        }

        // valueObj may be null, because HashMap permits null values
        Map schemes = new HashMap();
        StringTokenizer st = new StringTokenizer(str, delim);
        while (st.hasMoreTokens()) {
            String pfx = st.nextToken();
            if (schemes.containsKey(pfx) && getLogger().isWarnEnabled()) {
                getLogger().warn("Duplicate configuration entry found for attribute '" +
                                 pfx + "', overwriting previous configuration");
            }
            schemes.put(pfx, valueObj);
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
            throws ProcessingException, IOException, SAXException {
        boolean matched = false;

        for (int attrIdx = 0; attrIdx < attr.getLength(); attrIdx++) {
            String attrName = attr.getQName(attrIdx);

            String attrValue = createTransformedAttr(attrName, attr.getValue(attrIdx));
            if (attrValue != null) {
                if (!matched) {
                    attr = new AttributesImpl(attr);
                    matched = true;
                }
                ((AttributesImpl) attr).setValue(attrIdx, attrValue);
            }
        }
        super.startTransformingElement(uri, name, raw, attr);
    }

    /**
     * Rewrite set of links in an attribute.
     *
     * @param attrName QName of the attribute containing unconverted link(s).
     * @param oldAttrValue value of the attribute containing unconverted link(s).
     * @return new value of the attribute based on <code>oldAttrValue</code>, but with link(s) rewritten. If not
     * modified, returns null (for example, if attribute not found in <code>linkAttrs</code> or not matched to
     * regexp pattern).
     */
    private String createTransformedAttr(
        String attrName,
        String oldAttrValue) {
        if (!linkAttrs.containsKey(attrName)) {
            return null;
        }

        String newAttrValue = null;
        Object reProgram = linkAttrs.get(attrName);
        if (reProgram == NO_REGEXP) {
            newAttrValue = createTransformedLink(oldAttrValue);
        } else {
            // must be instanceof REProgram
            RE r = new RE((REProgram) reProgram);
            if (r.match(oldAttrValue)) {
                StringBuffer bufOut = new StringBuffer(oldAttrValue);
                int offset = 0;
                String link = null;
                String newLink = null;
                boolean modified = false;

                // skip the first paren
                for (int i = 1; i < r.getParenCount(); i++) {
                    link = r.getParen(i);
                    newLink = createTransformedLink(link);
                    if (newLink != null) {
                        bufOut.replace(r.getParenStart(i) + offset,
                                       r.getParenEnd(i) + offset,
                                       newLink);
                        offset += newLink.length() - r.getParenLength(i);
                        modified = true;
                    }
                }
                if (modified) {
                    newAttrValue = bufOut.toString();
                }
            }
        }

        return newAttrValue;
    }

    /**
     * Rewrite a link - use InputModule to obtain new value for the link based on <code>oldLink</code>.
     *
     * @param oldLink value of the unconverted link.
     * @return new value of the link. If not modified, returns null (for example, if link scheme
     * is in <code>outSchemes</code>.
     */
    private String createTransformedLink(String oldLink) {
        String newLink = null;
        int i = oldLink.indexOf(":");
        if (i != -1) {
            String scheme = oldLink.substring(0, i);
            String addr = oldLink.substring(i + 1);
            if (outSchemes.contains(scheme)) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Ignoring link '" + oldLink + "'");
                }
            } else if (inSchemes.contains(scheme) || inSchemes.size() == 0) {
                // If the link wasn't deliberately excluded from a
                // list of 'good' links, then include it.
                try {
                    newLink = (String) modHelper.getAttribute(this.objectModel,
                                                              getConf(scheme),
                                                              scheme,
                                                              addr,
                                                              (badLinkStr != null? badLinkStr: scheme + ":" + addr));
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Converted link '" + oldLink + "' to '" + newLink + "'");
                    }
                } catch (org.apache.avalon.framework.CascadingRuntimeException e) {
                    // Rethrow Configuration errors
                    if (e.getCause() instanceof ConfigurationException) {
                        throw e;
                    }

                    // Swallow IM errors, usually prefixes like 'telnet' that aren't
                    // bound to an InputModule. These should really be declared in
                    // 'exclude-schemes', hence the 'error' classification of this log.
                    if (getLogger().isErrorEnabled()) {
                        getLogger().error("Error rewriting link '" + oldLink + "': " +
                                          e.getMessage());
                    }
                }
            }
        }
        return newLink;
    }

    /**
     * Retrieve a dynamic configuration for a specific InputModule.
     *
     * @param scheme InputModule name
     * @return Configuration for specified scheme, from the map:transformer block.
     */
    private Configuration getConf(String scheme) {
        Configuration[] schemeConfs = this.conf.getChildren("input-module");
        for (int i = 0; i < schemeConfs.length; i++) {
            if (scheme.equals(schemeConfs[i].getAttribute("name", null))) {
                return schemeConfs[i];
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.modHelper != null) {
            this.modHelper.releaseAll();
            this.modHelper = null;
        }
    }
}
