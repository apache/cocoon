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

import java.util.Collection;
import java.util.HashMap;
import java.util.Vector;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.apache.cocoon.Constants;
import org.apache.cocoon.portal.RequestContext;
import org.apache.pluto.services.information.PortalContextProvider;

/**
 * Information about the portal.
 *
 * @version $Id$
 */
public class PortalContextProviderImpl
implements PortalContextProvider {

    /** Portal information */
    protected String info;

    /** supported portlet modes by this portal */
    protected Vector modes;

    /** supported window states by this portal */
    protected Vector states;

    /** portal properties */
    protected HashMap properties;

    /** The host name */
    protected String hostNameHTTP;

    /** The host name */
    protected String hostNameHTTPS;

    /** The host name */
    protected String contextHTTP;

    /** The host name */
    protected String contextHTTPS;

    /**
     * Constructor
     */
    public PortalContextProviderImpl(RequestContext context) {
        // these are the minimum modes that the portal needs to support
        this.modes = this.getDefaultModes();
        // these are the minimum states that the portal needs to support
        this.states = this.getDefaultStates();
        // set info
        this.info = "Apache Cocoon/" + Constants.VERSION;
        this.properties = new HashMap();
        this.init(context);
    }

    /**
     * @see org.apache.pluto.services.information.PortalContextProvider#getProperty(java.lang.String)
     */
    public String getProperty(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Property name == null");
        }

        return(String) properties.get(name);
    }

    /**
     * @see org.apache.pluto.services.information.PortalContextProvider#getPropertyNames()
     */
    public Collection getPropertyNames() {
        return properties.keySet();
    }

    /**
     * @see org.apache.pluto.services.information.PortalContextProvider#getSupportedPortletModes()
     */
    public Collection getSupportedPortletModes() {
        return this.modes;
    }

    /**
     * @see org.apache.pluto.services.information.PortalContextProvider#getSupportedWindowStates()
     */
    public Collection getSupportedWindowStates() {
        return this.states;
    }

    /**
     * @see org.apache.pluto.services.information.PortalContextProvider#getPortalInfo()
     */
    public String getPortalInfo() {
        return this.info;
    }

    /**
     * Return all default modes.
     */
    protected Vector getDefaultModes() {
        Vector m = new Vector();

        m.add(new PortletMode("view"));
        m.add(new PortletMode("edit"));
        m.add(new PortletMode("help"));
        m.add(new PortletMode("config"));

        return m;
    }

    /**
     * Return all default states.
     */
    protected Vector getDefaultStates() {
        Vector s = new Vector();

        s.add(new WindowState("normal"));
        s.add(new WindowState("minimized"));
        s.add(new WindowState("maximized"));

        return s;
    }

    /**
     * Initialize some infos.
     */
    protected void init(final RequestContext requestContext) {
        final HttpServletRequest request = requestContext.getRequest();
        final String hostName   = request.getServerName();
        final String contextRoot = request.getContextPath();
        final int hostPortHTTP  = request.getServerPort();
        final int hostPortHTTPS = 443;

        StringBuffer hostHTTP = new StringBuffer("http://");
        hostHTTP.append(hostName);
        if (hostPortHTTP != 80) {
            hostHTTP.append(":");
            hostHTTP.append(hostPortHTTP);
        }
        this.hostNameHTTP = hostHTTP.toString();
        hostHTTP.append('/');
        hostHTTP.append(contextRoot);
        this.contextHTTP = hostHTTP.toString();

        StringBuffer hostHTTPS = new StringBuffer("https://");
        hostHTTPS.append(hostName);
        if (hostPortHTTPS != 443) {
            hostHTTPS.append(":");
            hostHTTPS.append(hostPortHTTPS);
        }
        this.hostNameHTTPS = hostHTTPS.toString();
        hostHTTPS.append('/');
        hostHTTPS.append(contextRoot);
        this.contextHTTPS = hostHTTPS.toString();
    }

    public String getBaseURLexcludeContext(boolean secure) {
        return (secure?this.hostNameHTTPS : this.hostNameHTTP);
    }

    public String getBaseURL(boolean secure) {
        return (secure?this.contextHTTPS : this.contextHTTP);
    }
}
