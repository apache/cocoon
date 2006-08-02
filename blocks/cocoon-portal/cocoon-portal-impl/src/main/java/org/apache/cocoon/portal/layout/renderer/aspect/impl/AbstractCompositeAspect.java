/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
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
import org.apache.cocoon.portal.layout.LayoutFeatures;
import org.apache.cocoon.portal.layout.LayoutFeatures.RenderInfo;
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
 * @version $Id$
 */
public abstract class AbstractCompositeAspect
    extends AbstractAspect {

	/**
	 * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
	 */
	public void toSAX(RendererAspectContext rendererContext,
                	  Layout                layout,
                	  PortalService         service,
                	  ContentHandler        handler)
	throws SAXException {
        if ( layout instanceof CompositeLayout) {
            CompositeLayout compositeLayout = (CompositeLayout)layout;
        	// check for maximized information
        	final RenderInfo maximizedInfo = LayoutFeatures.getRenderInfo(layout);
            if ( maximizedInfo != null ) {
	        	// loop over all rows
	            for (Iterator iter = compositeLayout.getItems().iterator(); iter.hasNext();) {
	                Item item = (Item) iter.next();
	                if ( item.equals(maximizedInfo.item) ) {
	                    this.processMaximizedItem(rendererContext, item, maximizedInfo.layout, handler, service);
	                } else if ( item.getLayout().isStatic() ) {
	                    this.processItem(rendererContext, item, handler, service);	                	
	                }
	            }            	
            } else {
	        	// loop over all rows
	            for (Iterator iter = compositeLayout.getItems().iterator(); iter.hasNext();) {
	                Item item = (Item) iter.next();
	                this.processItem(rendererContext, item, handler, service);
	            }
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
    protected abstract void processItem(RendererAspectContext rendererContext,
                                        Item                  item,
                                        ContentHandler        handler,
                                        PortalService         service)
        throws SAXException;

    /**
     * Process an item containing a maximized layout.
     *
     * @param item layout item to be processed
     * @param maximizedLayout The maximized layout.
     * @param handler SAX handler taking events
     * @param service portal service providing component access
     * @throws SAXException
     */
    protected abstract void processMaximizedItem(RendererAspectContext rendererContext, Item item, Layout maximizedLayout, ContentHandler handler, PortalService service)
    throws SAXException;

    /**
     * Default implementation for processing a Layout. Calls the associated
     * renderer for a layout to render it.
     */
    protected void processLayout(Layout layout, PortalService service, ContentHandler handler) throws SAXException {
        if ( layout != null ) {
            final String rendererName = service.getLayoutFactory().getRendererName(layout);
            final Renderer renderer = service.getRenderer(rendererName);
            renderer.toSAX(layout, service, handler);
        }
    }
}
