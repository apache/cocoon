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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
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
 * @version CVS $Id: DefaultRendererContext.java,v 1.6 2004/04/25 20:09:34 haul Exp $
 */
public final class DefaultRendererContext implements RendererAspectContext {

    private Iterator iterator;
    private Iterator configIterator;
    private Object config;
    private Map attributes;
    private Map objectModel;
    
    public DefaultRendererContext(RendererAspectChain chain) {
        this.iterator = chain.getIterator();
        this.configIterator = chain.getConfigIterator();
    }
    
	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.layout.renderer.RendererAspectContext#invokeNext(org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
	 */
	public void invokeNext(Layout layout,
                    		PortalService service,
                    		ContentHandler handler)
	throws SAXException {
		if (iterator.hasNext()) {
            this.config = this.configIterator.next();
            final RendererAspect aspect = (RendererAspect) iterator.next();
            aspect.toSAX(this, layout, service, handler);
		}

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
