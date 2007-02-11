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
package org.apache.cocoon.portal.event;

import org.apache.cocoon.ProcessingException;

/**
 * This component manages the event handling mechanism in the portal.
 * The event mechanism is based on the publisher/subscriber principle.
 * An interested component (a {@link org.apache.cocoon.portal.event.Receiver}
 * can subscribe itself for a specific class (or classes) of events.
 * All Events have a common ancestor type {@link Event} and the event types are 
 * identified by a (sub)class
 *
 * @version $Id$
 */
public interface EventManager {

    /**
     * Represents the role of the service
     */
    String ROLE = EventManager.class.getName(); 

    /**
     * Process the events
     */
    void processEvents()
    throws ProcessingException;

    /**
     * Publish an event. All registered receivers get notified.
     * @param event The event to broadcast.
     */
    void send(Event event);

    /**
     * Subscribes a receiver for a specific type of event.  
     */
    void subscribe(Receiver receiver);

    /**
     * Unsubscribes a receiver for all events.
     */
    void unsubscribe(Receiver receiver);
}
