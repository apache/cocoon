/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.caching.impl;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CachedResponse;
import org.apache.cocoon.caching.EventAware;
import org.apache.cocoon.caching.EventRegistry;
import org.apache.cocoon.caching.PipelineCacheKey;
import org.apache.cocoon.caching.validity.Event;
import org.apache.cocoon.caching.validity.EventValidity;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AbstractAggregatedValidity;

/**
 * This implementation holds all mappings between Events and PipelineCacheKeys 
 * in two MultiHashMap to facilitate efficient lookup by either as Key.
 * 
 * @author Geoff Howard (ghoward@apache.org)
 * @version $Id: EventAwareCacheImpl.java,v 1.9 2004/03/05 13:01:56 bdelacretaz Exp $
 */
public class EventAwareCacheImpl extends CacheImpl implements Initializable,
                                                              EventAware {
    
    private ServiceManager m_manager;

	private EventRegistry m_eventRegistry;

	/** 
     * Clears the entire Cache, including all registered event-pipeline key 
     * mappings..
	 */
	public void clear() {
		super.clear();
        m_eventRegistry.clear();
	}
    
	/** 
     * When a new Pipeline key is stored, it needs to be have its 
     * <code>SourceValidity</code> objects examined.  For every 
     * <code>EventValidity</code> found, its <code>Event</code> will be 
     * registered with this key in the <code>EventRegistry</code>.
     * 
     * <code>AggregatedValidity</code> is handled recursively.
	 */
	public void store(Serializable key,
                		CachedResponse response)
                		throws ProcessingException {
        SourceValidity[] validities = response.getValidityObjects();
        for (int i=0; i< validities.length;i++) {
            SourceValidity val = validities[i];
            examineValidity(val, key);
        }
        super.store(key, response);
	}

    /* (non-Javadoc)
     * @see org.apache.cocoon.caching.Cache#store(java.io.Serializable, org.apache.cocoon.caching.CachedResponse)
     
    public void store(Serializable key, CachedResponse response)
        throws ProcessingException {
        // TODO Auto-generated method stub
        super.store(key, response);
    }*/

    /**
     * Look up the EventRegistry 
     */
	public void service(ServiceManager manager) throws ServiceException {
		this.m_manager = manager;
        super.service(manager);
        this.m_eventRegistry = (EventRegistry)manager.lookup(EventRegistry.ROLE);
	}

	/**
     * Un-register this key in the EventRegistry in addition to 
     * removing it from the Store
	 */
	public void remove(PipelineCacheKey key) {
		super.remove(key);
        m_eventRegistry.removeKey(key);
	}
    
    /**
     * Receive notification about the occurrence of an Event.
     * If this event has registered pipeline keys, remove them 
     * from the Store and unregister them
     * @param e The Event to be processed.
     */
    public void processEvent(Event e) {
        if (e == null) return;
        Serializable[] keys = m_eventRegistry.keysForEvent(e);
        if (keys == null) return;
        for (int i=0;i<keys.length; i++) {
            if (keys[i] != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Processing cache event, found Pipeline key: " + keys[i].toString());
                }
                /* every pck associated with this event needs to be
                 * removed -- regardless of event mapping. and every 
                 * event mapped to those keys needs to be removed 
                 * recursively.
                 */ 
                remove(keys[i]);
            }
        }
    }
    
    /**
     * Get the EventRegistry ready, and make sure it does not contain 
     * orphaned Event/PipelineKey mappings.
     */
	public void initialize() throws Exception {
		if (!m_eventRegistry.wasRecoverySuccessful()) {
            super.clear();
        } else {
            // Not sure if we want this overhead here, but where else?
            veryifyEventCache();
        }
	}
    
    /**
     * Ensure that all PipelineCacheKeys registered to events still 
     * point to valid cache entries.  Having an isTotallyEmpty() on 
     * Store might make this less necessary, as the most likely time 
     * to discover orphaned entries is at startup.  This is because
     * stray events could hang around indefinitely if the cache is 
     * removed abnormally or is not configured with persistence.
     */
    public void veryifyEventCache() {
        Serializable[] keys = m_eventRegistry.allKeys();
        if (keys == null) return;
        for (int i=0; i<keys.length; i++) {
            if (!this.containsKey(keys[i])) {
                m_eventRegistry.removeKey(keys[i]);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Cache key no longer valid: " + 
                            keys[i]);
                }
            }
        }
    }

    /**
     * Release resources
     */
	public void dispose() {
        m_manager.release(m_eventRegistry);
		super.dispose();
        m_manager = null;
        m_eventRegistry = null;
	}

    private void examineValidity(SourceValidity val, Serializable key) {
        if (val instanceof AbstractAggregatedValidity) {
            handleAggregatedValidity((AbstractAggregatedValidity)val, key);
        } else if (val instanceof EventValidity) {
            handleEventValidity((EventValidity)val, key);
        }
    }

    private void handleAggregatedValidity(
                                    AbstractAggregatedValidity val,
                                    Serializable key) {
        // AggregatedValidity must be investigated further.
         Iterator it = val.getValidities().iterator();
         while (it.hasNext()) {
             SourceValidity thisVal = (SourceValidity)it.next();
             // Allow recursion
             examineValidity(thisVal, key);
         }
    }

    private void handleEventValidity(EventValidity val, Serializable key) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Found EventValidity: " + val.toString());
        }
        m_eventRegistry.register(val.getEvent(),key); 
    }

}
