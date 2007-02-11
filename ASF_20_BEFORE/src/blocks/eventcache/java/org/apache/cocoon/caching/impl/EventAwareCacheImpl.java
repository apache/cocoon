/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

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
 * @version $Id: EventAwareCacheImpl.java,v 1.8 2004/02/07 15:20:09 joerg Exp $
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
