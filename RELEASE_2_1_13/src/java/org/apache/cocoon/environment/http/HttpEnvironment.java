/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.environment.AbstractEnvironment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.PermanentRedirector;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.util.NetUtils;

/**
 * HTTP Servlet environment.
 *
 * @author <a herf="mailto:dev@cocoon.apache.org">Apache Cocoon Team</a>
 * @version $Id$
 */
public class HttpEnvironment extends AbstractEnvironment
                             implements Redirector, PermanentRedirector {

    public static final String HTTP_REQUEST_OBJECT = "httprequest";
    public static final String HTTP_RESPONSE_OBJECT= "httpresponse";
    public static final String HTTP_SERVLET_CONTEXT= "httpservletcontext";

    /** The HttpRequest */
    private HttpRequest request;

    /** The HttpResponse */
    private HttpResponse response;

    /** The HttpContext */
    private HttpContext webcontext;

    /** Cache content type as there is no getContentType() in reponse object */
    private String contentType;

    /** Did we redirect ? */
    private boolean hasRedirected = false;

    /**
     * Constructs a HttpEnvironment object from a HttpServletRequest
     * and HttpServletResponse objects
     */
    public HttpEnvironment(String uri,
                           String root,
                           HttpServletRequest req,
                           HttpServletResponse res,
                           ServletContext servletContext,
                           HttpContext context,
                           String containerEncoding,
                           String defaultFormEncoding)
    throws MalformedURLException, IOException {
        super(uri, null, root, null);

        this.request = new HttpRequest(req, this);

        if (req.getCharacterEncoding() == null) { // use the value from web.xml
            this.request.setContainerEncoding(containerEncoding != null ? containerEncoding : "ISO-8859-1");
        } else { // use what we have been given
            this.request.setContainerEncoding(req.getCharacterEncoding());
        }
        this.request.setCharacterEncoding(defaultFormEncoding != null ? defaultFormEncoding : "UTF-8");
        
        this.response = new HttpResponse(res);
        this.webcontext = context;

        setView(extractView(this.request));
        setAction(extractAction(this.request));

        this.objectModel.put(ObjectModelHelper.REQUEST_OBJECT, this.request);
        this.objectModel.put(ObjectModelHelper.RESPONSE_OBJECT, this.response);
        this.objectModel.put(ObjectModelHelper.CONTEXT_OBJECT, this.webcontext);

        // This is a kind of a hack for the components that need
        // the real servlet objects to pass them along to other
        // libraries.
        this.objectModel.put(HTTP_REQUEST_OBJECT, req);
        this.objectModel.put(HTTP_RESPONSE_OBJECT, res);
        this.objectModel.put(HTTP_SERVLET_CONTEXT, servletContext);
    }

    public void redirect(boolean sessionmode, String newURL) throws IOException {
        doRedirect(sessionmode, newURL, false);
    }

    public void permanentRedirect(boolean sessionmode, String newURL) throws IOException {
        doRedirect(sessionmode, newURL, true);
    }

    public void sendStatus(int sc) {
        setStatus(sc);
        this.hasRedirected = true;
    }

    /**
     *  Redirect the client to new URL with session mode
     */
    private void doRedirect(boolean sessionmode, String newURL, boolean permanent)
    throws IOException {
        this.hasRedirected = true;

        // check if session mode shall be activated
        if (sessionmode) {
            String s = request.getRequestedSessionId();
            if (getLogger().isDebugEnabled()) {
                if (s != null) {
                    getLogger().debug("Redirect: Requested session ID <" + s + "> is " +
                                      (request.isRequestedSessionIdValid()? "valid" : "invalid"));
                } else {
                    getLogger().debug("Redirect: No session found in request");
                }
            }

            // Get session from request, or create new session
            Session session = request.getSession();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug ("Redirect: Obtained session <" + session.getId() + ">");
            }
        }

        // Redirect
        String redirect = this.response.encodeRedirectURL(newURL);

        // FIXME (VG): WebSphere 4.0/4.0.1 bug
        if (!newURL.startsWith("/") && newURL.indexOf(':') == -1 && redirect.indexOf(':') != -1) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Redirect: WebSphere Bug Detected!");
            }
            String base = NetUtils.getPath(request.getRequestURI());
            if (base.startsWith("/")) {
                base = base.substring(1);
            }
            redirect = response.encodeRedirectURL(base + '/' + newURL);
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Sending redirect to '" + redirect + "'");
        }

        if (permanent) {
            this.response.sendPermanentRedirect(redirect);
        } else {
            this.response.sendRedirect(redirect);
        }
    }

    public boolean hasRedirected() {
        return this.hasRedirected;
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
        this.contentType = contentType;
    }

    /**
     * Get the ContentType
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Set the length of the generated content
     */
    public void setContentLength(int length) {
        this.response.setContentLength(length);
    }
    
    public void commitResponse() throws IOException
    {
        if (this.secureOutputStream != null) {
            // COCOOON-2358: for an HEAD request, it is wrong to set the content length to the actual response body size
            if (this.secureOutputStream.isResettable() && !request.getMethod().equals("HEAD")) {
                this.setContentLength(this.secureOutputStream.getCount());
            }
            this.secureOutputStream.flush();
        } else if (this.outputStream != null) {
            this.outputStream.flush();
        }
    }

    /**
     * Check if the response has been modified since the same
     * "resource" was requested.
     * The caller has to test if it is really the same "resource"
     * which is requested.
     * @return true if the response is modified or if the
     *         environment is not able to test it
     */
    public boolean isResponseModified(long lastModified) {
        if (lastModified != 0) {
            long if_modified_since = this.request.getDateHeader("If-Modified-Since");
            this.response.setDateHeader("Last-Modified", lastModified);
            return (if_modified_since / 1000 < lastModified  / 1000);
        }
        return true;
    }

    /**
     * Mark the response as not modified.
     */
    public void setResponseIsNotModified() {
        this.response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    }

    /**
     * Reset the response if possible. This allows error handlers to have
     * a higher chance to produce clean output if the pipeline that raised
     * the error has already output some data.
     *
     * @return true if the response was successfully reset
     */
    public boolean tryResetResponse()
    throws IOException {
        if (!super.tryResetResponse()) {
            try {
                if (!this.response.isCommitted()) {
                    this.response.reset();
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Response successfully reset");
                    }
                    return true;
                }
            } catch (Exception e) {
                // Log the error, but don't transmit it
                getLogger().warn("Problem resetting response", e);
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Response wasn't reset");
            }
            return false;
        }
        return true;
    }


    /**
     * Get the output stream where to write the generated resource.
     * The returned stream is buffered by the environment. If the
     * buffer size is -1 then the complete output is buffered.
     * If the buffer size is 0, no buffering takes place.
     * This method replaces {@link #getOutputStream()}.
     */
    public OutputStream getOutputStream(final int bufferSize)
    throws IOException {
        if (this.outputStream == null) {
            this.outputStream = this.response.getOutputStream();
        }
        return super.getOutputStream(bufferSize);
    }

    /**
     * Always return <code>true</code>.
     */
    public boolean isExternal() {
        return true;
    }
}
