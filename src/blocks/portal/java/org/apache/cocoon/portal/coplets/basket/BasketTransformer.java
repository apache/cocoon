/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.portal.coplets.basket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.wrapper.RequestParameters;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.transformation.AbstractSAXTransformer;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This transformer supports the basket feature. It can generate links to
 * add content and to upload files into the basket.
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a> 
 * 
 * @version CVS $Id: BasketTransformer.java,v 1.1 2004/02/23 14:52:50 cziegeler Exp $
 */
public class BasketTransformer
extends AbstractSAXTransformer {

    /** The namespace URI to listen for. */
    public static final String NAMESPACE_URI = "http://apache.org/cocoon/portal/basket/1.0";
    
    /** Element to add a link */
    protected static final String ADD_ITEM_ELEMENT = "add-item";
    
    /** Element to upload an item */
    protected static final String UPLOAD_ITEM_ELEMENT = "upload-item";
    
    /** Element for the upload form */
    protected static final String UPLOAD_FORM_ELEMENT = "upload-form";

    /** Upload element list */
    protected List uploadElements = new ArrayList();
    
    /**
     * Constructor
     */
    public BasketTransformer() {
        this.namespaceURI = NAMESPACE_URI;
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        super.recycle();
        this.uploadElements.clear();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#endTransformingElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endTransformingElement(String uri, String name, String raw)
    throws ProcessingException, IOException, SAXException {
        if ( ADD_ITEM_ELEMENT.equals(name) ) {
            this.endElement("", "a", "a");
        } else if ( UPLOAD_ITEM_ELEMENT.equals(name) ) {
            this.endElement("", "input", "input");
        } else if ( UPLOAD_FORM_ELEMENT.equals(name) ) {
            this.endElement("", "form", "form");
            this.uploadElements = new ArrayList();
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#startTransformingElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startTransformingElement(String uri, String name,
                                         String raw, Attributes attr)
    throws ProcessingException, IOException, SAXException {
        if ( ADD_ITEM_ELEMENT.equals(name) ) {
            String value = attr.getValue("content");
            boolean addContent = false;
            if ( value != null ) {
                addContent = new Boolean(value).booleanValue();
            }
            String href = attr.getValue("href");
            PortalService service = null;
            try {
                service = (PortalService)this.manager.lookup(PortalService.ROLE);
                ContentItem ci = new ContentItem(href, addContent);
                Event e = new AddItemEvent(ci);
                AttributesImpl ai = new AttributesImpl();
                ai.addCDATAAttribute("href", service.getComponentManager().getLinkService().getLinkURI(e));
                this.startElement("", "a", "a", ai);
            } catch (ServiceException se) {
                throw new SAXException("Unable to lookup portal service.", se);
            } finally {
                this.manager.release(service);
            }
        } else if ( UPLOAD_ITEM_ELEMENT.equals(name) ) {
            this.uploadElements.add(attr.getValue("name"));
            this.startElement("", "input", "input", attr);
        } else if ( UPLOAD_FORM_ELEMENT.equals(name) ) {
            AttributesImpl ai = new AttributesImpl(attr);
            PortalService service = null;
            String parameters;
            try {
                service = (PortalService)this.manager.lookup(PortalService.ROLE);
                Event e = new UploadItemEvent(this.uploadElements);
                parameters = service.getComponentManager().getLinkService().getLinkURI(e);
                int pos = parameters.indexOf('?');
                ai.addCDATAAttribute("action", parameters.substring(0, pos));
                parameters = parameters.substring(pos+1);
            } catch (ServiceException se) {
                throw new SAXException("Unable to lookup portal service.", se);
            } finally {
                this.manager.release(service);
            }
            this.startElement("", "form", "form", ai);
            if ( parameters != null && parameters.length() > 0 ) {
                // create hidden input fields
                RequestParameters pars = new RequestParameters(parameters);
                Enumeration enum = pars.getParameterNames();
                while ( enum.hasMoreElements() ) {
                    String pName = (String)enum.nextElement();
                    String pValue = pars.getParameter(pName);
                    AttributesImpl hiddenAttrs = new AttributesImpl();
                    hiddenAttrs.addCDATAAttribute("type", "hidden");
                    hiddenAttrs.addCDATAAttribute("name", pName);
                    hiddenAttrs.addCDATAAttribute("value", pValue);
                    this.startElement("", "input", "input", hiddenAttrs);
                    this.endElement("", "input", "input");
                }
            }
        }
    }

}