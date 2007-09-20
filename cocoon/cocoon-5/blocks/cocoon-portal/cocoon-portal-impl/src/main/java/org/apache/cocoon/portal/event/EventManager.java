/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
