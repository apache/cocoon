/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.environment.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.AbstractEnvironment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.util.NetUtils;

/**
 * @author ?
 * @version CVS $Id: HttpEnvironment.java,v 1.8 2003/05/16 07:04:56 cziegeler Exp $
 */
public class HttpEnvironment extends AbstractEnvironment implements Redirector {

    public static final String HTTP_REQUEST_OBJECT = "httprequest";
    public static final String HTTP_RESPONSE_OBJECT= "httpresponse";
    public static final String HTTP_SERVLET_CONTEXT= "httpservletcontext";

    /** The HttpRequest */
    private HttpRequest request = null;

    /** The HttpResponse */
    private HttpResponse response = null;

    /** The HttpContext */
    private HttpContext webcontext = null;

    /** Cache content type as there is no getContentType() in reponse object */
    private String contentType = null;

    /** Did we redirect ? */
    private boolean hasRedirected = false;

    /**
     * Constructs a HttpEnvironment object from a HttpServletRequest
     * and HttpServletResponse objects
     */
    public HttpEnvironment (String uri,
                            String root,
                            HttpServletRequest req,
                            HttpServletResponse res,
                            ServletContext servletContext,
                            HttpContext context,
                            String containerEncoding,
                            String defaultFormEncoding)
     throws MalformedURLException, IOException {
        super(uri, req.getParameter(Constants.VIEW_PARAM), root, extractAction(req));

        this.request = new HttpRequest(req, this);
        this.request.setCharacterEncoding(defaultFormEncoding);
        this.request.setContainerEncoding(containerEncoding);
        this.response = new HttpResponse(res);
        this.webcontext = context;
        
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

   /**
    * extract the action portion from the request
    * (must be static because it's called in the super() constructor.
    *  should maybe go into a helper or directly into sitemap)
    */
    private final static String extractAction(HttpServletRequest req) {
      String action = req.getParameter(Constants.ACTION_PARAM);
      if (action != null) {
        /* TC: still support the deprecated syntax */
        return(action);
      }
      else {
        for(Enumeration e = req.getParameterNames(); e.hasMoreElements(); ) {
          String name = (String)e.nextElement();
          if (name.startsWith(Constants.ACTION_PARAM_PREFIX)) {
            if (name.endsWith(".x") || name.endsWith(".y")) {
              return(name.substring(Constants.ACTION_PARAM_PREFIX.length(),name.length()-2));
            }
            else {
              return(name.substring(Constants.ACTION_PARAM_PREFIX.length()));
            }
          }
        }
        return(null);
      }
    }

   /**
    *  Redirect the client to new URL with session mode
    */
    public void redirect(boolean sessionmode, String newURL) throws IOException {
        this.hasRedirected = true;

        // check if session mode shall be activated
        if (sessionmode) {
            // The session
            Session session = null;
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("redirect: entering session mode");
            }
            String s = request.getRequestedSessionId();
            if (s != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Old session ID found in request, id = " + s);
                    if ( request.isRequestedSessionIdValid() ) {
                        getLogger().debug("And this old session ID is valid");
                    }
                }
            }
            // get session from request, or create new session
            session = request.getSession(true);
            if (session == null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("redirect session mode: unable to get session object!");
                }
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug ("redirect: session mode completed, id = " + session.getId() );
            }
        }
        // redirect
        String redirect = this.response.encodeRedirectURL(newURL);

        // FIXME (VG): WebSphere 4.0/4.0.1 bug
        if (!newURL.startsWith("/") && newURL.indexOf(':') == -1 && redirect.indexOf(':') != -1) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Redirect: WebSpehere Bug Detected!");
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
        this.response.sendRedirect (redirect);
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
            return (if_modified_since < lastModified);
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
        if ( this.outputStream == null) {
            this.outputStream = this.response.getOutputStream();
        }
        return super.getOutputStream( bufferSize );
    }

}
