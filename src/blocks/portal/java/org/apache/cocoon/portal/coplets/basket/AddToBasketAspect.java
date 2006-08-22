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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplets.basket.events.AddItemEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.portal.layout.renderer.aspect.impl.AbstractAspect;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This renderer adds a link to add this coplet to the basket.
 * It checks the coplet data for the attributes
 * basket-content and basket-link (boolean values) to stream
 * out the elements.
 * 
 * @version CVS $Id$
 */
public final class AddToBasketAspect 
extends AbstractAspect 
implements Disposable {

    /** The basket manager */
    protected BasketManager basketManager;
    
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.basketManager = (BasketManager)this.manager.lookup(BasketManager.ROLE);
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.basketManager);
            this.basketManager = null;
            this.manager = null;
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.renderer.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
     */
    public void toSAX(RendererAspectContext context,
                        Layout layout,
                        PortalService service,
                        ContentHandler contenthandler)
    throws SAXException {
        final CopletInstanceData cid = ((CopletLayout)layout).getCopletInstanceData();
        final ContentStore store;
        final String elementName;
        if ( context.getAspectConfiguration().equals(Boolean.TRUE) ) {
            store = this.basketManager.getBasket();
            elementName = "basket-add-content";
        } else {
            store = this.basketManager.getBriefcase();
            elementName = "briefcase-add-content";
        }
        
        Boolean b = (Boolean)cid.getCopletData().getAttribute("basket-content");
        if ( b != null && b.equals(Boolean.TRUE) ) {
            Object item = new ContentItem(cid, true);
            Event event = new AddItemEvent(store, item);
            XMLUtils.createElement(contenthandler, elementName, service.getComponentManager().getLinkService().getLinkURI(event));
        }
        b = (Boolean)cid.getCopletData().getAttribute("basket-link");
        if ( b != null && b.equals(Boolean.TRUE) ) {
            Object item = new ContentItem(cid, false);
            Event event = new AddItemEvent(store, item);
            XMLUtils.createElement(contenthandler, elementName, service.getComponentManager().getLinkService().getLinkURI(event));            
        }
        
        context.invokeNext( layout, service, contenthandler );
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#prepareConfiguration(org.apache.avalon.framework.parameters.Parameters)
     */
    public Object prepareConfiguration(Parameters configuration)
    throws ParameterException {
        if ( configuration.getParameter("use-store", "basket").equalsIgnoreCase("basket") ) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
