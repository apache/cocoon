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
package org.apache.cocoon.environment.mock;

import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;

import junit.framework.AssertionFailedError;

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;

public class MockRequest implements Request {

    private Hashtable attributes = new Hashtable();
    private String scheme;
    private String protocol = "HTTP/1.1";
    private String requestURI;
    private String contextPath = "";
    private String servletPath;
    private String pathInfo;
    private String queryString;
    private String method = "GET";
    private String contentType;
    private Locale locale;
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

    private Hashtable parameters = new Hashtable();
    private Hashtable headers = new Hashtable();
    private HashMap cookies = new HashMap();

    private MockSession session;

    public Object get(String name) {
        return getAttribute(name);
    }

    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    public Enumeration getAttributeNames() {
        return attributes.keys();
    }

    public void setAttribute(String name, Object o) {
        if (o == null)
            attributes.remove(name);
        else
            attributes.put(name, o);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
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
        if( null == param )
            return null;
        else {
            if (param.getClass().isArray()) {
                return (String[]) param;
            } else {
                return new String[] {(String) param};
            }
        }
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

    public Enumeration getLocales() {
        return Collections.enumeration(Collections.singleton(getLocale()));
    }

    public boolean isSecure() {
        if(scheme==null){
            return false;
        } else{
            return scheme.equalsIgnoreCase("HTTPS");
        }
    }

    public Cookie[] getCookies() {
        if (cookies.isEmpty())
            return null;
        else {
            Cookie[] cookieArray = new Cookie[cookies.size()];
            return (Cookie []) cookies.values().toArray(cookieArray);
        }
    }

    public Map getCookieMap() {
        return cookies;
    }

    public long getDateHeader(String name) {
        String s1 = getHeader(name);
        if(s1 == null)
            return -1L;
        try
        {
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
        return requestURI;
    }

    public void setRequestURI(String uri) {
        requestURI = uri;
    }

    public String getSitemapURI() {
        return requestURI;
    }

    public String getServletPath() {
        return servletPath;
    }

    public Session getSession(boolean create) {
        if ((session == null) && (create))
            this.session = new MockSession();
        else if ((session != null) && (!(session).isValid()) && (create))
            this.session = new MockSession();
        if ((session != null) && ((session).isValid()))
            return this.session;
        else
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
        } else
            return false;
    }

    public boolean isRequestedSessionIdFromCookie() {
        return true;
    }

    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    public void reset() {
        attributes.clear();
        scheme = null;
        protocol = "HTTP/1.1";
        requestURI = null;
        contextPath = null;
        servletPath = null;
        pathInfo = null;
        queryString = null;
        method = "GET";
        contentType = null;
        locale = null;
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
}
