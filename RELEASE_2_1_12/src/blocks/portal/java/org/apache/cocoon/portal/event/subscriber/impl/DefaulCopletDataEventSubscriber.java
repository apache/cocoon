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
package org.apache.cocoon.portal.event.subscriber.impl;

import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.CopletDataEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.impl.ChangeCopletsJXPathEvent;
import org.apache.cocoon.portal.event.impl.CopletJXPathEvent;
import org.apache.cocoon.portal.profile.ProfileManager;

/**
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id$
 */
public final class DefaulCopletDataEventSubscriber 
implements Receiver {

    /**
     * Constructor
     */
    public DefaulCopletDataEventSubscriber() {
        // nothing to do 
    }

    /**
     * @see Receiver
     */
    public void inform(CopletDataEvent e, PortalService service) {
        CopletData data = (CopletData)e.getTarget();
        List instances = null;

        ProfileManager profileManager = service.getComponentManager().getProfileManager();
        instances = profileManager.getCopletInstanceData(data);

        if ( instances != null && e instanceof ChangeCopletsJXPathEvent ) {
            EventManager eventManager = service.getComponentManager().getEventManager();
            final String path = ((ChangeCopletsJXPathEvent)e).getPath();
            final Object value = ((ChangeCopletsJXPathEvent)e).getValue();
                    
            Iterator i = instances.iterator();
            while ( i.hasNext() ) {
                CopletInstanceData current = (CopletInstanceData) i.next();
                Event event = new CopletJXPathEvent(current, path, value);
                eventManager.send(event);
            }
        }
    }

}
