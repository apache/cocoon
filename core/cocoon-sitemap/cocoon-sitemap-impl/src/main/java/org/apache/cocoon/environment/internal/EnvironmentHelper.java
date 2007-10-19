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
package org.apache.cocoon.environment.internal;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.excalibur.source.Source;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.source.util.SourceUtil;
import org.apache.cocoon.core.container.spring.avalon.AvalonUtils;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.spring.configurator.WebAppContextUtils;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.xml.XMLConsumer;

/**
 * Helper class for maintaining the environment stack.
 *
 * This is an internal class, and it might change in an incompatible way over time.
 * For developing your own components/applications based on Cocoon, you shouldn't
 * really need it.
 *
 * INTERNAL CLASS. Do not use this, can be removed without warning or deprecation cycle.
 *
 * @since 2.2
 * @version $Id$
 */
public class EnvironmentHelper extends AbstractLogEnabled
                               implements SourceResolver, Serviceable, Disposable {

    private static final Log logger = LogFactory.getLog(EnvironmentHelper.class);

    /** The environment information */
    static protected final ThreadLocal environmentStack = new ThreadLocal();

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
    public EnvironmentHelper(URL context) {
        if (context != null) {
            this.context = context.toExternalForm();
        }
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

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager avalonManager) throws ServiceException {
        this.manager = avalonManager;
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

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( this.resolver );
            this.resolver = null;
            this.manager = null;
        }
    }

    /**
     * @see org.apache.excalibur.source.SourceResolver#release(org.apache.excalibur.source.Source)
     */
    public void release(Source source) {
        this.resolver.release(source);
    }

    /**
     * @see SourceResolver#resolveURI(java.lang.String, java.lang.String, java.util.Map)
     */
    public Source resolveURI(final String location,
                             String baseURI,
                             final Map    parameters)
    throws IOException {
        return this.resolver.resolveURI(location,
                                        (baseURI == null ? this.context : baseURI),
                                        parameters);
    }

    /**
     * @see org.apache.excalibur.source.SourceResolver#resolveURI(java.lang.String)
     */
    public Source resolveURI(final String location) throws IOException {
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
        if (this.lastPrefix != null) {
            final String uri = env.getURI();
            if (!uri.startsWith(this.lastPrefix)) {
                throw new ProcessingException("The current URI (" + uri +
                                              ") doesn't start with given prefix (" + this.lastPrefix + ")");
            }
            // we don't need to check for slash at the beginning
            // of uri - the prefix always ends with a slash!
            final int l = this.lastPrefix.length();
            env.setURI(this.prefix, uri.substring(l));
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
        } else {
            this.lastPrefix = null;
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
     * This hook must be called by the sitemap each time a sitemap is entered.
     *
     * <p>This method should never raise an exception, except when the
     * parameters are not set!</p>
     *
     * @throws ProcessingException if processor is null
     */
    public static void enterProcessor(Processor   processor,
                                      Environment env)
    throws ProcessingException {
        if (null == processor) {
            throw new ProcessingException("Processor is not set.");
        }
        EnvironmentStack stack = (EnvironmentStack) environmentStack.get();
        if (stack == null) {
            stack = new EnvironmentStack();
            environmentStack.set(stack);
        }
        stack.pushInfo(new EnvironmentInfo(processor, stack.getOffset(), env));
        stack.setOffset(stack.size() - 1);
    }

    /**
     * This hook must be called by the sitemap each time a sitemap is left.
     *
     * <p>It's the counterpart to the {@link #enterProcessor(Processor, Environment)}
     * method.</p>
     */
    public static void leaveProcessor() {
        final EnvironmentStack stack = (EnvironmentStack) environmentStack.get();
        final EnvironmentInfo info = (EnvironmentInfo) stack.pop();
        stack.setOffset(info.oldStackCount);
    }

    /**
     * This method is used for entering a new environment.
     *
     * @throws ProcessingException if there is no current processing environment
     */
    public static void enterEnvironment(Environment env)
    throws ProcessingException {
        final EnvironmentStack stack = (EnvironmentStack) environmentStack.get();
        EnvironmentInfo info;
        if (stack != null && !stack.isEmpty()) {
            info = stack.getCurrentInfo();
        } else {
            throw new ProcessingException("There must be a current processing environment.");
        }

        stack.pushInfo(new EnvironmentInfo(info.processor, stack.getOffset(), env));
        stack.setOffset(stack.size() - 1);
    }

    /**
     * This method is used for leaving the current environment.
     * 
     * <p>It's the counterpart to the {@link #enterEnvironment(Environment)} method.</p>
     */
    public static Environment leaveEnvironment() {
        final EnvironmentStack stack = (EnvironmentStack) environmentStack.get();
        final EnvironmentInfo info = (EnvironmentInfo) stack.pop();
        stack.setOffset(info.oldStackCount);
        return info.environment;
    }

    /**
     * INTERNAL METHOD. Do not use, can be removed without warning or deprecation cycle.
     */
    public static int markEnvironment() {
        // TODO (CZ): This is only for testing - remove it later on. See also Cocoon.java.
        final EnvironmentStack stack = (EnvironmentStack) environmentStack.get();
        if (stack != null) {
            return stack.size();
        }

        return 0;
    }

    /**
     * INTERNAL METHOD. Do not use this, can be removed without warning or deprecation cycle.
     */
    public static void checkEnvironment(int depth)
    throws Exception {
        // TODO (CZ): This is only for testing - remove it later on. See also Cocoon.java.
        final EnvironmentStack stack = (EnvironmentStack) environmentStack.get();
        int currentDepth = stack != null ? stack.size() : 0;
        if (currentDepth != depth) {
            logger.error("ENVIRONMENT STACK HAS NOT BEEN CLEANED PROPERLY!");
            throw new ProcessingException("Environment stack has not been cleaned up properly. " +
                                          "Please report this (and if possible, together with a test case) " +
                                          "to the Cocoon developers.");
        }
    }

    /**
     * Return the environment.
     * INTERNAL METHOD. Do not use this, can be removed without warning or deprecation cycle.
     */
    public static Environment getCurrentEnvironment() {
        final EnvironmentStack stack = (EnvironmentStack) environmentStack.get();
        if (stack != null && !stack.empty()) {
            final EnvironmentInfo info = stack.getCurrentInfo();
            return info.environment;
        }
        return null;
    }

    /**
     * Return the current processor
     * INTERNAL METHOD. Do not use this, can be removed without warning or deprecation cycle.
     */
    public static Processor getCurrentProcessor() {
        final EnvironmentStack stack = (EnvironmentStack) environmentStack.get();
        if (stack != null && !stack.isEmpty()) {
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
     * INTERNAL METHOD. Do not use this, can be removed without warning or deprecation cycle.
     */
    static public ServiceManager getSitemapServiceManager() {
        final EnvironmentStack stack = (EnvironmentStack) environmentStack.get();
        if (stack != null && !stack.isEmpty()) {
            return (ServiceManager) WebAppContextUtils.getCurrentWebApplicationContext().getBean(AvalonUtils.SERVICE_MANAGER_ROLE);
        }
        return null;
    }

    /**
     * Create an environment aware xml consumer for the cocoon
     * protocol.
     * INTERNAL METHOD. Do not use this, can be removed without warning or deprecation cycle.
     */
    public static XMLConsumer createEnvironmentAwareConsumer(XMLConsumer consumer) {
        final EnvironmentStack stack = (EnvironmentStack)environmentStack.get();
        final EnvironmentInfo info = stack.getCurrentInfo();
        return stack.getEnvironmentAwareConsumerWrapper(consumer, info.oldStackCount);
    }

    /**
     * Create an environment aware xml consumer that push an
     * environment before calling the consumer.
     */
    public static XMLConsumer createPushEnvironmentConsumer(XMLConsumer consumer, Environment environment) {
        return new PushEnvironmentChanger(consumer, environment);
    }

    /**
     * Create an environment aware xml consumer that pop and save the
     * current environment before calling the consumer.
     */
    public static XMLConsumer createPopEnvironmentConsumer(XMLConsumer consumer) {
        return new PopEnvironmentChanger(consumer);
    }

    /**
     * A runnable wrapper that inherits the environment stack of the thread it is
     * created in.
     * <p>
     * It's defined as an abstract class here to use some internals of EnvironmentHelper, and
     * should only be used through its public counterpart, <code>org.apache.cocoon.environment.CocoonRunnable</code>.
     */
    public static abstract class AbstractCocoonRunnable implements Runnable {
        private Object parentStack = null;

        public AbstractCocoonRunnable() {
            // Clone the environment stack of the calling thread.
            // We'll use it in run() below
            Object stack = EnvironmentHelper.environmentStack.get();
            if (stack != null) {
                this.parentStack = ((EnvironmentStack)stack).clone();
            }
        }

        /**
         * Calls {@link #doRun()} within the environment context of the creating thread.
         */
        public final void run() {
            // Install the stack from the parent thread and run the Runnable
            Object oldStack = environmentStack.get();
            EnvironmentHelper.environmentStack.set(this.parentStack);
            try {
                doRun();
            } finally {
                // Restore the previous stack
                EnvironmentHelper.environmentStack.set(oldStack);
            }
            // FIXME: Check the lifetime of this run compared to the parent thread.
            // A CocoonThread is meant to start and die within the execution period of the parent request,
            // and it is an error if it lives longer as the parent environment is no more valid.
        }

        abstract protected void doRun();
    }
}
