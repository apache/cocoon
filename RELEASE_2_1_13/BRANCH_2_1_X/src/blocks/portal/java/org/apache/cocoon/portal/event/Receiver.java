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
 * A receiver registers its interest in a class
 * of events through the {@link org.apache.cocoon.portal.event.EventManager}.
 * An event is an object of the interface {@link org.apache.cocoon.portal.event.Event}
 * or a subclass/interface of it. Usually a receiver is not interested in
 * every event but only for some specific event types. These types are represented
 * by an own subclass/interface.
 * When a receiver subscribes itself at the event manager, the manager checks (using
 * reflection) for occurances of the method "inform" on the receiver. The signature
 * of the method consists of two parameters, where the first one is the event subclass
 * and the second one the PortalService.
 * If for example a receiver is interested in all {@link org.apache.cocoon.portal.event.CopletInstanceEvent}s
 * then it subscribes using the event manager and should provide an inform method
 * with the following signature:
 * public void inform(CopletInstanceEvent event, PortalService).
 *
 * If a receiver is interested in more than one event type, then it can implement
 * several inform methods each with the corresponding event class as the first
 * parameter. However, it is important to notice that the current implementation
 * of the event manager has some restrictions! A receiver should not implement to
 * inform methods where one event is a subclass or subinterface of the other event.
 * In that case only one method is called and it's not deterministic which one will be called
 * (this may vary for example on the used operating system the code runs on!).
 * This problem will be solved in 2.2.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 *
 * @version CVS $Id$
 */
public interface Receiver {

    // THIS IS JUST A MARKER INTERFACE!
}
