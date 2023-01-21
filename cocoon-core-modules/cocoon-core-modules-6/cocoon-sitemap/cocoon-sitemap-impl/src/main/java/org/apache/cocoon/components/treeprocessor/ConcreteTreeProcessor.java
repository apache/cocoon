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
package org.apache.cocoon.components.treeprocessor;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.source.impl.SitemapSourceInfo;
import org.apache.cocoon.core.container.spring.avalon.AvalonUtils;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ForwardRedirector;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.environment.internal.ForwardEnvironmentWrapper;
import org.apache.cocoon.environment.wrapper.MutableEnvironmentFacade;
import org.apache.cocoon.sitemap.EnterSitemapEvent;
import org.apache.cocoon.sitemap.EnterSitemapEventListener;
import org.apache.cocoon.sitemap.ExecutionContext;
import org.apache.cocoon.sitemap.LeaveSitemapEvent;
import org.apache.cocoon.sitemap.LeaveSitemapEventListener;
import org.apache.cocoon.sitemap.SitemapExecutor;
import org.apache.cocoon.spring.configurator.WebAppContextUtils;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.util.location.Location;
import org.apache.cocoon.util.location.LocationImpl;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * The concrete implementation of {@link Processor}, containing the evaluation tree and associated
 * data such as component manager.
 *
 * @version $Id$
 */
