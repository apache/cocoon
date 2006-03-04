/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.components.treeprocessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.ChainedConfiguration;
import org.apache.cocoon.components.source.impl.SitemapSourceInfo;
import org.apache.cocoon.core.container.spring.CocoonBeanFactory;
import org.apache.cocoon.core.container.spring.NameForAliasAware;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ForwardRedirector;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
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
import org.apache.cocoon.util.location.Location;
import org.apache.cocoon.util.location.LocationImpl;
import org.apache.commons.jci.listeners.NotificationListener;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * The concrete implementation of {@link Processor}, containing the evaluation tree and associated
 * data such as component manager.
 *
 * @version $Id$
 */
public class ConcreteTreeProcessor extends AbstractLogEnabled
                                   implements Processor, Disposable, ExecutionContext, NotificationListener {

    private static final String BEAN_FACTORY_STACK_REQUEST_ATTRIBUTE = CocoonBeanFactory.class.getName() + "/Stack";

    /** Our ServiceManager */
    private ServiceManager manager;

    /** The processor that wraps us */
    private TreeProcessor wrappingProcessor;

    /** Processing nodes that need to be disposed with this processor */
    private List disposableNodes;

    /** Root node of the processing tree */
    private ProcessingNode rootNode;

    private Configuration componentConfigurations;

    /** The sitemap configurations. */
    protected Map sitemapComponentConfigurations;

    /** Number of simultaneous uses of this processor (either by concurrent request or by internal requests) */
    private int requestCount;

    /** The sitemap executor */
    private SitemapExecutor sitemapExecutor;

    /** Optional event listeners for the enter sitemap event */
    private List enterSitemapEventListeners = new ArrayList();

    /** Optional event listeners for the leave sitemap event */
    private List leaveSitemapEventListeners = new ArrayList();

    /** Needs a reload? */
    protected volatile boolean needsReload = false;
    
    /** Processor attributes */
    protected Map processorAttributes = new HashMap();

    /** Bean Factory for this sitemap. */
    protected ConfigurableBeanFactory beanFactory;

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

    public void handleNotification() {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(this + " got notified that a reload is required");
        }
        needsReload = true;
    }
    
    /** Set the processor data, result of the treebuilder job */
    public void setProcessorData(ConfigurableBeanFactory beanFactory,
                                 ServiceManager manager,
                                 ProcessingNode rootNode,
                                 List disposableNodes,
                                 List enterSitemapEventListeners,
                                 List leaveSitemapEventListeners) {
        if (this.rootNode != null) {
            throw new IllegalStateException("setProcessorData() can only be called once");
        }

        this.beanFactory = beanFactory;
        this.manager = manager;
        this.rootNode = rootNode;
        this.disposableNodes = disposableNodes;
        this.enterSitemapEventListeners = enterSitemapEventListeners;
        this.leaveSitemapEventListeners = leaveSitemapEventListeners;
    }

    /** Set the sitemap component configurations (called as part of the tree building process) */
    public void setComponentConfigurations(Configuration componentConfigurations) {
        this.componentConfigurations = componentConfigurations;
        this.sitemapComponentConfigurations = null;
    }

    /**
     * @see org.apache.cocoon.Processor#getComponentConfigurations()
     */
    public Map getComponentConfigurations() {
        // do we have the sitemap configurations prepared for this processor?
        if ( null == this.sitemapComponentConfigurations ) {

            synchronized (this) {

                if ( this.sitemapComponentConfigurations == null ) {
                    // do we have configurations?
                    final Configuration[] childs = (this.componentConfigurations == null
                                                     ? null
                                                     : this.componentConfigurations.getChildren());

                    if ( null != childs ) {

                        if ( null == this.wrappingProcessor.parent ) {
                            this.sitemapComponentConfigurations = new HashMap(12);
                        } else {
                            // copy all configurations from parent
                            this.sitemapComponentConfigurations = new HashMap(
                                        this.wrappingProcessor.parent.getComponentConfigurations());
                        }

                        // and now check for new configurations
                        for(int m = 0; m < childs.length; m++) {
                            String r = childs[m].getName();
                            // FIXME: No alias handling in non bean context environment
                            if (this.beanFactory != null && this.beanFactory instanceof NameForAliasAware)
                                r = ((NameForAliasAware)this.beanFactory).getNameForAlias(r);
                            this.sitemapComponentConfigurations.put(r, new ChainedConfiguration(childs[m],
                                                                             (ChainedConfiguration)this.sitemapComponentConfigurations.get(r)));
                        }
                    } else {
                        // we don't have configurations
                        if ( null == this.wrappingProcessor.parent ) {
                            this.sitemapComponentConfigurations = Collections.EMPTY_MAP;
                        } else {
                            // use configuration from parent
                            this.sitemapComponentConfigurations = this.wrappingProcessor.parent.getComponentConfigurations();
                        }
                    }
                }
            }
        }
        return this.sitemapComponentConfigurations;
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

    boolean isReloadNeeded() {
        return needsReload;
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
        context.enableLogging(getLogger());
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
        context.enableLogging(getLogger());
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
     * @see org.apache.cocoon.sitemap.EnterSitemapEventListener#enteredSitemap(org.apache.cocoon.sitemap.EnterSitemapEvent)
     */
    protected void enteredSitemap(EnterSitemapEvent event) {
        final Request request = ObjectModelHelper.getRequest(event.getEnvironment().getObjectModel());
        final Object oldContext = request.getAttribute(CocoonBeanFactory.BEAN_FACTORY_REQUEST_ATTRIBUTE, Request.REQUEST_SCOPE);
        if ( oldContext != null ) {
            Stack stack = (Stack)request.getAttribute(BEAN_FACTORY_STACK_REQUEST_ATTRIBUTE, Request.REQUEST_SCOPE);
            if ( stack == null ) {
                stack = new Stack();
                request.setAttribute(BEAN_FACTORY_STACK_REQUEST_ATTRIBUTE, stack, Request.REQUEST_SCOPE);
            }
            stack.push(oldContext);
        }
        request.setAttribute(CocoonBeanFactory.BEAN_FACTORY_REQUEST_ATTRIBUTE, this.beanFactory, Request.REQUEST_SCOPE);
    }

    /**
     * @see org.apache.cocoon.sitemap.LeaveSitemapEventListener#leftSitemap(org.apache.cocoon.sitemap.LeaveSitemapEvent)
     */
    protected void leftSitemap(LeaveSitemapEvent event) {
        final Request request = ObjectModelHelper.getRequest(event.getEnvironment().getObjectModel());
        final Stack stack = (Stack)request.getAttribute(BEAN_FACTORY_STACK_REQUEST_ATTRIBUTE, Request.REQUEST_SCOPE);
        if ( stack == null ) {
            request.removeAttribute(CocoonBeanFactory.BEAN_FACTORY_REQUEST_ATTRIBUTE, Request.REQUEST_SCOPE);
        } else {
            final Object oldContext = stack.pop();
            request.setAttribute(CocoonBeanFactory.BEAN_FACTORY_REQUEST_ATTRIBUTE, oldContext, Request.REQUEST_SCOPE);
            if ( stack.size() == 0 ) {
                request.removeAttribute(BEAN_FACTORY_STACK_REQUEST_ATTRIBUTE, Request.REQUEST_SCOPE);
            }
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

        try {
            // invoke listeners
            // only invoke if pipeline is not internally
            if ( !context.isBuildingPipelineOnly() ) {
                final EnterSitemapEvent enterEvent = new EnterSitemapEvent(this, environment);
                this.enteredSitemap(enterEvent);
                final Iterator enterSEI = this.enterSitemapEventListeners.iterator();
                while ( enterSEI.hasNext() ) {
                    final TreeBuilder.EventComponent current = (TreeBuilder.EventComponent)enterSEI.next();
                    ((EnterSitemapEventListener)current.component).enteredSitemap(enterEvent);
                }
            }

            this.sitemapExecutor.enterSitemap(this, environment.getObjectModel(), this.wrappingProcessor.source.getURI());
            // and now process
            EnvironmentHelper.enterProcessor(this, this.manager, environment);
            final Redirector oldRedirector = context.getRedirector();

            // Build a redirector
            TreeProcessorRedirector redirector = new TreeProcessorRedirector(environment, context);
            setupLogger(redirector);
            context.setRedirector(redirector);
            context.service(this.manager);
            context.setLastProcessor(this);

            try {
                final boolean success = this.rootNode.invoke(environment, context);
                return success;
            } finally {
                EnvironmentHelper.leaveProcessor();
                // Restore old redirector
                context.setRedirector(oldRedirector);
            }

        } finally {
            this.sitemapExecutor.leaveSitemap(this, environment.getObjectModel());
            // invoke listeners
            // only invoke if pipeline is not internally
            if ( !context.isBuildingPipelineOnly() ) {
                final LeaveSitemapEvent leaveEvent = new LeaveSitemapEvent(this, environment);
                this.leftSitemap(leaveEvent);
                final Iterator leaveSEI = this.leaveSitemapEventListeners.iterator();
                while ( leaveSEI.hasNext() ) {
                    final TreeBuilder.EventComponent current = (TreeBuilder.EventComponent)leaveSEI.next();
                    ((LeaveSitemapEventListener)current.component).leftSitemap(leaveEvent);
                }
            }

            // Decrement the concurrent request count
            synchronized (this) {
                requestCount--;
            }

            if (requestCount < 0) {
                // Marked for disposal and no more concurrent requests.
                dispose();
            }
        }
    }

    protected boolean handleCocoonRedirect(String uri, Environment environment, InvokeContext context)
    throws Exception {
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
        Environment newEnv = new ForwardEnvironmentWrapper(environment, info, getLogger());
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

        // dispose listeners
        this.disposeListeners(this.enterSitemapEventListeners);
        this.disposeListeners(this.leaveSitemapEventListeners);
        if ( this.beanFactory != null ) {
            this.beanFactory.destroySingletons();
            this.beanFactory = null;
        }
    }

    protected void disposeListeners(List l) {
        Iterator i = l.iterator();
        while ( i.hasNext() ) {
            final TreeBuilder.EventComponent current = (TreeBuilder.EventComponent)i.next();
            if ( current.releaseUsingManager ) {
                this.manager.release(current.component);
            } else {
                ContainerUtil.dispose(current.component);
            }
        }
        l.clear();        
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

}
