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

import org.apache.avalon.framework.component.ComponentException;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.event.impl.LinkEvent;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This transformer searches for event descriptions in the XML. 
 * For each one an event is created and the event link is inserted into the XML 
 * instead of the description.
 *  
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Björn Lütkemeier</a>
 * 
 * @version CVS $Id: EventLinkTransformer.java,v 1.1 2003/05/08 11:54:00 cziegeler Exp $
 */
public class EventLinkTransformer 
extends AbstractSAXTransformer {
    
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
                        LinkService linkService = null;
                        try {
                            linkService = (LinkService)this.manager.lookup(LinkService.ROLE);
        
                            // create event link
                            LinkEvent event = new LinkEvent(link);
                            String eventLink = linkService.getLinkURI(event);
        
                            // insert event link
                            attr.setValue(index, eventLink);
                        } catch (ComponentException e) {
                            throw new SAXException(e);
                        } finally {
                            this.manager.release(linkService);
                        }
                    }
                }
                
                String eventLink = null;
                DocumentFragment fragment = null;
                                
                // process element that contains link
                if (this.elementName != null && name.equals(this.elementName)) {
                    String link = this.endTextRecording();

                    LinkService linkService = null;
                    try {
                        linkService = (LinkService)this.manager.lookup(LinkService.ROLE);
        
                        // create event link
                        LinkEvent event = new LinkEvent(link);
                        eventLink = linkService.getLinkURI(event);
                    } catch (ComponentException e) {
                        throw new SAXException(e);
                    } finally {
                        this.manager.release(linkService);
                    }
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
