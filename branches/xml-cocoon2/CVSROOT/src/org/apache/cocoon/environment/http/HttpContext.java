/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.environment.http;

import java.lang.String;
import java.net.MalformedURLException;
import java.net.URL;
import javax.servlet.ServletContext;
import org.apache.cocoon.environment.Context;

/**
 *
 * Implements the {@link org.apache.cocoon.environment.Context} interface
 */

public class HttpContext implements Context {

    /** The ServletContext */
    private ServletContext servletContext = null;

    /**
     * Constructs a HttpContext object from a ServletContext object
     */
    public HttpContext (ServletContext servletContext) {
                this.servletContext = servletContext;
        }

    public Object getAttribute(String name) {
                return servletContext.getAttribute(name);
        }

        public URL getResource(String path)
                         throws MalformedURLException {
                return servletContext.getResource(path);
        }

        public String getRealPath(String path)
                         throws MalformedURLException {
                return servletContext.getRealPath(path);
        }

        public java.lang.String getMimeType(String file) {
                return servletContext.getMimeType(file);
        }
}
