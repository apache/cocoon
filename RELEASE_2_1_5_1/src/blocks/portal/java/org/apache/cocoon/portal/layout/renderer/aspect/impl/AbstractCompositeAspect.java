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
package org.apache.cocoon.portal.layout.renderer.aspect.impl;

import java.util.Iterator;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.renderer.Renderer;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Insert a composite layout's elements into the resulting XML. Elements (items)
 * are processed in order. Concrete descendents of this class need to implement the
 * actual handling of layout elements.
 * 
 * <h2>Applicable to:</h2>
 * <ul>
 *  <li>{@link org.apache.cocoon.portal.layout.CompositeLayout}</li>
 * </ul>
 * 
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: AbstractCompositeAspect.java,v 1.7 2004/04/25 20:09:34 haul Exp $
 */
public abstract class AbstractCompositeAspect
    extends AbstractAspect {

	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.layout.renderer.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
	 */
	public void toSAX(RendererAspectContext context,
                		Layout layout,
                		PortalService service,
                		ContentHandler handler)
	throws SAXException {
        if ( layout instanceof CompositeLayout) {
            CompositeLayout compositeLayout = (CompositeLayout)layout;
            // loop over all rows
            for (Iterator iter = compositeLayout.getItems().iterator(); iter.hasNext();) {
                Item item = (Item) iter.next();
                this.processItem(item, handler, service);
            }
        } else {
            throw new SAXException("CompositeLayout expected.");
        }
	}

    /**
     * Process a single layout element. 
     * 
     * @param item layout item to be processed
     * @param handler SAX handler taking events
     * @param service portal service providing component access
     * @throws SAXException
     */
    protected abstract void processItem(Item item, ContentHandler handler, PortalService service)
        throws SAXException;

    /**
     * Default implementation for processing a Layout. Calls the associated
     * renderer for a layout to render it.
     */
    protected void processLayout(Layout layout, PortalService service, ContentHandler handler) throws SAXException {
        final String rendererName = layout.getRendererName();
        final Renderer renderer = service.getComponentManager().getRenderer(rendererName);
        renderer.toSAX(layout, service, handler);
    }

}
