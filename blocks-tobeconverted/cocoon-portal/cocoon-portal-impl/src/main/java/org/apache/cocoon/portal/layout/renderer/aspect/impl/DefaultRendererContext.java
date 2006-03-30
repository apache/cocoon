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
 * @version $Id$
 */
public final class DefaultRendererContext implements RendererAspectContext {

    private Iterator iterator;
    private Iterator configIterator;
    private Object config;
    private Map objectModel;

    public DefaultRendererContext(RendererAspectChain chain) {
        this.iterator = chain.getIterator();
        this.configIterator = chain.getConfigIterator();
    }

	/**
	 * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext#invokeNext(org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
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

	/**
	 * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext#getAspectConfiguration()
	 */
	public Object getAspectConfiguration() {
		return this.config;
	}

    /**
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
