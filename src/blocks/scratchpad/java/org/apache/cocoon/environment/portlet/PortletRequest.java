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
 * @version CVS $Id: PortletRequest.java,v 1.2 2003/12/03 13:20:29 vgritsenko Exp $
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

    private Cookie[] wrappedCookies = null;
    private Map wrappedCookieMap = null;
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
                this.session = new PortletSession(serverSession);
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
        // TODO protocol should be available somehow
        // return this.request.getProtocol();
        return null;
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
