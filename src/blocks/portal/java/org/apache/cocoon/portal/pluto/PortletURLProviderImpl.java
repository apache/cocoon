/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.CopletInstanceEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.pluto.om.PortletEntityImpl;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.information.PortletURLProvider;

/**
 * Creste the URL for a portlet
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletURLProviderImpl.java,v 1.3 2004/03/15 14:29:09 cziegeler Exp $
 */
public class PortletURLProviderImpl 
       implements PortletURLProvider, CopletInstanceEvent {

    /** The service manager */
    protected final ServiceManager manager;
    
    /** The portlet window (target) */
    protected final PortletWindow portletWindow;
    
    /** The new portlet mode */
    protected PortletMode mode;
    
    /** The new window state */
    protected WindowState state;
    
    /** Is this an action */
    protected boolean action;
    
    /** Secure link? */
    protected boolean secure;
    
    /** Clear parameters */
    protected boolean clearParameters;
    
    /** Parameters */
    protected Map parameters;
    
    /**
     * Constructor
     */
    public PortletURLProviderImpl(PortletWindow portletWindow,
                                  ServiceManager manager) {
        this.manager = manager;
        this.portletWindow = portletWindow;
    }

    /**
     * Return the window
     */
    public PortletWindow getPortletWindow() {
        return this.portletWindow;
    }
    
    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.PortletURLProvider#setPortletMode(javax.portlet.PortletMode)
     */
    public void setPortletMode(PortletMode mode) {
        this.mode = mode;
    }

    /** 
     * Return the portlet mode
     */
    public PortletMode getPortletMode() {
        return this.mode;
    }
    
    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.PortletURLProvider#setWindowState(javax.portlet.WindowState)
     */
    public void setWindowState(WindowState state) {
        this.state = state;
    }

    /** 
     * Return the portlet mode
     */
    public WindowState getWindowState() {
        return this.state;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.PortletURLProvider#setAction()
     */
    public void setAction() {
        this.action = true;
    }

    /**
     * Is this an action?
     */
    public boolean isAction() {
        return this.action;
    }
        
    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.PortletURLProvider#setSecure()
     */
    public void setSecure() {
        this.secure = true;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.PortletURLProvider#clearParameters()
     */
    public void clearParameters() {
        this.clearParameters = true;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.PortletURLProvider#setParameters(java.util.Map)
     */
    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }
    
    /**
     * Return the parameters
     */
    public Map getParameters() {
        if ( this.parameters == null ) {
            return Collections.EMPTY_MAP;
        }
        return this.parameters;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        PortalService service = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            LinkService linkService = service.getComponentManager().getLinkService();
            
            //TODO - secure
            return linkService.getLinkURI(this);
            
        } catch (ServiceException se) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", se);
        } finally {
            this.manager.release(service);
        }
    }

    /**
     * Get the URI and add the event
     */
    public String toString(Event additionalEvent) {
        PortalService service = null;
        try {
            service = (PortalService) this.manager.lookup(PortalService.ROLE);
            LinkService linkService = service.getComponentManager().getLinkService();
            
            //TODO - secure
            List l = new ArrayList();
            if ( additionalEvent != null ) {
                l.add(additionalEvent);
            }
            l.add(this);
            return linkService.getLinkURI(l);
            
        } catch (ServiceException se) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", se);
        } finally {
            this.manager.release(service);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.event.ActionEvent#getTarget()
     */
    public Object getTarget() {        
        return ((PortletEntityImpl)this.portletWindow.getPortletEntity()).getCopletInstanceData();
    }

}
