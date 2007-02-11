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

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
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
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: AddToBasketAspect.java,v 1.2 2004/03/05 13:02:11 bdelacretaz Exp $
 */
public final class AddToBasketAspect extends AbstractAspect {

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.renderer.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
     */
    public void toSAX(RendererAspectContext context,
                        Layout layout,
                        PortalService service,
                        ContentHandler contenthandler)
    throws SAXException {
        CopletInstanceData cid = ((CopletLayout)layout).getCopletInstanceData();
        Boolean b = (Boolean)cid.getCopletData().getAttribute("basket-content");
        if ( b != null && b.equals(Boolean.TRUE) ) {
            Object item = new ContentItem(cid, true);
            Event event = new AddItemEvent(item);
            XMLUtils.createElement(contenthandler, "basket-add-content", service.getComponentManager().getLinkService().getLinkURI(event));
        }
        b = (Boolean)cid.getCopletData().getAttribute("basket-link");
        if ( b != null && b.equals(Boolean.TRUE) ) {
            Object item = new ContentItem(cid, false);
            Event event = new AddItemEvent(item);
            XMLUtils.createElement(contenthandler, "basket-add-link", service.getComponentManager().getLinkService().getLinkURI(event));            
        }
        
        context.invokeNext( layout, service, contenthandler );
    }

}
