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
