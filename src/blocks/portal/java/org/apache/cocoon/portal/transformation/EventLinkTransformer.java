/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.transformation;

import java.util.Stack;

import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.impl.CopletLinkEvent;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This transformer searches for event descriptions in the XML. 
 * For each one an event is created and the event link is inserted into the XML 
 * instead of the description.<br><br>
 *  
 * Example:<br><br>
 * 
 * <pre>&lt;root xmlns:event="http://apache.org/cocoon/portal/eventlink/1.0"&gt;
 * 	&lt;event:event attribute="href"&gt;
 * 		&lt;a href="http://eventlinkexample"/&gt;
 * 	&lt;/event:event&gt;
 * 	&lt;event:event element="uri"&gt;
 * 		&lt;link>&lt;uri&gt;http://eventlinkexample&lt;/uri&gt;&lt;/link&gt;
 * 	&lt;/event:event&gt;
 * &lt;/root&gt;<br></pre>
 *
 * The transformer will create two CopletLinkEvents and insert corresponding links 
 * to them to the XML instead of "http://eventlinkexample". If such a link is pressed 
 * the corresponding CopletLinkEvent is sent to the Subscribers to be handled.<br>
 * Please see also the documentation of superclass AbstractCopletTransformer for how
 * the coplet instance data are acquired.
 *   
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * 
 * @version CVS $Id: EventLinkTransformer.java,v 1.8 2004/03/16 09:16:59 cziegeler Exp $
 */
public class EventLinkTransformer 
extends AbstractCopletTransformer {
    
    /**
     * The namespace URI to listen for.
     */
    public static final String NAMESPACE_URI = "http://apache.org/cocoon/portal/eventlink/1.0";
    
    /**
     * The XML element name to listen for.
     */
    public static final String EVENT_ELEM = "event";
    
    /**
     * An attribute's name of EVENT_ELEMENT.
     */
    public static final String ATTRIBUTE_ATTR = "attribute";
    
    /**
     * An attribute's name of EVENT_ELEMENT.
     */
    public static final String ELEMENT_ATTR = "element";

    /**
     * Used to signal whether the transformer is inside an EVENT_ELEM tag.
     */
    private boolean insideEvent = false;

    /**
     * The attribute defining the link inside an EVENT_ELEM tag.
     */
    private String attributeName = null;

    /**
     * The element defining the link inside an EVENT_ELEM tag.
     */
    private String elementName = null;
    
    /**
     * Used to store elements' attributes between startTransformingElement and endTransformingElement. 
     */
    private Stack attrStack = new Stack();

    /**
     * Overridden from superclass.
     */
    public void recycle() {
        super.recycle();
        this.insideEvent = false;
        this.attributeName = null;
        this.elementName = null;
        this.attrStack.clear();
    }

    /**
     * Overridden from superclass.
     */
	public void startElement(String uri, String name, String raw, Attributes attr)
	throws SAXException {

        if (uri.equals(NAMESPACE_URI) && name.equals(EVENT_ELEM)) {
            if (this.insideEvent) {
                throw new SAXException("Elements "+EVENT_ELEM+" must not be nested.");
            }
            this.insideEvent = true;
            
            // get element or attribute name that contains links
            this.attributeName = attr.getValue(ATTRIBUTE_ATTR);
            this.elementName = attr.getValue(ELEMENT_ATTR);

            // at least one of them must be set
            if (this.attributeName == null && this.elementName == null) {
                throw new SAXException("Element "+EVENT_ELEM+" must have one of attributes "+ATTRIBUTE_ATTR+" and "+ELEMENT_ATTR+".");
            }
        } else {
            if (this.insideEvent) {
                // store attributes for endTransformingElement
                this.attrStack.push(new AttributesImpl(attr));
                
                /* Record element content. In case of an element we asume, that no
                 * children exist but only text content, since the text content shall 
                 * be the link. Therefore we do startTextRecording. Otherwise we 
                 * record the whole subtree.
                 */
                if (this.elementName != null && name.equals(this.elementName)) {
                    this.startTextRecording();
                } else {
                    this.startRecording();
                }
            } else {
                super.startElement(uri, name, raw, attr);
            }
        }
	}

    /**
     * Overridden from superclass.
     */
    public void endElement(String uri, String name, String raw)
    throws SAXException {

        if (uri.equals(NAMESPACE_URI) && name.equals(EVENT_ELEM)) {
            this.attributeName = null;
            this.elementName = null;
            this.insideEvent = false;
        } else {
            if (this.insideEvent) {
                AttributesImpl attr = (AttributesImpl)this.attrStack.pop();

                // process attribute that contains link
                if (this.attributeName != null) {
                    int index = attr.getIndex(this.attributeName);
                    String link = attr.getValue(index);

                    // if attribute found that contains a link
                    if (link != null) {
                        CopletInstanceData cid = this.getCopletInstanceData();                       
                        LinkService linkService = this.getPortalService().getComponentManager().getLinkService();
        
                        // create event link
                        CopletLinkEvent event = new CopletLinkEvent(cid, link);
                        String eventLink = linkService.getLinkURI(event);
    
                        // insert event link
                        attr.setValue(index, eventLink);
                    }
                }
                
                String eventLink = null;
                DocumentFragment fragment = null;
                                
                // process element that contains link
                if (this.elementName != null && name.equals(this.elementName)) {
                    String link = this.endTextRecording();

                    CopletInstanceData cid = this.getCopletInstanceData();                       
                    LinkService linkService = this.getPortalService().getComponentManager().getLinkService();

                    // create event link
                    CopletLinkEvent event = new CopletLinkEvent(cid, link);
                    eventLink = linkService.getLinkURI(event);
                } else {
                    fragment = this.endRecording();
                }
                
                // stream element
                super.startElement(uri, name, raw, attr);
                if (eventLink != null) {
                    // insert event link
                    super.characters(eventLink.toCharArray(), 0, eventLink.length());
                } else if (fragment != null) {
                    super.sendEvents(fragment);
                }
                super.endElement(uri, name, raw);
            } else {
                super.endElement(uri, name, raw);
            }
        }
    }

}
