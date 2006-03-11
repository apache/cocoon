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
package org.apache.cocoon.environment.mock;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import junit.framework.AssertionFailedError;

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Request;
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
    
    private MockSession session;
    private Environment environment = null;
    
    private boolean isRequestedSessionIdFromCookie = true;
    private boolean isRequestedSessionIdFromURL = false;

    // Needed to get getSitemapURI and getSitemapPath right
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    /* (non-Javadoc)
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
    
    public String getAuthType() {
        return authType;
    }
    
    public String getCharacterEncoding() {
        return charEncoding;
    }

    public void setCharacterEncoding(String enc) throws java.io.UnsupportedEncodingException {
        charEncoding = enc;
    }

    public int getContentLength() {
        return -1;
    }

    public String getContentType() {
        return contentType;
    }

    public String getParameter(String name) {
        return (String)parameters.get(name);
    }

    public Enumeration getParameterNames() {
        return parameters.keys();
    }

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

    public void addParameter(String name, String value) {
        parameters.put(name, value);
    }

    public String getProtocol() {
        return protocol;
    }

    public String getScheme() {
        return scheme;
    }

    public String getServerName() {
        return serverName;
    }

    public int getServerPort() {
        return port;
    }
    
    public String getRemoteAddr() {
        return remoteAddr;
    }
    
    public String getRemoteHost() {
        return remoteHost;
    }
    
    public Locale getLocale() {
        return locale;
    }
    
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Enumeration getLocales() {
        return Collections.enumeration(Collections.singleton(getLocale()));
    }
    
    public boolean isSecure() {
        if (scheme==null) {
            return false;
        }
        return scheme.equalsIgnoreCase("HTTPS");
    }
    
    public Cookie[] getCookies() {
        if (cookies.isEmpty()) {
            return null;
        }
        Cookie[] cookieArray = new Cookie[cookies.size()];
        return (Cookie []) cookies.values().toArray(cookieArray);
    }

    public Map getCookieMap() {
        return cookies;
    }

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

    public String getHeader(String name) {
        return (String) headers.get(name);
    }

    public Enumeration getHeaders(String name) {
        throw new AssertionFailedError("Not implemented");
    }
    
    public Enumeration getHeaderNames() {
        return headers.keys();
    }
    
    public String getMethod() {
        return method;
    }
    
    public String getPathInfo() {
        return pathInfo;
    }
    
    public String getPathTranslated() {
        throw new AssertionFailedError("Not implemented");
    }
    
    public String getContextPath() {
        return contextPath;
    }
    
    public void setContextPath(String path) {
        contextPath = path;
    }
    
    public String getQueryString() {
        return queryString;
    }
    
    public void setQueryString(String string) {
        queryString = string;
    }
    
    public String getRemoteUser() {
        return remoteUser;
    }
    
    public Principal getUserPrincipal() {
        return principal;
    }
    
    public boolean isUserInRole(String role) {
        return userRole.equals(role);
    }
    
    public String getRequestedSessionId() {
        return reqSessionId;
    }
    
    public String getRequestURI() {
        if (this.environment == null) {
            return requestURI;
        }
        return this.environment.getURI();
    }
    
    public void setRequestURI(String uri) {
        requestURI = uri;
    }
    
    public String getSitemapURI() {
        if (this.environment == null) {
            return requestURI;
        }
        return this.environment.getURI();
    }
    
    public String getSitemapPath() {
        if (this.environment == null) {
            return "";
        }
        return this.environment.getURIPrefix();
    }

    public String getSitemapURIPrefix() {
        return "";
    }

    public String getServletPath() {
        return servletPath;
    }

    public Session getSession(boolean create) {
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

    public Session getSession() {
        return getSession(true);
    }

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

    public boolean isRequestedSessionIdFromCookie() {
        return isRequestedSessionIdFromCookie;
    }

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
        return this.globalAttributes.get(name);
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getAttributeNames(int)
     */
    public Enumeration getAttributeNames(int scope) {
        if ( scope == Request.REQUEST_SCOPE ) {
            return this.attributes.keys();
        }
        return this.globalAttributes.keys();
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#setAttribute(java.lang.String, java.lang.Object, int)
     */
    public void setAttribute(String name, Object value, int scope) {
        if ( scope == Request.REQUEST_SCOPE ) {
            this.attributes.put(name, value);
        } else {
            this.globalAttributes.put(name, value);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#removeAttribute(java.lang.String, int)
     */
    public void removeAttribute(String name, int scope) {
        if ( scope == Request.REQUEST_SCOPE ) {
            this.attributes.remove(name);
        } else {
            this.globalAttributes.remove(name);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getInputStream()
     */
    public InputStream getInputStream() throws IOException, UnsupportedOperationException {
        return this.inputStream;
    }

    public void setInputStream(InputStream is) {
        this.inputStream = is;
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
