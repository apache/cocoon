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
package org.apache.cocoon.generation;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.excalibur.xml.sax.XMLizable;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.util.Map;

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
 * @version CVS $Id: SessionAttributeGenerator.java,v 1.3 2003/10/08 12:38:58 bruno Exp $
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
