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
package org.apache.cocoon.portal.event.impl;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.EventConverter;
import org.apache.cocoon.portal.event.Publisher;
import org.apache.cocoon.portal.event.aspect.EventAspect;
import org.apache.cocoon.portal.event.aspect.EventAspectContext;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: DefaultEventAspectContext.java,v 1.3 2004/03/05 13:02:12 bdelacretaz Exp $
 */
public final class DefaultEventAspectContext 
    implements EventAspectContext {

    private Iterator iterator;
    private Iterator configIterator;
    private Parameters config;
    
    private Publisher publisher;
    private Map objectModel;
    private EventConverter converter;

    public DefaultEventAspectContext(EventAspectChain chain) {
        this.iterator = chain.getIterator();
        this.configIterator = chain.getConfigIterator();
    }
    
	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.layout.renderer.RendererAspectContext#invokeNext(org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
	 */
	public void invokeNext(PortalService service) {
		if (iterator.hasNext()) {
            this.config = (Parameters) this.configIterator.next();
            final EventAspect aspect = (EventAspect) iterator.next();
            aspect.process( this, service );
		}

	}

	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.layout.renderer.RendererAspectContext#getConfiguration()
	 */
	public Parameters getAspectParameters() {
		return this.config;
	}

    /**
     * Get the encoder
     */
    public EventConverter getEventConverter(){
        return this.converter;
    }
    
    /**
     * Get the publisher
     */
    public Publisher getEventPublisher(){
        return this.publisher;
    }
    
    /**
     * Get the object model
     */
    public Map getObjectModel() {
        return this.objectModel;
    }

	/**
	 * @param converter
	 */
	public void setEventConverter(EventConverter converter) {
		this.converter = converter;
	}

	/**
	 * @param map
	 */
	public void setObjectModel(Map map) {
		objectModel = map;
	}

	/**
	 * @param publisher
	 */
	public void setEventPublisher(Publisher publisher) {
		this.publisher = publisher;
	}

}
