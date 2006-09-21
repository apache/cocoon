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
package org.apache.cocoon.environment.commandline;

import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.impl.AbstractRequest;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.NotImplementedException;

/**
 * Creates a specific servlet request simulation from command line usage.
 *
 * @version $Id$
 */
/*
 * NOTE: method with a non-compliant implementation are marked with FIXME
 * and should be fixed in the future if required
 */
public class CommandLineRequest extends AbstractRequest {

    private class EmptyEnumeration implements Enumeration {
        public boolean hasMoreElements() {
            return false;
        }
        public Object nextElement() {
            return null;
        }
    }

    private Environment env;
    private String contextPath;
    private String servletPath;
    private String pathInfo;
    private Map globalAttributes;
    private Map attributes;
    private Map parameters;
    private Map headers;
    private String characterEncoding;

    public CommandLineRequest(Environment env,
                              String contextPath,
                              String servletPath,
                              String pathInfo) {
        this(env, contextPath, servletPath, pathInfo, null, null, null);
    }

    public CommandLineRequest(Environment env,
                              String contextPath,
                              String servletPath,
                              String pathInfo,
                              Map attributes) {
        this(env, contextPath, servletPath, pathInfo, attributes, null, null);
    }

    public CommandLineRequest(Environment env,
                              String contextPath,
                              String servletPath,
                              String pathInfo,
                              Map attributes,
                              Map parameters) {
        this(env, contextPath, servletPath, pathInfo, attributes, parameters, null);
    }

    public CommandLineRequest(Environment env,
                              String contextPath,
                              String servletPath,
                              String pathInfo,
                              Map attributes,
                              Map parameters,
                              Map headers) {
        this.env = env;
        this.contextPath = contextPath;
        this.servletPath = servletPath;
        this.pathInfo = pathInfo;
        this.globalAttributes = (attributes == null ? new HashMap() : attributes);
        this.attributes = new HashMap();
        this.parameters = parameters;
        this.headers = headers;
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

    public String getContextPath() { return contextPath; }
    public String getServletPath() { return servletPath; }
    public String getPathInfo() { return pathInfo; }
    public String getRequestURI() {
        StringBuffer buffer = new StringBuffer();
        if (servletPath != null) buffer.append(servletPath);
        if (contextPath != null) buffer.append(contextPath);
        if (pathInfo != null) buffer.append(pathInfo);
        return buffer.toString();
    }
    // FIXME
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

    public String getQueryString() { return null; } // use parameters instead
    public String getPathTranslated() { return null; } // FIXME (SM) this is legal but should we do something more?

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
            return IteratorUtils.asEnumeration(this.attributes.keySet().iterator());
        }
        return IteratorUtils.asEnumeration(this.globalAttributes.keySet().iterator());
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

    public String getParameter(String name) {
        if (this.parameters == null) {
            return null;
        }

        final Object value = this.parameters.get(name);
        if (value instanceof String) {
            return (String)value;
        } else if (value == null) {
            return null;
        } else {
            final String[] values = (String[]) value;
            if (values.length == 0) {
                return null;
            }
            return values[0];
        }
    }

    public Enumeration getParameterNames() {
        return (this.parameters != null) ? IteratorUtils.asEnumeration(this.parameters.keySet().iterator()) : null;
    }

    public String[] getParameterValues(String name) {
        final Object value = this.parameters.get(name);
        if (value instanceof String) {
            return new String[] { (String)value };
        }
        return (String[]) value;
    }

    public String getHeader(String name) {
        return (headers != null) ? (String) headers.get(name.toLowerCase()) : null;
    }

    public int getIntHeader(String name) {
        String header = (headers != null) ? (String) headers.get(name.toLowerCase()) : null;
        return (header != null) ? Integer.parseInt(header) : -1;
    }

    public long getDateHeader(String name) {
        //FIXME
        return 0;
    }

    public Enumeration getHeaders(String name) {
        // FIXME
        return new EmptyEnumeration();
    }

    public Enumeration getHeaderNames() {
        if (headers != null) {
            return IteratorUtils.asEnumeration(headers.keySet().iterator());
        }
        return new EmptyEnumeration();
    }

