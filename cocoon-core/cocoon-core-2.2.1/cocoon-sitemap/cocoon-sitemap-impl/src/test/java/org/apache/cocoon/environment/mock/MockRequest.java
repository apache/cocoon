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
package org.apache.cocoon.environment.mock;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpSession;

import junit.framework.AssertionFailedError;

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.impl.AbstractRequest;

/**
 * @version $Id$
 */
public class MockRequest extends AbstractRequest {
    
    private Hashtable attributes = new Hashtable();
    private Hashtable globalAttributes = new Hashtable();
    private String scheme;
    private String protocol = "HTTP/1.1";
    private String requestURI;
    private String contextPath = "";
    private String servletPath;
    private String pathInfo;
    private String queryString;
    private String method = "GET";
    private String contentType;
    private Locale locale = Locale.US;
    private Principal principal;
    private String remoteAddr;
    private String remoteHost;
    private String remoteUser;
    private String userRole;
    private String reqSessionId;
    private String authType;
    private String charEncoding;
    private String serverName;
    private int port = 80;
    private InputStream inputStream;
    
    private Hashtable parameters = new Hashtable();
    private Hashtable headers = new Hashtable();
    private Map cookies = new HashMap();
    private Map cocoonCookies = new HashMap();
    
    private MockSession session;
    private Environment environment;
    
    private boolean isRequestedSessionIdFromCookie = true;
    private boolean isRequestedSessionIdFromURL = false;

