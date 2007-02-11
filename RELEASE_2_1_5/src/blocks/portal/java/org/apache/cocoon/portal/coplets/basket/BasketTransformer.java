/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
 * @version CVS $Id: BasketTransformer.java,v 1.2 2004/03/05 13:02:11 bdelacretaz Exp $
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