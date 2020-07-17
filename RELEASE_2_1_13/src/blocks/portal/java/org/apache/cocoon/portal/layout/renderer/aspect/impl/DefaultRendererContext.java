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
package org.apache.cocoon.portal.layout.renderer.aspect.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.portal.layout.renderer.Renderer;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * The renderer aspect context is passed to every renderer aspect.
 * Using this context, a renderer aspect can get it's configuration
 * and it can invoke (if wanted) the next aspect in the aspect chain.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id$
 */
public final class DefaultRendererContext implements RendererAspectContext {

    private Iterator iterator;
    private Iterator configIterator;
    private Object config;
    private Map attributes;
    private Map objectModel;
    private boolean isRendering;
    private boolean isRequired;
    
    public DefaultRendererContext(RendererAspectChain chain, Layout layout, PortalService service) {
        this.iterator = chain.getIterator();
        this.configIterator = chain.getConfigIterator();
        this.isRequired = chain.isRequired();
        Layout entryLayout = service.getEntryLayout(null);
        if (service.isRenderable().booleanValue()) {
            this.isRendering = true;
            return;
        }
        if (entryLayout == layout) {
            this.isRendering = true;
            service.setRenderable(Boolean.TRUE);
        }
    }
    
	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.layout.renderer.RendererAspectContext#invokeNext(org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
	 */
	public void invokeNext(Layout layout,
                    		PortalService service,
                    		ContentHandler handler)
	throws SAXException {
        if (!this.isRendering && !this.isRequired) {
            if (layout instanceof CompositeLayout) {
                CompositeLayout compositeLayout = (CompositeLayout)layout;
                for (Iterator iter = compositeLayout.getItems().iterator(); iter.hasNext();) {
                    Layout itemLayout = ((Item) iter.next()).getLayout();
                    if ( itemLayout != null ) {
                        final String rendererName = itemLayout.getRendererName();
                        final Renderer renderer = service.getComponentManager().getRenderer(rendererName);
                        renderer.toSAX(itemLayout, service, handler);
                    }
                }
            }
            return;
        }
        if (iterator.hasNext()) {
            this.config = this.configIterator.next();
            final RendererAspect aspect = (RendererAspect) iterator.next();
            aspect.toSAX(this, layout, service, handler);
		}
	}

    public boolean isRendering() {
        return this.isRendering;
    }

    /* (non-Javadoc)
    * @see org.apache.cocoon.portal.layout.renderer.RendererAspectContext#getConfiguration()
    */
	public Object getAspectConfiguration() {
		return this.config;
	}

    /**
     * Set an attribute
     */
    public void setAttribute(String key, Object attribute) {
        if ( key != null ) {
            if ( this.attributes == null ) {
                this.attributes = new HashMap(10); 
            }
            this.attributes.put( key, attribute );
        }
    }

    /**
     * Get an attribute
     */
    public Object getAttribute(String key) {
        if ( key != null && this.attributes != null) {
            return this.attributes.get( key );
        }
        return null;
    }

    /**
     * Remove an attribute
     */
    public void removeAttribute(String key) {
        if ( this.attributes != null && key != null) {
            this.attributes.remove( key );
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext#getObjectModel()
     */
    public Map getObjectModel() {
        return this.objectModel;
    }

    /**
     * Set the object model
     * @param map The object model
     */
    public void setObjectModel(Map map) {
        this.objectModel = map;
    }

}
