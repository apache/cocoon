/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.environment.http;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletOutputStream;

import org.apache.cocoon.environment.Response;

/**
 *
 * Implements the {@link HttpServletResponse} interface to provide HTTP-specific
 * functionality in sending a response.  For example, it has methods
 * to access HTTP headers and cookies.
 */

public class HttpResponse extends HttpServletResponseWrapper implements Response {

    /**
     * Creates a HttpServletResponse based on a real HttpServletResponse object
     */
    protected HttpResponse (HttpServletResponse res) {
        super (res);
    }

    /* The ServletResponse interface methods */

    public ServletOutputStream getOutputStream() throws IOException {
        //throw new IllegalStateException ("you are not a serializer or reader");
        return super.getOutputStream();
    }

    public PrintWriter getWriter() throws IOException {
        //throw new IllegalStateException ("you are not a serializer or reader");
        return super.getWriter();
    }
}

