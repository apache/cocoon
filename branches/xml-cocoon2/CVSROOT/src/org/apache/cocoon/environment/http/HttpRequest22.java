/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.environment.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.ServletInputStream;
import javax.servlet.RequestDispatcher;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * Implements the {@link javax.servlet.http.HttpServletRequest} interface
 * to provide request information for HTTP servlets.
 */

public class HttpRequest implements HttpServletRequest {

    /** The real HttpServletRequest object */
    private HttpServletRequest req = null;

    /** The HttpEnvironment object */
    private HttpEnvironment env = null;

    /**
     * Creates a HttpServletRequest based on a real HttpServletRequest object
     */
    protected HttpRequest (HttpServletRequest req, HttpEnvironment env) {
        super ();
        this.req = req;
        this.env = env;
    }

    /* The HttpServletRequest interface methods */

    public Object get(String name) {
        if (this.req instanceof RequestWrapper) {
            return ((RequestWrapper) this.req).get(name);
        } else {
            String[] values = this.getParameterValues(name);

            if (values == null) return null;

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
        }

        return null;
    }

    public String getAuthType() {
        return this.req.getAuthType();
    }

    public Cookie[] getCookies() {
        return this.req.getCookies();
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

    public String getRequestURI() {
        return this.env.getURI();
    }

    public String getServletPath() {
        return this.req.getServletPath();
    }

    public HttpSession getSession(boolean create) {
        return this.req.getSession(create);
    }

    public HttpSession getSession() {
        return this.req.getSession();
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
     * @deprecated		As of Version 2.1 of the Java Servlet
     *				API, use {@link #isRequestedSessionIdFromURL}
     *				instead.
     */
    public boolean isRequestedSessionIdFromUrl() {
        return this.req.isRequestedSessionIdFromUrl();
    }

    /* The ServletRequest interface methods */

    public Object getAttribute(String name) {
        return this.req.getAttribute(name);
    }

    public Enumeration getAttributeNames() {
        return this.req.getAttributeNames();
    }

    public String getCharacterEncoding() {
        return this.req.getCharacterEncoding();
    }

    public int getContentLength() {
        return this.req.getContentLength();
    }

    public String getContentType() {
        return this.req.getContentType();
    }

    public ServletInputStream getInputStream() throws IOException {
        return this.req.getInputStream();
    }

    public String getParameter(String name) {
        return this.req.getParameter(name);
    }

    public Enumeration getParameterNames() {
        return this.req.getParameterNames();
    }

    public String[] getParameterValues(String name) {
        return this.req.getParameterValues(name);
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

    public void setAttribute(String name, Object o) {
        this.req.setAttribute(name, o);
    }

    public void removeAttribute(String name) {
        this.req.removeAttribute(name);
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
     * @deprecated 	As of Version 2.1 of the Java Servlet API,
     * 			use {@link javax.servlet.ServletContext#getRealPath} instead.
     */
    public String getRealPath(String path) {
        return this.req.getRealPath(path);
    }
}
