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
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.portlet.multipart.MultipartActionRequest;

import org.apache.avalon.framework.CascadingRuntimeException;

import javax.portlet.PortalContext;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.WindowState;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * Implements the {@link Request} interface for the JSR-168 (Portlet) environment.
 *
 * @author <a href="mailto:alex.rudnev@dc.gov">Alex Rudnev</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: PortletRequest.java,v 1.3 2004/06/18 16:45:57 vgritsenko Exp $
 */
public abstract class PortletRequest implements Request {

    /** Portlet request does not has servletPath, so it will be passed via constructor */
    private String servletPath;

    /** Portlet request does not has pathInfo, so it will be passed via constructor */
    private String pathInfo;

    /** The real PortletRequest object */
    private final javax.portlet.PortletRequest request;

    /** The HttpEnvironment object */
    private final PortletEnvironment environment;

    /** The character encoding of parameters */
    private String form_encoding;

    /** The default form encoding of the servlet container */
    private String container_encoding;

    /** The current session */
    private PortletSession session;

    private Cookie[] wrappedCookies;
    private Map wrappedCookieMap;
    protected String portletRequestURI;


    /**
     * Creates a PortletRequest based on a real PortletRequest object
     */
    protected PortletRequest(String servletPath,
                             String pathInfo,
                             javax.portlet.PortletRequest request,
                             PortletEnvironment environment) {
        super();
        this.servletPath = servletPath;
        this.pathInfo = pathInfo;
        this.request = request;
        this.environment = environment;
    }

    public Object get(String name) {
        // if the request has been wrapped then access its method
        if (request instanceof MultipartActionRequest) {
            return ((MultipartActionRequest) request).get(name);
        } else {
            String[] values = request.getParameterValues(name);
            if (values == null) {
                return null;
            }
            if (values.length == 1) {
                return values[0];
            }
            if (values.length > 1) {
                Vector vect = new Vector(values.length);
                for (int i = 0; i < values.length; i++) {
                    vect.add(values[i]);
                }
                return vect;
            }
            return null;
        }
    }

    /* The Request interface methods */

    public String getAuthType() {
        return this.request.getAuthType();
    }

    private synchronized void wrapCookies() {
        this.wrappedCookieMap = new HashMap();
        PortletPreferences cookies = this.request.getPreferences();
        if (cookies != null) {
            this.wrappedCookies = new Cookie[cookies.getMap().size()];
            int i = 0;
            for (Enumeration e = cookies.getNames(); e.hasMoreElements(); i++) {
                String name = (String) e.nextElement();
                PortletCookie cookie = new PortletCookie(name, cookies.getValue(name, null));
                this.wrappedCookies[i] = cookie;
                this.wrappedCookieMap.put(cookie.getName(), cookie);
            }
        }
        this.wrappedCookieMap = Collections.unmodifiableMap(this.wrappedCookieMap);
    }

    public Cookie[] getCookies() {
        if (this.wrappedCookieMap == null) {
            wrapCookies();
        }
        return this.wrappedCookies;
    }

    public Map getCookieMap() {
        if (this.wrappedCookieMap == null) {
            wrapCookies();
        }
        return this.wrappedCookieMap;
    }

    public long getDateHeader(String name) {
        return Long.parseLong(this.request.getProperty(name));
    }

    public String getHeader(String name) {
        if (PortletEnvironment.HEADER_PORTLET_MODE.equals(name)) {
            return this.request.getPortletMode().toString();
        } else if (PortletEnvironment.HEADER_WINDOW_STATE.equals(name)) {
            return this.request.getWindowState().toString();
        } else {
            return this.request.getProperty(name);
        }
    }

    public Enumeration getHeaders(String name) {
        return this.request.getProperties(name);
    }

