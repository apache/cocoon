/*

 ============================================================================
 The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

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

 */
package org.apache.cocoon.portal.pluto.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.cocoon.portal.pluto.PortletURLProviderImpl;
import org.apache.pluto.om.window.PortletWindow;

/**
 * Our request wrapper
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: URICopletAdapter.java,v 1.11 2003/10/20 13:36:41 cziegeler
 *          Exp $
 */
public class ServletRequestImpl implements HttpServletRequest {

    final protected HttpServletRequest request;

    /** Cache the parameter map */
    protected Map portletParameterMap;

    final protected PortletURLProviderImpl provider;

    protected PortletWindow window;

    public ServletRequestImpl(HttpServletRequest request,
                              PortletURLProviderImpl provider) {
        this.request = request;
        this.provider = provider;
    }

    public ServletRequestImpl(HttpServletRequest request,
                              PortletURLProviderImpl provider,
                              PortletWindow window) {
        this.request = request;
        this.provider = provider;
        this.window = window;
    }

    public ServletRequestImpl getRequest(PortletWindow window) {
        return new ServletRequestImpl(request, provider, window);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getAuthType()
     */
    public String getAuthType() {
        return this.request.getAuthType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getContextPath()
     */
    public String getContextPath() {
        return this.request.getContextPath();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getCookies()
     */
    public Cookie[] getCookies() {
        return this.request.getCookies();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
     */
    public long getDateHeader(String arg0) {
        return this.request.getDateHeader(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
     */
    public String getHeader(String arg0) {
        return this.request.getHeader(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
     */
    public Enumeration getHeaderNames() {
        return this.request.getHeaderNames();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
     */
    public Enumeration getHeaders(String arg0) {
        return this.request.getHeaders(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
     */
    public int getIntHeader(String arg0) {
        return this.request.getIntHeader(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getMethod()
     */
    public String getMethod() {
        return this.request.getMethod();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getPathInfo()
     */
    public String getPathInfo() {
        return this.request.getPathInfo();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
     */
    public String getPathTranslated() {
        return this.request.getPathTranslated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getQueryString()
     */
    public String getQueryString() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
     */
    public String getRemoteUser() {
        return this.request.getRemoteUser();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
     */
    public String getRequestedSessionId() {
        return this.request.getRequestedSessionId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getRequestURI()
     */
    public String getRequestURI() {
        return this.request.getRequestURI();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getRequestURL()
     */
    public StringBuffer getRequestURL() {
        //return this.request.getRequestURL();
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getServletPath()
     */
    public String getServletPath() {
        return this.request.getServletPath();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getSession()
     */
    public HttpSession getSession() {
        return this.request.getSession();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
     */
    public HttpSession getSession(boolean arg0) {
        return this.request.getSession(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
     */
    public Principal getUserPrincipal() {
        return this.request.getUserPrincipal();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
     */
    public boolean isRequestedSessionIdFromCookie() {
        return this.request.isRequestedSessionIdFromCookie();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
     */
    public boolean isRequestedSessionIdFromUrl() {
        return this.request.isRequestedSessionIdFromURL();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
     */
    public boolean isRequestedSessionIdFromURL() {
        return this.request.isRequestedSessionIdFromURL();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
     */
    public boolean isRequestedSessionIdValid() {
        return this.request.isRequestedSessionIdValid();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
     */
    public boolean isUserInRole(String arg0) {
        return this.request.isUserInRole(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
     */
    public Object getAttribute(String arg0) {
        return this.request.getAttribute(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        return this.request.getAttributeNames();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getCharacterEncoding()
     */
    public String getCharacterEncoding() {
        return this.request.getCharacterEncoding();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getContentLength()
     */
    public int getContentLength() {
        return this.request.getContentLength();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getInputStream()
     */
    public ServletInputStream getInputStream() throws IOException {
        return this.request.getInputStream();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getLocale()
     */
    public Locale getLocale() {
        return this.request.getLocale();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getLocales()
     */
    public Enumeration getLocales() {
        return this.request.getLocales();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getProtocol()
     */
    public String getProtocol() {
        return this.request.getProtocol();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getReader()
     */
    public BufferedReader getReader() throws IOException {
        return this.request.getReader();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
     */
    public String getRealPath(String arg0) {
        return this.request.getRealPath(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getRemoteAddr()
     */
    public String getRemoteAddr() {
        return this.request.getRemoteAddr();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getRemoteHost()
     */
    public String getRemoteHost() {
        return this.request.getRemoteHost();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
     */
    public RequestDispatcher getRequestDispatcher(String arg0) {
        return this.request.getRequestDispatcher(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getScheme()
     */
    public String getScheme() {
        return this.request.getScheme();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getServerName()
     */
    public String getServerName() {
        return this.request.getServerName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getServerPort()
     */
    public int getServerPort() {
        return this.request.getServerPort();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#isSecure()
     */
    public boolean isSecure() {
        return this.request.isSecure();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String arg0) {
        this.request.removeAttribute(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#setAttribute(java.lang.String,
     *      java.lang.Object)
     */
    public void setAttribute(String arg0, Object arg1) {
        this.request.setAttribute(arg0, arg1);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
     */
    public void setCharacterEncoding(String arg0)
    throws UnsupportedEncodingException {
        //this.request.setCharacterEncoding(arg0);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getContentType()
     */
    public java.lang.String getContentType() {
        String contentType = "text/html";
        if (getCharacterEncoding() != null) {
            contentType += ";" + getCharacterEncoding();
        }
        return contentType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
     */
    public String getParameter(String name) {
        return (String) this.getParameterMap().get(name);

    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getParameterMap()
     */
    public Map getParameterMap() {
        if (this.portletParameterMap == null) {
            //get control params
            this.portletParameterMap = new HashMap();

            if (this.provider != null
                && this.provider.getPortletWindow().equals(this.window)) {
                // get render parameters
                Iterator i = this.provider.getParameters().entrySet().iterator();
                while (i.hasNext()) {
                    Map.Entry entry = (Map.Entry) i.next();
                    // convert simple values to string arrays
                    if (entry.getValue() instanceof String) {
                        this.portletParameterMap.put(
                            entry.getKey(),
                            new String[] {(String) entry.getValue()});
                    } else {
                        this.portletParameterMap.put(
                            entry.getKey(),
                            entry.getValue());
                    }
                }

                //get request params
                Enumeration parameters = this.request.getParameterNames();
                while (parameters.hasMoreElements()) {
                    String paramName = (String) parameters.nextElement();
                    String[] paramValues =
                        (String[]) this.request.getParameterValues(paramName);
                    String[] values =
                        (String[]) this.portletParameterMap.get(paramName);

                    if (values != null) {
                        String[] temp =
                            new String[paramValues.length + values.length];
                        System.arraycopy(
                            paramValues,
                            0,
                            temp,
                            0,
                            paramValues.length);
                        System.arraycopy(
                            values,
                            0,
                            temp,
                            paramValues.length,
                            values.length);
                        paramValues = temp;
                    }
                    this.portletParameterMap.put(paramName, paramValues);
                }
            }
        }

        return this.portletParameterMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getParameterNames()
     */
    public Enumeration getParameterNames() {
        return Collections.enumeration(this.getParameterMap().keySet());
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(String name) {
        return (String[]) this.getParameterMap().get(name);
    }
}