/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.wrapper.EnvironmentWrapper;

/**
 * A base class for <code>Redirector</code>s that handle forward redirects, i.e. internal
 * redirects using the "cocoon:" pseudo-protocol.
 * <p>
 * Concrete subclasses have to define the <code>cocoonRedirect()</code> method.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ForwardRedirector.java,v 1.13 2004/03/05 13:02:54 bdelacretaz Exp $
 */
public abstract class ForwardRedirector extends AbstractLogEnabled implements Redirector, PermanentRedirector {

    /**
     * Was there a call to <code>redirect()</code> ?
     */
    private boolean hasRedirected = false;
    
    /** The <code>Environment to use for redirection (either internal or external) */
    protected Environment env;

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
        } else {
            this.env.redirect(sessionMode, url);
        }

        this.hasRedirected = true;
    }

    public void permanentRedirect(boolean sessionMode, String url) throws IOException, ProcessingException {
        if (getLogger().isInfoEnabled()) {
            getLogger().info("Redirecting to '" + url + "'");
        }

        if (url.startsWith("cocoon:")) {
            cocoonRedirect(url);
        } else if (env instanceof PermanentRedirector) {
            ((PermanentRedirector)env).permanentRedirect(sessionMode, url);
        } else {
            this.env.redirect(sessionMode, url);
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
        } else if (env instanceof EnvironmentWrapper) {
            ((EnvironmentWrapper)env).globalRedirect(sessionMode,url);
        } else {
            this.env.redirect(sessionMode, url);
        }
        this.hasRedirected = true;
    }

    protected abstract void cocoonRedirect(String uri)  throws IOException, ProcessingException;

    /**
     * Perform check on whether redirection has occured or not
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
}
