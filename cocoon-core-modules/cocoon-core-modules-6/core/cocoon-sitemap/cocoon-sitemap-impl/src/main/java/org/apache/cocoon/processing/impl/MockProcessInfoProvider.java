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
package org.apache.cocoon.processing.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

import org.apache.cocoon.environment.Cookie;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.processing.ProcessInfoProvider;

/**
 * Mock implementation of {@link ProcessInfoProvider} interface.
 *
 * @version $Id$
 * @since 2.2
 */
public class MockProcessInfoProvider implements ProcessInfoProvider {
    
    private Map objectModel;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ServletContext servletContext;

    public Map getObjectModel() {
        return objectModel;
    }
    
    public void setObjectModel(Map objectModel) {
        this.objectModel = objectModel;
    }

    public HttpServletRequest getRequest() {
        return request;
    }
    
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletResponse getResponse() {
        return response;
    }
    
    public void setResponse(HttpServletResponse response) {
        this.response = response;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    
    /**
     * This is a stub implementation of {@link HttpServletRequest}.
     * 
     * @see http://thread.gmane.org/gmane.text.xml.cocoon.devel/74276
     */
    static public class StubRequest implements HttpServletRequest {
        private Request request;
        
        public StubRequest(Request request) {
            this.request = request;
        }

        public Object get(String name) {
            return request.get(name);
        }

        public Object getLocalAttribute(String name) {
            return request.getLocalAttribute(name);
        }

        public Object getAttribute(String name) {
            return request.getAttribute(name);
        }

        public Enumeration getAttributeNames() {
            return request.getAttributeNames();
        }

        public Enumeration getLocalAttributeNames() {
            return request.getLocalAttributeNames();
        }

        public Map getAttributes() {
            return request.getAttributes();
        }

        public String getAuthType() {
            return request.getAuthType();
        }

        public String getCharacterEncoding() {
            return request.getCharacterEncoding();
        }

        public int getContentLength() {
            return request.getContentLength();
        }

        public String getContentType() {
            return request.getContentType();
        }

        public String getContextPath() {
            return request.getContextPath();
        }

        public Map getCookieMap() {
            return request.getCookieMap();
        }

        public javax.servlet.http.Cookie[] getCookies() {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public long getDateHeader(String name) {
            return request.getDateHeader(name);
        }

        public String getHeader(String name) {
            return request.getHeader(name);
        }

        public Enumeration getHeaderNames() {
            return request.getHeaderNames();
        }

        public Map getHeaders() {
            return request.getHeaders();
        }

        public Enumeration getHeaders(String name) {
            return request.getHeaders(name);
        }

        public ServletInputStream getInputStream() throws IOException, UnsupportedOperationException {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public Locale getLocale() {
            return request.getLocale();
        }

        public Enumeration getLocales() {
            return request.getLocales();
        }

        public String getMethod() {
            return request.getMethod();
        }

        public String getParameter(String name) {
            return request.getParameter(name);
        }

        public Enumeration getParameterNames() {
            return request.getParameterNames();
        }

        public Map getParameters() {
            return request.getParameters();
        }

        public String[] getParameterValues(String name) {
            return request.getParameterValues(name);
        }

        public String getPathInfo() {
            return request.getPathInfo();
        }

        public String getPathTranslated() {
            return request.getPathTranslated();
        }

        public String getProtocol() {
            return request.getProtocol();
        }

        public String getQueryString() {
            return request.getQueryString();
        }

        public String getRemoteAddr() {
            return request.getRemoteAddr();
        }

        public String getRemoteHost() {
            return request.getRemoteHost();
        }

        public String getRemoteUser() {
            return request.getRemoteUser();
        }

        public String getRequestedSessionId() {
            return request.getRequestedSessionId();
        }

        public String getRequestURI() {
            return request.getRequestURI();
        }

        public String getScheme() {
            return request.getScheme();
        }

        public String getServerName() {
            return request.getServerName();
        }

        public int getServerPort() {
            return request.getServerPort();
        }

        public String getServletPath() {
            return request.getServletPath();
        }

        public HttpSession getSession() {
            return getSession(true);
        }

        public HttpSession getSession(boolean create) {
            return new StubSession(request.getSession(create));
        }

        public String getSitemapPath() {
            return request.getSitemapPath();
        }

        public String getSitemapURI() {
            return request.getSitemapURI();
        }

        public String getSitemapURIPrefix() {
            return request.getSitemapURIPrefix();
        }

        public Principal getUserPrincipal() {
            return request.getUserPrincipal();
        }

        public boolean isRequestedSessionIdFromCookie() {
            return request.isRequestedSessionIdFromCookie();
        }

        public boolean isRequestedSessionIdFromURL() {
            return request.isRequestedSessionIdFromURL();
        }

        public boolean isRequestedSessionIdValid() {
            return request.isRequestedSessionIdValid();
        }

        public boolean isSecure() {
            return request.isSecure();
        }

        public boolean isUserInRole(String role) {
            return request.isUserInRole(role);
        }

        public void removeLocalAttribute(String name) {
            request.removeAttribute(name);
        }

        public void removeAttribute(String name) {
            request.removeAttribute(name);
        }

        public Object searchAttribute(String name) {
            return request.searchAttribute(name);
        }

        public void setLocalAttribute(String name, Object o) {
            request.setLocalAttribute(name, o);
        }

        public void setAttribute(String name, Object o) {
            request.setAttribute(name, o);
        }

        public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
            request.setCharacterEncoding(enc);
        }

        public int getIntHeader(String name) {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public StringBuffer getRequestURL() {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public boolean isRequestedSessionIdFromUrl() {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public String getLocalAddr() {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public String getLocalName() {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public int getLocalPort() {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public Map getParameterMap() {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public BufferedReader getReader() throws IOException {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public String getRealPath(String path) {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public int getRemotePort() {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public RequestDispatcher getRequestDispatcher(String path) {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

    }
    
    /**
     * This is a stub implementation of {@link HttpServletResponse}.
     * 
     * @see http://thread.gmane.org/gmane.text.xml.cocoon.devel/74276
     */
    static public class StubResponse implements HttpServletResponse {
        private Response response;
        
        public StubResponse(Response response) {
            this.response = response;
        }

        public void addCookie(Cookie cookie) {
            response.addCookie(cookie);
        }

        public void addDateHeader(String name, long date) {
            response.addDateHeader(name, date);
        }

        public void addHeader(String name, String value) {
            response.addHeader(name, value);
        }

        public void addIntHeader(String name, int value) {
            response.addIntHeader(name, value);
        }

        public boolean containsHeader(String name) {
            return response.containsHeader(name);
        }

        public javax.servlet.http.Cookie createCookie(String name, String value) {
            return response.createCookie(name, value);
        }

        public String encodeURL(String url) {
            return response.encodeURL(url);
        }

        public String getCharacterEncoding() {
            return response.getCharacterEncoding();
        }

        public Locale getLocale() {
            return response.getLocale();
        }

        public void setDateHeader(String name, long date) {
            response.setDateHeader(name, date);
        }

        public void setHeader(String name, String value) {
            response.setHeader(name, value);
        }

        public void setIntHeader(String name, int value) {
            response.setIntHeader(name, value);
        }

        public void setLocale(Locale loc) {
            response.setLocale(loc);
        }

        public void addCookie(javax.servlet.http.Cookie cookie) {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public String encodeRedirectURL(String url) {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public String encodeRedirectUrl(String url) {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public String encodeUrl(String url) {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public void sendError(int sc) throws IOException {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public void sendError(int sc, String msg) throws IOException {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public void sendRedirect(String location) throws IOException {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public void setStatus(int sc) {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public void setStatus(int sc, String sm) {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public void flushBuffer() throws IOException {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public int getBufferSize() {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public String getContentType() {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public ServletOutputStream getOutputStream() throws IOException {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public PrintWriter getWriter() throws IOException {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public boolean isCommitted() {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public void reset() {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public void resetBuffer() {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public void setBufferSize(int size) {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public void setCharacterEncoding(String charset) {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public void setContentLength(int len) {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public void setContentType(String type) {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

    }
    
    static public class StubSession implements HttpSession {
        private HttpSession session;
        
        public StubSession(HttpSession session) {
            this.session = session;
        }

        public Object getAttribute(String name) {
            return session.getAttribute(name);
        }

        public Enumeration getAttributeNames() {
            return session.getAttributeNames();
        }

        public Map getAttributes() {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public long getCreationTime() {
            return session.getCreationTime();
        }

        public String getId() {
            return session.getId();
        }

        public long getLastAccessedTime() {
            return session.getLastAccessedTime();
        }

        public int getMaxInactiveInterval() {
            return session.getMaxInactiveInterval();
        }

        public void invalidate() {
            session.invalidate();
        }

        public boolean isNew() {
            return session.isNew();
        }

        public void removeAttribute(String name) {
            session.removeAttribute(name);
        }

        public void setAttribute(String name, Object value) {
            session.setAttribute(name, value);
        }

        public void setMaxInactiveInterval(int interval) {
            session.setMaxInactiveInterval(interval);
        }

        public ServletContext getServletContext() {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public HttpSessionContext getSessionContext() {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public Object getValue(String name) {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public String[] getValueNames() {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public void putValue(String name, Object value) {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }

        public void removeValue(String name) {
            //FIXME: Implement this method if needed
            throw new UnsupportedOperationException();
        }
        
    }

}
