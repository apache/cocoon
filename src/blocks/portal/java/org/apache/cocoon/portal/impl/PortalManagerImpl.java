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
package org.apache.cocoon.portal.impl;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.PortalManager;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.renderer.Renderer;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id$
 */
public class PortalManagerImpl
	extends AbstractLogEnabled
	implements PortalManager, Serviceable, Disposable, ThreadSafe {

    /** The service manager */
    protected ServiceManager manager;

    /** The portal service */
    protected PortalService portalService;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(ServiceManager)
     */
    public void service(ServiceManager serviceManager)
    throws ServiceException {
        this.manager = serviceManager;
        this.portalService = (PortalService)this.manager.lookup(PortalService.ROLE);
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.portalService);
            this.portalService = null;
            this.manager = null;
        }
    }

    /**
     * @see org.apache.cocoon.portal.PortalManager#process()
     */
    public void process()
    throws ProcessingException {
        EventManager eventManager = this.portalService.getComponentManager().getEventManager();
        eventManager.processEvents();
    }

	/**
	 * @see PortalManager#showPortal(ContentHandler, Parameters)
	 */
	public void showPortal(ContentHandler contentHandler, Parameters parameters) 
    throws SAXException {
        // first check for a full screen layout
        Layout portalLayout = this.portalService.getEntryLayout(null);
        if ( portalLayout == null ) {
            portalLayout = this.portalService.getComponentManager().getProfileManager().getPortalLayout(null, null);
        }

        Renderer portalLayoutRenderer = this.portalService.getComponentManager().getRenderer( portalLayout.getRendererName());       

        contentHandler.startDocument();
        portalLayoutRenderer.toSAX(portalLayout, this.portalService, contentHandler);
        contentHandler.endDocument();
	}
}
