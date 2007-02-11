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
package org.apache.cocoon.environment.portlet;

import org.apache.cocoon.environment.AbstractEnvironment;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.PermanentRedirector;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Session;

import javax.portlet.PortletContext;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;

/**
 * Implements {@link Environment} interface for the JSR-168 Portlet environment.
 *
 * @author <a href="mailto:alex.rudnev@dc.gov">Alex Rudnev</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: PortletEnvironment.java,v 1.2 2003/12/03 13:20:29 vgritsenko Exp $
 */
public class PortletEnvironment extends AbstractEnvironment implements Redirector, PermanentRedirector {

    /**
     * As portlets do not have a pathInfo in the request, we can simulate this by passing
     * a parameter. If parameter is null, this will translate to the absent pathInfo,
     * and portlets will be matched in the sitemap using only servletPath.
     */
    public static final String PARAMETER_PATH_INFO = "cocoon-portlet-path";

    /**
     * This header can be read from any portlet request,
     * and can be set on action response.
     */
    public static final String HEADER_PORTLET_MODE = "X-Portlet-Mode";

    /**
     * This header can be read from any portlet request,
     * and can be set on action response.
     */
    public static final String HEADER_WINDOW_STATE = "X-Window-State";

    /**
     * This header can be set only, and only on render response.
     */
    public static final String HEADER_PORTLET_TITLE = "X-Portlet-Title";

    /**
     * This is the prefix for application scope session attributes.
     */
    public static final String SESSION_APPLICATION_SCOPE = "portlet-application-";


    /**
     * The PortletRequest
     */
    private PortletRequest request;

    /**
     * The PortletResponse
     */
    private PortletResponse response;

    /**
     * The PortletContext
     */
    private Context context;


    /**
     * Cache content type as there is no getContentType()
     * method on reponse object
     */
    private String contentType;

    /**
     * Did we redirect?
     */
    private boolean hasRedirected;


    /**
     * Constructs a PortletEnvironment object from a PortletRequest
     * and PortletResponse objects
     */
    public PortletEnvironment(String servletPath,
                              String uri,
                              String root,
                              javax.portlet.ActionRequest request,
                              javax.portlet.ActionResponse response,
                              PortletContext portletContext,
                              Context context,
                              String containerEncoding,
                              String defaultFormEncoding)
    throws MalformedURLException, IOException {
        super(uri, null, root, null);

        String pathInfo = request.getParameter(PARAMETER_PATH_INFO);

        this.request = new ActionRequest(servletPath, pathInfo, request, this);
        this.request.setCharacterEncoding(defaultFormEncoding);
        this.request.setContainerEncoding(containerEncoding);
        this.response = new ActionResponse(response, request.getPreferences(), (ActionRequest) this.request, uri);
        this.context = context;

        setView(extractView(this.request));
        setAction(extractAction(this.request));

        initObjectModel(request, response, portletContext);
    }

    /**
     * Constructs a PortletEnvironment object from a PortletRequest
     * and PortletResponse objects
     */
    public PortletEnvironment(String servletPath,
                              String uri,
                              String root,
                              javax.portlet.RenderRequest request,
                              javax.portlet.RenderResponse response,
                              PortletContext portletContext,
                              Context context,
                              String containerEncoding,
                              String defaultFormEncoding)
    throws MalformedURLException, IOException {
        super(uri, null, root, null);

        String pathInfo = request.getParameter(PARAMETER_PATH_INFO);

        this.request = new RenderRequest(servletPath, pathInfo, request, this);
        this.request.setCharacterEncoding(defaultFormEncoding);
        this.request.setContainerEncoding(containerEncoding);
        this.response = new RenderResponse(response, request.getPreferences());
        this.context = context;

        setView(extractView(this.request));
        setAction(extractAction(this.request));

        initObjectModel(request, response, portletContext);
    }

    private void initObjectModel(javax.portlet.PortletRequest portletRequest,
                                 javax.portlet.PortletResponse portletResponse,
                                 PortletContext portletContext) {
        this.objectModel.put(ObjectModelHelper.REQUEST_OBJECT, this.request);
        this.objectModel.put(ObjectModelHelper.RESPONSE_OBJECT, this.response);
        this.objectModel.put(ObjectModelHelper.CONTEXT_OBJECT, this.context);

        // This is a kind of a hack for the components that need
        // the real portlet objects to pass them along to other
        // libraries.
        PortletObjectModelHelper.setPortletRequest(this.objectModel, portletRequest);
        PortletObjectModelHelper.setPortletResponse(this.objectModel, portletResponse);
        PortletObjectModelHelper.setPortletContext(this.objectModel, portletContext);
    }


    public void redirect(boolean sessionmode, String newURL) throws IOException {
        this.hasRedirected = true;

        // check if session mode shall be activated
        if (sessionmode) {
            if (getLogger().isDebugEnabled()) {
                String s = request.getRequestedSessionId();
                if (s != null) {
                    getLogger().debug("Session ID in request = " + s +
                                      (request.isRequestedSessionIdValid() ? " (valid)" : " (invalid)"));
                }
            }

            // get session from request, or create new session
            Session session = request.getSession(true);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Session ID = " + session.getId());
            }
        }

        // redirect
        String redirect = newURL;
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Sending redirect to '" + redirect + "'");
        }

        this.response.sendRedirect(redirect);
    }

    /**
     * In portlet environment this is the same as {@see #redirect(boolean, String)}
     */
    public void permanentRedirect(boolean sessionmode, String newURL) throws IOException {
        redirect(sessionmode, newURL);
    }

    public boolean hasRedirected() {
        return this.hasRedirected;
    }

    /**
     * Portlet environment does not support response status code.
     */
    public void setStatus(int statusCode) {
    }

    /**
     * Portlet environment does not support response status code.
     */
    public void sendStatus(int sc) {
        throw new AbstractMethodError("Not Implemented");
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
     * Portlet environment does not support response content length.
     * This method does nothing.
     */
    public void setContentLength(int length) {
    }

    /**
     * This method always returns true because portlet environment
     * does not support response codes.
     */
    public boolean isResponseModified(long lastModified) {
        return true;
    }

    /**
     * Portlet environment does not support response status code.
     * This method does nothing.
     */
    public void setResponseIsNotModified() {
    }

    /**
     * Reset the response if possible. This allows error handlers to have
     * a higher chance to produce clean output if the pipeline that raised
     * the error has already output some data.
     *
     * @return true if the response was successfully reset
     */
    public boolean tryResetResponse() throws IOException {
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
    public OutputStream getOutputStream(final int bufferSize) throws IOException {
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
