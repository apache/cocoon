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
package org.apache.cocoon.portal.event.impl;

import java.util.Iterator;
import java.util.Properties;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.EventConverter;
import org.apache.cocoon.portal.event.aspect.EventAspect;
import org.apache.cocoon.portal.event.aspect.EventAspectContext;

/**
 *
 * @version $Id$
 */
public final class DefaultEventAspectContext 
    implements EventAspectContext {

    private Iterator iterator;
    private Iterator configIterator;
    private Properties config;

    private EventConverter converter;

    public DefaultEventAspectContext(EventAspectChain chain) {
        this.iterator = chain.getIterator();
        this.configIterator = chain.getConfigIterator();
    }

	/**
	 * @see org.apache.cocoon.portal.event.aspect.EventAspectContext#invokeNext(org.apache.cocoon.portal.PortalService)
	 */
	public void invokeNext(PortalService service) {
		if (iterator.hasNext()) {
            this.config = (Properties) this.configIterator.next();
            final EventAspect aspect = (EventAspect) iterator.next();
            aspect.process( this, service );
		}

	}

	/**
	 * @see org.apache.cocoon.portal.event.aspect.EventAspectContext#getAspectProperties()
	 */
	public Properties getAspectProperties() {
		return this.config;
	}

    /**
     * Get the encoder
     */
    public EventConverter getEventConverter(){
        return this.converter;
    }

	/**
	 * @param converter
	 */
	public void setEventConverter(EventConverter converter) {
		this.converter = converter;
	}
}
