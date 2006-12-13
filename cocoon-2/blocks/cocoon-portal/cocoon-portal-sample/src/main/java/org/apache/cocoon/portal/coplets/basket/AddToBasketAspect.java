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

import java.util.Properties;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.portal.LayoutException;
import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.coplets.basket.events.AddItemEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.portal.layout.renderer.aspect.impl.AbstractAspect;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.CopletLayout;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This renderer adds a link to add this coplet to the basket.
 * It checks the coplet data for the attributes
 * basket-content and basket-link (boolean values) to stream
 * out the elements.
 *
 * @version $Id$
 */
public final class AddToBasketAspect 
extends AbstractAspect {

    /** The basket manager. */
    protected BasketManager basketManager;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager serviceManager) throws ServiceException {
        super.service(serviceManager);
        this.basketManager = (BasketManager)this.manager.lookup(BasketManager.ROLE);
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.basketManager);
            this.basketManager = null;
        }
        super.dispose();
    }

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.om.Layout, org.xml.sax.ContentHandler)
     */
    public void toSAX(RendererAspectContext rendererContext,
                      Layout layout,
                      ContentHandler contenthandler)
    throws SAXException, LayoutException {
        final CopletInstance cid = this.getCopletInstance(((CopletLayout)layout).getCopletInstanceId());
        final ContentStore store;
        final String elementName;
        if ( rendererContext.getAspectConfiguration().equals(Boolean.TRUE) ) {
            store = this.basketManager.getBasket();
            elementName = "basket-add-content";
        } else {
            store = this.basketManager.getBriefcase();
            elementName = "briefcase-add-content";
        }

        Boolean b = (Boolean)cid.getCopletDefinition().getAttribute("basket-content");
        if ( b != null && b.equals(Boolean.TRUE) ) {
            Object item = new ContentItem(cid, true);
            Event event = new AddItemEvent(store, item);
            XMLUtils.createElement(contenthandler, elementName, rendererContext.getPortalService().getLinkService().getLinkURI(event));
        }
        b = (Boolean)cid.getCopletDefinition().getAttribute("basket-link");
        if ( b != null && b.equals(Boolean.TRUE) ) {
            Object item = new ContentItem(cid, false);
            Event event = new AddItemEvent(store, item);
            XMLUtils.createElement(contenthandler, elementName, rendererContext.getPortalService().getLinkService().getLinkURI(event));            
        }

        rendererContext.invokeNext( layout, contenthandler );
    }

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.impl.AbstractAspect#prepareConfiguration(java.util.Properties)
     */
    public Object prepareConfiguration(Properties configuration)
    throws PortalException {
        if ( configuration.getProperty("use-store", "basket").equalsIgnoreCase("basket") ) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
}
