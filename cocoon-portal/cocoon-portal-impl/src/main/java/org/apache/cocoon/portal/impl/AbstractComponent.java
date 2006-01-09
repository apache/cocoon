/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.impl;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.Receiver;

/**
 * This class can be used as a base class for all portal related components.
 * It already implements some Avalon lifecycle interfaces and stores
 * the portal service in an instance variable ({@link #portalService}) and
 * the Avalon component context in another one.
 *
 * If the sub class implements the {@link org.apache.cocoon.portal.event.Receiver}
 * interface, the component is subscribed/unsubcribed to/from the {@link org.apache.cocoon.portal.event.EventManager}.
 *
 * @version $Id$
 */
public class AbstractComponent
    extends AbstractLogEnabled
    implements Contextualizable, Serviceable, Disposable, ThreadSafe, Initializable {
    
    // Implement Preloadable so that it automatically subscribes to events.

    /** The service manager. */
    protected ServiceManager manager;

    /** The portal service. */
    protected PortalService portalService;

    /** The application context */
    protected Context context;

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.portalService = (PortalService) this.manager.lookup(PortalService.ROLE);
    }
    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            if ( this instanceof Receiver && this.portalService != null ) {
                this.portalService.getEventManager().unsubscribe((Receiver)this);
            }
            this.manager.release(this.portalService);
            this.portalService = null;
            this.manager = null;
        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        if ( this instanceof Receiver ) {
            this.portalService.getEventManager().subscribe((Receiver)this);
        }
    }
}
