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
package org.apache.cocoon.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * 
 * @version $Id$
 * @since 2.2
 */
public class UriHttpServletRequestWrapper extends HttpServletRequestWrapper {

    final private String servletPath;

    final private String pathInfo;

    final private String uri;

    public UriHttpServletRequestWrapper(HttpServletRequest request, String servletPath, String pathInfo) {
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

