/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.portal.transformation;

import java.util.Stack;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.impl.CopletLinkEvent;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This transformer ist used to replace links (URIs) from elements
 * like &lt;a href="URI"&gt; or &lt;form action="URI"&gt; with portal
 * event uris. Therefore the transformer searches for &lt;eventlink&gt;
 * elements replaces the URI form the attribute which is specified within
 * an attribute called "attribute" and renames the element as specified
 * within an attribute called "element".
 * 
 * Example:<br><br>
 * 
 * <pre>
 * &lt;root xmlns:ev="http://apache.org/cocoon/portal/eventlink/1.0"&gt;
 *   &lt;ev:eventlink href="http://eventlinkexample" element="a" attribute="href"&gt;linktext&lt;/ev:eventlink&gt;
 * &lt;/root&gt;<br></pre>
 *
 * will be replaced with something like:<br><br>
 * 
 * <pre>
 * &lt;root&gt;
 *   &lt;a href="portal?cocoon-portal-event=8"&gt;linktext&lt;/a&gt;
 * &lt;/root&gt;<br></pre>
 * 
 * The transformer will create two CopletLinkEvents and insert corresponding links 
 * to them to the XML instead of "http://eventlinkexample". If such a link is pressed 
 * the corresponding CopletLinkEvent is sent to the Subscribers to be handled.<br>
 * Please see also the documentation of superclass AbstractCopletTransformer for how
 * the coplet instance data are acquired.
 *   
 * @author <a href="mailto:gernot.koller@rizit.at">Gernot Koller</a>
 * 
 * @version CVS $Id: NewEventLinkTransformer.java,v 1.4 2003/12/10 17:02:04 cziegeler Exp $
 */
public class NewEventLinkTransformer extends AbstractCopletTransformer {
    /**
     * The namespace URI to listen for.
     */
    public static final String NAMESPACE_URI =
        "http://apache.org/cocoon/portal/eventlink/1.0";

    /**
     * The XML element name to listen for.
     */
    public static final String EVENT_ELEM = "eventlink";

    /**
     * An attribute's name of EVENT_ELEMENT.
     */
    public static final String ATTRIBUTE_ATTR = "attribute";

    /**
     * An attribute's name of EVENT_ELEMENT.
     */
    public static final String ELEMENT_ATTR = "element";

    /**
     * Used to store elements' name between startTransformingElement and endTransformingElement. 
     */
    private Stack elementStack = new Stack();

    /**
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        super.recycle();
        this.elementStack.clear();
    }

    /**
     * @see java.lang.Object#Object()
     */
    public NewEventLinkTransformer() {
        this.namespaceURI = NAMESPACE_URI;
    }

