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
import org.apache.cocoon.portal.event.Filter;
import org.apache.cocoon.portal.event.Publisher;
import org.apache.cocoon.portal.event.Subscriber;
import org.apache.cocoon.portal.event.aspect.EventAspect;
import org.apache.cocoon.portal.event.aspect.EventAspectContext;
import org.apache.cocoon.portal.event.impl.FullScreenCopletEvent;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.profile.ProfileManager;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: FullScreenCopletEventAspect.java,v 1.6 2004/03/05 13:02:12 bdelacretaz Exp $
 */
public class FullScreenCopletEventAspect
	extends AbstractLogEnabled
	implements EventAspect, 
                ThreadSafe, 
                Serviceable,
                Disposable, 
                Subscriber, 
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
            final Publisher publisher = context.getEventPublisher();
            for(int i=0; i<values.length; i++) {
                final String current = values[i];
                Event e = context.getEventConverter().decode(current);
                if ( null != e ) {
                    publisher.publish(e);
                    FullScreenCopletEvent fsce = (FullScreenCopletEvent)e;
                    if ( fsce.getLayout() != null) {
                        service.getComponentManager().getLinkService().addEventToLink( e );
                    }
                }
            }
        }
        // and invoke next one
        context.invokeNext( service );
	}

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.Subscriber#getEventType()
     */
    public Class getEventType() {
        return FullScreenCopletEvent.class;
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
    public void inform(Event event) {
        FullScreenCopletEvent e = (FullScreenCopletEvent) event;
        final Layout startingLayout = (CopletLayout)e.getLayout();
        PortalService portalService = null;
        try {
            portalService = (PortalService) this.manager.lookup(PortalService.ROLE);
            ProfileManager pm = portalService.getComponentManager().getProfileManager();
            final Layout old = pm.getEntryLayout();
            if ( old != null && old instanceof CopletLayout) {
                ((CopletLayout)old).getCopletInstanceData().setAspectData("fullScreen", Boolean.FALSE);
            }
            pm.setEntryLayout( startingLayout );
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
            eventManager.getRegister().subscribe( this );
        } finally {
            this.manager.release( eventManager );
        }
    }

    public void dispose() {
        if ( this.manager != null ) {
            EventManager eventManager = null;
            try {
                eventManager = (EventManager) this.manager.lookup( EventManager.ROLE );
                eventManager.getRegister().unsubscribe( this );
            } catch (Exception ignore) {
            } finally {
                this.manager.release( eventManager );
            }
        }
    }
}
