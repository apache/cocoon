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
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.excalibur.source.Source;

/**
 * Experimental code for cleaning up the environment handling
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: EnvironmentHelper.java,v 1.7 2003/10/30 11:30:12 cziegeler Exp $
 * @since 2.2
 */
public class EnvironmentHelper
extends AbstractLogEnabled
implements SourceResolver, Serviceable, Disposable {

    /** The key used to store the current environment context
     * in the object model */
    static final String PROCESS_KEY = EnvironmentHelper.class.getName();

    /** The real source resolver */
    protected org.apache.excalibur.source.SourceResolver resolver;
    
    /** The service manager */
    protected ServiceManager manager;
    
    /** The complete prefix */
    protected String prefix;

     /** The Context path */
    protected String context;

    /** The root context path */
    protected String rootContext;

    /** The last prefix, which is stripped off from the request uri */
    protected String lastPrefix;
    
    /** The environment information */
    protected static InheritableThreadLocal environmentStack = new CloningInheritableThreadLocal();

    /**
     * Constructor
     *
     */
    public EnvironmentHelper(String context) {
        this.context = context;
        this.rootContext = context;
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (org.apache.excalibur.source.SourceResolver)
                          this.manager.lookup(org.apache.excalibur.source.SourceResolver.ROLE);
        if (this.context != null) {
            Source source = null;
            try {
                source = this.resolver.resolveURI(this.context);
                this.context = source.getURI();
                    
                if (this.rootContext == null) {// hack for EnvironmentWrapper
                    this.rootContext = this.context;
                }
            } catch (IOException ioe) {
                throw new ServiceException("EnvironmentHelper", "Unable to resolve environment context. ", ioe);
            } finally {
                this.resolver.release(source);
            }
            this.context = null;
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
    
    public void changeContext(Environment env) 
    throws ProcessingException {
        if ( this.lastPrefix != null ) {
            String uris = env.getURI();
            if (!uris.startsWith(this.lastPrefix)) {
                String message = "The current URI (" + uris +
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
     * Adds an prefix to the overall stripped off prefix from the request uri
     */
    public void changeContext(String newPrefix, String newContext)
    throws IOException {
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
    
    /**
     * This hook must be called by the sitemap each time a sitemap is entered
     * This method should never raise an exception, except when the
     * parameters are not set!
     */
    public static void enterProcessor(Processor processor,
                                      ServiceManager manager,
                                      Environment env) {
        if ( null == processor) {
            throw new IllegalArgumentException("Processor is not set.");
        }

        EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        if (stack == null) {
            stack = new EnvironmentStack();
            environmentStack.set(stack);
        }
        stack.pushInfo(new EnvironmentInfo(processor, stack.getOffset(), manager, env));
        stack.setOffset(stack.size()-1);
        // FIXME - Put it somewhere else
        env.setAttribute("EnvironmentHelper.processor", processor);
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
    public static Object startProcessing(Environment env) {
        if ( null == env) {
            throw new RuntimeException("EnvironmentHelper.startProcessing: environment must be set.");
        }
        final EnvironmentContext desc = new EnvironmentContext(env);
        env.getObjectModel().put(PROCESS_KEY, desc);
        env.startingProcessing();
        return desc;
    }

    /**
     * Return the environment context
     */
    public static EnvironmentContext getCurrentContext() {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        final EnvironmentInfo info = stack.getCurrentInfo();
        final Map objectModel = info.environment.getObjectModel();
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
     * Return the processor that is actually processing the request
     */
    public static Processor getLastProcessor(Environment env) {
        // FIXME - Put it somewhere else
        return (Processor)env.getAttribute("EnvironmentHelper.processor");
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

