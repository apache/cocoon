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

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.environment.wrapper.EnvironmentWrapper;

import java.io.IOException;
import java.net.MalformedURLException;

/**
 * A <code>Redirector</code> that handles forward redirects, i.e. internal
 * redirects using the "cocoon:" pseudo-protocol.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ForwardRedirector.java,v 1.4 2003/06/24 15:20:28 upayavira Exp $
 */
public class ForwardRedirector extends AbstractLogEnabled implements Redirector, PermanentRedirector {

    /** Was there a call to <code>redirect()</code> ? */
    private boolean hasRedirected = false;

    /** The <code>Environment to use for redirection (either internal or external) */
    private Environment env;

    /** The <code>Processor</code> that owns this redirector and which will be used
        to handle relative "cocoon:/..." redirects */
    private Processor processor;

    /** The component manager which gives access to the top-level <code>Processor</code>
        to handle absolute "cocoon://..." redirects */
    private ComponentManager manager;

    /** Is this internal*/
    private boolean internal;

    public ForwardRedirector(Environment env,
                             Processor processor,
                             ComponentManager manager,
                             boolean internal) {
        this.env = env;
        this.processor = processor;
        this.manager = manager;
        this.internal = internal;
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
            cocoonRedirect(sessionMode, url);
        } else {
            env.redirect(sessionMode, url);
        }
        this.hasRedirected = true;
    }

    public void permanentRedirect(boolean sessionMode, String url) throws IOException, ProcessingException {
        if (getLogger().isInfoEnabled()) {
            getLogger().info("Redirecting to '" + url + "'");
        }

        if (url.startsWith("cocoon:")) {
            cocoonRedirect(sessionMode, url);
        } else if (env instanceof PermanentRedirector) {
            ((PermanentRedirector)env).permanentRedirect(sessionMode, url);
        } else {
            env.redirect(sessionMode, url);
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
            cocoonRedirect(sessionMode, url);
        } else {
            if (env instanceof EnvironmentWrapper) {
              ((EnvironmentWrapper)env).globalRedirect(sessionMode,url);
            }
            else {
              env.redirect(sessionMode, url);
            }
        }
        this.hasRedirected = true;
    }

    private void cocoonRedirect(boolean sessionMode, String uri)
    throws IOException, ProcessingException {
        Processor actualProcessor = null;
        try {
            boolean rawMode = false;
            String prefix;

            // remove the protocol
            int protocolEnd = uri.indexOf(':');
            if (protocolEnd != -1) {
                uri = uri.substring(protocolEnd + 1);
                // check for subprotocol
                if (uri.startsWith("raw:")) {
                    uri = uri.substring(4);
                    rawMode = true;
                }
            }

            Processor usedProcessor;

            // Does the uri point to this sitemap or to the root sitemap?
            if (uri.startsWith("//")) {
                uri = uri.substring(2);
                prefix = ""; // start at the root
                try {
                    actualProcessor = (Processor)this.manager.lookup(Processor.ROLE);
                    usedProcessor = actualProcessor;
                } catch (ComponentException e) {
                    throw new ProcessingException("Cannot get Processor instance", e);
                }

            } else if (uri.startsWith("/")) {
                prefix = null; // means use current prefix
                uri = uri.substring(1);
                usedProcessor = this.processor;

            } else {
                throw new ProcessingException("Malformed cocoon URI.");
            }

            // create the queryString (if available)
            String queryString = null;
            int queryStringPos = uri.indexOf('?');
            if (queryStringPos != -1) {
                queryString = uri.substring(queryStringPos + 1);
                uri = uri.substring(0, queryStringPos);
            }

            // build the request uri which is relative to the context
            String requestURI = (prefix == null ? env.getURIPrefix() + uri : uri);

            ForwardEnvironmentWrapper newEnv =
                new ForwardEnvironmentWrapper(env, requestURI, queryString, getLogger(), rawMode);
            newEnv.setURI(prefix, uri);

            boolean processingResult;

            // FIXME - What to do here?
            Object processKey = CocoonComponentManager.startProcessing(newEnv);
            try {
                
                if ( !this.internal ) {
                    processingResult = usedProcessor.process(newEnv);
                } else {
                    ProcessingPipeline pp = usedProcessor.processInternal(newEnv);
                    if (pp != null) pp.release();
                    processingResult = pp != null;
                }
            } finally {
                CocoonComponentManager.endProcessing(newEnv, processKey);
            }


            if (!processingResult) {
                throw new ProcessingException("Couldn't process URI " + requestURI);
            }

        } catch(IOException ioe) {
            throw ioe;
        } catch(ProcessingException pe) {
            throw pe;
        } catch(Exception e) {
            String msg = "Error while redirecting to " + uri;
            getLogger().error(msg, e);
            throw new ProcessingException(msg, e);
        } finally {
            this.manager.release( actualProcessor );
        }
    }

    /**
     * Perform check on whether redirection has occured or not
     */
    public boolean hasRedirected() {
        return this.hasRedirected;
    }

    /**
     * Local extension of EnvironmentWrapper to propagate otherwise blocked
     * methods to the actual environment.
     */
    private final class ForwardEnvironmentWrapper extends EnvironmentWrapper {

        public ForwardEnvironmentWrapper(Environment env,
                              String      requestURI,
                              String      queryString,
                              Logger      logger,
                              boolean     rawMode) throws MalformedURLException {
            super(env, requestURI, queryString, logger, rawMode);
        }

        public void setStatus(int statusCode) {
            environment.setStatus(statusCode);
        }

        public void setContentLength(int length) {
            environment.setContentLength(length);
        }

        public void setContentType(String contentType) {
            environment.setContentType(contentType);
        }

        public String getContentType() {
            return environment.getContentType();
        }
    }
}
