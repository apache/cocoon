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
package org.apache.cocoon.environment.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ForwardRedirector;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.Source;

/**
 * Experimental code for cleaning up the environment handling
 * This is an internal class, and it might change in an incompatible way over time.
 * For developing your own components/applications based on Cocoon, you shouldn't 
 * really need it.
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: EnvironmentHelper.java,v 1.3 2004/01/27 13:27:54 unico Exp $
 * @since 2.2
 */
public class EnvironmentHelper
extends AbstractLogEnabled
implements SourceResolver, Serviceable, Disposable {

    /** The key used to store the current SourceResolver 
     * in the environment context */
    private static final String SOURCE_RESOLVER_KEY = "global:" + SourceResolver.class.getName();

    /** The key used to store the current redirector 
     * in the environment context */
    private static final String REDIRECTOR_KEY = "global:" + Redirector.class.getName();

    /** The key used to store the current environment context
     * in the object model */
    static protected final String PROCESS_KEY = EnvironmentHelper.class.getName();

    /** The key used to store the last processor information
     * in the environment context
     */
    static protected final String LAST_PROCESSOR_KEY = "global:" + PROCESS_KEY + "/processor";
    
    /** The environment information */
    static protected final InheritableThreadLocal environmentStack = new CloningInheritableThreadLocal();
    
    /** The real source resolver */
    protected org.apache.excalibur.source.SourceResolver resolver;
    
    /** The service manager */
    protected ServiceManager manager;
    
    /** The complete prefix */
    protected String prefix;

     /** The Context path */
    protected String context;

    /** The last prefix, which is stripped off from the request uri */
    protected String lastPrefix;
    

    /**
     * Constructor
     *
     */
    public EnvironmentHelper(String context) {
        this.context = context;
    }
    
    /**
     * Constructor
     *
     */
    public EnvironmentHelper(EnvironmentHelper parent) {
        this.context = parent.context;
        this.lastPrefix = parent.lastPrefix;
        this.prefix = parent.prefix;
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (org.apache.excalibur.source.SourceResolver)
                          this.manager.lookup(org.apache.excalibur.source.SourceResolver.ROLE);
        Source source = null;
        try {
            source = this.resolver.resolveURI(this.context);
            this.context = source.getURI();
                
        } catch (IOException ioe) {
            throw new ServiceException("EnvironmentHelper", "Unable to resolve environment context. ", ioe);
        } finally {
            this.resolver.release(source);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( this.resolver );
            this.resolver = null;
            this.manager = null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.SourceResolver#release(org.apache.excalibur.source.Source)
     */
    public void release(Source source) {
        this.resolver.release(source);
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.SourceResolver#resolveURI(java.lang.String, java.lang.String, java.util.Map)
     */
    public Source resolveURI(final String location,
                             String baseURI,
                             final Map    parameters)
    throws MalformedURLException, IOException {
        return this.resolver.resolveURI(location, 
                                        (baseURI == null ? this.context : baseURI),
                                        parameters);
    }

    /* (non-Javadoc)
     * @see org.apache.excalibur.source.SourceResolver#resolveURI(java.lang.String)
     */
    public Source resolveURI(final String location)
    throws MalformedURLException, IOException {
        return this.resolveURI(location, null, null);
    }

    /**
     * Return the current context URI
     */
    public String getContext() {
        return this.context;
    }
    
    /**
     * Return the prefix
     */
    public String getPrefix() {
        return this.prefix;
    }
    
    /**
     * Change the context of the environment.
     * @param env The environment to change
     * @throws ProcessingException
     */
    public void changeContext(Environment env) 
    throws ProcessingException {
        if ( this.lastPrefix != null ) {
            final String uris = env.getURI();
            if (!uris.startsWith(this.lastPrefix)) {
                final String message = "The current URI (" + uris +
                                 ") doesn't start with given prefix (" + this.lastPrefix + ")";
                throw new ProcessingException(message);
            }      
            // we don't need to check for slash at the beginning
            // of uris - the prefix always ends with a slash!
            final int l = this.lastPrefix.length();
            env.setURI(this.prefix, uris.substring(l));
        }
    }
    
    /**
     * Set the context of the environment.
     * @param env The environment to change
     * @throws ProcessingException
     */
    public void setContext(Environment env) 
    throws ProcessingException {
        if ( this.prefix != null ) {
            // FIXME - This is not correct!
            final String uris = env.getURIPrefix() + env.getURI();
            if (!uris.startsWith(this.prefix)) {
                final String message = "The current URI (" + uris +
                                 ") doesn't start with given prefix (" + this.prefix + ")";
                throw new ProcessingException(message);
            }      
            // we don't need to check for slash at the beginning
            // of uris - the prefix always ends with a slash!
            final int l = this.prefix.length();
            env.setURI(this.prefix, uris.substring(l));
        }
    }

    /**
     * Adds an prefix to the overall stripped off prefix from the request uri
     */
    public void changeContext(Source newSource, String newPrefix)
    throws IOException {
        final String newContext = newSource.getURI();
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Changing Cocoon context");
            getLogger().debug("  from context(" + this.context + ") and prefix(" + this.prefix + ")");
            getLogger().debug("  to context(" + newContext + ") and prefix(" + newPrefix + ")");
        }
        int l = newPrefix.length();
        if (l >= 1) {
            this.lastPrefix = newPrefix;
            if ( this.prefix == null ) {
                this.prefix = "";
            }
            final StringBuffer buffer = new StringBuffer(this.prefix);
            buffer.append(newPrefix);
            // check for a slash at the beginning to avoid problems with subsitemaps
            if ( buffer.charAt(buffer.length()-1) != '/') {
                buffer.append('/');
                this.lastPrefix = this.lastPrefix + '/';
            }
            this.prefix = buffer.toString();
        }

        if (SourceUtil.getScheme(this.context).equals("zip")) {
            // if the resource is zipped into a war file (e.g. Weblogic temp deployment)
            // FIXME (VG): Is this still required? Better to unify both cases.
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Base context is zip: " + this.context);
            }
            
            org.apache.excalibur.source.Source source = null;
            try {
                source = this.resolver.resolveURI(this.context + newContext);
                this.context = source.getURI();
            } finally {
                this.resolver.release(source);
            }
        } else {
            String sContext;
            // if we got a absolute context or one with a protocol resolve it
            if (newContext.charAt(0) == '/') {
                // context starts with the '/' - absolute file URL
                sContext = "file:" + newContext;
            } else if (newContext.indexOf(':') > 1) {
                // context have ':' - absolute URL
                sContext = newContext;
            } else {
                // context is relative to old one
                sContext = this.context + '/' + newContext;
            }

            // Cut the file name part from context (if present)
            int i = sContext.lastIndexOf('/');
            if (i != -1 && i + 1 < sContext.length()) {
                sContext = sContext.substring(0, i + 1);
            }
            
            Source source = null;
            try {
                source = this.resolver.resolveURI(sContext);
                this.context = source.getURI();
            } finally {
                this.resolver.release(source);
            }
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("New context is " + this.context);
        }
    }
    
    public void redirect(Environment env, 
                         boolean sessionmode, 
                         String newURL) 
    throws IOException {
        this.doRedirect(env, sessionmode, newURL, false, false);
    }

    public void globalRedirect(Environment env, 
                               boolean sessionmode, 
                               String newURL) 
    throws IOException {
        this.doRedirect(env, sessionmode, newURL, false, true);
    }

    public void permanentRedirect(Environment env, boolean sessionmode, String newURL) 
    throws IOException {
        this.doRedirect(env, sessionmode, newURL, true, false);
    }

    /**
     *  Redirect the client to new URL with session mode
     */
    protected void doRedirect(Environment env, 
                             boolean sessionmode, 
                             String newURL, 
                             boolean permanent,
                             boolean global) 
    throws IOException {
        final Request request = ObjectModelHelper.getRequest(env.getObjectModel());
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
        final Response response = ObjectModelHelper.getResponse(env.getObjectModel());
        env.redirect(newURL, global, permanent);
    }

    /**
     * This hook must be called by the sitemap each time a sitemap is entered
     * This method should never raise an exception, except when the
     * parameters are not set!
     */
    public static void enterProcessor(Processor processor,
                                      ServiceManager manager,
                                      Environment env) 
    throws ProcessingException {
        if ( null == processor) {
            throw new ProcessingException("Processor is not set.");
        }

        EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if (stack == null) {
            stack = new EnvironmentStack();
            environmentStack.set(stack);
        }
        stack.pushInfo(new EnvironmentInfo(processor, stack.getOffset(), manager, env));
        stack.setOffset(stack.size()-1);
        
        EnvironmentContext ctx = (EnvironmentContext)env.getObjectModel().get(PROCESS_KEY);
        ctx.addAttribute(LAST_PROCESSOR_KEY, processor);
        ctx.addAttribute(SOURCE_RESOLVER_KEY, processor.getEnvironmentHelper());
        
        ForwardRedirector redirector = new ForwardRedirector(env);
        redirector.enableLogging(processor.getEnvironmentHelper().getLogger());
        ctx.addAttribute(REDIRECTOR_KEY, redirector);

    }

    /**
     * This hook must be called by the sitemap each time a sitemap is left.
     * It's the counterpart to {@link #enterProcessor(Processor)}.
     */
    public static void leaveProcessor() {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        final EnvironmentInfo info = (EnvironmentInfo)stack.pop();
        stack.setOffset(info.oldStackCount);
    }

    public static void checkEnvironment(Logger logger)
    throws Exception {
        // TODO (CZ): This is only for testing - remove it later on
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if (stack != null && !stack.isEmpty() ) {
            logger.error("ENVIRONMENT STACK HAS NOT BEEN CLEANED PROPERLY");
            throw new ProcessingException("Environment stack has not been cleaned up properly. "
                                          +"Please report this (if possible together with a test case) "
                                          +"to the Cocoon developers.");
        }
    }

    /**
     * This hook has to be called before a request is processed.
     * The hook is called by the Cocoon component and by the
     * cocoon protocol implementation.
     * This method should never raise an exception, except when
     * the environment is not set.
     *
     * @return A unique key within this thread.
     */
    public static Object startProcessing(Environment env) 
    throws ProcessingException {
        if ( null == env) {
            throw new ProcessingException("EnvironmentHelper.startProcessing: environment must be set.");
        }
        final EnvironmentContext desc = new EnvironmentContext(env);
        env.getObjectModel().put(PROCESS_KEY, desc);
        env.startingProcessing();
        return desc;
    }

    /**
     * Return the environment context
     */
    public static EnvironmentContext getCurrentEnvironmentContext() {
        final EnvironmentStack stack = (EnvironmentStack) environmentStack.get();
        if ( stack != null && !stack.empty() ) {
            final EnvironmentInfo info = stack.getCurrentInfo();
            final Map objectModel = info.environment.getObjectModel();
            return (EnvironmentContext)objectModel.get(PROCESS_KEY);
        }
        return null;
    }
    
    /**
     * Return the SourceResolver
     */
    public static SourceResolver getSourceResolver() {
        final EnvironmentContext ctx = getCurrentEnvironmentContext();
        if (ctx != null) {
            return (SourceResolver) ctx.getAttribute(SOURCE_RESOLVER_KEY);
        }
        return null;
    }
    
    /**
     * Return the Redirector
     */
    public static Redirector getRedirector() {
        final EnvironmentContext ctx = getCurrentEnvironmentContext();
        if (ctx != null) {
            return (Redirector) ctx.getAttribute(REDIRECTOR_KEY);
        }
        return null;
    }
    
    /**
     * Return the environment context
     */
    public static EnvironmentContext getEnvironmentContext(Environment environment) {
        final Map objectModel = environment.getObjectModel();
        return (EnvironmentContext)objectModel.get(PROCESS_KEY);
    }

    /**
     * This hook has to be called before a request is processed.
     * The hook is called by the Cocoon component and by the
     * cocoon protocol implementation.
     * @param key A unique key within this thread return by
     *         {@link #startProcessing(Environment)}.
     */
    public static void endProcessing(Environment env, Object key) {
        env.finishingProcessing();
        final EnvironmentContext desc = (EnvironmentContext)key;
        desc.dispose();
        env.getObjectModel().remove(PROCESS_KEY);
    }

    /**
     * Return the current processor
     */
    public static Processor getCurrentProcessor() {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if ( stack != null && !stack.isEmpty()) {
            final EnvironmentInfo info = stack.getCurrentInfo();
            return info.processor;
        }
        return null;
    }
    
    /**
     * Get the current sitemap component manager.
     * This method return the current sitemap component manager. This
     * is the manager that holds all the components of the currently
     * processed (sub)sitemap.
     */
    static public ServiceManager getSitemapServiceManager() {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if ( stack != null && !stack.isEmpty()) {
            final EnvironmentInfo info = stack.getCurrentInfo();
            return info.manager;
        }
        return null;
    }

    /**
     * Return the processor that is actually processing the request
     */
    public static Processor getLastProcessor(Environment env) {
        EnvironmentContext context = (EnvironmentContext) env.getObjectModel().get(PROCESS_KEY);
        return (Processor)env.getAttribute(LAST_PROCESSOR_KEY);
    }

    /**
     * Create an environment aware xml consumer for the cocoon
     * protocol
     */
    public static XMLConsumer createEnvironmentAwareConsumer(XMLConsumer consumer) {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        final EnvironmentInfo info = stack.getCurrentInfo();
        return stack.getEnvironmentAwareConsumerWrapper(consumer, info.oldStackCount);
    }
}

final class CloningInheritableThreadLocal
    extends InheritableThreadLocal {

    /**
     * Computes the child's initial value for this InheritableThreadLocal
     * as a function of the parent's value at the time the child Thread is
     * created.  This method is called from within the parent thread before
     * the child is started.
     * <p>
     * This method merely returns its input argument, and should be overridden
     * if a different behavior is desired.
     *
     * @param parentValue the parent thread's value
     * @return the child thread's initial value
     */
    protected Object childValue(Object parentValue) {
        if ( null != parentValue) {
            return ((EnvironmentStack)parentValue).clone();
        } else {
            return null;
        }
    }
}

