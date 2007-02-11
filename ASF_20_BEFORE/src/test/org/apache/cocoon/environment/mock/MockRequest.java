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
