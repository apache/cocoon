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

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CachedResponse;
import org.apache.cocoon.caching.PipelineCacheKey;
import org.apache.cocoon.caching.validity.Event;
import org.apache.cocoon.caching.validity.EventValidity;
import org.apache.commons.collections.MultiHashMap;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AggregatedValidity;

/**
 * Very experimental start at external cache invalidation.
 * Warning - API very unstable.  Do not use!  
 * 
 * This implementation holds all mappings between Events and PipelineCacheKeys 
 * in two MultiHashMap to facilitate efficient lookup by either as Key.
 * 
 * TODO: Implement Persistence.
 * TODO: Test performance.
 * 
 * @author Geoff Howard (ghoward@apache.org)
 * @version $Id: EventAwareCacheImpl.java,v 1.1 2003/07/01 04:38:48 ghoward Exp $
 */
public class EventAwareCacheImpl extends CacheImpl {
    

	/** 
     * Clears the entire Cache, including all held event-pipeline key 
     * mappings..
     * 
	 * @see org.apache.cocoon.caching.Cache#clear()
	 */
	public void clear() {
		super.clear();
        m_keyMMap.clear();
        m_eventMMap.clear();
	}
    
    /**
	 * Compose
     * 
     * TODO: the Maps should not be initialized here (and should not be hardcoded size)
     * TODO: Attempt to recover/deserialize persisted event listing. (but not here)
     * 
     * @see org.apache.avalon.framework.component.Composable#compose(org.apache.avalon.framework.component.ComponentManager)
	 */
	public void compose(ComponentManager manager) throws ComponentException {
		super.compose(manager);
        this.m_eventMMap = new MultiHashMap(100); // TODO: don't hardcode initial size
        this.m_keyMMap = new MultiHashMap(100); // TODO: don't hardcode initial size
	}
    
    

	/** 
     * When a new Pipeline key is stored, it needs to be registered in 
     * the local Event-PipelineKey mapping.
     * 
	 * @see org.apache.cocoon.caching.Cache#store(java.util.Map, org.apache.cocoon.caching.PipelineCacheKey, org.apache.cocoon.caching.CachedResponse)
	 */
	public void store(Map objectModel,
                		PipelineCacheKey key,
                		CachedResponse response)
                		throws ProcessingException {
        SourceValidity[] validities = response.getValidityObjects();
        for (int i=0; i< validities.length;i++) {
            if (validities[i] instanceof AggregatedValidity) {
                 // AggregatedValidity must be investigated further.
                 Iterator it = ((AggregatedValidity)validities[i]).getValidities().iterator();
                 SourceValidity sv = null;
                 while (it.hasNext()) {
                     sv = (SourceValidity)it.next();
                     if (sv instanceof EventValidity) {
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("Found EventValidity in AggregatedValidity: " + sv.toString());
                        }
                        registerEvent( ((EventValidity)sv).getEvent(),key);                       
                      }   
                 }
            } else if (validities[i] instanceof EventValidity) {
                // Found a plain EventValidity.
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Found EventValidity: " + validities[i].toString());
                }
                registerEvent( ((EventValidity)validities[i]).getEvent(),key); 
            }
        
        }
		super.store(objectModel, key, response);
	}

	/**
     * When a CachedResponse is removed from the Cache, any entries in the event mapping 
     * must be cleaned up.
     * 
	 * @see org.apache.cocoon.caching.Cache#remove(org.apache.cocoon.caching.PipelineCacheKey)
	 */
	public void remove(PipelineCacheKey key) {
		super.remove(key);
        Collection coll = (Collection)m_keyMMap.get(key);
        if (coll==null || coll.isEmpty()) {
            return;
        } else {
            // get the iterator over all matching PCK keyed 
            // entries in the key-indexed MMap.
            Iterator it = coll.iterator();

            while (it.hasNext()) {
                // remove all entries in the event-indexed map where this PCK key 
                // is the value.
                Object o = it.next();
                if (o != null) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Removing from event mapping: " + o.toString());
                    }
                    m_eventMMap.remove((Event)o,key);            
                }
            }
        }
        // remove all entries in the key-indexed map where this PCK key 
        // is the key -- confused yet?
        m_keyMMap.remove(key);
	}
    
    public void processEvent(Event e) {
        Collection coll = (Collection)m_eventMMap.get(e);
        if (coll==null || coll.isEmpty()) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("The event map returned empty");
            }
            // return silently with no action
            return;
        } else {
            /* get the array of all matching event keyed entries 
             * in the event-indexed MMap.  Using an iterator gives 
             * a concurrent modification exception.
             */
            Object[] obs = coll.toArray();
            for (int i=0;i<obs.length; i++) {
                if (obs[i] != null) {
                    PipelineCacheKey pck = (PipelineCacheKey)obs[i];
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Processing cache event, found Pipeline key: " + pck.toString());
                    }
                    /* every pck associated with this event needs to be
                     * removed -- regardless of event mapping. and every 
                     * event mapped to those keys needs to be removed 
                     * recursively.
                     * 
                     * TODO: what happens in this recursive removal?  is 
                     * it a deadlock danger, or NPE danger?? 
                     */ 
                    remove(pck);
                }
            }
        }
        // This may be unnecessary because the pck removal is done recursively.
        m_eventMMap.remove(e);
    }

    /**
     * Registers (stores) a two-way mapping between this Event and this 
     * PipelineCacheKey for later retrieval on receipt of an event.
     * 
     * @param event 
     * @param key
     */
    private void registerEvent(Event e, PipelineCacheKey key) {
        m_keyMMap.put(key,e);
        m_eventMMap.put(e,key);
    }
    
    private MultiHashMap m_keyMMap;
    private MultiHashMap m_eventMMap;
    
	/** Release all held components.  
     * 
     * TODO: is this the place to persist the event mappings?
     * 
	 * @see org.apache.avalon.framework.activity.Disposable#dispose()
	 */
	public void dispose() {
		// TODO need to store event listing persistently - serialize?
        // for now: TODO: uncache all events.
		super.dispose();
	}

}