public class ConcreteTreeProcessor extends AbstractLogEnabled
                                   implements Processor,
                                              Disposable,
                                              ExecutionContext {

    /** Our ServiceManager */
    private ServiceManager manager;

    /** The processor that wraps us */
    private TreeProcessor wrappingProcessor;

    /** Processing nodes that need to be disposed with this processor */
    private List disposableNodes;

    /** Root node of the processing tree */
    private ProcessingNode rootNode;

    /**
     * Number of simultaneous uses of this processor (either by concurrent
     * request or by internal requests)
     */
    private int requestCount;

    /** The sitemap executor */
    private SitemapExecutor sitemapExecutor;

    /** Optional event listeners for the enter sitemap event */
    private List enterSitemapEventListeners = Collections.EMPTY_LIST;

    /** Optional event listeners for the leave sitemap event */
    private List leaveSitemapEventListeners = Collections.EMPTY_LIST;

    /** Processor attributes */
    protected Map processorAttributes = new HashMap();

    /** Container for this sitemap. */
    protected WebApplicationContext webAppContext;

    /** Classloader for this sitemap. */
    protected ClassLoader classLoader;

    /**
     * Builds a concrete processig, given the wrapping processor
     */
    public ConcreteTreeProcessor(TreeProcessor wrappingProcessor,
                                 SitemapExecutor sitemapExecutor) {
        // Store our wrapping processor
        this.wrappingProcessor = wrappingProcessor;

        // Get the sitemap executor - we use the same executor for each sitemap
        this.sitemapExecutor = sitemapExecutor;
    }

    /** Set the processor data, result of the treebuilder job */
    public void setProcessorData(WebApplicationContext webAppContext,
                                 ProcessingNode rootNode,
                                 List disposableNodes,
                                 List enterSitemapEventListeners,
                                 List leaveSitemapEventListeners) {
        if (this.rootNode != null) {
            throw new IllegalStateException("setProcessorData() can only be called once");
        }
        this.classLoader = webAppContext.getClassLoader();
        this.webAppContext = webAppContext;
        this.manager = (ServiceManager)this.webAppContext.getBean(AvalonUtils.SERVICE_MANAGER_ROLE);
        this.rootNode = rootNode;
        this.disposableNodes = disposableNodes;
        this.enterSitemapEventListeners = enterSitemapEventListeners;
        this.leaveSitemapEventListeners = leaveSitemapEventListeners;
    }

    /**
     * Mark this processor as needing to be disposed. Actual call to {@link #dispose()} will occur when
     * all request processings on this processor will be terminated.
     */
    public void markForDisposal() {
        // Decrement the request count (negative number means dispose)
        synchronized(this) {
            this.requestCount--;
        }

        if (this.requestCount < 0) {
            // No more users : dispose right now
            dispose();
        }
    }

    public TreeProcessor getWrappingProcessor() {
        return this.wrappingProcessor;
    }

    /**
     * @see org.apache.cocoon.Processor#getRootProcessor()
     */
    public Processor getRootProcessor() {
        return this.wrappingProcessor.getRootProcessor();
    }

    /**
     * Process the given <code>Environment</code> producing the output.
     * @return If the processing is successfull <code>true</code> is returned.
     *         If not match is found in the sitemap <code>false</code>
     *         is returned.
     * @throws org.apache.cocoon.ResourceNotFoundException If a sitemap component tries
     *                                   to access a resource which can not
     *                                   be found, e.g. the generator
     *         ConnectionResetException  If the connection was reset
     */
    public boolean process(Environment environment) throws Exception {
        InvokeContext context = new InvokeContext();
        try {
            return process(environment, context);
        } finally {
            context.dispose();
        }
    }

    /**
     * Process the given <code>Environment</code> to assemble
     * a <code>ProcessingPipeline</code>.
     * @since 2.1
     */
    public InternalPipelineDescription buildPipeline(Environment environment)
    throws Exception {
        InvokeContext context = new InvokeContext(true);
        try {
            if (process(environment, context)) {
                return context.getInternalPipelineDescription(environment);
            }

            return null;
        } finally {
            context.dispose();
        }
    }

    /**
     * Do the actual processing, be it producing the response or just building the pipeline
     * @param environment
     * @param context
     * @return true if the pipeline was successfully built, false otherwise.
     * @throws Exception
     */
    protected boolean process(Environment environment, InvokeContext context)
    throws Exception {

        // Increment the concurrent requests count
        synchronized (this) {
            requestCount++;
        }

        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.classLoader);
        Object handle = null;
        try {
            handle = WebAppContextUtils.enteringContext(this.webAppContext);
            // invoke listeners
            // only invoke if pipeline is not internally
            if (!context.isBuildingPipelineOnly()) {
                final EnterSitemapEvent enterEvent = new EnterSitemapEvent(this, environment);
                final Iterator i = this.enterSitemapEventListeners.iterator();
                while (i.hasNext()) {
                    final EnterSitemapEventListener current = (EnterSitemapEventListener) i.next();
                    current.enteredSitemap(enterEvent);
                }
            }

            this.sitemapExecutor.enterSitemap(this, environment.getObjectModel(), this.wrappingProcessor.source.getURI());

            // and now process
            EnvironmentHelper.enterProcessor(this, environment);
            final Redirector oldRedirector = context.getRedirector();
            try {
                // Build a redirector
                TreeProcessorRedirector redirector = new TreeProcessorRedirector(environment, context);
                context.setRedirector(redirector);
                context.service(this.manager);
                context.setLastProcessor(this);

                return this.rootNode.invoke(environment, context);
            } finally {
                EnvironmentHelper.leaveProcessor();
                // Restore old redirector
                context.setRedirector(oldRedirector);
            }

        } finally {
            if (handle != null) {
                this.sitemapExecutor.leaveSitemap(this, environment.getObjectModel());
                // invoke listeners
                // only invoke if pipeline is not internally
                if (!context.isBuildingPipelineOnly()) {
                    final LeaveSitemapEvent leaveEvent = new LeaveSitemapEvent(this, environment);
                    final Iterator i = this.leaveSitemapEventListeners.iterator();
                    while (i.hasNext()) {
                        final LeaveSitemapEventListener current = (LeaveSitemapEventListener) i.next();
                        current.leftSitemap(leaveEvent);
                    }
                }
                WebAppContextUtils.leavingContext(this.webAppContext, handle);
            }

            // Decrement the concurrent request count
            synchronized (this) {
                requestCount--;
            }

            if (requestCount < 0) {
                // Marked for disposal and no more concurrent requests.
                dispose();
            }
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    protected boolean handleCocoonRedirect(String uri, Environment env, InvokeContext context)
    throws Exception {
        Environment environment = env;
        // Build an environment wrapper
        // If the current env is a facade, change the delegate and continue processing the facade, since
        // we may have other redirects that will in turn also change the facade delegate

        MutableEnvironmentFacade facade = environment instanceof MutableEnvironmentFacade ?
            ((MutableEnvironmentFacade)environment) : null;

        if (facade != null) {
            // Consider the facade delegate (the real environment)
            environment = facade.getDelegate();
        }

        // test if this is a call from flow
        boolean isRedirect = (environment.getObjectModel().remove("cocoon:forward") == null);
        final SitemapSourceInfo info = SitemapSourceInfo.parseURI(environment, uri);
        Environment newEnv = new ForwardEnvironmentWrapper(environment, info);
        if (isRedirect) {
            ((ForwardEnvironmentWrapper) newEnv).setInternalRedirect(true);
        }

        if (facade != null) {
            // Change the facade delegate
            facade.setDelegate((ForwardEnvironmentWrapper)newEnv);
            newEnv = facade;
        }

        // Get the processor that should process this request
        ConcreteTreeProcessor processor;
        if (newEnv.getURIPrefix().equals("")) {
            processor = ((TreeProcessor)getRootProcessor()).concreteProcessor;
        } else {
            processor = this;
        }

        // Process the redirect
        // No more reset since with TreeProcessorRedirector, we need to pop values from the redirect location
        // context.reset();
        // The following is a fix for bug #26854 and #26571
        final boolean result = processor.process(newEnv, context);
        if (facade != null) {
            newEnv = facade.getDelegate();
        }
        if (((ForwardEnvironmentWrapper) newEnv).getRedirectURL() != null) {
            environment.redirect(((ForwardEnvironmentWrapper) newEnv).getRedirectURL(), false, false);
        }
        return result;
    }
    
    protected boolean handleServletRedirect(String uri, Environment environment) throws IOException, ProcessingException {
        SourceFactory sourceFactory;
        try {
            sourceFactory = (SourceFactory) this.manager.lookup("org.apache.excalibur.source.SourceFactory/servlet");
        } catch (ServiceException e) {
            throw new ProcessingException("Could not find ServletSourceFactory. Are you trying to use servlet: source " +
                    "without using Servlet Service Framework (components)?", e);
        }

        Source source = sourceFactory.getSource(uri, null);
        org.apache.commons.io.IOUtils.copy(source.getInputStream(), environment.getOutputStream(0));

        return true;
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.disposableNodes != null) {
            // we must dispose the nodes in reverse order
            // otherwise selector nodes are freed before the components node
            for (int i = this.disposableNodes.size() - 1; i > -1; i--) {
                ((Disposable) disposableNodes.get(i)).dispose();
            }
            this.disposableNodes = null;
        }

        // Ensure it won't be used anymore
        this.rootNode = null;
        this.sitemapExecutor = null;

        // clear listeners
        this.enterSitemapEventListeners.clear();
        this.leaveSitemapEventListeners.clear();
        if ( this.webAppContext != null ) {
            if ( webAppContext instanceof ConfigurableApplicationContext ) {
                ((ConfigurableApplicationContext)webAppContext).close();
            } else if ( webAppContext instanceof ConfigurableBeanFactory ) {
                ((ConfigurableBeanFactory)webAppContext).destroySingletons();
            }
            this.webAppContext = null;
        }
    }

    private class TreeProcessorRedirector extends ForwardRedirector {
        private InvokeContext context;

        public TreeProcessorRedirector(Environment env, InvokeContext context) {
            super(env);
            this.context = context;
        }

        protected void cocoonRedirect(String uri) throws IOException, ProcessingException {
            try {
                ConcreteTreeProcessor.this.handleCocoonRedirect(uri, this.env, this.context);
            } catch (IOException e) {
                throw e;
            } catch (ProcessingException e) {
                throw e;
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new ProcessingException(e);
            }
        }

        protected void servletRedirect(String uri) throws IOException, ProcessingException {
            handleServletRedirect(uri, this.env);
        }

    }

    public SourceResolver getSourceResolver() {
        return wrappingProcessor.getSourceResolver();
    }

    public String getContext() {
        return wrappingProcessor.getContext();
    }
    /**
     * Return the sitemap executor
     */
    public SitemapExecutor getSitemapExecutor() {
        return this.sitemapExecutor;
    }

    public ServiceManager getServiceManager() {
        return this.manager;
    }

    /**
     * @see org.apache.cocoon.Processor#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return this.processorAttributes.get(name);
    }

    /**
     * @see org.apache.cocoon.Processor#removeAttribute(java.lang.String)
     */
    public Object removeAttribute(String name) {
        return this.processorAttributes.remove(name);
    }

    /**
     * @see org.apache.cocoon.Processor#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        this.processorAttributes.put(name, value);
    }

    /**
     * @see org.apache.cocoon.sitemap.ExecutionContext#getLocation()
     */
    public Location getLocation() {
        return new LocationImpl("[sitemap]", this.wrappingProcessor.source.getURI());
    }

    /**
     * @see org.apache.cocoon.sitemap.ExecutionContext#getType()
     */
    public String getType() {
        return "sitemap";
    }

    /**
     * @see org.apache.cocoon.Processor#getParent()
     */
    public Processor getParent() {
        return this.wrappingProcessor.getParent();
    }
}
