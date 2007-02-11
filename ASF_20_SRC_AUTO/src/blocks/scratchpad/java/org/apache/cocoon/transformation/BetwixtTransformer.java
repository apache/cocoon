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