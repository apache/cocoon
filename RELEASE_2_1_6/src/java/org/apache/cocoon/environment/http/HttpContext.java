/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.environment.http;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.environment.Context;

/**
 *
 * Implements the {@link org.apache.cocoon.environment.Context} interface
 * @author ?
 * @version CVS $Id: HttpContext.java,v 1.3 2004/05/07 17:23:42 joerg Exp $
 */

public final class HttpContext implements Context {

    /** The ServletContext */
    private final ServletContext servletContext;

    /**
     * Constructs a HttpContext object from a ServletContext object
     */
    public HttpContext (ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public Object getAttribute(String name) {
        return servletContext.getAttribute(name);
    }

    public void setAttribute(String name, Object value) {
        servletContext.setAttribute(name, value);
    }

    public void removeAttribute(String name) {
        servletContext.removeAttribute(name);
    }

    public Enumeration getAttributeNames() {
        return servletContext.getAttributeNames();
    }

    public URL getResource(String path)
       throws MalformedURLException {
       return servletContext.getResource(path);
    }

    public InputStream getResourceAsStream(String path) {
    return servletContext.getResourceAsStream(path);
    }

    public String getRealPath(String path) {
        if (path.equals("/")) {
            String value = servletContext.getRealPath(path);
            if (value == null) {
                // Try to figure out the path of the root from that of WEB-INF
                try {
                value = this.servletContext.getResource("/WEB-INF").toString();
                } catch (MalformedURLException mue) {
                    throw new CascadingRuntimeException("Cannot determine the base URL for " + path, mue);
                }
                value = value.substring(0,value.length()-"WEB-INF".length());
            }
            return value;
        }
        return servletContext.getRealPath(path);
    }

    public String getMimeType(String file) {
      return servletContext.getMimeType(file);
    }

    public String getInitParameter(String name) {
        return servletContext.getInitParameter(name);
    }

    /*
     * These methods are not in Cocoon's Context interface, but in the
     * ServletContext. To use them you have to downcast Cocoon's Context
     * to this HttpContext until we decide to add them to the Context
     * interface too.
     * 
     * The following methods are deprecated since Servlet API 2.0 or 2.1
     * and will not be implemented here:
     * - public Servlet getServlet(String name)
     * - public Enumeration getServletNames()
     * - public Enumeration getServlets()
     * - public void log(Exception exception, String msg)
     */

    public ServletContext getContext(String uripath) {
        return this.servletContext.getContext(uripath);
    }

    public Enumeration getInitParameterNames() {
        return this.servletContext.getInitParameterNames();
    }

    public int getMajorVersion() {
        return this.servletContext.getMajorVersion();
    }

    public int getMinorVersion() {
        return this.servletContext.getMinorVersion();
    }

    public RequestDispatcher getNamedDispatcher(String name) {
        return this.servletContext.getNamedDispatcher(name);
    }

    public RequestDispatcher getRequestDispatcher(String path) {
        return this.servletContext.getRequestDispatcher(path);
    }

    public String getServerInfo() {
        return this.servletContext.getServerInfo();
    }

    public void log(String msg) {
        this.servletContext.log(msg);
    }

    public void log(String msg, Throwable throwable) {
        this.servletContext.log(msg, throwable);
    }
}
