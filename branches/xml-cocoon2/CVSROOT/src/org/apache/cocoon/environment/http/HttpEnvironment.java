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

import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.AbstractEnvironment;
import org.apache.cocoon.environment.Session;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class HttpEnvironment extends AbstractEnvironment {

    /** The HttpRequest */
    private HttpRequest request = null;

    /** The HttpResponse */
    private HttpResponse response = null;

    /** The HttpContext */
    private HttpContext context = null;

    /** The OutputStream */
    private OutputStream outputStream = null;

    /**
     * Constructs a HttpEnvironment object from a HttpServletRequest
     * and HttpServletResponse objects
     */
    public HttpEnvironment (String uri, HttpServletRequest req,
                            HttpServletResponse res,
                            ServletContext servletContext)
    throws MalformedURLException, IOException {
        super(uri, req.getParameter(Constants.VIEW_PARAM), servletContext.getRealPath("/"), req.getParameter(Constants.ACTION_PARAM));

        this.request = new HttpRequest (req, this);
        this.response = new HttpResponse (res);
        this.context = new HttpContext (servletContext);
        this.outputStream = response.getOutputStream();
        this.objectModel.put(Constants.REQUEST_OBJECT, this.request);
        this.objectModel.put(Constants.RESPONSE_OBJECT, this.response);
        this.objectModel.put(Constants.CONTEXT_OBJECT, this.context);
    }

   /**
    *  Redirect the client to new URL with session mode
    */
    public void redirect(boolean sessionmode, String newURL) throws IOException {
        if (request == null) {
            getLogger().debug("redirect: something's broken, request = null");
            return;
        }
        // check if session mode shall be activated
        if (sessionmode) {
            // The session
            Session session = null;
            getLogger().debug("redirect: entering session mode");
            String s = request.getRequestedSessionId();
            if (s != null) {
                getLogger().debug("Old session ID found in request, id = " + s);
                if ( request.isRequestedSessionIdValid() ) {
                    getLogger().debug("And this old session ID is valid");
                }
            }
            // get session from request, or create new session
            session = request.getSession(true);
            if (session == null) {
                getLogger().debug("redirect session mode: unable to get session object!");
            }
            getLogger().debug ("redirect: session mode completed, id = " + session.getId() );
        }
        // redirect
        String qs = request.getQueryString();
        String redirect = this.response.encodeRedirectURL(newURL);

        if (qs != null)
            redirect = redirect + "?" + qs;

        getLogger().debug("Sending redirect to '" + redirect + "'");
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
     * Set the length of the generated content
     */
    public void setContentLength(int length) {
        this.response.setContentLength(length);
    }

    /**
     * Get the OutputStream
     */
    public OutputStream getOutputStream() throws IOException {
        return this.outputStream;
    }

    /**
     * Check if the response has been modified since the same
     * "resource" was requested.
     * The caller has to test if it is really the same "resource"
     * which is requested.
     * @result true if the response is modified or if the
     *         environment is not able to test it
     */
    public boolean isResponseModified(long lastModified) {
        long if_modified_since = this.request.getDateHeader("If-Modified-Since");

        this.response.setDateHeader("Last-Modified", lastModified);
        return (if_modified_since < lastModified);
    }

    /**
     * Mark the response as not modified.
     */
    public void setResponseIsNotModified() {
        this.response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    }

}
