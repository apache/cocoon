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

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;


/**
 * @cocoon.sitemap.component.documentation
 * With this transformer, an object that is stored in the session, can be inserted
 * in the SAX stream at a given position, using usual &lt;xsp:expr&gt; rules.
 * Object can be DOM Node, XMLizable, or any other object supported by &lt;xsp:expr&gt;.
 * 
 * @cocoon.sitemap.component.name   readDOMsession
 * @cocoon.sitemap.component.logger sitemap.transformer.readDOMsession
 * 
 * With this transformer, an object that is stored in the session, can be inserted
 * in the SAX stream at a given position, using usual &lt;xsp:expr&gt; rules.
 * Object can be DOM Node, XMLizable, or any other object supported by &lt;xsp:expr&gt;.
 *
 * Usage in sitemap:
 * <pre>
 *    &lt;map:transform type="read-session"&gt;
 *      &lt;map:parameter name="attribute-name" value="companyInfo"/&gt;
 *      &lt;map:parameter name="trigger-element" value="company"/&gt;
 *      &lt;map:parameter name="position" value="after"/&gt;
 *    &lt;/map:transform&gt;
 * </pre>
 *
 * Where:
 * <ul>
 *  <li><b>attribute-name</b> is the name of the object in the session
 *  <li><b>trigger-element</b> is the element that we need to insert the SAX events
 *  <li><b>postion</b> is the actual place where the stream will be inserted, ie before, after or in
 *  the trigger-element
 * </ul>
 *
 * @author <a href="mailto:sven.beauprez@the-ecorp.com">Sven Beauprez</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: ReadDOMSessionTransformer.java,v 1.4 2004/06/17 14:55:24 cziegeler Exp $
 */
public class ReadDOMSessionTransformer extends AbstractTransformer  {

    public static final String ATTRIBUTE_NAME = "attribute-name";
    public static final String TRIGGER_ELEMENT = "trigger-element";

    /*
      position where the sax events from the dom should be insterted
      this can be: 'before', 'after' or 'in'
    */
    public static final String POSITION = "position";

    Session session;
    String attributeName;
    String trigger;
    String position;

    /** BEGIN SitemapComponent methods **/
    public void setup(SourceResolver resolver,
                      Map objectModel,
                      String source,
                      Parameters parameters)
            throws ProcessingException, SAXException, IOException {
        Request request = ObjectModelHelper.getRequest(objectModel);
        session = request.getSession(false);
        if (session != null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Session is available. ID=" + session.getId());
            }
            this.attributeName = parameters.getParameter(ATTRIBUTE_NAME, null);
            if (this.attributeName == null) {
                // Try old syntax
                this.attributeName = parameters.getParameter("dom-name", null);
            }

            this.trigger = parameters.getParameter(TRIGGER_ELEMENT, null);
            this.position = parameters.getParameter(POSITION, "in");
            if (getLogger().isDebugEnabled()) {
                getLogger().debug(ATTRIBUTE_NAME + "=" + attributeName + ", "
                        + TRIGGER_ELEMENT + "=" + trigger + ", "
                        + POSITION + "=" + position);
            }
        } else {
            getLogger().warn("No session object: Nothing to do.");
        }
    }
    /** END SitemapComponent methods **/

    /** BEGIN SAX ContentHandler handlers **/
    public void startElement(String uri, String name, String raw, Attributes attributes)
            throws SAXException {
        // Start streaming after certain startelement is encountered
        if (name.equalsIgnoreCase(trigger)) {
            getLogger().debug("Trigger encountered");
            if ("before".equalsIgnoreCase(position))  {
                stream();
                super.contentHandler.startElement(uri,name,raw,attributes);
            } else if ("in".equalsIgnoreCase(position))  {
                super.contentHandler.startElement(uri,name,raw,attributes);
                stream();
            } else if ("after".equalsIgnoreCase(position))  {
                super.contentHandler.startElement(uri,name,raw,attributes);
            }
        } else {
            super.contentHandler.startElement(uri,name,raw,attributes);
        }
    }

    public void endElement(String uri,String name,String raw)
            throws SAXException  {
        super.contentHandler.endElement(uri,name,raw);
        if (name.equalsIgnoreCase(trigger)) {
            if ("after".equalsIgnoreCase(position))  {
                stream();
            }
        }
    }
    /** END SAX ContentHandler handlers **/

    /** own methods **/
    private void stream() throws SAXException  {
        if (attributeName != null)  {
            Object node = session.getAttribute(attributeName);
            if (node != null)  {
                getLogger().debug("Start streaming");
                XMLUtils.valueOf(new IncludeXMLConsumer(super.xmlConsumer), node);
            } else {
                getLogger().error("No attribute " + attributeName + " in session");
            }
        } else {
            getLogger().error("No "+ ATTRIBUTE_NAME + " parameter specified");
        }
    }
}
