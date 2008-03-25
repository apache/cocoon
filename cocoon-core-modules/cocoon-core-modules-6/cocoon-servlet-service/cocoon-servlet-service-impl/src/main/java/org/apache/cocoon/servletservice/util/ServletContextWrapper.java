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
package org.apache.cocoon.servletservice.util;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * @version $Id$
 * @since 1.0.0
 */
public class ServletContextWrapper implements ServletContext {

    protected ServletContext servletContext;

    /**
     * @param servletContext The servletContext to set.
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public ServletContext getContext(String uripath) {
        return this.servletContext.getContext(uripath);
    }

    public int getMajorVersion() {
        return this.servletContext.getMajorVersion();
    }

    public int getMinorVersion() {
        return this.servletContext.getMinorVersion();
    }

    public String getMimeType(String file) {
        return this.servletContext.getMimeType(file);
    }

    public Set getResourcePaths(String paths) {
        return this.servletContext.getResourcePaths(paths);
    }

    public URL getResource(String path) throws MalformedURLException {
        return this.servletContext.getResource(path);
    }

    public InputStream getResourceAsStream(String path) {
        return this.servletContext.getResourceAsStream(path);
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return this.servletContext.getRequestDispatcher(path);
    }

    public RequestDispatcher getNamedDispatcher(String name) {
        return this.servletContext.getNamedDispatcher(name);
    }

    public Servlet getServlet(String name) throws ServletException {
        return this.servletContext.getServlet(name);
    }

    public Enumeration getServlets() {
        return this.servletContext.getServlets();
    }

    public Enumeration getServletNames() {
        return this.servletContext.getServletNames();
    }

    public void log(String msg) {
        this.servletContext.log(msg);
    }

    public void log(Exception exception, String msg) {
        this.servletContext.log(exception, msg);
    }

    public void log(String msg, Throwable throwable) {
        this.servletContext.log(msg, throwable);
    }

    public String getRealPath(String path) {
        return this.servletContext.getRealPath(path);
    }

    public String getServerInfo() {
        return this.servletContext.getServerInfo();
    }

    public String getInitParameter(String path) {
        return this.servletContext.getInitParameter(path);
    }

    public Enumeration getInitParameterNames() {
        return this.servletContext.getInitParameterNames();
    }

    public Object getAttribute(String name) {
        return this.servletContext.getAttribute(name);
    }

    public Enumeration getAttributeNames() {
        return this.servletContext.getAttributeNames();
    }

    public void setAttribute(String name, Object value) {
        this.servletContext.setAttribute(name, value);
    }

    public void removeAttribute(String name) {
        this.servletContext.removeAttribute(name);
    }

    public String getServletContextName() {
        return this.servletContext.getServletContextName();
    }

}
