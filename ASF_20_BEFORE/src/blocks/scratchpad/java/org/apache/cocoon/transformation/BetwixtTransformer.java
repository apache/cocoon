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

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.commons.betwixt.XMLIntrospector;
import org.apache.commons.betwixt.io.SAXBeanWriter;
import org.apache.commons.betwixt.strategy.ClassNormalizer;
import org.apache.commons.logging.impl.LogKitLogger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Betwixt transformer marshals a object from the Sitemap, Session, Request or
 * the Conext into a series of SAX events.
 *
 * Configuation: The betwixt transformer can be configured to not output element
 * reference ids. The default setting is to output reference IDs.
 * <pre>
 *   &lt;map:transformer name="betwixt" src="org.apache.cocoon.transformation.BetwixtTransformer"
 *     &lt;ref-ids&gt;true&lt;/ref-ids&gt;
 *   &lt;/map:transformer&gt;
 * </pre>
 *
 * A sample for the use:
 * <pre>
 *   &lt;root xmlns:betwixt="http://apache.org/cocoon/betwixt/1.0"&gt;
 *     &lt;betwixt:include name="invoice" /&gt;
 *     &lt;betwixt:include name="product" scope="sitemap" /&gt;
 *     &lt;betwixt:include name="product2" element="other-product" /&gt;
 *   &lt;/root&gt;
 * </pre>
 * The <code>BetwixtTransfomer</code> support only one Element <code>betwixt:include</code>.
 * This element is replaced with the marshalled object. The Object given through the
 * attribute <code>name</code> will be searched in the <code>request</code>, <code>session</code>,
 * <code>context</code> and at least in <code>sitemap</code>.
 * If the scope is explicitly given, the object will ge located only there.
 * The attribute <code>element</code> can be given to specify an alternativ
 * root element for the object. Collections are marshalled by marshalling
 * each object it contains.
 *
 * @see <a href="http://jakarta.apache.org/commons/betwixt/">Betwixt Projekt Homepage</a>
 * @author <a href="mailto:cgaffga@triplemind.com">Christoph Gaffga</a>
 */
public class BetwixtTransformer
    extends AbstractTransformer
    implements Configurable {
    private static final String BETWIXT_NSURI =
        "http://apache.org/cocoon/betwixt/1.0";

    private final static String CMD_INCLUDE   = "include";
    private final static String ATTR_NAME     = "name";
    private final static String ATTR_SCOPE    = "scope";
    private final static String SCOPE_SITEMAP = "sitemap";
    private final static String SCOPE_SESSION = "session";
    private final static String SCOPE_REQUEST = "request";
    private final static String SCOPE_CONTEXT = "context";
    private final static String ATTR_ELEMENT  = "element";
    private final static String CONF_REFIDS   = "ref-ids";

    // Introspector to share XMLBeanInfo cache
    private static XMLIntrospector introspector;

    private boolean refIds = true;

    private Map objectModel = null;
    private boolean inBetwixtElement = false;
    private SAXBeanWriter beanWriter = null;

    public void configure(Configuration conf) throws ConfigurationException {
        final String refIds = conf.getChild(CONF_REFIDS).getValue("true");
        this.refIds = "false".equals(refIds) || "no".equals(refIds);
    }

    public void setup( SourceResolver resolver, Map objectModel, String src, Parameters par) {
        this.objectModel = objectModel;
    }

    public void recycle() {
        this.objectModel = null;
        this.inBetwixtElement = false;
    }

    public void endElement(String uri, String name, String raw)
        throws SAXException {
        if (BETWIXT_NSURI.equals(uri)) {
            this.inBetwixtElement = false;
        } else {
            super.endElement(uri, name, raw);
        }
    }

    public void startElement( String uri, String name, String raw, Attributes attr)
        throws SAXException {
            
        if (BETWIXT_NSURI.equals(uri)) {
            this.inBetwixtElement = true;
            process(name, attr);
        } else {
            super.startElement(uri, name, raw, attr);
        }
    }

    public void characters(char[] ch, int start, int len) throws SAXException {
        if (!this.inBetwixtElement) {
            super.characters(ch, start, len);
        }
    }

    private void process(String command, Attributes attr) throws SAXException {
        if (CMD_INCLUDE.equals(command)) {
            final String scope = attr.getValue(ATTR_SCOPE);
            final String name = attr.getValue(ATTR_NAME);
            if (name == null) {
                throw new SAXException(
                    "Required attribute name is missing on element "
                        + CMD_INCLUDE);
            }

            final String element = attr.getValue(ATTR_ELEMENT);
            /* (RP) element is optional, isn't it?
            if (element == null) {
                throw new SAXException(
                    "Attribute " + ATTR_ELEMENT + " can not be empty");
            }
            */

            Object bean = null;
            if (scope == null || SCOPE_REQUEST.equals(scope)) {
                final Request request =
                    ObjectModelHelper.getRequest(objectModel);
                bean = request.getAttribute(name);
            }
            if ((scope == null && bean == null)
                || SCOPE_SESSION.equals(scope)) {
                final Session session =
                    ObjectModelHelper.getRequest(objectModel).getSession(false);
                if (session != null) {
                    bean = session.getAttribute(name);
                }
            }
            if ((scope == null && bean == null)
                || SCOPE_CONTEXT.equals(scope)) {
                final Context context =
                    ObjectModelHelper.getContext(objectModel);
                if (context != null) {
                    bean = context.getAttribute(name);
                }
            }
            if ((scope == null && bean == null)
                || SCOPE_SITEMAP.equals(scope)) {
                bean = objectModel.get(name);
            }

            if (bean != null) {
                includeBean(name, bean, element);
            } else {
                getLogger().warn("Bean " + name + " could not be found");
            }
        } else {
            throw new SAXException("Unknown command: " + command);
        }
    }

    private void includeBean(String name, Object bean, String element) {
        try {
            if (this.beanWriter == null) {
                this.beanWriter = new SAXBeanWriter(this.contentHandler);
                this.beanWriter.setCallDocumentEvents(false);

                synchronized (BetwixtTransformer.class) {
                    if (introspector == null) {
                        introspector = this.beanWriter.getXMLIntrospector();
                        introspector.setLog(new LogKitLogger("betwixt"));
                        // The following is needed for EJB
                        introspector.setClassNormalizer(new ClassNormalizer() {
                            public Class normalize(Class clazz) {
                                if (Proxy.isProxyClass(clazz)
                                    && clazz.getInterfaces().length > 0) {
                                    return clazz.getInterfaces()[0];
                                }
                                return super.normalize(clazz);
                            }
                        });

                    } else {
                        this.beanWriter.setXMLIntrospector(introspector);
                    }
                }

                beanWriter.getBindingConfiguration().setMapIDs(this.refIds);
            }

            if (bean instanceof Collection) {
                Iterator i = ((Collection) bean).iterator();
                while (i.hasNext()) {
                    if (element == null) {
                        beanWriter.write(bean);
                    } else {
                        beanWriter.write(element, bean);
                    }
                }
            } else {
                if (element == null) {
                    beanWriter.write(bean);
                } else {
                    beanWriter.write(element, bean);
                }
            }
        } catch (Exception e) {
            getLogger().warn("Failed to marshal bean " + name, e);
        }
    }

}