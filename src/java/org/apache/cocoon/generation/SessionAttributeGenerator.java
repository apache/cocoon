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
package org.apache.cocoon.generation;

import java.io.IOException;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.excalibur.xml.sax.XMLizable;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Generates a document from a session attribute. The attribute may be a DOM
 * node, an <code>XMLizable</code>, or any other object, and is streamed using
 * the same rules as for &lt;xsp:expr&gt; in XSPs (see {@link
 * org.apache.cocoon.components.language.markup.xsp.XSPObjectHelper}).
 * <p>
 * Name of the session attribute is specified using src attribute of the generate
 * tag, or, if no src tag present, using attr-name parameter.
 * <p>
 * This generator has 2 parameters:
 * <ul>
 * <li><code>attr-name</code> : the session attribute name (mandatory if no src
 *     attribute specified).
 * </li>
 * <li><code>root-element</code> (optional) : the name of the root element of the
 *     produced document. This parameter is optional if the session attribute is
 *     a DOM or an <code>XMLizable</code>.
 * </li>
 * </ul>
 * <p>
 * Example usage :
 * <pre>
 *   &lt;map:generator name="session-attr" logger="sitemap.generator.session-attr"
 *     src="org.apache.cocoon.generation.SessionAttributeGenerator"/&gt;
 *   ...
 *   &lt;map:generate type="session-attr"&gt;
 *     &lt;map:parameter name="attr-name" value="myAttribute"/&gt;
 *     &lt;map:parameter name="root-element" value="root"/&gt;
 *   &lt;/map:generate&gt;
 * </pre>
 *
 * @see org.apache.cocoon.transformation.ReadDOMSessionTransformer
 * @see org.apache.cocoon.transformation.WriteDOMSessionTransformer
 * @author <a href="mailto:cedric.damioli@anyware-tech.com">C&eacute;dric Damioli</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: SessionAttributeGenerator.java,v 1.5 2004/03/08 14:02:45 cziegeler Exp $
 */
public class SessionAttributeGenerator extends AbstractGenerator {

    public static final String ATTR_NAME = "attr-name";
    public static final String ELEMENT_NAME = "root-element";

    /** The object to generate */
    private Object attrObject;

    /** The element name */
    private String elementName;

    /**
     * Setup the file generator :try to retrieve the session attribute given as sitemap parameter
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
      throws ProcessingException, SAXException, IOException {
        
        super.setup(resolver, objectModel, src, par);
        
        // Get the element name (can be null if the object is a DOM or an XMLizable)
        this.elementName = par.getParameter(ELEMENT_NAME, null);
        
        // Get the attribute name
        String attrName = par.getParameter(ATTR_NAME, src);
        if (attrName == null) {
            String msg = "SessionAttributeGenerator needs an attribute name !";
            getLogger().error(msg);
            throw new ProcessingException(msg);
        }

        // Get the object to stream
        Request request = ObjectModelHelper.getRequest(objectModel);
        Session session = request.getSession(false);
        if (session != null) {
            this.attrObject = session.getAttribute(attrName);
        }
        
        // Controls
        if (this.attrObject == null) {
            if (this.elementName == null) {
                // Can't generate nothing...
                String msg = "Session attribute '" + attrName + "' doesn't exist";
                getLogger().error(msg);
                throw new ProcessingException(msg);
            } else {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Session attribute '" + attrName +
                        "' doesn't exist : will generate a single '" + this.elementName +
                        "' element.");
                }
            }
        } else {
            // Need an element name for non-xml objects
            if (this.elementName == null &&
                ! (this.attrObject instanceof XMLizable) &&
                ! (this.attrObject instanceof Node)) {

                String msg = "Session attribute '" + attrName + "' needs an enclosing element : class is " + 
                    this.attrObject.getClass().getName();

                getLogger().warn(msg);
                throw new ProcessingException(msg);
            }
        }
    }

    /**
     * Generate XML data
     */
    public void generate() throws IOException, SAXException, ProcessingException {
        xmlConsumer.startDocument();

        if (this.elementName != null) {
            xmlConsumer.startElement("", this.elementName, this.elementName, new AttributesImpl());
            XMLUtils.valueOf(new IncludeXMLConsumer(xmlConsumer), this.attrObject);
            xmlConsumer.endElement("", this.elementName, this.elementName);
        } else {
            XMLUtils.valueOf(new IncludeXMLConsumer(xmlConsumer), this.attrObject);
        }
        
        xmlConsumer.endDocument();
    }
}
