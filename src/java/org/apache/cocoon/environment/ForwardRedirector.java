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
package org.apache.cocoon.environment;

import java.io.IOException;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.treeprocessor.TreeProcessor;
import org.apache.cocoon.environment.wrapper.EnvironmentWrapper;

/**
 * A <code>Redirector</code> that handles forward redirects, i.e. internal
 * redirects using the "cocoon:" pseudo-protocol.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ForwardRedirector.java,v 1.11 2004/01/18 22:27:26 sylvain Exp $
 */
public class ForwardRedirector extends AbstractLogEnabled implements Redirector, PermanentRedirector {

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

    protected void cocoonRedirect(String uri)  throws IOException, ProcessingException {
        // Simply notify the Processor.
        this.env.setAttribute(TreeProcessor.COCOON_REDIRECT_ATTR, uri);
    }

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
