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
package org.apache.cocoon.portal.pluto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.CopletInstanceEvent;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.ConvertableEvent;
import org.apache.cocoon.portal.event.impl.FullScreenCopletEvent;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.pluto.om.PortletEntityImpl;
import org.apache.cocoon.portal.pluto.om.PortletWindowImpl;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.information.PortletURLProvider;

/**
 * Create the URL for a portlet.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id$
 */
public class PortletURLProviderImpl 
       implements PortletURLProvider, CopletInstanceEvent, ConvertableEvent {
    
    /** The portlet window (target) */
    protected final PortletWindow portletWindow;
    
    /** The new portlet mode */
    protected PortletMode mode;
    
    /** The new window state */
    protected WindowState state;
    
    /** Is this an action */
    protected boolean action;
    
    /** Secure link? */
    protected Boolean secure;
    
    /** Clear parameters */
    protected boolean clearParameters;
    
    /** Parameters */
    protected Map parameters;
    
    /** The generated url */
    protected String generatedURL;
    private final LinkService linkService;
    private static final String DEFAULT_PORTLET_URL_REQUEST_PARAM = "url";

    /**
     * Constructor
     */
    public PortletURLProviderImpl(PortletWindow portletWindow,
                                  ServiceManager manager) {
        this.portletWindow = portletWindow;
        PortalService service = null;
        try {
            service = (PortalService) manager.lookup(PortalService.ROLE);
            this.linkService = service.getComponentManager().getLinkService();
        } catch (ServiceException se) {
            throw new CascadingRuntimeException("Unable to lookup portal service.", se);
        } finally {
            manager.release(service);
        }
    }

    /**
     * Constructor for factory
     * @param service
     * @param eventData
     */
    PortletURLProviderImpl(PortalService service, String eventData) {
        this.linkService = service.getComponentManager().getLinkService();
        PortletURLConverter urlConverter = new PortletURLConverter(eventData);
        String copletId = urlConverter.getPortletId();
        CopletInstanceData cid = service.getComponentManager().getProfileManager()
            .getCopletInstanceData(copletId);
        this.portletWindow = (PortletWindow)cid.getTemporaryAttribute("window");
        this.mode = urlConverter.getMode();
        this.state = urlConverter.getState();
        this.action = urlConverter.isAction();
        this.parameters = urlConverter.getParameters();
        this.clearParameters = false;
        this.secure = null;
    }

    /**
     * Copy constructor
     */
    private PortletURLProviderImpl(PortletURLProviderImpl original) {
        this.linkService = original.linkService;
        this.portletWindow = original.portletWindow;
        this.mode = original.mode;
        this.state = original.state;
        this.action = original.action;
        this.secure = original.secure;
        this.clearParameters = original.clearParameters;
        this.generatedURL = original.generatedURL;
        if (original.parameters != null) {
            this.parameters = new HashMap(original.parameters.size());
            this.parameters.putAll(original.parameters);
        }
    }

    /**
     * Return the window
     */
    public PortletWindow getPortletWindow() {
        return this.portletWindow;
    }
    
    /**
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
    
    /**
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

    /**
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
        
    /**
     * @see org.apache.pluto.services.information.PortletURLProvider#setSecure()
     */
    public void setSecure() {
        this.secure = Boolean.TRUE;
    }

    /**
     * @see org.apache.pluto.services.information.PortletURLProvider#clearParameters()
     */
    public void clearParameters() {
        this.clearParameters = true;
    }

    /**
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

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new PortletURLProviderImpl(this).getURL();
    }

    /**
     * @see java.lang.Object#toString()
     */
    private String getURL() {
        if ( this.generatedURL == null ) {
            final PortletWindowImpl impl = (PortletWindowImpl)this.portletWindow;
            final CopletLayout cl = impl.getLayout();
            Event sizingEvent = null;
            if ( cl != null ) {
                final CopletInstanceData cid = cl.getCopletInstanceData();
                String oldStateString = (String)cid.getTemporaryAttribute("window-state");
                WindowState oldState = null;
                if ( oldStateString != null ) {
                    oldState = new WindowState(oldStateString);
                } else {
                    oldState = WindowState.NORMAL;
                }
                if ( this.state != null && !this.state.equals(oldState) ) {
                    if ( oldState.equals(WindowState.MAXIMIZED) ) {
                        sizingEvent = new FullScreenCopletEvent( cid, null );                    
                    } else {
                        if ( this.state.equals(WindowState.MAXIMIZED) ) {
                            sizingEvent = new FullScreenCopletEvent( cid, cl );                                            
                        }
                    }
                }
            }

            List l = new ArrayList();
            if ( sizingEvent != null ) {
                l.add(sizingEvent);
            }
            l.add(this);
            if (secure == null) {
                this.generatedURL = this.linkService.getLinkURI(l);
            } else {
                this.generatedURL = this.linkService.getLinkURI(l, secure);
            }
        }
        return linkService.encodeURL(this.generatedURL);
    }

    /**
     * @see org.apache.cocoon.portal.event.ActionEvent#getTarget()
     */
    public Object getTarget() {        
        return ((PortletEntityImpl)this.portletWindow.getPortletEntity()).getCopletInstanceData();
    }

    /**
     * Return the URL as a String
     *
     * @return The URL as a String
     */
    public String asString() {

        final PortletWindowImpl impl = (PortletWindowImpl) this.portletWindow;
        final CopletLayout cl = impl.getLayout();
        if (cl == null) {
            return "";
        }
        final CopletInstanceData cid = cl.getCopletInstanceData();
        PortletURLConverter urlConverter = new PortletURLConverter(cid);

        if (this.mode != null) {
            urlConverter.setMode(this.mode);
        }

        if (this.state != null) {
            urlConverter.setState(this.state);
        }

        if (this.action) {
            urlConverter.setAction();
        }

        if (this.parameters != null) {
            Iterator entries = this.parameters.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry)entries.next();
                String name = (String) entry.getKey();
                Object value = entry.getValue();
                String[] values = value instanceof String ?
                    new String[]{(String) value} : (String[]) value;
                urlConverter.setParam(name, values);
            }
        }

        return urlConverter.toString();
    }

    /**
     * The request parameter to be used for this event (if events are not hidden)
     *
     * @return The request parameter name for this event.
     */
    public String getRequestParameterName() {
        return DEFAULT_PORTLET_URL_REQUEST_PARAM;
    }

}
