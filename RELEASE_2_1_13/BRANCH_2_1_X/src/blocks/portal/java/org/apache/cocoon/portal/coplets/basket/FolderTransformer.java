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
package org.apache.cocoon.portal.coplets.basket;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.wrapper.RequestParameters;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplets.basket.events.UploadItemEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This transformer supports the basket feature. It can generate links to
 * add content and to upload files into the basket.
 *
 * @version $Id: BasketTransformer.java 47047 2004-09-22 12:27:27Z vgritsenko $
 */
public class FolderTransformer extends AbstractBasketTransformer {

    /** Element to upload an item */
    protected static final String UPLOAD_ITEM_ELEMENT = "upload-item";

    /** Element for the upload form */
    protected static final String UPLOAD_FORM_ELEMENT = "upload-form";

    /** Upload element list */
    protected List uploadElements = new ArrayList();

    /* (non-Javadoc)
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        this.uploadElements.clear();
        super.recycle();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.transformation.AbstractSAXTransformer#endTransformingElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endTransformingElement(String uri, String name, String raw)
    throws ProcessingException, IOException, SAXException {
        if ( UPLOAD_ITEM_ELEMENT.equals(name) ) {
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
        if ( UPLOAD_ITEM_ELEMENT.equals(name) ) {
            this.uploadElements.add(attr.getValue("name"));
            this.startElement("", "input", "input", attr);
        } else if ( UPLOAD_FORM_ELEMENT.equals(name) ) {
            AttributesImpl ai = new AttributesImpl(attr);
            PortalService service = null;
            String parameters;
            try {
                service = (PortalService)this.manager.lookup(PortalService.ROLE);
                Event e = new UploadItemEvent(this.basketManager.getFolder(), this.uploadElements);
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
                Enumeration enumPars = pars.getParameterNames();
                while ( enumPars.hasMoreElements() ) {
                    String pName = (String)enumPars.nextElement();
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
