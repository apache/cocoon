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
package org.apache.cocoon.components.jsp;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Stub implementation of HttpServletRequest.
 */
public class JSPEngineServletRequest implements HttpServletRequest {

    /** The servlet include path. */
    private static final String INC_SERVLET_PATH = "javax.servlet.include.servlet_path";
    /** The servlet request uri, needed for Resin. */
    private static final String INC_REQUEST_URI = "javax.servlet.include.request_uri";
    
    private final HttpServletRequest request;
    private final String jspFile;

    public JSPEngineServletRequest(HttpServletRequest request, String jspFile) {
        this.request = request;
        this.jspFile = jspFile;
    }
    public String getAuthType(){ return request.getAuthType(); }
    public Cookie[] getCookies(){ return request.getCookies(); }
    public long getDateHeader(String s){ return request.getDateHeader(s); }
    public String getHeader(String s){ return request.getHeader(s); }
    public Enumeration getHeaders(String s){ return request.getHeaders(s); }
    public Enumeration getHeaderNames(){ return request.getHeaderNames(); }
    public int getIntHeader(String s){ return request.getIntHeader(s); }
    public String getMethod(){ return request.getMethod(); }
    public String getPathInfo(){ return request.getPathInfo(); }
    public String getPathTranslated(){ return request.getPathTranslated(); }
    public String getContextPath(){ return request.getContextPath(); }
    public String getQueryString(){ return request.getQueryString(); }
    public String getRemoteUser(){ return request.getRemoteUser(); }
    public boolean isUserInRole(String s){ return request.isUserInRole(s); }
    public Principal getUserPrincipal(){ return request.getUserPrincipal(); }
    public String getRequestedSessionId(){ return request.getRequestedSessionId(); }
    public String getRequestURI(){ return request.getRequestURI(); }
    public String getServletPath(){ return request.getServletPath(); }
    public HttpSession getSession(boolean flag){ return request.getSession(flag); }
    public HttpSession getSession(){ return request.getSession(); }
    public boolean isRequestedSessionIdValid(){ return request.isRequestedSessionIdValid(); }
    public boolean isRequestedSessionIdFromCookie(){ return request.isRequestedSessionIdFromCookie(); }
    public boolean isRequestedSessionIdFromURL(){ return request.isRequestedSessionIdFromURL(); }
    /** @deprecated use isRequestedSessionIdFromURL instead. */
    public boolean isRequestedSessionIdFromUrl(){ return request.isRequestedSessionIdFromUrl(); }
    public Object getAttribute(String s){
        if (s != null && (s.equals(INC_SERVLET_PATH) || s.equals(INC_REQUEST_URI))) {
            return jspFile;
        }
        return request.getAttribute(s);
    }
    public Enumeration getAttributeNames(){ return request.getAttributeNames(); }
    public String getCharacterEncoding(){ return request.getCharacterEncoding(); }
    public int getContentLength(){ return request.getContentLength(); }
    public String getContentType(){ return request.getContentType(); }
    public ServletInputStream getInputStream() throws IOException{ return request.getInputStream(); }
    public String getParameter(String s){ return request.getParameter(s); }
    public Enumeration getParameterNames(){ return request.getParameterNames(); }
    public String[] getParameterValues(String s){ return request.getParameterValues(s); }
    public String getProtocol(){ return request.getProtocol(); }
    public String getScheme(){ return request.getScheme(); }
    public String getServerName(){ return request.getServerName(); }
    public int getServerPort(){ return request.getServerPort(); }
    public BufferedReader getReader()
        throws IOException{ return request.getReader(); }
    public String getRemoteAddr(){ return request.getRemoteAddr(); }
    public String getRemoteHost(){ return request.getRemoteHost(); }
    public void setAttribute(String s, Object obj){ request.setAttribute(s,obj); }
    public void removeAttribute(String s){ request.removeAttribute(s); }
    public Locale getLocale(){ return request.getLocale(); }
    public Enumeration getLocales(){ return request.getLocales(); }
    public boolean isSecure(){ return request.isSecure(); }
    public RequestDispatcher getRequestDispatcher(String s){ return request.getRequestDispatcher(s); }
    /** @deprecated use ServletContext.getRealPath(java.lang.String) instead. */
    public String getRealPath(String s){ return request.getRealPath(s); }
    public java.lang.StringBuffer getRequestURL() { return null; }
    public java.util.Map getParameterMap() { return null; }
    public void setCharacterEncoding(java.lang.String s) { }
}
