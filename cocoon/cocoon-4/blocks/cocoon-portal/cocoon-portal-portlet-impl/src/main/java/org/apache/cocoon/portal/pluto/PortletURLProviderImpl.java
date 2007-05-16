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

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.portal.PortalRuntimeException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.CopletInstanceEvent;
import org.apache.cocoon.portal.event.ConvertableEvent;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.pluto.adapter.PortletAdapter;
import org.apache.cocoon.portal.pluto.om.PortletEntityImpl;
import org.apache.cocoon.portal.pluto.om.PortletWindowImpl;
import org.apache.cocoon.portal.services.LinkService;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.information.PortletURLProvider;

/**
 * Create the URL for a portlet.
 *
 * @version $Id$
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

    /**
     * Constructor
     */
    public PortletURLProviderImpl(PortletWindow portletWindow,
                                  ServiceManager manager) {
        this.portletWindow = portletWindow;
        PortalService service = null;
        try {
            service = (PortalService) manager.lookup(PortalService.class.getName());
            this.linkService = service.getLinkService();
        } catch (ServiceException se) {
            throw new PortalRuntimeException("Unable to lookup portal service.", se);
        } finally {
            manager.release(service);
        }
    }

    /**
     * Constructor for factory
     * @param service
     * @param eventData
     */
    public PortletURLProviderImpl(PortalService service, String eventData) {
        this.linkService = service.getLinkService();
        PortletURLConverter urlConverter = new PortletURLConverter(eventData);
        String copletId = urlConverter.getPortletId();
        CopletInstance cid = service.getProfileManager()
            .getCopletInstance(copletId);
        this.portletWindow = (PortletWindow)cid.getTemporaryAttribute(PortletAdapter.PORTLET_WINDOW_ATTRIBUTE_NAME);
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
        this.secure =  Boolean.TRUE;
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
            this.generatedURL = this.linkService.getLinkURI(this, this.secure);
        }
        return linkService.encodeURL(this.generatedURL);
    }

    /**
     * @see org.apache.cocoon.portal.event.CopletInstanceEvent#getTarget()
     */
    public CopletInstance getTarget() {        
        return ((PortletEntityImpl)this.portletWindow.getPortletEntity()).getCopletInstanceData();
    }

    /**
     * Return the URL as a String
     *
     * @return The URL as a String
     */
    public String asString() {

        final PortletWindowImpl impl = (PortletWindowImpl) this.portletWindow;
        final CopletInstance cid = impl.getCopletInstanceData();
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

    public LinkService getLinkService() {
        return this.linkService;
    }
}
