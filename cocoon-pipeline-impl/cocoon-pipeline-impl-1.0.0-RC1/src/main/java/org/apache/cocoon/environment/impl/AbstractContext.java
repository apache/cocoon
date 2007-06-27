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
package org.apache.cocoon.environment.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.cocoon.environment.Context;

/**
 * Base class for any context
 *
 * @version $Id$
 */
public abstract class AbstractContext 
    implements Context, ServletContext {

    /** Attributes. */
    protected final Map attributes = new HashMap();

    /**
     * @see javax.servlet.ServletContext#getAttribute(java.lang.String)
     */
    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    /**
     * @see javax.servlet.ServletContext#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    /**
     * @see javax.servlet.ServletContext#getContext(java.lang.String)
     */
    public ServletContext getContext(String arg0) {
        return this;
    }

    /**
     * @see javax.servlet.ServletContext#getInitParameter(java.lang.String)
     */
    public String getInitParameter(String arg0) {
        return null;
    }

    /**
     * @see javax.servlet.ServletContext#getInitParameterNames()
     */
    public Enumeration getInitParameterNames() {
        return Collections.enumeration(Collections.EMPTY_LIST);
    }

    /**
     * @see javax.servlet.ServletContext#getMajorVersion()
     */
    public int getMajorVersion() {
        return 2;
    }

    /**
     * @see javax.servlet.ServletContext#getMimeType(java.lang.String)
     */
    public String getMimeType(String arg0) {
        return null;
    }

    /**
     * @see javax.servlet.ServletContext#getMinorVersion()
     */
    public int getMinorVersion() {
        return 3;
    }

    /**
     * @see javax.servlet.ServletContext#getNamedDispatcher(java.lang.String)
     */
    public RequestDispatcher getNamedDispatcher(String arg0) {
        return null;
    }

    /**
     * @see javax.servlet.ServletContext#getRealPath(java.lang.String)
     */
    public String getRealPath(String arg0) {
        return null;
    }

    /**
     * @see javax.servlet.ServletContext#getRequestDispatcher(java.lang.String)
     */
    public RequestDispatcher getRequestDispatcher(String arg0) {
        return null;
    }

    /**
     * @see javax.servlet.ServletContext#getResource(java.lang.String)
     */
    public URL getResource(String arg0) throws MalformedURLException {
        return null;
    }

    /**
     * Get access to the resource as @link {@link InputStream}. If there is any problem,
     * <code>null</code> is returned.
     * 
     * @see javax.servlet.ServletContext#getResourceAsStream(java.lang.String)
     */
    public InputStream getResourceAsStream(String path) {
    	URL resourceURL = null;
		try {
			resourceURL = this.getResource(path);
		} catch (MalformedURLException e) {
			return null;
		}
    	if(resourceURL != null) {
    		try {
				return resourceURL.openStream();
			} catch (IOException e) {
				return null;
			}
    	}
        return null;
    }

    /**
     * @see javax.servlet.ServletContext#getResourcePaths(java.lang.String)
     */
    public Set getResourcePaths(String arg0) {
        return null;
    }

    /**
     * @see javax.servlet.ServletContext#getServerInfo()
     */
    public String getServerInfo() {
        return null;
    }

    /**
     * @see javax.servlet.ServletContext#getServlet(java.lang.String)
     */
    public Servlet getServlet(String arg0) throws ServletException {
        return null;
    }

    /**
     * @see javax.servlet.ServletContext#getServletContextName()
     */
    public String getServletContextName() {
        return null;
    }

    /**
     * @see javax.servlet.ServletContext#getServletNames()
     */
    public Enumeration getServletNames() {
        return null;
    }

    /**
     * @see javax.servlet.ServletContext#getServlets()
     */
    public Enumeration getServlets() {
        return null;
    }

    /**
     * @see javax.servlet.ServletContext#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String key) {
        this.attributes.remove(key);
    }

    /**
     * @see javax.servlet.ServletContext#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String key, Object object) {
        this.attributes.put(key, object);
    }

    /**
     * @see org.apache.cocoon.environment.Context#getAttributes()
     */
    public Map getAttributes() {
	    return new ContextMap(this);
    }
}
