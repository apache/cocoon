/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.environment.http;

import org.apache.cocoon.environment.Environment;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpEnvironment implements Environment {

    /** The current uri in progress */
    private String uri = null;

    /** The current prefix to strip off from the request uri */
    private StringBuffer prefix = new StringBuffer();

    /** The View requested */
    private String view = "";

    /** The HttpServletRequest */
    private HttpRequest req = null;

    /** The HttpServletResponse */
    private HttpResponse res = null;

    /**
     * Constructs a HttpEnvironment object from a HttpServletRequest 
     * and HttpServletResponse objects
     */
    public HttpEnvironment (HttpServletRequest req, HttpServletResponse res) {
        this.req = new HttpRequest (req);
        this.res = new HttpResponse (res);
        this.uri = this.req.getRequestURI(true);
        this.view = req.getHeader("cocoon-view");
    }
    /**
     * Adds an prefix to the overall stripped off prefix from the request uri
     */
    public void addUriPrefix (String prefix) {
        if (uri.startsWith (prefix)) {
            this.prefix.append (prefix);
            uri = uri.substring(prefix.length());
        } else {
            //FIXME: should we throw an error here ?
        }
    }

    /**
     * Returns the request view
     */
    public String getView () {
        return this.view;
    }

    /**
     * Returns the uri in progress. The prefix is stripped off
     */
    public String getUri () {
        return this.uri;
    }

    /**
     * Returns a wrapped HttpRequest object of the real HttpRequest in progress
     */
    public HttpServletRequest getRequest () {
        return this.req;
    }

    /**
     * Returns a wrapped HttpResponse object of the real HttpResponse in progress
     */
    public HttpServletResponse getResponse () {
        return this.res;
    }
}
