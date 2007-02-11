/*
 * Copyright 2005 The Apache Software Foundation.
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
package javax.servlet.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;

/**
 * Mock class providing the declarations required to compile the Cocoon code when
 * the actual library is not present.
 * 
 * @version $Id$
 */
public class HttpServletRequestWrapper implements HttpServletRequest {

    public HttpServletRequestWrapper(HttpServletRequest request) {
        
    }

    public String getAuthType() {
        return null;
    }

    public String getContextPath() {
        return null;
    }

    public Cookie[] getCookies() {
        return null;
    }

    public long getDateHeader(String arg0) {
        return 0;
    }

    public String getHeader(String arg0) {
        return null;
    }

    public Enumeration getHeaderNames() {
        return null;
    }

    public Enumeration getHeaders(String arg0) {
        return null;
    }

    public int getIntHeader(String arg0) {
        return 0;
    }

    public String getMethod() {
        return null;
    }

    public String getPathInfo() {
        return null;
    }

    public String getPathTranslated() {
        return null;
    }

    public String getQueryString() {
        return null;
    }

    public String getRemoteUser() {
        return null;
    }

    public String getRequestedSessionId() {
        return null;
    }

    public String getRequestURI() {
        return null;
    }

    public String getServletPath() {
        return null;
    }

    public HttpSession getSession() {
        return null;
    }

    public HttpSession getSession(boolean arg0) {
        return null;
    }

    public Principal getUserPrincipal() {
        return null;
    }

    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    public boolean isRequestedSessionIdValid() {
        return false;
    }

    public boolean isUserInRole(String arg0) {
        return false;
    }

    public Object getAttribute(String arg0) {
        return null;
    }

    public Enumeration getAttributeNames() {
        return null;
    }

    public String getCharacterEncoding() {
        return null;
    }

    public int getContentLength() {
        return 0;
    }

    public String getContentType() {
        return null;
    }

    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    public Locale getLocale() {
        return null;
    }

    public Enumeration getLocales() {
        return null;
    }

    public String getParameter(String arg0) {
        return null;
    }

    public Enumeration getParameterNames() {
        return null;
    }

    public String[] getParameterValues(String arg0) {
        return null;
    }

    public String getProtocol() {
        return null;
    }

    public BufferedReader getReader() throws IOException {
        return null;
    }

    public String getRealPath(String arg0) {
        return null;
    }

    public String getRemoteAddr() {
        return null;
    }

    public String getRemoteHost() {
        return null;
    }

    public RequestDispatcher getRequestDispatcher(String arg0) {
        return null;
    }

    public String getScheme() {
        return null;
    }

    public String getServerName() {
        return null;
    }

    public int getServerPort() {
        return 0;
    }

    public boolean isSecure() {
        return false;
    }

    public void removeAttribute(String arg0) {
        
    }

    public void setAttribute(String arg0, Object arg1) {
        
    }

    public ServletRequest getRequest() {
        return null;
    }
}