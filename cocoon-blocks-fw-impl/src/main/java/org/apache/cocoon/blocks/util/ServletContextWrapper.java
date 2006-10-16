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
package org.apache.cocoon.blocks.util;

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
 * 
 */
public class ServletContextWrapper implements ServletContext {

    protected ServletContext servletContext;

    /**
     * @param servletContext The servletContext to set.
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getContext(java.lang.String)
     */
    public ServletContext getContext(String uripath) {
        return this.servletContext.getContext(uripath);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getMajorVersion()
     */
    public int getMajorVersion() {
        return this.servletContext.getMajorVersion();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getMinorVersion()
     */
    public int getMinorVersion() {
        return this.servletContext.getMinorVersion();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getMimeType(java.lang.String)
     */
    public String getMimeType(String file) {
        return this.servletContext.getMimeType(file);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getResourcePaths(java.lang.String)
     */
    public Set getResourcePaths(String paths) {
        return this.servletContext.getResourcePaths(paths);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getResource(java.lang.String)
     */
    public URL getResource(String path) throws MalformedURLException {
        return this.servletContext.getResource(path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
     */
    public InputStream getResourceAsStream(String path) {
        return this.servletContext.getResourceAsStream(path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getRequestDispatcher(java.lang.String)
     */
    public RequestDispatcher getRequestDispatcher(String path) {
        return this.servletContext.getRequestDispatcher(path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
     */
    public RequestDispatcher getNamedDispatcher(String name) {
        return this.servletContext.getNamedDispatcher(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getServlet(java.lang.String)
     */
    public Servlet getServlet(String name) throws ServletException {
        return this.servletContext.getServlet(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getServlets()
     */
    public Enumeration getServlets() {
        return this.servletContext.getServlets();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getServletNames()
     */
    public Enumeration getServletNames() {
        return this.servletContext.getServletNames();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#log(java.lang.String)
     */
    public void log(String msg) {
        this.servletContext.log(msg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#log(java.lang.Exception,
     *      java.lang.String)
     */
    public void log(Exception exception, String msg) {
        this.servletContext.log(exception, msg);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#log(java.lang.String,
     *      java.lang.Throwable)
     */
    public void log(String msg, Throwable throwable) {
        this.servletContext.log(msg, throwable);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
     */
    public String getRealPath(String path) {
        return this.servletContext.getRealPath(path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getServerInfo()
     */
    public String getServerInfo() {
        return this.servletContext.getServerInfo();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
     */
    public String getInitParameter(String path) {
        return this.servletContext.getInitParameter(path);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getInitParameterNames()
     */
    public Enumeration getInitParameterNames() {
        return this.servletContext.getInitParameterNames();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return this.servletContext.getAttribute(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        return this.servletContext.getAttributeNames();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#setAttribute(java.lang.String,
     *      java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        this.servletContext.setAttribute(name, value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        this.servletContext.removeAttribute(name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.ServletContext#getServletContextName()
     */
    public String getServletContextName() {
        return this.servletContext.getServletContextName();
    }

}
