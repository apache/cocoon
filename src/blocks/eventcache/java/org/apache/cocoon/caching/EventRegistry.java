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
package org.apache.cocoon.caching;

import org.apache.avalon.framework.component.Component;
import org.apache.cocoon.caching.validity.Event;

/**
 * The <code>EventRegistry</code> is responsible for the two-way many-to-many
 * mapping between cache <code>Event</code>s and 
 * <code>PipelineCacheKey</code>s necessary to allow for efficient 
 * event-based cache invalidation.
 *  
 * @since 2.1
 * @author <a href="mailto:ghoward@apache.org">Geoff Howard</a>
 * @version CVS $Id: EventRegistry.java,v 1.1 2003/07/14 02:50:45 ghoward Exp $
 */
public interface EventRegistry extends Component {
    
    /**
     * The Avalon ROLE for this component
     */
    String ROLE = EventRegistry.class.getName();
    
    /**
     * Map an event to a key
     * 
     * @param event
     * @param key
     */
    public void register(Event e, PipelineCacheKey key);
    
    /**
     * Remove all occurances of the specified key from the registry.
     * 
     * @param key - The key to remove.
     */
    public void removeKey(PipelineCacheKey key);
    
    /**
     * Retrieve an array of all keys mapped to this event.
     * 
     * @param event
     * @return an array of keys which should not be modified or null if 
     *      no keys are mapped to this event.
     */
    public PipelineCacheKey[] keysForEvent(Event e);
    
    /**
     * Retrieve an array of all keys regardless of event mapping, or null if
     * no keys are registered..
     * 
     * @return an array of keys which should not be modified
     */
    public PipelineCacheKey[] allKeys(); 
    
    /**
     * Clear all event-key mappings from the registry.
     */
    public void clear();
    
    /**
     * Request that the registry get ready for normal operation.  Depending 
     * on the implementation, the component may need this opportunity to 
     * retrieve persisted data.
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
    public boolean init();
}
