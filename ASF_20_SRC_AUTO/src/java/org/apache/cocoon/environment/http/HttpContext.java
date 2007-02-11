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

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.environment.Context;

import javax.servlet.ServletContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.io.InputStream;

/**
 *
 * Implements the {@link org.apache.cocoon.environment.Context} interface
 * @author ?
 * @version CVS $Id: HttpContext.java,v 1.2 2004/03/05 13:02:55 bdelacretaz Exp $
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
}
