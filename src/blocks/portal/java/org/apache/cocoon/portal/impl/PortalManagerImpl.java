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
package org.apache.cocoon.portal.impl;

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
 * @version CVS $Id: PortalManagerImpl.java,v 1.7 2004/03/05 13:02:13 bdelacretaz Exp $
 */
public class PortalManagerImpl
	extends AbstractLogEnabled
	implements PortalManager, Serviceable, ThreadSafe {

    protected ServiceManager manager;
        
    public void process()
    throws ProcessingException {
        EventManager eventManager = null;
        try {
            eventManager = (EventManager)this.manager.lookup(EventManager.ROLE);
            eventManager.processEvents();
        } catch (ServiceException ce) {
            throw new ProcessingException("Unable to lookup portal service.", ce);
        } finally {
            this.manager.release(eventManager);
        }
    }

	/**
	 * @see PortalManager#showPortal(ContentHandler, Parameters)
	 */
	public void showPortal(ContentHandler contentHandler, Parameters parameters) 
    throws SAXException {
//        final boolean useContentDeliverer = (parameters == null ? true :
//                                               parameters.getParameterAsBoolean("use-content-deliverer", true));
        
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            Layout portalLayout = service.getComponentManager().getProfileManager().getPortalLayout(null, null);

            Renderer portalLayoutRenderer = service.getComponentManager().getRenderer( portalLayout.getRendererName());       

            contentHandler.startDocument();
            portalLayoutRenderer.toSAX(portalLayout, service, contentHandler);
            contentHandler.endDocument();
        } catch (ServiceException ce) {
            throw new SAXException("Unable to lookup portal service.", ce);
        } finally {
            this.manager.release(service);
        }
	}

	/**
	 * @see org.apache.avalon.framework.service.Serviceable#service(ServiceManager)
	 */
	public void service(ServiceManager serviceManager)
    throws ServiceException {
        this.manager = serviceManager;
	}

}
