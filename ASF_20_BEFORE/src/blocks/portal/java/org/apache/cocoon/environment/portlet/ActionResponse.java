/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: ActionResponse.java,v 1.1 2004/02/23 15:14:01 cziegeler Exp $
 */
public final class ActionResponse extends PortletResponse {

    private String uri;
    private ActionRequest request;

    /**
     * Creates a ActionResponse based on a real
     * {@see ActionResponse} object
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
     * set on response using {@see ActionResponse#setRenderParameter(String, String)}
     * method, {@see ActionResponse#sendRedirect(String)} method is not called.
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
     * Type cast portletResponse to {@see ActionResponse}
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
