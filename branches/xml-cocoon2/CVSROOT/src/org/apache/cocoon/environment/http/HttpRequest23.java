/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.environment.http;

//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.UnsupportedEncodingException;
//import java.util.Enumeration;
//import java.util.Locale;
//import java.util.StringBuffer;

//import javax.servlet.ServletInputStream;
//import javax.servlet.RequestDispatcher;

//import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
//import javax.servlet.http.HttpSession;

/**
 *
 * Implements the {@link javax.servlet.http.HttpServletRequest} interface
 * to provide request information for HTTP servlets.
 */

public class HttpRequest extends HttpServletRequestWrapper {

    /** The HttpEnvironment object */
    private HttpEnvironment env = null;

    /**
     * Creates a HttpServletRequest based on a real HttpServletRequest object
     */
    protected HttpRequest (HttpServletRequest req, HttpEnvironment env) {
        super (req);
        this.env = env;
    }

    /* The HttpServletRequest interface methods */

    public String getRequestURI() {
        return this.env.getURI();
    }

    public StringBuffer getRequestURL() {
        return new StringBuffer (this.env.getURI());
    }
}