    /**
     * @throws SAXException when the eventlink element does not contain the necessary attributes
     * 			"element" and "attribute", retrieving the LinkURI from the LinkService fails,
     *          or an unknown element within the namespaces in encountered.
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#startTransformingElement(String, String, String, Attributes)
     */
    public void startTransformingElement(
        String uri,
        String name,
        String raw,
        Attributes attributes)
        throws SAXException {
        if (!EVENT_ELEM.equals(name)) {
            throw new SAXException("Unknown element encountered: " + name);
        }

        String attributeName = attributes.getValue(ATTRIBUTE_ATTR);
        String elementName = attributes.getValue(ELEMENT_ATTR);

        if (attributeName == null) {
            throw new SAXException(
                "Element "
                    + EVENT_ELEM
                    + " must have an attribute "
                    + ATTRIBUTE_ATTR
                    + ".");
        }

        if (elementName == null) {
            throw new SAXException(
                "Element "
                    + EVENT_ELEM
                    + " must have an attribute "
                    + ELEMENT_ATTR
                    + ".");
        }

        //remove ATTRIBUTE_ATTR and ELEMENT_ATTR from attributes
        AttributesImpl newAttributes = null;

        if (attributes instanceof AttributesImpl) {
            newAttributes = (AttributesImpl) attributes;
        }
        else {
            newAttributes = new AttributesImpl(attributes);
        }

        //always iterate backwards when removing elements!
        for (int i = newAttributes.getLength() - 1; i >= 0; i--) {
            if (ELEMENT_ATTR.equals(attributes.getLocalName(i))
                || ATTRIBUTE_ATTR.equals(attributes.getLocalName(i))) {
                newAttributes.removeAttribute(i);
            }
        }

        int index = newAttributes.getIndex(attributeName);
        String link = newAttributes.getValue(index);

        boolean formSpecialTreatment = false;
        if ("form".equals(elementName)) {
            //cut all query parameters from actions with method get, as these will be normaly ignored!
            formSpecialTreatment = true;
            if ("GET".equalsIgnoreCase(newAttributes.getValue("method"))
                && link.indexOf('?') > 0) {
                link = link.substring(0, link.indexOf('?'));
            }
        }

        String portalAction = null;
        String portalEvent = null;

        // if attribute found that contains a link
        if (link != null) {
            CopletInstanceData cid = this.getCopletInstanceData();
            LinkService linkService = null;
            try {
                linkService =
                    (LinkService) this.manager.lookup(LinkService.ROLE);
                // create event link
                CopletLinkEvent event = new CopletLinkEvent(cid, link);
                String eventLink = linkService.getLinkURI(event);

                //form elements need hidden inputs to change request parameters
                if (formSpecialTreatment) {
                    int begin =
                        eventLink.indexOf("cocoon-portal-action=")
                            + "cocoon-portal-action=".length();
                    int end = eventLink.indexOf('&', begin);
                    if (end == -1) {
                        end = eventLink.length();
                    }

                    portalAction = eventLink.substring(begin, end);

                    begin =
                        eventLink.indexOf("cocoon-portal-event=")
                            + "cocoon-portal-event=".length();
                    end = eventLink.indexOf('&', begin);
                    if (end == -1) {
                        end = eventLink.length();
                    }
                    portalEvent = eventLink.substring(begin, end);

                    eventLink =
                        eventLink.substring(0, eventLink.indexOf('?'));
                }

                // insert event link
                newAttributes.setValue(index, eventLink);
            }
            catch (ServiceException e) {
                throw new SAXException(e);
            }
            finally {
                this.manager.release(linkService);
            }
        }

        elementStack.push(elementName);

        contentHandler.startElement(
            "",
            elementName,
            elementName,
            newAttributes);

        //generate hidden inputs to add request parameters to the form action
        if (formSpecialTreatment) {
            sendHiddenFields(contentHandler, portalAction, portalEvent);
        }
    }

    /**
     * With forms the uri in the action attribute cannot be enhanced with request parameters.
     * Instead hidden input fields must be inserted into the SAX stream to add request parameters.
     * This method sends two hidden inputs adding the "cocoon-portal-action" parameter and
     * the "cocoon-portal-event" parameter.
     * @param contentHandler the content handler recieving the SAX events
     * @param portalAction value of the "cocoon-portal-action" parameter
     * @param portalEvent value of the "cocoon-portal-event" parameter
     * @throws SAXException if sending the SAX events failed
     */
    private void sendHiddenFields(
        ContentHandler contentHandler,
        String portalAction,
        String portalEvent)
        throws SAXException {
        AttributesImpl attributes = new AttributesImpl();
        attributes.addAttribute("", "type", "type", "CDATA", "hidden");
        attributes.addAttribute(
            "",
            "name",
            "name",
            "CDATA",
            "cocoon-portal-action");
        attributes.addAttribute("", "value", "value", "CDATA", portalAction);
        contentHandler.startElement("", "input", "input", attributes);
        contentHandler.endElement("", "input", "input");

        attributes = new AttributesImpl();
        attributes.addAttribute("", "type", "type", "CDATA", "hidden");
        attributes.addAttribute(
            "",
            "name",
            "name",
            "CDATA",
            "cocoon-portal-event");
        attributes.addAttribute("", "value", "value", "CDATA", portalEvent);
        contentHandler.startElement("", "input", "input", attributes);
        contentHandler.endElement("", "input", "input");
    }

    /**
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#endTransformingElement(String, String, String)
     */
    public void endTransformingElement(String uri, String name, String raw)
        throws SAXException {
        String elementName = (String) elementStack.pop();
        contentHandler.endElement("", elementName, elementName);
    }
}
