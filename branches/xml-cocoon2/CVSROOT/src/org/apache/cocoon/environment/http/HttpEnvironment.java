/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.environment.http;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.environment.AbstractEnvironment;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class HttpEnvironment extends AbstractEnvironment {

    /** The HttpServletRequest */
    private HttpRequest request = null;
    private HttpServletRequest servletRequest = null;

    /** The HttpServletResponse */
    private HttpResponse response = null;
    private HttpServletResponse servletResponse = null;

    /** The ServletContext */
    private ServletContext servletContext = null;

    /** The OutputStream */
    private OutputStream outputStream = null;

    /**
     * Constructs a HttpEnvironment object from a HttpServletRequest
     * and HttpServletResponse objects
     */
    public HttpEnvironment (String uri, HttpServletRequest request,
                            HttpServletResponse response,
                            ServletContext servletContext)
    throws MalformedURLException, IOException {
        super(uri, request.getParameter(Cocoon.VIEW_PARAM), servletContext.getRealPath("/"), request.getParameter(Cocoon.ACTION_PARAM));
        this.request = new HttpRequest (request, this);
        this.servletRequest = request;
        this.response = new HttpResponse (response);
        this.servletResponse = response;
        this.servletContext = servletContext;
        this.outputStream = response.getOutputStream();
        this.objectModel.put(Cocoon.REQUEST_OBJECT, this.request);
        this.objectModel.put(Cocoon.RESPONSE_OBJECT, this.response);
        this.objectModel.put(Cocoon.CONTEXT_OBJECT, this.servletContext);
    }

    /**
     * Redirect the client to a new URL
     */
    public void redirect(String newURL) throws IOException {
        String qs = request.getQueryString();
        String redirect = this.response.encodeRedirectURL(newURL);

        if (qs != null)
            redirect = redirect + "?" + qs;

        log.debug("Sending redirect to '" + redirect + "'");
        this.response.sendRedirect (redirect);
    }

    /**
     * Set the StatusCode
     */
    public void setStatus(int statusCode) {
        this.response.setStatus(statusCode);
    }

    /**
     * Set the ContentType
     */
    public void setContentType(String contentType) {
        this.response.setContentType(contentType);
    }

    /**
     * Get the OutputStream
     */
    public OutputStream getOutputStream() throws IOException {
        return this.outputStream;
    }
}
