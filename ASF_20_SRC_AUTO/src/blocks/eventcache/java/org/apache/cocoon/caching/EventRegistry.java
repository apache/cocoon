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
package org.apache.cocoon.caching;

import java.io.Serializable;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.caching.validity.Event;

/**
 * The <code>EventRegistry</code> is responsible for the two-way many-to-many
 * mapping between cache <code>Event</code>s and 
 * <code>PipelineCacheKey</code>s necessary to allow for efficient 
 * event-based cache invalidation.
 * 
 * Because persistence and recovery between application shutdown and startup are 
 * internal concerns they are not defined here even though it is expected that most 
 * real-world implementers of this interface would require these features.  
 * On the other hand, EventRegistry must help the Cache to ensure that outdated 
 * content is never served, even if that means discarding potentially valid cached 
 * entries.  For this reason, wasRecoverySuccessful() is defined here as part of 
 * the public contract with the Cache.
 *  
 * @since 2.1
 * @author <a href="mailto:ghoward@apache.org">Geoff Howard</a>
 * @version CVS $Id: EventRegistry.java,v 1.6 2004/03/05 13:01:56 bdelacretaz Exp $
 */
public interface EventRegistry extends Component {
    
    /**
     * The Avalon ROLE for this component
     */
    String ROLE = EventRegistry.class.getName();
    
    /**
     * Map an event to a key
     * 
     * @param e event
     * @param key key
     */
    public void register(Event e, Serializable key);
    
    /**
     * Remove all occurances of the specified key from the registry.
     * 
     * @param key - The key to remove.
     */
    public void removeKey(Serializable key);
    
    /**
     * Retrieve an array of all keys mapped to this event.
     * 
     * @param e event
     * @return an array of keys which should not be modified or null if 
     *      no keys are mapped to this event.
     */
    public Serializable[] keysForEvent(Event e);
    
    /**
     * Retrieve an array of all keys regardless of event mapping, or null if
     * no keys are registered..
     * 
     * @return an array of keys which should not be modified
     */
    public Serializable[] allKeys(); 
    
    /**
     * Clear all event-key mappings from the registry.
     */
    public void clear();
    
    /**
     * Returns whether the registry was successful in retrieving its 
     * persisted state during startup.
     * 
     * If recovering persisted data was not successful, the component must 
     * signal that the Cache may contain orphaned EventValidity objects by 
     * returning false.  The Cache should then ensure that all pipelines 
     * associated with EventValidities are either removed or re-associated 
     * (if possible).
     * 
     * @return true if the Component recovered its state successfully, 
     *          false otherwise.
     */
    public boolean wasRecoverySuccessful();
}
