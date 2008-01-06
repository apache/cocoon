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
package org.apache.cocoon.environment.wrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpSession;

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.impl.AbstractRequest;
import org.apache.commons.collections.IteratorUtils;


/**
 * This is a wrapper class for the <code>Request</code> object. It
 * just forwards every methods. It is the base class for all wrapper
 * implementations.
 *
 * @version $Id$
 * @since 2.2
 */
public abstract class AbstractRequestWrapper extends AbstractRequest {

    /** The real {@link Request} object */
    protected final Request req;

    private final Map requestAttributes = new HashMap();

    /**
     * Constructor
     * @param request The Request being wrapped.
     */
    public AbstractRequestWrapper(Request request) {
        this.req = request;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#get(java.lang.String)
     */
    public Object get(String name) {
        return this.req.get(name);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return this.req.getAttribute(name);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        return this.req.getAttributeNames();
    }


    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getLocalAttribute(java.lang.String)
     */
    public Object getLocalAttribute(String name) {
        return this.requestAttributes.get( name );
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getLocalAttributeNames()
     */
    public Enumeration getLocalAttributeNames() {
        return IteratorUtils.asEnumeration(this.requestAttributes.keySet().iterator());
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#removeLocalAttribute(java.lang.String)
     */
    public void removeLocalAttribute(String name) {
        this.requestAttributes.remove( name );
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#setLocalAttribute(java.lang.String, java.lang.Object)
     */
    public void setLocalAttribute(String name, Object o) {
        this.requestAttributes.put( name, o );
    }

    /**
     * @see org.apache.cocoon.environment.Request#searchAttribute(java.lang.String)
     */
    public Object searchAttribute(String name) {
        Object result = this.getLocalAttribute(name);
        if ( result == null ) {
            result = this.getAttribute(name);
            if ( result == null ) {
                result = this.req.getLocalAttribute(name);
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getCharacterEncoding()
     */
    public String getCharacterEncoding() {
        return this.req.getCharacterEncoding();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#setCharacterEncoding(java.lang.String)
     */
    public void setCharacterEncoding(String enc)
    throws java.io.UnsupportedEncodingException {
        this.req.setCharacterEncoding(enc);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getContentLength()
     */
    public int getContentLength() {
        return this.req.getContentLength();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getContentType()
     */
    public String getContentType() {
        return this.req.getContentType();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getParameter(java.lang.String)
     */
    public String getParameter(String name) {
        return this.req.getParameter(name);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getParameterNames()
     */
    public Enumeration getParameterNames() {
        return this.req.getParameterNames();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(String name) {
        return this.req.getParameterValues(name);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getProtocol()
     */
    public String getProtocol() {
        return this.req.getProtocol();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getScheme()
     */
    public String getScheme() {
        return this.req.getScheme();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getServerName()
     */
    public String getServerName() {
        return this.req.getServerName();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getServerPort()
     */
    public int getServerPort() {
        return this.req.getServerPort();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getRemoteAddr()
     */
    public String getRemoteAddr() {
        return this.req.getRemoteAddr();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getRemoteHost()
     */
    public String getRemoteHost() {
        return this.req.getRemoteHost();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object o) {
        this.req.setAttribute(name, o);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        this.req.removeAttribute(name);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getLocale()
     */
    public Locale getLocale() {
        return this.req.getLocale();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getLocales()
     */
    public Enumeration getLocales() {
        return this.req.getLocales();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#isSecure()
     */
    public boolean isSecure() {
        return this.req.isSecure();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getCookies()
     */
    public javax.servlet.http.Cookie[] getCookies() {
        return this.req.getCookies();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getCookieMap()
     */
    public Map getCookieMap() {
        return this.req.getCookieMap();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getDateHeader(java.lang.String)
     */
    public long getDateHeader(String name) {
        return this.req.getDateHeader(name);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getHeader(java.lang.String)
     */
    public String getHeader(String name) {
        return this.req.getHeader(name);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getHeaders(java.lang.String)
     */
    public Enumeration getHeaders(String name) {
        return this.req.getHeaders(name);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getHeaderNames()
     */
    public Enumeration getHeaderNames() {
        return this.req.getHeaderNames();
    }

	/* (non-Javadoc)
	 * @see org.apache.cocoon.environment.Request#getInputStream()
	 */
	public ServletInputStream getInputStream() throws IOException, UnsupportedOperationException {
		return this.req.getInputStream();
	}

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getMethod()
     */
    public String getMethod() {
        return this.req.getMethod();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getPathInfo()
     */
    public String getPathInfo() {
        return this.req.getPathInfo();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getPathTranslated()
     */
    public String getPathTranslated() {
        return this.req.getPathTranslated();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getContextPath()
     */
    public String getContextPath() {
        return this.req.getContextPath();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getQueryString()
     */
    public String getQueryString() {
        return this.req.getQueryString();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getRemoteUser()
     */
    public String getRemoteUser() {
        return this.req.getRemoteUser();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getRequestedSessionId()
     */
    public String getRequestedSessionId() {
        return this.req.getRequestedSessionId();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getRequestURI()
     */
    public String getRequestURI() {
        return this.req.getRequestURI();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getSitemapURI()
     */
    public String getSitemapURI() {
        return this.req.getSitemapURI();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getSitemapPath()
     */
    public String getSitemapPath() {
        return this.req.getSitemapPath();
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
    public HttpSession getSession(boolean create) {
        return this.req.getSession(create);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getSession()
     */
    public HttpSession getSession() {
        return this.req.getSession();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#isRequestedSessionIdValid()
     */
    public boolean isRequestedSessionIdValid() {
        return this.req.isRequestedSessionIdValid();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#isRequestedSessionIdFromCookie()
     */
    public boolean isRequestedSessionIdFromCookie()  {
        return this.req.isRequestedSessionIdFromCookie();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#isRequestedSessionIdFromURL()
     */
    public boolean isRequestedSessionIdFromURL() {
        return this.req.isRequestedSessionIdFromURL();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getUserPrincipal()
     */
    public Principal getUserPrincipal() {
        return this.req.getUserPrincipal();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#isUserInRole(java.lang.String)
     */
    public boolean isUserInRole(String role) {
        return this.req.isUserInRole(role);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getAuthType()
     */
    public String getAuthType() {
        return this.req.getAuthType();
    }       

    /**
     * @see org.apache.cocoon.environment.Request#getSitemapURIPrefix()
     */
    public String getSitemapURIPrefix() {
        return this.req.getSitemapURIPrefix();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getCocoonCookieMap()
     */
    public Map getCocoonCookieMap() {
        return this.req.getCocoonCookieMap();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getCocoonCookies()
     */
    public Cookie[] getCocoonCookies() {
        return this.req.getCocoonCookies();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getCocoonSession(boolean)
     */
    public Session getCocoonSession(boolean create) {
        return this.req.getCocoonSession(create);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getCocoonSession()
     */
    public Session getCocoonSession() {
        return this.req.getCocoonSession();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
     */
    public int getIntHeader(String name) {
        return this.req.getIntHeader(name);
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#getRequestURL()
     */
    public StringBuffer getRequestURL() {
        return this.req.getRequestURL();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
     */
    public boolean isRequestedSessionIdFromUrl() {
        return this.req.isRequestedSessionIdFromUrl();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getLocalAddr()
     */
    public String getLocalAddr() {
        return this.req.getLocalAddr();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getLocalName()
     */
    public String getLocalName() {
        return this.req.getLocalName();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getLocalPort()
     */
    public int getLocalPort() {
        return this.req.getLocalPort();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    public Map getParameterMap() {
        return this.req.getParameterMap();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getReader()
     */
    public BufferedReader getReader() throws IOException {
        return this.req.getReader();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
     */
    public String getRealPath(String path) {
        return this.req.getRealPath(path);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRemotePort()
     */
    public int getRemotePort() {
        return this.req.getRemotePort();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
     */
    public RequestDispatcher getRequestDispatcher(String path) {
        return this.req.getRequestDispatcher(path);
    }

}
