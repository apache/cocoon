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
package org.apache.cocoon.portal.event.aspect;

import org.apache.cocoon.portal.event.EventManager;

/**
 * An event aspect can be used to extend the functionality of an {@link EventManager}
 * without the need to change the current implementation of the event manager.
 *
 * Each event manager implementation can be configured with a set of event aspects.
 * The aspects are lined up in a chain and the event manager invokes the first event
 * aspect for each call to {@link EventManager#processEvents()}.
 * As the aspects are chained up, it's the responsibility of an event aspect to
 * invoke the next aspect in the chain. However, depending on the functionality
 * and implementation of the aspect, it's up to the aspect to decide whether it's
 * appropriate to invoke the next aspect in the chain. To invoke the next aspect,
 * {@link EventAspectContext#invokeNext()} has to be called.
 *
 * @version $Id$
 */
public interface EventAspect {

    /**
     * Process the current event phase.
     * @param context The event aspect context to access environment information and invoke
     *                the next event aspect in the chain.
     */
    void process(EventAspectContext context);
}