    public Enumeration getHeaderNames() {
        final Enumeration names = this.request.getPropertyNames();
        // return this.request.getPropertyNames();
        return new Enumeration() {
            int position;

            public boolean hasMoreElements() {
                return names.hasMoreElements() || position < 2;
            }

            public Object nextElement() throws NoSuchElementException {
                if (names.hasMoreElements()) {
                    return names.nextElement();
                }

                if (position == 0) {
                    position++;
                    return PortletEnvironment.HEADER_PORTLET_MODE;
                } else if (position == 1) {
                    position++;
                    return PortletEnvironment.HEADER_WINDOW_STATE;
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    /**
     * Concrete request object will implement this
     */
    public abstract String getMethod();

    public String getPathInfo() {
        return this.pathInfo;
    }

    public String getPathTranslated() {
        // TODO: getPathTranslated
        return null;
    }

    public String getContextPath() {
        return this.request.getContextPath();
    }

    public String getQueryString() {
        // TODO: getQueryString
        return "";
    }

    public String getRemoteUser() {
        return this.request.getRemoteUser();
    }

    public boolean isUserInRole(String role) {
        return this.request.isUserInRole(role);
    }

    public java.security.Principal getUserPrincipal() {
        return this.request.getUserPrincipal();
    }

    public String getRequestedSessionId() {
        return this.request.getRequestedSessionId();
    }

    public String getRequestURI() {
        // TODO: getRequestURI
        if (this.portletRequestURI == null) {
            this.portletRequestURI = this.request.getContextPath();
            /*
            this.portletRequestURI = this.request.getRequestURI();
            if (this.portletRequestURI.equals("/")) {
                String s = this.request.getServletPath();
                final StringBuffer buffer = new StringBuffer();
                if (null != s)
                    buffer.append(s);
                s = this.request.getPathInfo();
                if (null != s)
                    buffer.append(s);
                this.portletRequestURI = buffer.toString();
            }
            */
        }
        return this.portletRequestURI;
    }

    public String getSitemapURI() {
        return this.environment.getURI();
    }

    public String getServletPath() {
        return this.servletPath;
    }

    public Session getSession(boolean create) {
        javax.portlet.PortletSession serverSession = this.request.getPortletSession(create);
        if (null != serverSession) {
            if (null != this.session) {
                if (this.session.session != serverSession) {
                    // update wrapper
                    this.session.session = serverSession;
                }
            } else {
                // new wrapper
                this.session = new PortletSession(serverSession,
                                                  this.environment.getDefaultSessionScope());
            }
        } else {
            // invalidate
            this.session = null;
        }
        return this.session;
    }

    public Session getSession() {
        return this.getSession(true);
    }

    public boolean isRequestedSessionIdValid() {
        return this.request.isRequestedSessionIdValid();
    }

    /**
     * Portlet does not know how portal manages session.
     * This method returns false always.
     */
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    /**
     * Portlet does not know how portal manages session.
     * This method returns true always.
     */
    public boolean isRequestedSessionIdFromURL() {
        return true;
    }

    /* The ServletRequest interface methods */

    public Object getAttribute(String name) {
        return this.request.getAttribute(name);
    }

    public Enumeration getAttributeNames() {
        return this.request.getAttributeNames();
    }

    public String getCharacterEncoding() {
        return this.form_encoding;
    }

    public void setCharacterEncoding(String form_encoding) throws java.io.UnsupportedEncodingException {
        this.form_encoding = form_encoding;
    }

    /**
     * Sets the default encoding of the servlet container.
     */
    public void setContainerEncoding(String container_encoding) {
        this.container_encoding = container_encoding;
    }

    public int getContentLength() {
        // TODO getContentLength
        // return this.request.getContentLength();
        return -1;
    }

    public String getContentType() {
        // TODO getContentType
        // return this.request.getContentType();
        return null;
    }

    public String getParameter(String name) {
        String value = this.request.getParameter(name);
        if (this.form_encoding == null || value == null) {
            return value;
        }
        return decode(value);
    }

    private String decode(String str) {
        if (str == null) {
            return null;
        }

        try {
            if (this.container_encoding == null) {
                this.container_encoding = "ISO-8859-1";
            }
            byte[] bytes = str.getBytes(this.container_encoding);
            return new String(bytes, form_encoding);
        } catch (java.io.UnsupportedEncodingException uee) {
            throw new CascadingRuntimeException("Unsupported Encoding Exception", uee);
        }
    }

    public Enumeration getParameterNames() {
        return this.request.getParameterNames();
    }

    public String[] getParameterValues(String name) {
        String[] values = this.request.getParameterValues(name);
        if (values == null) {
            return null;
        } else if (this.form_encoding == null) {
            return values;
        }
        String[] decoded_values = new String[values.length];
        for (int i = 0; i < values.length; ++i) {
            decoded_values[i] = decode(values[i]);
        }
        return decoded_values;
    }

    public String getProtocol() {
        return "JSR168";
    }

    public String getScheme() {
        return this.request.getScheme();
    }

    public String getServerName() {
        return this.request.getServerName();
    }

    public int getServerPort() {
        return this.request.getServerPort();
    }

    public String getRemoteAddr() {
        // TODO getRemoteAddr
        // return this.request.getRemoteAddr();
        return null;
    }

    public String getRemoteHost() {
        // TODO getRemoteHost
        // return this.request.getRemoteHost();
        return null;
    }

    public void setAttribute(String name, Object o) {
        this.request.setAttribute(name, o);
    }

    public void removeAttribute(String name) {
        this.request.removeAttribute(name);
    }

    public Locale getLocale() {
        return this.request.getLocale();
    }

    public Enumeration getLocales() {
        return this.request.getLocales();
    }

    public boolean isSecure() {
        return this.request.isSecure();
    }


    /* The PortletRequest interface methods */

    /**
     * Returns underlying portlet API request object
     * @return portlet requesst object
     */
    public javax.portlet.PortletRequest getPortletRequest() {
        return request;
    }

    public Map getParameterMap() {
        return this.request.getParameterMap();
    }

    public PortalContext getPortalContext() {
        return this.request.getPortalContext();
    }

    public PortletMode getPortletMode() {
        return this.request.getPortletMode();
    }

    public javax.portlet.PortletSession getPortletSession() {
        return this.request.getPortletSession();
    }

    public javax.portlet.PortletSession getPortletSession(boolean create) {
        return this.request.getPortletSession(create);
    }

    public PortletPreferences getPreferences() {
        return this.request.getPreferences();
    }

    public Enumeration getProperties(String name) {
        return this.request.getProperties(name);
    }

    public String getProperty(String name) {
        return this.request.getProperty(name);
    }

    public Enumeration getPropertyNames() {
        return this.request.getPropertyNames();
    }

    public String getResponseContentType() {
        return this.request.getResponseContentType();
    }

    public Enumeration getResponseContentTypes() {
        return this.request.getResponseContentTypes();
    }

    public WindowState getWindowState() {
        return this.request.getWindowState();
    }

    public boolean isPortletModeAllowed(PortletMode mode) {
        return this.request.isPortletModeAllowed(mode);
    }

    public boolean isWindowStateAllowed(WindowState state) {
        return this.request.isWindowStateAllowed(state);
    }
}
