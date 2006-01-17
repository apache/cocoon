/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.spring;

import java.util.Stack;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.servlet.CocoonServlet;
import org.apache.cocoon.sitemap.ComponentLocator;
import org.apache.cocoon.sitemap.EnterSitemapEvent;
import org.apache.cocoon.sitemap.EnterSitemapEventListener;
import org.apache.cocoon.sitemap.LeaveSitemapEvent;
import org.apache.cocoon.sitemap.LeaveSitemapEventListener;
import org.apache.excalibur.source.SourceResolver;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextException;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.WebApplicationContext;

/**
 * This is the connection between Cocoon and Spring.
 * We create an own web application context.
 *
 * @version $Id$
 */
public class SpringComponentLocator
    extends AbstractLogEnabled
    implements ComponentLocator,
               Contextualizable,
               Serviceable,
               Configurable,
               Initializable,
               Disposable,
               EnterSitemapEventListener,
               LeaveSitemapEventListener {

    public static final String APPLICATION_CONTEXT_REQUEST_ATTRIBUTE = "application-context";

    protected ServletContext servletContext;
    protected EnvironmentHelper environmentHelper;
    protected ServiceManager manager;
    protected SourceResolver resolver;
    protected Core cocoon;
    protected Context context;

    protected CocoonApplicationContext wac;

    protected String contextClassName;
    protected String configLocation;
    protected String locatorFactorySelector;
    protected String parentContextKey;

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.servletContext = ((ServletConfig) context.get(CocoonServlet.CONTEXT_SERVLET_CONFIG)).getServletContext();
        // FIXME - we shouldn't use the environment helper
        this.environmentHelper = (EnvironmentHelper)context.get(Constants.CONTEXT_ENV_HELPER);
        this.context = context;
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager aManager) throws ServiceException {
        this.manager = aManager;
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
        this.cocoon = (Core)this.manager.lookup(Core.ROLE);
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        this.contextClassName = config.getChild("contextClass").getValue(null);
        this.configLocation = config.getChild("contextConfigLocation").getValue(null);
        this.locatorFactorySelector = config.getChild("locatorFactorySelector").getValue(null);
        this.parentContextKey = config.getChild("parentContextKey").getValue(null);
    }

    /**
     * Initialize Spring's web application context.
     * @throws BeansException if the context couldn't be initialized
     *
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        if ( this.getLogger().isInfoEnabled() ) {
            this.getLogger().info("Loading Spring web application context");
        }

        try {
            // Determine parent for application context, if any.
            ApplicationContext parent = this.getParentContext();

            // Make the core available as a bean, if not already done by a parent context
            if ( parent == null || !parent.containsBean("cocoon-core") ) {
                parent = this.createCocoonAppContext(parent);
            }

            this.wac = this.createWebApplicationContext(parent);

            if (this.getLogger().isInfoEnabled()) {
                this.getLogger().info("Using context class [" + wac.getClass().getName() +
                        "] for root WebApplicationContext");
                this.getLogger().info("Web application context: initialization completed");
            }
        } catch (RuntimeException ex) {
            this.getLogger().error("Context initialization failed", ex);
            throw ex;
        } catch (Error err) {
            this.getLogger().error("Context initialization failed", err);
            throw err;
        }
    }

    /**
     * Close Spring's web application context.
     *
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.getLogger().isInfoEnabled()) {
            this.getLogger().info("Closing Spring web application context");
        }
        this.wac.close();
        if ( this.manager != null ) {
            this.manager.release(this.resolver);
            this.resolver = null;
            this.manager.release(this.cocoon);
            this.cocoon = null;
            this.manager = null;
        }
    }

    /**
     * @see org.apache.cocoon.sitemap.ComponentLocator#hasComponent(java.lang.String)
     */
    public boolean hasComponent(String key) {
        return this.wac.containsBean(key);
    }

    /**
     * @see org.apache.cocoon.sitemap.ComponentLocator#getComponent(java.lang.String)
     */
    public Object getComponent(String key) 
    throws ProcessingException {
        return this.wac.getBean(key);
    }
    
    /**
     * @see org.apache.cocoon.sitemap.ComponentLocator#release(java.lang.Object)
     */
    public void release(Object component) {
        // nothing to do
    }

    /**
     * Instantiate the web application context for this loader, either a
     * default CocoonApplicationContext or a custom context class if specified.
     * <p>This implementation expects custom contexts to be a subclass of
     * CocoonApplicationContext.
     * @param parent the parent ApplicationContext to use, or null if none
     * @throws BeansException if the context couldn't be initialized
     */
    protected CocoonApplicationContext createWebApplicationContext(ApplicationContext parent)
    throws BeansException {
        Class contextClass = CocoonApplicationContext.class;
        if (this.contextClassName != null) {
            try {
                contextClass = Class.forName(this.contextClassName, true, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException ex) {
                throw new ApplicationContextException("Failed to load context class [" + contextClassName + "]", ex);
            }
            if (!CocoonApplicationContext.class.isAssignableFrom(contextClass)) {
                throw new ApplicationContextException("Custom context class [" + contextClassName +
                        "] is not of type CocoonApplicationContext");
            }
        }

        CocoonApplicationContext cwac =
                (CocoonApplicationContext) BeanUtils.instantiateClass(contextClass);      

        cwac.addBeanFactoryPostProcessor(new CocoonSettingsConfigurer(this.cocoon));
        
        cwac.setSourceResolver(this.resolver);
        cwac.setEnvironmentHelper(this.environmentHelper);
        cwac.setParent(parent);
        cwac.setServletContext(this.servletContext);
        if (this.configLocation != null) {
            cwac.setConfigLocations(StringUtils.tokenizeToStringArray(configLocation,
                    ConfigurableWebApplicationContext.CONFIG_LOCATION_DELIMITERS));
        }

        cwac.refresh();     
        
        return cwac;
    }

    /**
     * Create an application context that contains Cocoon specific beans.
     * @return A new application context
     */
    protected ApplicationContext createCocoonAppContext(ApplicationContext parent) {
        GenericApplicationContext gac = new GenericApplicationContext();
        gac.setParent(parent);
        gac.refresh();
        this.registerDefaults(gac.getBeanFactory());
        
        return gac;
    }

    /**
     * Register cocoon components that will be available for spring components.
     * @param factory The factory to register with.
     */
    protected void registerDefaults(ConfigurableListableBeanFactory factory) {
        factory.registerSingleton("cocoon-core", this.cocoon);
        factory.registerSingleton("cocoon-service-manager", this.manager);
        factory.registerSingleton("cocoon-context", this.context);
    }

    /**
     * Get the parent application context: this is either the context of a parent
     * sitemap or an optional web application context set by the spring servlet (filter).
     * @return A parent application context or null
     */
    protected ApplicationContext getParentContext() {
        final Request request = ObjectModelHelper.getRequest(this.cocoon.getCurrentObjectModel());
        ApplicationContext parentContext = (ApplicationContext)request.getAttribute(APPLICATION_CONTEXT_REQUEST_ATTRIBUTE, Request.REQUEST_SCOPE);
        
        if ( parentContext == null ) {
            // there is no parent sitemap with an application context
            // let's search for a global one
            parentContext = (ApplicationContext)this.servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        }

        return parentContext;
    }

    /**
     * @see org.apache.cocoon.sitemap.EnterSitemapEventListener#enteredSitemap(org.apache.cocoon.sitemap.EnterSitemapEvent)
     */
    public void enteredSitemap(EnterSitemapEvent event) {
        final Request request = ObjectModelHelper.getRequest(event.getEnvironment().getObjectModel());
        final Object oldContext = request.getAttribute(APPLICATION_CONTEXT_REQUEST_ATTRIBUTE, Request.REQUEST_SCOPE);
        if ( oldContext != null ) {
            Stack stack = (Stack)request.getAttribute("ac-stack", Request.REQUEST_SCOPE);
            if ( stack == null ) {
                stack = new Stack();
                request.setAttribute("ac-stack", stack, Request.REQUEST_SCOPE);
            }
            stack.push(oldContext);
        }
        request.setAttribute(APPLICATION_CONTEXT_REQUEST_ATTRIBUTE, this.wac, Request.REQUEST_SCOPE);
    }

    /**
     * @see org.apache.cocoon.sitemap.LeaveSitemapEventListener#leftSitemap(org.apache.cocoon.sitemap.LeaveSitemapEvent)
     */
    public void leftSitemap(LeaveSitemapEvent event) {
        final Request request = ObjectModelHelper.getRequest(event.getEnvironment().getObjectModel());
        final Stack stack = (Stack)request.getAttribute("ac-stack", Request.REQUEST_SCOPE);
        if ( stack == null ) {
            request.removeAttribute(APPLICATION_CONTEXT_REQUEST_ATTRIBUTE, Request.REQUEST_SCOPE);
        } else {
            final Object oldContext = stack.pop();
            request.setAttribute(APPLICATION_CONTEXT_REQUEST_ATTRIBUTE, oldContext, Request.REQUEST_SCOPE);
            if ( stack.size() == 0 ) {
                request.removeAttribute("ac-stack", Request.REQUEST_SCOPE);
            }
        }
    }
}