    public String getCharacterEncoding() { return characterEncoding; }
    public int getContentLength() { return -1; }

    public String getContentType() { return null; }
    public String getProtocol()  { return "cli"; }
    public String getScheme() { return "cli"; }
    public String getServerName() { return Constants.COMPLETE_NAME; }
    public int getServerPort() { return -1; }
    public String getRemoteAddr() { return "127.0.0.1"; }
    public String getRemoteHost() { return "localhost"; }
    public String getMethod() { return "get"; }
    public String getRemoteUser() { return SystemUtils.USER_NAME; }

    public Cookie[] getCookies() { return null; }
    public Map getCookieMap() {
        return Collections.unmodifiableMap(new HashMap());
    }

    /**
     * Returns the current session associated with this request,
     * or if the request does not have a session, creates one.
     *
     * @return                the <code>Session</code> associated
     *                        with this request
     *
     * @see        #getSession(boolean)
     */
    public Session getSession() {
        return this.getSession(true);
    }

    /**
     * Returns the current <code>Session</code>
     * associated with this request or, if if there is no
     * current session and <code>create</code> is true, returns
     * a new session.
     *
     * <p>If <code>create</code> is <code>false</code>
     * and the request has no valid <code>Session</code>,
     * this method returns <code>null</code>.
     *
     * <p>To make sure the session is properly maintained,
     * you must call this method before
     * the response is committed.
     *
     * @param create  <code>true</code> to create a new session for this request
     *                if necessary;
     *                <code>false</code> to return <code>null</code> if there's
     *                no current session
     *
     * @return  the <code>Session</code> associated with this request or
     *          <code>null</code> if <code>create</code> is <code>false</code>
     *          and the request has no valid session
     *
     * @see  #getSession()
     */
    public Session getSession(boolean create) {
        return CommandLineSession.getSession(create);
    }

    /**
     * Returns the session ID specified by the client. This may
     * not be the same as the ID of the actual session in use.
     * For example, if the request specified an old (expired)
     * session ID and the server has started a new session, this
     * method gets a new session with a new ID. If the request
     * did not specify a session ID, this method returns
     * <code>null</code>.
     *
     *
     * @return                a <code>String</code> specifying the session
     *                        ID, or <code>null</code> if the request did
     *                        not specify a session ID
     *
     * @see                #isRequestedSessionIdValid()
     */
    public String getRequestedSessionId() {
        return (CommandLineSession.getSession(false) != null) ?
                CommandLineSession.getSession(false).getId() : null;
    }

    /**
     * Checks whether the requested session ID is still valid.
     *
     * @return                        <code>true</code> if this
     *                                request has an id for a valid session
     *                                in the current session context;
     *                                <code>false</code> otherwise
     *
     * @see                        #getRequestedSessionId()
     * @see                        #getSession()
     */
    public boolean isRequestedSessionIdValid() {
        return (CommandLineSession.getSession(false) != null);
    }

    /**
     * Checks whether the requested session ID came in as a cookie.
     *
     * @return                        <code>true</code> if the session ID
     *                                came in as a
     *                                cookie; otherwise, <code>false</code>
     *
     *
     * @see                        #getSession()
     */
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    /**
     * Checks whether the requested session ID came in as part of the
     * request URL.
     *
     * @return                        <code>true</code> if the session ID
     *                                came in as part of a URL; otherwise,
     *                                <code>false</code>
     *
     *
     * @see                        #getSession()
     */
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    public Locale getLocale() { return Locale.getDefault(); }
    public Enumeration getLocales() {
        // FIXME
        throw new NotImplementedException (getClass().getName() + ".getLocales() method not yet implemented!");
    }

    public String getAuthType() { return null; }
    public boolean isSecure() { return false; }
    public boolean isUserInRole(String role) { return false; }
    public java.security.Principal getUserPrincipal() { return null; }

    public java.util.Map getParameterMap() { return parameters; }
    public void setCharacterEncoding(java.lang.String env)
                          throws java.io.UnsupportedEncodingException { characterEncoding = env; }
    public StringBuffer getRequestURL() { return null; }

	/*
	 * @see org.apache.cocoon.environment.Request#getInputStream()
	 */
	public InputStream getInputStream() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
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
