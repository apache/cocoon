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

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;


/**
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
 * @version CVS $Id: ReadDOMSessionTransformer.java,v 1.1 2003/03/09 00:09:39 pier Exp $
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
                XMLUtils.valueOf(super.xmlConsumer, node);
            } else {
                getLogger().error("No attribute " + attributeName + " in session");
            }
        } else {
            getLogger().error("No "+ ATTRIBUTE_NAME + " parameter specified");
        }
    }
}
