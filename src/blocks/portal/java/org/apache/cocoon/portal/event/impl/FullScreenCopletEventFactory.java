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

package org.apache.cocoon.portal.event.impl;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.ConvertableEventFactory;
import org.apache.cocoon.portal.event.ConvertableEvent;

/**
 * Factory to create FullScreenEvents from marshalled data.
 *
 * @author <a href="mailto:rgoers@apache.org">Ralph Goers</a>
 * @version SVN $Id: $
 */
public class FullScreenCopletEventFactory implements ConvertableEventFactory {

    /**
     * Create an instance of the event
     * @param service The PortalService
     * @param eventData The marshalled data
     * @return an instance of the event
     */
    public ConvertableEvent createEvent(PortalService service, String eventData) {
        return new FullScreenCopletEvent(service, eventData);
    }
}