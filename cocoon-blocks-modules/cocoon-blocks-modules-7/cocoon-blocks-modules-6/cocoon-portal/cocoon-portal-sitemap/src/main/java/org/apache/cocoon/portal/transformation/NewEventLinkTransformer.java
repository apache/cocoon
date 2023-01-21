/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.transformation;

import org.apache.cocoon.portal.event.impl.CopletLinkEvent;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This transformer is used to replace links (URIs) from elements
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
 * @version $Id$
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
     * @see java.lang.Object#Object()
     */
    public NewEventLinkTransformer() {
        this.defaultNamespaceURI = NAMESPACE_URI;
        this.removeOurNamespacePrefixes = true;
    }

    /**
     * @throws SAXException when the eventlink element does not contain the necessary attributes
     * 			"element" and "attribute", retrieving the LinkURI from the LinkService fails,
     *          or an unknown element within the namespaces in encountered.
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#startTransformingElement(String, String, String, Attributes)
     */
    public void startTransformingElement(String uri,
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

        // remove ATTRIBUTE_ATTR, ELEMENT_ATTR and "coplet" from attributes
        AttributesImpl newAttributes = this.getMutableAttributes(attributes);
        newAttributes.removeAttribute(ELEMENT_ATTR);
        newAttributes.removeAttribute(ATTRIBUTE_ATTR);
        newAttributes.removeAttribute("coplet");

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
            CopletInstance cid = this.getCopletInstanceData(attributes.getValue("coplet"));
            // create event link
            CopletLinkEvent event = new CopletLinkEvent(cid, link);
            String eventLink = this.portalService.getLinkService().getLinkURI(event);

            //form elements need hidden inputs to change request parameters
            if (formSpecialTreatment) {
                int pos = eventLink.indexOf("cocoon-portal-action=");
                if ( pos != -1 ) {
                    int begin = pos + "cocoon-portal-action=".length();
                    int end = eventLink.indexOf('&', begin);
                    if (end == -1) {
                        end = eventLink.length();
                    }
                    portalAction = eventLink.substring(begin, end);
                }
                pos = eventLink.indexOf("cocoon-portal-event=");
                if ( pos != -1 ) {
                    int begin = pos + "cocoon-portal-event=".length();
                    int end = eventLink.indexOf('&', begin);
                    if (end == -1) {
                        end = eventLink.length();
                    }
                    portalEvent = eventLink.substring(begin, end);
                }
                pos = eventLink.indexOf('?');
                if ( pos != -1 ) {
                    eventLink = eventLink.substring(0, eventLink.indexOf('?'));
                }
            }

            // insert event link
            newAttributes.setValue(index, eventLink);
        }

        this.stack.push(elementName);

        XMLUtils.createElement(contentHandler, elementName, newAttributes);

        // generate hidden inputs to add request parameters to the form action
        if (formSpecialTreatment) {
            sendHiddenFields(contentHandler, portalAction, portalEvent);
        }
    }

    /**
     * With forms the uri in the action attribute cannot be enhanced with request parameters.
     * Instead hidden input fields must be inserted into the SAX stream to add request parameters.
     * This method sends two hidden inputs adding the "cocoon-portal-action" parameter and
     * the "cocoon-portal-event" parameter.
     * @param handler the content handler recieving the SAX events
     * @param portalAction value of the "cocoon-portal-action" parameter
     * @param portalEvent value of the "cocoon-portal-event" parameter
     * @throws SAXException if sending the SAX events failed
     */
    private void sendHiddenFields(ContentHandler handler,
                                  String portalAction,
                                  String portalEvent)
    throws SAXException {
        if ( portalAction != null && portalAction.trim().length() > 0 ) {
            final AttributesImpl attributes = new AttributesImpl();
            attributes.addCDATAAttribute("type", "hidden");
            attributes.addCDATAAttribute("name", "cocoon-portal-action");
            attributes.addCDATAAttribute("value", portalAction);
            XMLUtils.createElement(handler, "input", attributes);
        }
 
        if ( portalEvent != null && portalEvent.trim().length() > 0 ) {
            final AttributesImpl attributes = new AttributesImpl();
            attributes.addCDATAAttribute("type", "hidden");
            attributes.addCDATAAttribute("name", "cocoon-portal-event");
            attributes.addCDATAAttribute("value", portalEvent);
            XMLUtils.createElement(handler, "input", attributes);
        }
    }

    /**
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#endTransformingElement(String, String, String)
     */
    public void endTransformingElement(String uri, String name, String raw)
    throws SAXException {
        XMLUtils.createElement(contentHandler, (String) this.stack.pop());
    }
}
