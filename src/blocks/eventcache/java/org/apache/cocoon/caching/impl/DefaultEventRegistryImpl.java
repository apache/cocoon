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

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.caching.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Constants;
import org.apache.cocoon.caching.EventRegistry;
import org.apache.cocoon.caching.validity.Event;
import org.apache.commons.collections.MultiHashMap;

/**
 * This implementation of <code>EventRegistry</code> stores the event-key 
 * mappings in a simple pair of <code>MultiMap</code>s.  It handles 
 * persistence by serializing an <code>EventRegistryDataWrapper</code> to 
 * disk.
 * 
 * @since 2.1
 * @author <a href="mailto:ghoward@apache.org">Geoff Howard</a>
 * @version CVS $Id: DefaultEventRegistryImpl.java,v 1.10 2004/02/27 17:27:04 unico Exp $
 */
public class DefaultEventRegistryImpl 
        extends AbstractLogEnabled
        implements EventRegistry, 
           Initializable,
           ThreadSafe,
           Disposable,
           Contextualizable {

	private boolean m_init_success = false;
	private File m_persistentFile;
	private static final String PERSISTENT_FILE = "ev_cache.ser";
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
     * Set up the persistence file.
     */
	public void contextualize(Context context) throws ContextException {
        org.apache.cocoon.environment.Context ctx =
                (org.apache.cocoon.environment.Context) context.get(
                                    Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        // set up file 
        m_persistentFile = new File(
                    ctx.getRealPath("/WEB-INF"), 
                        DefaultEventRegistryImpl.PERSISTENT_FILE);
        if (m_persistentFile == null) {
            throw new ContextException("Could not obtain persistent file. " +
                "The cache event registry cannot be " +
                "used inside an unexpanded WAR file.");
        }
	}

    /**
     * Recover state by de-serializing the data wrapper.  If this fails 
     * a new empty mapping is initialized and the Cache is signalled of 
     * the failure so it can clean up.
     * 
     * @return true if de-serializing was successful, false otherwise.
     */
	/*public boolean init() {
        return recover();
	}*/

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
     * @return true if persistent state existed and was recovered successfully.
     */
    public boolean wasRecoverySuccessful() {
        return m_init_success;
    }

    /** 
     * Clean up resources at container shutdown.  An EventRegistry must persist 
     * its data.  If the serialization fails, an error is logged but not thrown  
     * because missing/invalid state is handled at startup.
     */
    public void dispose() {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(
                                        new FileOutputStream(this.m_persistentFile));
            EventRegistryDataWrapper ecdw = wrapRegistry();
            oos.writeObject(ecdw);
            oos.flush();
        } catch (FileNotFoundException e) {
            getLogger().error("Unable to persist EventRegistry", e);
        } catch (IOException e) {
            getLogger().error("Unable to persist EventRegistry", e);
        } finally {
            try {
                if (oos != null) oos.close();
            } catch (IOException e) {}
        }
        m_keyMMap.clear();
        m_keyMMap = null;
        m_eventMMap.clear();
        m_eventMMap = null;
    }

	protected EventRegistryDataWrapper wrapRegistry() {
		EventRegistryDataWrapper ecdw = new EventRegistryDataWrapper();
		ecdw.setupMaps(this.m_keyMMap, this.m_eventMMap);
		return ecdw;
	}

    /* 
     * I don't think this needs to get synchronized because it should 
     * only be called during initialize, which should only be called 
     * once by the container.
     */
    protected boolean recover() {
        if (this.m_persistentFile.exists()) {
            ObjectInputStream ois = null;
            EventRegistryDataWrapper ecdw = null;
            try {
                ois = new ObjectInputStream(
                    new FileInputStream(this.m_persistentFile));
                ecdw = (EventRegistryDataWrapper)ois.readObject();
            } catch (FileNotFoundException e) {
                getLogger().error("Unable to retrieve EventRegistry", e);
                createBlankCache();
                return false;
            } catch (IOException e) {
                getLogger().error("Unable to retrieve EventRegistry", e);
                createBlankCache();
                return false;
            } catch (ClassNotFoundException e) {
                getLogger().error("Unable to retrieve EventRegistry", e);
                createBlankCache();
                return false;
            } finally {
                try {
                    if (ois != null) ois.close();
                } catch (IOException e) {
                    // ignore
                }
            }
            unwrapRegistry(ecdw);
        } else {
            getLogger().warn(this.m_persistentFile + " does not exist - Unable to " +
                "retrieve EventRegistry.");
            createBlankCache();
            return false;
        }
        return true;
    }

	protected void unwrapRegistry(EventRegistryDataWrapper ecdw) {
		this.m_eventMMap = ecdw.get_eventMap();
		this.m_keyMMap = ecdw.get_keyMap();
	}

    // TODO: don't hardcode initial size
    protected final void createBlankCache() {
        this.m_eventMMap = new MultiHashMap(100); 
        this.m_keyMMap = new MultiHashMap(100); 
    }

}
