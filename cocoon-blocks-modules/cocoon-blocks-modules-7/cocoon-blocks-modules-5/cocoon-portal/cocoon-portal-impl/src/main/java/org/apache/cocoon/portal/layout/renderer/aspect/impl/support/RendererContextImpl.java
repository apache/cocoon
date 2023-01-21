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
package org.apache.cocoon.portal.layout.renderer.aspect.impl.support;

import java.util.Iterator;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutException;
import org.apache.cocoon.portal.services.aspects.support.BasicAspectContextImpl;
import org.apache.commons.collections.iterators.EmptyIterator;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * The renderer aspect context is passed to every renderer aspect.
 * Using this context, a renderer aspect can get it's configuration
 * and it can invoke (if wanted) the next aspect in the aspect chain.
 *
 * @version $Id$
 */
public final class RendererContextImpl
    extends BasicAspectContextImpl
    implements RendererAspectContext {

    /** The current configuration object for the renderer aspect. */
    protected Object aspectConfiguration;

    /** The iterator used to iterate through the configuration objects. */
    protected final Iterator configurationIterator;

    public RendererContextImpl(PortalService service, RendererAspectChain chain) {
        super(service, chain);
        if ( chain != null ) {
            this.configurationIterator = chain.getConfigurationIterator();
        } else {
            this.configurationIterator = EmptyIterator.INSTANCE;
        }
    }

	/**
	 * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext#invokeNext(org.apache.cocoon.portal.om.Layout, org.xml.sax.ContentHandler)
	 */
	public void invokeNext(Layout        layout,
                           ContentHandler handler)
	throws SAXException, LayoutException {
        final RendererAspect aspect = (RendererAspect)this.getNext();
		if ( aspect != null ) {
            aspect.toSAX(this, layout, handler);
		}
	}

	/**
	 * @see org.apache.cocoon.portal.services.aspects.support.BasicAspectContextImpl#getNext()
	 */
	protected Object getNext() {
        final Object o = super.getNext();
        if ( o != null ) {
            this.aspectConfiguration = this.configurationIterator.next();
        }
        return o;
    }

    /**
	 * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext#getAspectConfiguration()
	 */
	public Object getAspectConfiguration() {
		return this.aspectConfiguration;
	}
}
