/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.environment.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.impl.AbstractRequest;
import org.apache.cocoon.servlet.multipart.MultipartHttpServletRequest;
import org.apache.commons.collections.IteratorUtils;

/**
 * Implements the {@link org.apache.cocoon.environment.Request} interface
 * to provide request information in the HTTP servlets environment.
 *
 * @version $Id$
 */
public final class HttpRequest extends AbstractRequest {

    /** The real HttpServletRequest object */
    private final HttpServletRequest req;

    /** The HttpEnvironment object */
    private final HttpEnvironment env;

    /** The character encoding of parameters */
    private String form_encoding;

    /** The default form encoding of the servlet container */
    private String container_encoding;

    /**
     * The map to assure 1:1-mapping of server sessions and Cocoon session wrappers
     */
    private static final Map sessions = new WeakHashMap();

    private final Map attributes = new HashMap();

    /**
     * Creates a HttpRequest based on a real HttpServletRequest object
     */
    protected HttpRequest(HttpServletRequest req, HttpEnvironment env) {
        super();
        this.req = req;
        this.env = env;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#get(java.lang.String)
     */
    public Object get(String name) {
        // if the request has been wrapped then access its method
        if (req instanceof MultipartHttpServletRequest) {
            return ((MultipartHttpServletRequest) req).get(name);
        }
        String[] values = req.getParameterValues(name);
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

    /* The HttpServletRequest interface methods */

    public String getAuthType() {
        return this.req.getAuthType();
    }

    private Cookie[] wrappedCookies = null;
    private Map wrappedCookieMap = null;

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

    private synchronized void wrapCookies() {
        this.wrappedCookieMap = new HashMap();
        javax.servlet.http.Cookie[] cookies = this.req.getCookies();
        if (cookies != null) {
            this.wrappedCookies = new Cookie[cookies.length];
            for(int i=0; i<cookies.length;i++) {
                HttpCookie cookie = new HttpCookie(cookies[i]);
                this.wrappedCookies[i] = cookie;
                this.wrappedCookieMap.put(cookie.getName(),cookie);
            }
        }
        this.wrappedCookieMap = Collections.unmodifiableMap(this.wrappedCookieMap);
    }

    public long getDateHeader(String name) {
        return this.req.getDateHeader(name);
    }

    public String getHeader(String name) {
        return this.req.getHeader(name);
    }

    public Enumeration getHeaders(String name) {
        return this.req.getHeaders(name);
    }

    public Enumeration getHeaderNames() {
        return this.req.getHeaderNames();
    }

    public int getIntHeader(String name) {
        return this.req.getIntHeader(name);
    }

    public String getMethod() {
        return this.req.getMethod();
    }

    public String getPathInfo() {
        return this.req.getPathInfo();
    }

    public String getPathTranslated() {
        return this.req.getPathTranslated();
    }

    public String getContextPath() {
        return this.req.getContextPath();
    }

    public String getQueryString() {
        return this.req.getQueryString();
    }

    public String getRemoteUser() {
        return this.req.getRemoteUser();
    }

    public boolean isUserInRole(String role) {
        return this.req.isUserInRole(role);
    }

    public java.security.Principal getUserPrincipal() {
        return this.req.getUserPrincipal();
    }

    public String getRequestedSessionId() {
        return this.req.getRequestedSessionId();
    }

    protected String reqURI;

    public String getRequestURI() {
        if (this.reqURI == null) {
            this.reqURI = this.req.getRequestURI();
            if ( this.reqURI.equals("/") ) {
                String s = this.req.getServletPath();
                final StringBuffer buffer = new StringBuffer();
                if ( null != s ) buffer.append(s);
                s = this.req.getPathInfo();
                if ( null != s ) buffer.append(s);
                this.reqURI = buffer.toString();
            }
        }
        return this.reqURI;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getSitemapURI()
     */
    public String getSitemapURI() {
        return this.env.getURI();
    }

    public String getSitemapURIPrefix() {
        return this.env.getURIPrefix();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getSitemapPath()
     */
    public String getSitemapPath() {
        return this.env.getURIPrefix();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getServletPath()
     */
    public String getServletPath() {
        return this.req.getServletPath();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getSession(boolean)
     */
    public Session getSession(boolean create) {
        javax.servlet.http.HttpSession serverSession = this.req.getSession(create);
        HttpSession session;
        if (serverSession != null)
        {
            synchronized (sessions)
            {
                // retrieve existing wrapper
                WeakReference ref = (WeakReference) sessions.get(serverSession);
                if (ref == null || (session = (HttpSession) ref.get()) == null)
                {
                    // create new wrapper
                    session = new HttpSession(serverSession);
                    sessions.put(serverSession, new WeakReference(session));
                }
            }
        }
        else
        {
            // invalidate
            session = null;
        }
        return session;
    }

    public Session getSession() {
        return this.getSession(true);
    }

    public boolean isRequestedSessionIdValid() {
        return this.req.isRequestedSessionIdValid();
    }

    public boolean isRequestedSessionIdFromCookie()  {
        return this.req.isRequestedSessionIdFromCookie();
    }

    public boolean isRequestedSessionIdFromURL() {
        return this.req.isRequestedSessionIdFromURL();
    }

    /**
     * @deprecated As of Version 2.1 of the Java Servlet API, use
     *             {@link #isRequestedSessionIdFromURL()} instead.
     */
    public boolean isRequestedSessionIdFromUrl() {
        return this.req.isRequestedSessionIdFromURL();
    }

    /* The ServletRequest interface methods */

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return this.getAttribute(name, Request.GLOBAL_SCOPE);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        return this.getAttributeNames(Request.GLOBAL_SCOPE);
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        this.setAttribute(name, value, Request.GLOBAL_SCOPE);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        this.removeAttribute(name, Request.GLOBAL_SCOPE);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getAttribute(java.lang.String, int)
     */
    public Object getAttribute(String name, int scope) {
        if ( scope == Request.REQUEST_SCOPE ) {
            return this.attributes.get(name);
        }
        return this.req.getAttribute(name);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getAttributeNames(int)
     */
    public Enumeration getAttributeNames(int scope) {
        if ( scope == Request.REQUEST_SCOPE ) {
            return IteratorUtils.asEnumeration(this.attributes.keySet().iterator());
        }
        return this.req.getAttributeNames();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#setAttribute(java.lang.String, java.lang.Object, int)
     */
    public void setAttribute(String name, Object value, int scope) {
        if ( scope == Request.REQUEST_SCOPE ) {
            this.attributes.put(name, value);
        } else {
            this.req.setAttribute(name, value);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#removeAttribute(java.lang.String, int)
     */
    public void removeAttribute(String name, int scope) {
        if ( scope == Request.REQUEST_SCOPE ) {
            this.attributes.remove(name);
        } else {
            this.req.removeAttribute(name);
        }
    }

    public String getCharacterEncoding() {
        if (this.form_encoding == null) {
            return this.req.getCharacterEncoding();
        }
        return this.form_encoding;
    }

    public void setCharacterEncoding(String form_encoding)
    throws java.io.UnsupportedEncodingException {
        this.form_encoding = form_encoding;
    }

    /**
     * Sets the default encoding of the servlet container.
     */
    public void setContainerEncoding(String container_encoding) {
        this.container_encoding = container_encoding;
    }

    public int getContentLength() {
        return this.req.getContentLength();
    }

    public String getContentType() {
        return this.req.getContentType();
    }

    public InputStream getInputStream() throws IOException {
        return this.req.getInputStream();
    }

    public String getParameter(String name) {
        String value = this.req.getParameter(name);
        if (this.form_encoding == null || value == null) {
            return value;
        }
        return decode(value);
    }

    private String decode(String str) {
        if (str == null) return null;
        try {
            if (this.container_encoding == null)
                this.container_encoding = "ISO-8859-1";
            byte[] bytes = str.getBytes(this.container_encoding);
            return new String(bytes, form_encoding);
        } catch (java.io.UnsupportedEncodingException uee) {
            throw new RequestEncodingException("Unsupported Encoding Exception", uee);
        }
    }

    public Enumeration getParameterNames() {
        return this.req.getParameterNames();
    }

    public String[] getParameterValues(String name) {
        String[] values = this.req.getParameterValues(name);
        if (values == null) return null;
        if (this.form_encoding == null) {
            return values;
        }
        String[] decoded_values = new String[values.length];
        for (int i = 0; i < values.length; ++i) {
            decoded_values[i] = decode(values[i]);
        }
        return decoded_values;
    }

    public String getProtocol() {
        return this.req.getProtocol();
    }

    public String getScheme() {
        return this.req.getScheme();
    }

    public String getServerName() {
        return this.req.getServerName();
    }

    public int getServerPort() {
        return this.req.getServerPort();
    }

    public BufferedReader getReader() throws IOException {
        return this.req.getReader();
    }

    public String getRemoteAddr() {
        return this.req.getRemoteAddr();
    }

    public String getRemoteHost() {
        return this.req.getRemoteHost();
    }

    public Locale getLocale() {
        return this.req.getLocale();
    }

    public Enumeration getLocales() {
        return this.req.getLocales();
    }

    public boolean isSecure() {
        return this.req.isSecure();
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return this.req.getRequestDispatcher(path);
    }

    /**
     * @deprecated As of Version 2.1 of the Java Servlet API, use
     * {@link javax.servlet.ServletContext#getRealPath(java.lang.String)}instead.
     */
    public String getRealPath(String path) {
        return this.req.getRealPath(path);
    }

    /**
     * @see org.apache.cocoon.environment.Request#searchAttribute(java.lang.String)
     */
    public Object searchAttribute(String name) {
        Object result = this.getAttribute(name, REQUEST_SCOPE);
        if ( result == null ) {
            result = this.getAttribute(name, GLOBAL_SCOPE);
        }
        return result;
    }
}
