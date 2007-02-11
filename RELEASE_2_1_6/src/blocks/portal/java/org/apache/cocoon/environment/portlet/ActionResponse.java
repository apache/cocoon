/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.environment.portlet;

import org.apache.cocoon.util.NetUtils;

import org.apache.avalon.framework.CascadingRuntimeException;

import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletPreferences;
import javax.portlet.WindowState;
import javax.portlet.WindowStateException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Implements the {@link org.apache.cocoon.environment.Response} interface for
 * the JSR-168 (Portlet) environment.
 *
 * @author <a href="mailto:vadim.gritsenko@dc.gov">Vadim Gritsenko</a>
 * @version CVS $Id: ActionResponse.java,v 1.3 2004/05/01 00:05:44 joerg Exp $
 */
public final class ActionResponse extends PortletResponse {

    private String uri;
    private ActionRequest request;

    /**
     * Creates a ActionResponse based on a real
     * {@link ActionResponse} object
     */
    protected ActionResponse(javax.portlet.ActionResponse response,
                             PortletPreferences preferences,
                             ActionRequest request,
                             String uri) {
        super(response, preferences);
        this.request = request;
        this.uri = uri;
    }

    // Response API methods

    /**
     *
     * @see PortletEnvironment#HEADER_PORTLET_MODE
     * @see PortletEnvironment#HEADER_WINDOW_STATE
     */
    public void addHeader(String name, String value) {
        if (PortletEnvironment.HEADER_PORTLET_MODE.equals(name)) {
            try {
                this.getActionResponse().setPortletMode(new PortletMode(value));
            } catch (PortletModeException e) {
                throw new CascadingRuntimeException("Cant set portlet mode '" + value + "'", e);
            }
        } else if (PortletEnvironment.HEADER_WINDOW_STATE.equals(name)) {
            try {
                this.getActionResponse().setWindowState(new WindowState(value));
            } catch (WindowStateException e) {
                throw new CascadingRuntimeException("Cant set window state '" + value + "'", e);
            }
        } else {
            super.addHeader(name, value);
        }
    }

    /**
     *
     * @see PortletEnvironment#HEADER_PORTLET_MODE
     * @see PortletEnvironment#HEADER_WINDOW_STATE
     */
    public void setHeader(String name, String value) {
        if (PortletEnvironment.HEADER_PORTLET_MODE.equals(name)) {
            try {
                this.getActionResponse().setPortletMode(new PortletMode(value));
            } catch (PortletModeException e) {
                throw new CascadingRuntimeException("Cant set portlet mode '" + value + "'", e);
            }
        } else if (PortletEnvironment.HEADER_WINDOW_STATE.equals(name)) {
            try {
                this.getActionResponse().setWindowState(new WindowState(value));
            } catch (WindowStateException e) {
                throw new CascadingRuntimeException("Cant set window state '" + value + "'", e);
            }
        } else {
            super.setHeader(name, value);
        }
    }

    /**
     * Implements redirect.
     *
     * Redirects to self (starting with the question mark) are processed
     * differently from other redirects: redirect parameters are parsed and
     * set on response using {@link ActionResponse#setRenderParameter(String, String)}
     * method, {@link ActionResponse#sendRedirect(String)} method is not called.
     *
     * @param location
     * @throws IOException
     */
    public void sendRedirect(String location) throws IOException {
        String servletPath = this.request.getServletPath();

        // Strip off parameters
        Map parameters = new HashMap(7);
        String absLoc = NetUtils.deparameterize(location, parameters);

        // Absolutize
        if (absLoc.length() > 0) {
            String base = NetUtils.getPath(uri);
            absLoc = NetUtils.absolutize(base, absLoc);
            absLoc = NetUtils.normalize(absLoc);
        } else {
            absLoc = uri;
        }

        // Redirect within the portlet?
        if (absLoc.startsWith(servletPath)) {
            String pathInfo = absLoc.substring(servletPath.length());

            for (Iterator i = parameters.keySet().iterator(); i.hasNext();) {
                String name = (String) i.next();
                String value = (String) parameters.get(name);
                getActionResponse().setRenderParameter(name, value);
            }
            getActionResponse().setRenderParameter(PortletEnvironment.PARAMETER_PATH_INFO, pathInfo);
        } else {
            getActionResponse().sendRedirect(location);
        }

/*
        if (location.startsWith("?")) {
            Map parameters = new HashMap(7);
            NetUtils.deparameterize(location, parameters);
            for (Iterator i = parameters.keySet().iterator(); i.hasNext();) {
                String name = (String)i.next();
                String value = (String)parameters.get(name);
                getActionResponse().setRenderParameter(name, value);
            }
        } else {
            getActionResponse().sendRedirect(location);
        }
*/
    }

    // ActionResponse API methods

    /**
     * Type cast portletResponse to {@link ActionResponse}
     *
     * @return type casted portletResponse
     */
    public javax.portlet.ActionResponse getActionResponse() {
        return (javax.portlet.ActionResponse) getPortletResponse();
    }

    public void setPortletMode(PortletMode mode) throws PortletModeException {
        getActionResponse().setPortletMode(mode);
    }

    public void setRenderParameter(String key, String value) {
        getActionResponse().setRenderParameter(key, value);
    }

    public void setRenderParameter(String key, String[] values) {
        getActionResponse().setRenderParameter(key, values);
    }

    public void setRenderParameters(Map parameters) {
        getActionResponse().setRenderParameters(parameters);
    }

    public void setWindowState(WindowState state) throws WindowStateException {
        getActionResponse().setWindowState(state);
    }


    // Portlet Environment API methods

    /**
     * Action response is always committed (in a sense that you cannot reset() response)
     */
    boolean isCommitted() {
        return true;
    }
}
