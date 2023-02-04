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
package org.apache.cocoon.environment;

import java.io.IOException;
import javax.servlet.http.HttpSession;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * A base class for <code>Redirector</code>s that handle forward redirects, i.e. internal
 * redirects using the "cocoon:" pseudo-protocol.
 * <p>
 * Concrete subclasses have to define the <code>cocoonRedirect()</code> method.
 *
 * @version $Id$
 */
public abstract class ForwardRedirector extends AbstractLogEnabled
                                        implements Redirector, PermanentRedirector {
	
	private static final String SERVLET_PROTOCOL = "servlet:";

    /**
     * Was there a call to <code>redirect()</code> ?
     */
    private boolean hasRedirected = false;
    
    /** The <code>Environment to use for redirection (either internal or external) */
    protected Environment env;

    /**
     * Constructor
     */
    public ForwardRedirector(Environment env) {
        this.env = env;
    }

    /**
     * Redirects to a given URL. If this URL starts with "cocoon:", then an internal
     * redirect is performed. Otherwise, an external redirect is send to the
     * environment.
     */
    public void redirect(boolean sessionMode, String url) throws IOException, ProcessingException {
        if (getLogger().isInfoEnabled()) {
            getLogger().info("Redirecting to '" + url + "'");
        }

        if (url.startsWith("cocoon:")) {
            cocoonRedirect(url);
        } else if (url.startsWith(SERVLET_PROTOCOL)) {
            servletRedirect(url);
        } else {
            this.doRedirect(sessionMode, url, false, false);
        }

        this.hasRedirected = true;
    }

    public void permanentRedirect(boolean sessionMode, String url) throws IOException, ProcessingException {
        if (getLogger().isInfoEnabled()) {
            getLogger().info("Redirecting to '" + url + "'");
        }

        if (url.startsWith("cocoon:")) {
            cocoonRedirect(url);
        } else if (url.startsWith(SERVLET_PROTOCOL)) {
            servletRedirect(url);
        } else {
            this.doRedirect(sessionMode, url, true, false);
        }

        this.hasRedirected = true;
    }

    /**
     * Unconditionally redirects to a given URL, even it this redirector is part of a
     * subpipeline.
     */
    public void globalRedirect(boolean sessionMode, String url) throws IOException, ProcessingException {
        if (getLogger().isInfoEnabled()) {
            getLogger().info("Redirecting to '" + url + "'");
        }

        // FIXME : how to handle global redirect to cocoon: ?
        if (url.startsWith("cocoon:")) {
            cocoonRedirect(url);
        } else if (url.startsWith(SERVLET_PROTOCOL)) {
            throw new ProcessingException("Global redirect with servlet: protocol are not possible!");
        } else {
            this.doRedirect(sessionMode, url, false, true);
        }
        this.hasRedirected = true;
    }

    protected abstract void cocoonRedirect(String uri) throws IOException, ProcessingException;
    
    /**
     * Handles rediraction to <code>servlet:</code> protocol URI.
     * @param uri starting with <code>servlet:</code>
     * @throws IOException
     * @throws ProcessingException
     */
    protected abstract void servletRedirect(String uri) throws IOException, ProcessingException;

    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Redirector#hasRedirected()
     */
    public boolean hasRedirected() {
        return this.hasRedirected;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.environment.Redirector#sendStatus(int)
     */
    public void sendStatus(int sc) {
        env.setStatus(sc);
        this.hasRedirected = true;
    }


    /**
     * Redirect the client to new URL with session mode
     */
    protected void doRedirect(boolean sessionmode, 
                              String newURL,
                              boolean permanent,
                              boolean global)
    throws IOException {
        final Request request = ObjectModelHelper.getRequest(this.env.getObjectModel());
        
        // check if session mode shall be activated
        if (sessionmode) {
            // The session
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("redirect: entering session mode");
            }
            String s = request.getRequestedSessionId();
            if (s != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Old session ID found in request, id = " + s);
                    if (request.isRequestedSessionIdValid()) {
                        getLogger().debug("And this old session ID is valid");
                    }
                }
            }

            // get session from request, or create new session
            HttpSession session = request.getSession(true);
            if (session == null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("redirect session mode: unable to get session object!");
                }
            } else {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("redirect: session mode completed, id = " + session.getId());
                }
            }
        }

        // redirect
        this.env.redirect(newURL, global, permanent);
    }
}
