/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.environment.http;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import uk.co.weft.maybeupload.MaybeUploadRequestWrapper;

/**
 *
 * Implements the {@link javax.servlet.http.HttpServletRequest} interface
 * to provide request information for HTTP servlets.
 */

public class HttpRequest extends HttpServletRequestWrapper {

    /** The HttpEnvironment object */
    private HttpEnvironment env = null;

    /** The HttpServletRequest object */
    private HttpServletRequest req = null;

    /**
     * Creates a HttpServletRequest based on a real HttpServletRequest object
     */
    protected HttpRequest (HttpServletRequest req, HttpEnvironment env) {
        super (req);
        this.env = env;
        this.req = req;
    }

    /* The HttpServletRequest interface methods */

    public Object get(String name) {
        if (this.req instanceof MaybeUploadRequestWrapper) {
            return ((MaybeUploadRequestWrapper) this.req).get(name);
        } else {
            String[] values = this.getParameterValues(name);

            if (values == null) return null;

            if (values.length == 1) {
                return values[0];
            }

            if (values.length > 1) {
                Vector vect = new Vector(values.length);

                for (int i = 0; i < values.length; i++) {
                    vect.add(values[i]);
                }

                return vect;
            }
        }

        return null;
    }

    public String getRequestURI() {
        return this.env.getURI();
    }

    public StringBuffer getRequestURL() {
        return new StringBuffer (this.env.getURI());
    }
}
