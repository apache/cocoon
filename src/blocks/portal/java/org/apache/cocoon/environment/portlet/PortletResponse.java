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

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Response;

import org.apache.avalon.framework.CascadingRuntimeException;

import javax.portlet.PortletPreferences;
import javax.portlet.ReadOnlyException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Implements the {@link Response} interface for the JSR-168 (Portlet) environment.
 *
 * @author <a href="mailto:alex.rudnev@dc.gov">Alex Rudnev</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: PortletResponse.java,v 1.2 2004/03/05 13:02:08 bdelacretaz Exp $
 */
public abstract class PortletResponse implements Response {

    /** The real PortletResponse object */
    private final javax.portlet.PortletResponse response;

    private final PortletPreferences preferences;

    /**
     * Stores property names set on the response.
     * Portlet response does not have containsProperty() method.
     */
    private Map properties = new HashMap(5);


    /**
     * Creates a PortletResponse based on a real PortletResponse object
     */
    protected PortletResponse(javax.portlet.PortletResponse response,
                              PortletPreferences preferences) {
        this.response = response;
        this.preferences = preferences;
    }


    public boolean containsHeader(String name) {
        return properties.containsKey(name);
    }

    public void setHeader(String name, String value) {
        properties.put(name, name);
        response.setProperty(name, value);
    }

    public void setIntHeader(String name, int value) {
        setHeader(name, "" + value);
    }

    public void setDateHeader(String name, long date) {
        setHeader(name, "" + date);
    }

    public void addHeader(String name, String value) {
        properties.put(name, name);
        response.addProperty(name, value);
    }

    public void addIntHeader(String name, int value) {
        addHeader(name, "" + value);
    }

    public void addDateHeader(String name, long date) {
        addHeader(name, "" + date);
    }


    public String getCharacterEncoding() {
        return null;
    }

    public Cookie createCookie(String name, String value) {
        return new PortletCookie(name, value);
    }

    public void addCookie(Cookie cookie) {
        try {
            this.preferences.setValue(cookie.getName(), cookie.getValue());
// TODO: When is good time to persist changes?
            this.preferences.store();
        } catch (ReadOnlyException e) {
            throw new CascadingRuntimeException("Cannot set read-only preference '" + cookie.getName() + "'", e);
        } catch (Exception e) {
            throw new CascadingRuntimeException("Cannot set preference '" + cookie.getName() + "'", e);
        }
    }

    public void setLocale(Locale locale) {
    }

    public Locale getLocale() {
        return null;
    }

    public String encodeURL(String url) {
// TODO: Why this check?
        if (url != null && url.indexOf(";jsessionid=") != -1) {
            return url;
        }
        return this.response.encodeURL(url);
    }


    // Portlet API related methods

    /**
     * Provides access to the underlying response object
     * @return portlet API response object
     */
    public javax.portlet.PortletResponse getPortletResponse() {
        return response;
    }

    public void addProperty(String key, String value) {
        getPortletResponse().addProperty(key, value);
    }

    public void setProperty(String key, String value) {
        getPortletResponse().setProperty(key, value);
    }


    // Portlet Environment Methods

    OutputStream getOutputStream() throws IOException {
        throw new IllegalStateException("Operation 'getOutputStream' is not supported by '" + getClass().getName() + "'");
    }

    void setContentType(String type) {
        throw new IllegalStateException("Operation 'setContentType' is not supported by '" + getClass().getName() + "'");
    }

    void sendRedirect(String location) throws IOException {
        throw new IllegalStateException("Operation 'sendRedirect' is not supported by '" + getClass().getName() + "'");
    }

    boolean isCommitted() {
        throw new IllegalStateException("Operation 'isCommitted' is not supported by '" + getClass().getName() + "'");
    }

    void reset() {
        throw new IllegalStateException("Operation 'reset' is not supported by '" + getClass().getName() + "'");
    }
}
