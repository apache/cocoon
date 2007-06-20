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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.ConvertableEvent;
import org.apache.cocoon.portal.event.CopletInstanceEvent;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.services.LinkService;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.spi.PortletURLProvider;

/**
 * Create the URL for a portlet.
 *
 * @version $Id$
 */
public class PortletURLProviderImpl
       implements PortletURLProvider, CopletInstanceEvent, ConvertableEvent {

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

    /** Tbe coplet instance id. */
    protected final String copletInstanceId;

    /**
     * Constructor
     */
    public PortletURLProviderImpl(PortalService      service,
                                  PortletWindow      portletWindow,
                                  String             copletInstanceId) {
        this.linkService = service.getLinkService();
        this.copletInstanceId = copletInstanceId;
    }

    /**
     * Constructor for factory
     * @param service
     * @param eventData
     */
    public PortletURLProviderImpl(PortalService service,
                                  String        eventData) {
        this.linkService = service.getLinkService();
        final PortletURLConverter urlConverter = new PortletURLConverter(eventData);
        this.copletInstanceId = urlConverter.getCopletId();
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
        this.copletInstanceId = original.copletInstanceId;
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
     * @see org.apache.pluto.spi.PortletURLProvider#setPortletMode(javax.portlet.PortletMode)
     */
    public void setPortletMode(PortletMode mode) {
        this.mode = mode;
    }

    /**
     * @see org.apache.pluto.spi.PortletURLProvider#setWindowState(javax.portlet.WindowState)
     */
    public void setWindowState(WindowState state) {
        this.state = state;
    }

    /**
     * @see org.apache.pluto.spi.PortletURLProvider#setSecure()
     */
    public void setSecure() {
        this.secure =  Boolean.TRUE;
    }

    /**
     * @see org.apache.pluto.spi.PortletURLProvider#clearParameters()
     */
    public void clearParameters() {
        this.clearParameters = true;
    }

    /**
     * @see org.apache.pluto.spi.PortletURLProvider#setParameters(java.util.Map)
     */
    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return new PortletURLProviderImpl(this).getURL();
    }

    /**
     * @return
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
        return null;//((PortletEntityImpl)this.portletWindow.getPortletEntity()).getCopletInstanceData();
    }

    /**
     * Return the URL as a String.
     * @see org.apache.cocoon.portal.event.ConvertableEvent#asString()
     */
    public String asString() {
        PortletURLConverter urlConverter = new PortletURLConverter();
        urlConverter.setCopletId(this.copletInstanceId);
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
     * @see org.apache.pluto.spi.PortletURLProvider#setAction(boolean)
     */
    public void setAction(boolean flag) {
        this.action = flag;
    }
}