    // Needed to get getSitemapURI and getSitemapPath right
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /**
     * @see org.apache.cocoon.environment.Request#get(java.lang.String)
     */
    public Object get(String name) { 
        String[] values = this.getParameterValues(name);
        if (values == null || values.length == 0) {
            return null;
        } else if (values.length == 1) {
            return values[0];
        } else {
            Vector vect = new Vector(values.length);
            for (int i = 0; i < values.length; i++) {
                vect.add(values[i]);
            }
            return vect;
        }
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getAuthType()
     */
    public String getAuthType() {
        return authType;
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getCharacterEncoding()
     */
    public String getCharacterEncoding() {
        return charEncoding;
    }

    /**
     * @see org.apache.cocoon.environment.Request#setCharacterEncoding(java.lang.String)
     */
    public void setCharacterEncoding(String enc) throws java.io.UnsupportedEncodingException {
        charEncoding = enc;
    }

    /**
     * @see org.apache.cocoon.environment.Request#getContentLength()
     */
    public int getContentLength() {
        return -1;
    }

    /**
     * @see org.apache.cocoon.environment.Request#getContentType()
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @see org.apache.cocoon.environment.Request#getParameter(java.lang.String)
     */
    public String getParameter(String name) {
        return (String)parameters.get(name);
    }

    /**
     * @see org.apache.cocoon.environment.Request#getParameterNames()
     */
    public Enumeration getParameterNames() {
        return parameters.keys();
    }

    /**
     * @see org.apache.cocoon.environment.Request#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(String name) {
        Object param = parameters.get(name);
        if ( null == param ) {
            return null;
        }
        if (param.getClass().isArray()) {
            return (String[]) param;
        }
        return new String[] {(String) param};
    }

    /**
     * Add a parameter.
     * @param name The name of the parameter.
     * @param value The value of the parameter.
     */
    public void addParameter(String name, String value) {
        parameters.put(name, value);
    }

    /**
     * @see org.apache.cocoon.environment.Request#getProtocol()
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * @see org.apache.cocoon.environment.Request#getScheme()
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * @see org.apache.cocoon.environment.Request#getServerName()
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * @see org.apache.cocoon.environment.Request#getServerPort()
     */
    public int getServerPort() {
        return port;
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getRemoteAddr()
     */
    public String getRemoteAddr() {
        return remoteAddr;
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getRemoteHost()
     */
    public String getRemoteHost() {
        return remoteHost;
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getLocale()
     */
    public Locale getLocale() {
        return locale;
    }
    
    /**
     * @param locale The Locale to set to.
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * @see org.apache.cocoon.environment.Request#getLocales()
     */
    public Enumeration getLocales() {
        return Collections.enumeration(Collections.singleton(getLocale()));
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#isSecure()
     */
    public boolean isSecure() {
        if (scheme==null) {
            return false;
        }
        return scheme.equalsIgnoreCase("HTTPS");
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getCookies()
     */
    public javax.servlet.http.Cookie[] getCookies() {
        if (cookies.isEmpty()) {
            return null;
        }
        javax.servlet.http.Cookie[] cookieArray = new javax.servlet.http.Cookie[cookies.size()];
        return (javax.servlet.http.Cookie[]) cookies.values().toArray(cookieArray);
    }

    /**
     * @see org.apache.cocoon.environment.Request#getCookieMap()
     */
    public Map getCookieMap() {
        return cookies;
    }

    public Cookie[] getCocoonCookies() {
        if (cocoonCookies.isEmpty()) {
            return null;
        }
        Cookie[] cookieArray = new Cookie[cocoonCookies.size()];
        return (Cookie[]) cocoonCookies.values().toArray(cookieArray);
    }

    public Map getCocoonCookieMap() {
        return this.cocoonCookies;
    }

    /**
     * @see org.apache.cocoon.environment.Request#getDateHeader(java.lang.String)
     */
    public long getDateHeader(String name) {
        String s1 = getHeader(name);
        if (s1 == null) {
            return -1L;
        }
        try {
            DateFormat dateFormat = new SimpleDateFormat();
            return dateFormat.parse(s1).getTime();
        }
        catch(ParseException exception) {
            throw new IllegalArgumentException("Cannot parse date: " + s1);
        }
    }

    /**
     * @see org.apache.cocoon.environment.Request#getHeader(java.lang.String)
     */
    public String getHeader(String name) {
        return (String) headers.get(name);
    }

    /**
     * @see org.apache.cocoon.environment.Request#getHeaders(java.lang.String)
     */
    public Enumeration getHeaders(String name) {
        throw new AssertionFailedError("Not implemented");
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getHeaderNames()
     */
    public Enumeration getHeaderNames() {
        return headers.keys();
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getMethod()
     */
    public String getMethod() {
        return method;
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getPathInfo()
     */
    public String getPathInfo() {
        return pathInfo;
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getPathTranslated()
     */
    public String getPathTranslated() {
        throw new AssertionFailedError("Not implemented");
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getContextPath()
     */
    public String getContextPath() {
        return contextPath;
    }
    
    public void setContextPath(String path) {
        contextPath = path;
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getQueryString()
     */
    public String getQueryString() {
        return queryString;
    }
    
    public void setQueryString(String string) {
        queryString = string;
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getRemoteUser()
     */
    public String getRemoteUser() {
        return remoteUser;
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getUserPrincipal()
     */
    public Principal getUserPrincipal() {
        return principal;
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#isUserInRole(java.lang.String)
     */
    public boolean isUserInRole(String role) {
        if ( this.userRole == null ) {
            return role == null;
        }
        return userRole.equals(role);
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getRequestedSessionId()
     */
    public String getRequestedSessionId() {
        return reqSessionId;
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getRequestURI()
     */
    public String getRequestURI() {
        if (this.environment == null) {
            return requestURI;
        }
        return this.environment.getURI();
    }
    
    public void setRequestURI(String uri) {
        requestURI = uri;
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getSitemapURI()
     */
    public String getSitemapURI() {
        if (this.environment == null) {
            return requestURI;
        }
        return this.environment.getURI();
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getSitemapPath()
     */
    public String getSitemapPath() {
        if (this.environment == null) {
            return "";
        }
        return this.environment.getURIPrefix();
    }

    /**
     * @see org.apache.cocoon.environment.Request#getSitemapURIPrefix()
     */
    public String getSitemapURIPrefix() {
        return "";
    }

    /**
     * @see org.apache.cocoon.environment.Request#getServletPath()
     */
    public String getServletPath() {
        return servletPath;
    }

    /**
     * @see org.apache.cocoon.environment.Request#getSession(boolean)
     */
    public HttpSession getSession(boolean create) {
        if ((session == null) && (create)) {
            this.session = new MockSession();
        } else if ((session != null) && (!(session).isValid()) && (create)) {
            this.session = new MockSession();
        }
        if ((session != null) && ((session).isValid())) {
            return this.session;
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.environment.Request#getSession()
     */
    public HttpSession getSession() {
        return getSession(true);
    }

    /**
     * @see org.apache.cocoon.environment.Request#isRequestedSessionIdValid()
     */
    public boolean isRequestedSessionIdValid() {
        if (session != null) {
            try {
                session.getId();
                return true;
            } catch (IllegalStateException e) {
                return false;
            }
        }
        return false;
    }

    /**
     * @see org.apache.cocoon.environment.Request#isRequestedSessionIdFromCookie()
     */
    public boolean isRequestedSessionIdFromCookie() {
        return isRequestedSessionIdFromCookie;
    }

    /**
     * @see org.apache.cocoon.environment.Request#isRequestedSessionIdFromURL()
     */
    public boolean isRequestedSessionIdFromURL() {
        return isRequestedSessionIdFromURL;
    }

    public void reset() {
        attributes.clear();
        globalAttributes.clear();
        scheme = null;
        protocol = "HTTP/1.1";
        requestURI = null;
        contextPath = null;
        servletPath = null;
        pathInfo = null;
        queryString = null;
        method = "GET";
        contentType = null;
        locale = Locale.US;
        principal = null;
        remoteAddr = null;
        remoteHost = null;
        remoteUser = null;
        userRole = null;
        reqSessionId = null;
        authType = null;
        charEncoding = null;
        serverName = null;
        port = 80;
        
        parameters.clear();
        headers.clear();
    }

    public void setHeader( String key, String value ) {
        this.headers.put(key, value );
    }

    public void setMethod( String method ) {
        this.method = method;
    }

    public void clearSession() {
        this.session = null;
    }

    public void setIsRequestedSessionIdFromURL( boolean isRequestedSessionIdFromURL ) {
        this.isRequestedSessionIdFromURL = isRequestedSessionIdFromURL;
    }
    
    public void setIsRequestedSessionIdFromCooki( boolean isRequestedSessionIdFromCookie ) {
        this.isRequestedSessionIdFromCookie = isRequestedSessionIdFromCookie;
    }

    /**
     * @see org.apache.cocoon.environment.Request#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return this.globalAttributes.get(name);
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        return this.globalAttributes.keys();
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
            this.globalAttributes.put(name, value);
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        this.globalAttributes.remove(name);
    }

    /**
     * @see org.apache.cocoon.environment.Request#getLocalAttribute(java.lang.String)
     */
    public Object getLocalAttribute(String name) {
        return this.attributes.get(name);
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#getLocalAttributeNames()
     */
    public Enumeration getLocalAttributeNames() {
        return this.attributes.keys();
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#setLocalAttribute(java.lang.String, java.lang.Object)
     */
    public void setLocalAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }
    
    /**
     * @see org.apache.cocoon.environment.Request#removeLocalAttribute(java.lang.String)
     */
    public void removeLocalAttribute(String name) {
        this.attributes.remove(name);
    }

    /**
     * @see org.apache.cocoon.environment.Request#getInputStream()
     */
    public ServletInputStream getInputStream() throws IOException, UnsupportedOperationException {
        return new ServletInputStream() {
            public int read() throws IOException {
                return MockRequest.this.inputStream.read();
            }
        };
    }

    public void setInputStream(InputStream is) {
        this.inputStream = is;
    }

    /**
     * @see org.apache.cocoon.environment.Request#searchAttribute(java.lang.String)
     */
    public Object searchAttribute(String name) {
        Object result = this.getLocalAttribute(name);
        if ( result == null ) {
            result = this.getAttribute(name);
        }
        return result;
    }

    public void setUserRole(String value) {
        this.userRole = value;
    }

    public Session getCocoonSession(boolean create) {
        // TODO Auto-generated method stub
        return null;
    }

    public Session getCocoonSession() {
        // TODO Auto-generated method stub
        return null;
    }

}
