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
package org.apache.cocoon.portal.event.aspect.impl;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.event.Receiver;
import org.apache.cocoon.portal.event.aspect.EventAspect;
import org.apache.cocoon.portal.event.aspect.EventAspectContext;
import org.apache.cocoon.portal.event.impl.FullScreenCopletEvent;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.impl.CopletLayout;

import java.util.List;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id$
 */
public class FullScreenCopletEventAspect
	extends AbstractLogEnabled
	implements EventAspect, 
                ThreadSafe, 
                Serviceable,
                Disposable, 
                Receiver, 
                Initializable {

    protected ServiceManager manager;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.event.aspect.EventAspect#process(org.apache.cocoon.portal.event.aspect.EventAspectContext, org.apache.cocoon.portal.PortalService)
	 */
	public void process(EventAspectContext context, PortalService service) {
        final String requestParameterName = FullScreenCopletEvent.REQUEST_PARAMETER_NAME;
        final Request request = ObjectModelHelper.getRequest( context.getObjectModel() );
        String[] values = request.getParameterValues( requestParameterName );
        if ( values != null ) {
            final EventManager publisher = service.getComponentManager().getEventManager();
            for(int i=0; i<values.length; i++) {
                final String current = values[i];
                Event e = context.getEventConverter().decode(current);
                if ( null != e ) {
                    publisher.send(e);
                    FullScreenCopletEvent fsce = (FullScreenCopletEvent)e;
                    if ( fsce.getLayout() != null) {
                        service.getComponentManager().getLinkService().addEventToLink( e );
                    }
                }
            }
        } else {
            List list = (List) request.getAttribute("org.apache.cocoon.portal." + requestParameterName);
            if (list != null) {
                FullScreenCopletEvent[] events =
                    (FullScreenCopletEvent[]) list.toArray(new FullScreenCopletEvent[0]);
                final EventManager publisher = service.getComponentManager().getEventManager();
                for (int i = 0; i < events.length; i++) {
                    FullScreenCopletEvent e = events[i];
                    publisher.send(e);
                    if (e.getLayout() != null) {
                        service.getComponentManager().getLinkService().addEventToLink(e);
                    }
                }
            }
        }
        // and invoke next one
        context.invokeNext( service );
	}

    /**
     * @see Receiver
     */
    public void inform(FullScreenCopletEvent event, PortalService service) {
        final Layout startingLayout = event.getLayout();
        PortalService portalService = null;
        try {
            portalService = (PortalService) this.manager.lookup(PortalService.ROLE);
            final Layout old = portalService.getEntryLayout(null);
            if ( old != null && old instanceof CopletLayout) {
                ((CopletLayout)old).getCopletInstanceData().setAspectData("fullScreen", Boolean.FALSE);
            }
            portalService.setEntryLayout( null, startingLayout );
            if ( startingLayout != null && startingLayout instanceof CopletLayout) {
                ((CopletLayout)startingLayout).getCopletInstanceData().setAspectData("fullScreen", Boolean.TRUE);
            }
        } catch (ServiceException ce) {
            // ignore
        } finally {
            this.manager.release(portalService);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() 
    throws Exception {
        EventManager eventManager = null;
        try {
            eventManager = (EventManager) this.manager.lookup( EventManager.ROLE );
            eventManager.subscribe( this );
        } finally {
            this.manager.release( eventManager );
        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            EventManager eventManager = null;
            try {
                eventManager = (EventManager) this.manager.lookup( EventManager.ROLE );
                eventManager.unsubscribe( this );
            } catch (Exception ignore) {
                // ignore this here
            } finally {
                this.manager.release( eventManager );
            }
        }
    }
}
