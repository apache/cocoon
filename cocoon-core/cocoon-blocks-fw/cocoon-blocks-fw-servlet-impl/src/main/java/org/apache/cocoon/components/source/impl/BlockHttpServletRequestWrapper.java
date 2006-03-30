/*
 * Copyright 2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.source.impl;

import java.net.URI;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.cocoon.environment.wrapper.RequestParameters;

/**
 * Wraps the request object of the environment. All URI methods are overrided to reflect the
 * used block URI. Request parameters and attributes have local values as well, no fallback
 * to the original request, (is that needed?).
 * 
 * @version $Id$
 */
class BlockHttpServletRequestWrapper extends HttpServletRequestWrapper {
    
    private URI uri;
    private Hashtable attributes = new Hashtable();
    private RequestParameters parameters;

    public BlockHttpServletRequestWrapper(HttpServletRequest request, URI uri) {
        super(request);
        this.uri = uri;
        this.parameters = new RequestParameters(this.uri.getQuery());
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequestWrapper#getContextPath()
     */
    public String getContextPath() {
        return "";
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequestWrapper#getPathInfo()
     */
    public String getPathInfo() {
        return this.uri.getPath();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequestWrapper#getQueryString()
     */
    public String getQueryString() {
        return this.uri.getQuery();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequestWrapper#getRequestURI()
     */
    public String getRequestURI() {
        return this.getContextPath() + this.getServletPath() + this.getPathInfo();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequestWrapper#getRequestURL()
     */
    public StringBuffer getRequestURL() {
        return new StringBuffer(this.uri.getScheme()).append(':').append(this.getRequestURI());
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServletRequestWrapper#getServletPath()
     */
    public String getServletPath() {
        return "";
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getAttributeNames()
     */
    public Enumeration getAttributeNames() {
        return this.attributes.keys();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
     */
    public String getParameter(String name) {
        return this.parameters.getParameter(name);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameterMap()
     */
    public Map getParameterMap() {
        // TODO Implement this
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameterNames()
     */
    public Enumeration getParameterNames() {
        return this.parameters.getParameterNames();
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
     */
    public String[] getParameterValues(String name) {
        return this.parameters.getParameterValues(name);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    /* (non-Javadoc)
     * @see javax.servlet.ServletRequestWrapper#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        if (value != null)
            this.attributes.put(name, value);
        else
            this.removeAttribute(name);
    }
}