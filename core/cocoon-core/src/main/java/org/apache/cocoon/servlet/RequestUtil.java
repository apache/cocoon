/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.cocoon.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @version $Id$
 * @since 2.2
 */
public class RequestUtil {

    public static String getCompleteUri(HttpServletRequest request,
                                        HttpServletResponse response)
    throws IOException {
        // We got it... Process the request
        String uri = request.getServletPath();
        // uri should never be null, but we check it anyway
        if (uri == null) {
            uri = "";
        }
        String pathInfo = request.getPathInfo();
        if (pathInfo != null) {
            // VG: WebLogic fix: Both uri and pathInfo starts with '/'
            // This problem exists only in WL6.1sp2, not in WL6.0sp2 or WL7.0b.
            // Comment: The servletPath always starts with '/', so it seems
            //          that the above mentioned bug is only occuring if the servlet path
            //          is just a "/".
            if (uri.length() > 0 && uri.charAt(0) == '/') {
                uri = uri.substring(1);
            }
            uri += pathInfo;
        }

        if (uri.length() == 0) {
            /* empty relative URI
                 -> HTTP-redirect from /cocoon to /cocoon/ to avoid
                    StringIndexOutOfBoundsException when calling
                    "".charAt(0)
               else process URI normally
            */
            String serverAbsoluteUri = request.getRequestURI();
            if (serverAbsoluteUri == null) {
                serverAbsoluteUri = "/";
            } else {
                serverAbsoluteUri += "/";
            }

            response.sendRedirect(response.encodeRedirectURL(serverAbsoluteUri));
            return null;
        }

        if (uri.charAt(0) == '/') {
            uri = uri.substring(1);
        }
        return uri;
    }

    public static HttpServletRequest createRequestForUri(HttpServletRequest request, String servletPath, String pathInfo) {
        return new HttpServletRequestImpl(request, servletPath, pathInfo);
    }

    protected static final class HttpServletRequestImpl extends HttpServletRequestWrapper {

        final private String servletPath;

        final private String pathInfo;

        final private String uri;

        public HttpServletRequestImpl(HttpServletRequest request, String servletPath, String pathInfo) {
            super(request);
            this.servletPath = servletPath;
            this.pathInfo = pathInfo;
            final StringBuffer buffer = new StringBuffer();
            if ( request.getContextPath() != null ) {
                buffer.append(request.getContextPath());
            }
            if ( buffer.length() == 1 && buffer.charAt(0) == '/' ) {
                buffer.deleteCharAt(0);
            }
            if ( servletPath != null ) {
                buffer.append(servletPath);
            }
            if ( pathInfo != null ) {
                buffer.append(pathInfo);
            }
            if ( buffer.charAt(0) != '/' ) {
                buffer.insert(0, '/');
            }
            this.uri = buffer.toString();
            
        }

        /**
         * @see javax.servlet.http.HttpServletRequestWrapper#getPathInfo()
         */
        public String getPathInfo() {
            return this.pathInfo;
        }

        /**
         * @see javax.servlet.http.HttpServletRequestWrapper#getRequestURI()
         */
        public String getRequestURI() {
            return this.uri;
        }

        /**
         * @see javax.servlet.http.HttpServletRequestWrapper#getRequestURL()
         */
        public StringBuffer getRequestURL() {
            final StringBuffer buffer = new StringBuffer();
            buffer.append(this.getProtocol());
            buffer.append("://");
            buffer.append(this.getServerName());
            boolean appendPort = true;
            if ( this.getScheme().equals("http") && this.getServerPort() == 80 ) {
                appendPort = false;
            }
            if ( this.getScheme().equals("https") && this.getServerPort() == 443) {
                appendPort = false;
            }
            if ( appendPort ) {
                buffer.append(':');
                buffer.append(this.getServerPort());
            }
            buffer.append(this.uri);
            return buffer;
        }

        /**
         * @see javax.servlet.http.HttpServletRequestWrapper#getServletPath()
         */
        public String getServletPath() {
            return this.servletPath;
        }
    }
}
