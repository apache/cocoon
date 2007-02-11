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
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.caching.EventRegistry;
import org.apache.cocoon.caching.validity.Event;
import org.apache.commons.collections.MultiHashMap;

/**
 * This abstract base implementation of <code>EventRegistry</code> stores 
 * the event-key mappings in a simple pair of <code>MultiMap</code>s.  It 
 * leaves all persistence to its concrete subclasses.  To protect against 
 * future confusing inheritance trees, all internal implementation of the 
 * event-key mapping mechanism is hidden from its subclasses.  If future 
 * EventRegistry implementations desire to use a different event-key mapping 
 * strategy but share persistence code, this package should probably be 
 * refactored to employ composition rather than inheritance.  For now, 
 * simplicity favors inheritance.
 * 
 * @since 2.1
 * @author <a href="mailto:ghoward@apache.org">Geoff Howard</a>
 * @version CVS $Id: AbstractDoubleMapEventRegistry.java,v 1.2 2004/03/05 13:01:56 bdelacretaz Exp $
 */

public abstract class AbstractDoubleMapEventRegistry
    extends AbstractLogEnabled
    implements Initializable, EventRegistry, Disposable, ThreadSafe {

    private boolean m_init_success = false;
    private MultiHashMap m_keyMMap;
    private MultiHashMap m_eventMMap;
    
    /**
     * Registers (stores) a two-way mapping between this Event and this 
     * PipelineCacheKey for later retrieval.
     * 
     * @param e The event to 
     * @param key key
     */
    public void register(Event e, Serializable key) {
        synchronized(this) {
            m_keyMMap.put(key,e);
            m_eventMMap.put(e,key);
        }
    }

    /**
     * Remove all registered data.
     */
    public void clear() {
        synchronized(this) {
            m_keyMMap.clear();
            m_eventMMap.clear();
        }
    }

    /**
     * Retrieve all pipeline keys mapped to this event.
     */
    public Serializable[] keysForEvent(Event e) {
        Collection coll = (Collection)m_eventMMap.get(e);
        if (coll==null || coll.isEmpty()) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("The event map returned empty");
            }
            return null;
        } else {
            return (Serializable[])coll.toArray(new Serializable[coll.size()]);
        }
    }

    /**
     * Return all pipeline keys mapped to any event
     */
    public Serializable[] allKeys() {
        Set keys = this.m_keyMMap.keySet();
        return (Serializable[])keys.toArray(
                new Serializable[keys.size()]);
    }

    /**
     * When a CachedResponse is removed from the Cache, any entries 
     * in the event mapping must be cleaned up.
     */
    public void removeKey(Serializable key) {
        Collection coll = (Collection)m_keyMMap.get(key);
        if (coll==null || coll.isEmpty()) {
            return;
        } 
        // get the iterator over all matching PCK keyed 
        // entries in the key-indexed MMap.
        synchronized(this) {
            Iterator it = coll.iterator();
            while (it.hasNext()) {
                /* remove all entries in the event-indexed map where this
                 * PCK key is the value.
                 */ 
                Object o = it.next();
                if (o != null) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Removing from event mapping: " + o.toString());
                    }
                    m_eventMMap.remove(o,key);            
                }
            }
            
            // remove all entries in the key-indexed map where this PCK key 
            // is the key -- confused yet?
            m_keyMMap.remove(key);
        }
    }
    
    /**
     * Recover state by de-serializing the data wrapper.  If this fails 
     * a new empty mapping is initialized and the Cache is signalled of 
     * the failure so it can clean up.
     */
    public void initialize() throws Exception {
        if (recover()) {
            m_init_success = true;
        }
    }
    
    /** 
     * Delegate persistence to subclasses then clean up resources.
     */
    public void dispose() {
        EventRegistryDataWrapper ecdw = wrapRegistry();
        persist(ecdw);
        m_keyMMap.clear();
        m_keyMMap = null;
        m_eventMMap.clear();
        m_eventMMap = null;
    }

    /**
     * @return true if persistent state existed and was recovered successfully.
     */
    public boolean wasRecoverySuccessful() {
        return m_init_success;
    }
    
    protected EventRegistryDataWrapper wrapRegistry() {
        EventRegistryDataWrapper ecdw = new EventRegistryDataWrapper();
        ecdw.setupMaps(this.m_keyMMap, this.m_eventMMap);
        return ecdw;
    }
    
    protected void unwrapRegistry(EventRegistryDataWrapper ecdw) {
        this.m_eventMMap = ecdw.get_eventMap();
        this.m_keyMMap = ecdw.get_keyMap();
    }

    protected final void createBlankCache() {
        // TODO: don't hardcode initial size
        this.m_eventMMap = new MultiHashMap(100); 
        this.m_keyMMap = new MultiHashMap(100); 
    }
    
    /** 
     * An EventRegistry must recover its persisted data.  Failed 
     * recovery must be signaled so that the Cache will know not to 
     * serve potentially stale content.  Of course, at first start up 
     * failed recovery is a normal state.
     * 
     * @return boolean to signal success or failure of recovery.  
     */
    protected abstract boolean recover();
    
    /**
     *  An EventRegistry must persist its data.
     */ 
    protected abstract void persist(EventRegistryDataWrapper wrapper);  
    
}
