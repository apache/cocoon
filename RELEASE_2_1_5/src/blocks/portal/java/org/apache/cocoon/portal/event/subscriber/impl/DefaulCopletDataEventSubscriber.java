/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.event.subscriber.impl;

import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.CopletDataEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.Filter;
import org.apache.cocoon.portal.event.Publisher;
import org.apache.cocoon.portal.event.Subscriber;
import org.apache.cocoon.portal.event.impl.ChangeCopletsJXPathEvent;
import org.apache.cocoon.portal.event.impl.CopletJXPathEvent;
import org.apache.cocoon.portal.profile.ProfileManager;

/**
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: DefaulCopletDataEventSubscriber.java,v 1.2 2004/03/05 13:02:12 bdelacretaz Exp $
 */
public final class DefaulCopletDataEventSubscriber 
implements Subscriber, Serviceable {

    /** The service manager */
    protected ServiceManager manager;
    
    /**
     * Constructor
     */
    public DefaulCopletDataEventSubscriber() {
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#getEventType()
     */
    public Class getEventType() {
        return CopletDataEvent.class;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#getFilter()
     */
    public Filter getFilter() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#inform(org.apache.cocoon.portal.event.Event)
     */
    public void inform(Event e) {
        CopletData data = (CopletData) ((CopletDataEvent)e).getTarget();
        PortalService service = null;
        List instances = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            ProfileManager profileManager = service.getComponentManager().getProfileManager();
            instances = profileManager.getCopletInstanceData(data);
        } catch (Exception ignore) {
        } finally {
            this.manager.release(service);
        }
        if ( instances != null ) {
            Publisher publisher = null;
            EventManager eventManager = null;
            try {
                eventManager = (EventManager) this.manager.lookup(EventManager.ROLE);
                publisher = eventManager.getPublisher();
            } catch (Exception ignore) {
            } finally {
                this.manager.release(eventManager);
            }

            if ( publisher != null ) {
                if ( e instanceof ChangeCopletsJXPathEvent ) {
                    final String path = ((ChangeCopletsJXPathEvent)e).getPath();
                    final Object value = ((ChangeCopletsJXPathEvent)e).getValue();
                    
                    Iterator i = instances.iterator();
                    while ( i.hasNext() ) {
                        CopletInstanceData current = (CopletInstanceData) i.next();
                        Event event = new CopletJXPathEvent(current, path, value);
                        publisher.publish(event);
                    }
                }
            }
        }
    }

}
