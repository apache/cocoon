/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

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
 * @version CVS $Id: PortletURLProviderImpl.java,v 1.1 2004/01/22 14:01:21 cziegeler Exp $
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
            l.add(additionalEvent);
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
