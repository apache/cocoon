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

import org.apache.cocoon.caching.validity.Event;

/**
 * Defines the simple contract for components that need to receive notification 
 * of cache Events.
 * 
 * @author Geoff Howard (ghoward@apache.org)
 * @version CVS $Id: EventAware.java,v 1.3 2004/03/05 13:01:56 bdelacretaz Exp $
 */
public interface EventAware {

    /**
     * Receive notification of an Event.
     * 
     * @param e The Event
     */
    public void processEvent(Event e);

}
