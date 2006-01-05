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
package org.apache.cocoon.environment.wrapper;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.impl.AbstractRequest;


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

    /**
     * Constructor
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
    public Cookie[] getCookies() {
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
	public InputStream getInputStream() throws IOException, UnsupportedOperationException {
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
    public Session getSession(boolean create) {
        return this.req.getSession(create);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getSession()
     */
    public Session getSession() {
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

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getAttribute(java.lang.String, int)
     */
    public Object getAttribute(String name, int scope) {
        return this.req.getAttribute(name, scope);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#getAttributeNames(int)
     */
    public Enumeration getAttributeNames(int scope) {
        return this.req.getAttributeNames(scope);
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#removeAttribute(java.lang.String, int)
     */
    public void removeAttribute(String name, int scope) {
        this.req.removeAttribute(name,scope);
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Request#setAttribute(java.lang.String, java.lang.Object, int)
     */
    public void setAttribute(String name, Object o, int scope) {
        this.req.setAttribute(name, o, scope);
    }

    /**
     * @see org.apache.cocoon.environment.Request#getSitemapURIPrefix()
     */
    public String getSitemapURIPrefix() {
        return this.req.getSitemapURIPrefix();
    }

    /**
     * @see org.apache.cocoon.environment.Request#searchAttribute(java.lang.String)
     */
    public Object searchAttribute(String name) {
        return this.req.searchAttribute(name);
    }

}
