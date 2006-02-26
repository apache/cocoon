/*
 * Copyright 2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring;

import java.io.IOException;
import java.util.Map;
import java.util.Stack;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.SitemapConfigurable;
import org.apache.cocoon.core.container.util.DefaultSitemapConfigurationHolder;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.sitemap.EnterSitemapEvent;
import org.apache.cocoon.sitemap.EnterSitemapEventListener;
import org.apache.cocoon.sitemap.LeaveSitemapEvent;
import org.apache.cocoon.sitemap.LeaveSitemapEventListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.core.io.Resource;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * This is a Cocoon specific implementation of a Spring {@link ApplicationContext}.
 *
 * @since 2.2
 * @version $Id$
 */
public class CocoonXmlWebApplicationContext
    extends XmlWebApplicationContext
    implements EnterSitemapEventListener, LeaveSitemapEventListener, ApplicationListener, NameForAliasAware {

    public static final String APPLICATION_CONTEXT_REQUEST_ATTRIBUTE = "application-context";

    final private Resource avalonResource;

    protected final Logger avalonLogger;
    protected final Context avalonContext;
    protected final ConfigurationInfo avalonConfiguration;

    protected boolean destroyed = false;

    public CocoonXmlWebApplicationContext(ApplicationContext parent) {
        this(null, parent, null, null, null);
    }

    public CocoonXmlWebApplicationContext(Resource           avalonResource,
                                          ApplicationContext parent,
                                          Logger             avalonLogger,
                                          ConfigurationInfo  avalonConfiguration,
                                          Context            avalonContext) {
        this.setParent(parent);
        this.avalonResource = avalonResource;
        this.avalonLogger = avalonLogger;
        this.avalonConfiguration = avalonConfiguration;
        this.avalonContext = avalonContext;
        if ( parent instanceof CocoonXmlWebApplicationContext) {
            ((CocoonXmlWebApplicationContext)parent).registerChildContext(this);
            
        }
    }

    /**
     * Get the avalon configuration information.
     */
    public ConfigurationInfo getConfigurationInfo() {
        return this.avalonConfiguration;
    }

    public String getNameForAlias(String alias) {
        if ( this.avalonConfiguration != null ) {
            final String value = this.avalonConfiguration.getRoleForName(alias);
            if ( value != null ) {
                return value;
            }
            if ( this.getParent() instanceof CocoonXmlWebApplicationContext ) {
                return ((CocoonXmlWebApplicationContext)this.getParent()).getNameForAlias(alias);
            }
        }
        // default: we just return the alias
        return alias;
    }
    /**
     * Register a child context as a listener. This allows a child context to destroy itself
     * when the parent is destroyed.
     * @param childContext The child context.
     */
    public void registerChildContext(CocoonXmlWebApplicationContext childContext) {
        this.addListener(childContext);
    }

    /**
     * @see org.springframework.context.support.AbstractApplicationContext#destroy()
     */
    public void destroy() {
        if ( !this.destroyed ) {
            this.destroyed = true;
            super.destroy();
        }
    }

    /**
     * @see org.springframework.web.context.support.XmlWebApplicationContext#loadBeanDefinitions(org.springframework.beans.factory.xml.XmlBeanDefinitionReader)
     */
    protected void loadBeanDefinitions(XmlBeanDefinitionReader reader)
    throws BeansException, IOException {
        super.loadBeanDefinitions(reader);
        if ( this.avalonResource != null ) {
            reader.loadBeanDefinitions(this.avalonResource);
        }
    }

    /**
     * Create a new bean factory and add a bean post processor to handle
     * Avalon components.
     * @see org.springframework.context.support.AbstractRefreshableApplicationContext#createBeanFactory()
     */
    protected DefaultListableBeanFactory createBeanFactory() {
        DefaultListableBeanFactory beanFactory = super.createBeanFactory();
        if ( this.avalonConfiguration != null ) {
            AvalonPostProcessor processor = new AvalonPostProcessor(this.avalonConfiguration.getComponents(),
                                                                    this.avalonContext,
                                                                    this.avalonLogger,
                                                                    beanFactory);
            beanFactory.addBeanPostProcessor(processor);
        }
        return beanFactory;
    }

    /**
     * We don't have any default locations - this application context is a nested one
     * which is configured through a sitemap. All possible configuration files are
     * configured in the sitemap.
     * @return An empty array.
     */
    protected String[] getDefaultConfigLocations() {
        return new String[]{};
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
        request.setAttribute(APPLICATION_CONTEXT_REQUEST_ATTRIBUTE, this, Request.REQUEST_SCOPE);
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

    public CocoonXmlWebApplicationContext getCurrentApplicationContext() {
        final Request request = ContextHelper.getRequest(this.avalonContext);
        if ( this.avalonContext == null ) {
            return this;
        }
        if ( request.getAttribute(APPLICATION_CONTEXT_REQUEST_ATTRIBUTE) != null ) {
            return (CocoonXmlWebApplicationContext)request.getAttribute(APPLICATION_CONTEXT_REQUEST_ATTRIBUTE);
        }
        return this;
    }

    /**
     * @see org.springframework.context.ApplicationListener#onApplicationEvent(org.springframework.context.ApplicationEvent)
     */
    public void onApplicationEvent(ApplicationEvent event) {
        if ( event instanceof ContextClosedEvent ) {
            if (((ContextClosedEvent)event).getApplicationContext().equals(this.getParent()) ) {
                this.destroy();
            }
        }
    }

    /**
     * This is a Spring BeanPostProcessor adding support for the Avalon lifecycle interfaces.
     */
    protected static final class AvalonPostProcessor implements DestructionAwareBeanPostProcessor {

        protected static final Configuration EMPTY_CONFIG = new DefaultConfiguration("empty");

        protected final Logger logger;
        protected final Context context;
        protected final BeanFactory beanFactory;
        protected final Map components;

        public AvalonPostProcessor(Map         components,
                                   Context     context,
                                   Logger      logger,
                                   BeanFactory factory) {
            this.components = components;
            this.context = context;
            this.logger = logger;
            this.beanFactory = factory;
        }

        /**
         * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object, java.lang.String)
         */
        public Object postProcessAfterInitialization(Object bean, String beanName)
        throws BeansException {
            try {
                ContainerUtil.start(bean);
            } catch (Exception e) {
                throw new BeanInitializationException("Unable to start bean " + beanName, e);
            }
            return bean;
        }

        /**
         * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
         */
        public Object postProcessBeforeInitialization(Object bean, String beanName)
        throws BeansException {
            final ComponentInfo info = (ComponentInfo)this.components.get(beanName);
            try {
                if ( info == null ) {
                    // no info so we just return the bean and don't apply any lifecycle interfaces
                    return bean;
                }
                if ( info.getLoggerCategory() != null ) {
                    ContainerUtil.enableLogging(bean, this.logger.getChildLogger(info.getLoggerCategory()));
                } else {
                    ContainerUtil.enableLogging(bean, this.logger);
                }
                ContainerUtil.contextualize(bean, this.context);
                ContainerUtil.service(bean, (ServiceManager)this.beanFactory.getBean(ServiceManager.class.getName()));
                if ( info != null ) {
                    Configuration config = info.getConfiguration();
                    if ( config == null ) {
                        config = EMPTY_CONFIG;
                    }
                    if ( bean instanceof Configurable ) {
                        ContainerUtil.configure(bean, config);
                    } else if ( bean instanceof Parameterizable ) {
                        Parameters p = info.getParameters();
                        if ( p == null ) {
                            p = Parameters.fromConfiguration(config);
                            info.setParameters(p);
                        }
                        ContainerUtil.parameterize(bean, p);
                    }
                }
                if ( bean instanceof SitemapConfigurable ) {
                    ((SitemapConfigurable)bean).configure(new DefaultSitemapConfigurationHolder(beanName));
                }
                ContainerUtil.initialize(bean);
            } catch (Exception e) {
                throw new BeanCreationException("Unable to initialize Avalon component with role " + beanName, e);
            }
            return bean;
        }

        /**
         * @see org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor#postProcessBeforeDestruction(java.lang.Object, java.lang.String)
         */
        public void postProcessBeforeDestruction(Object bean, String beanName)
        throws BeansException {
            try {
                ContainerUtil.stop(bean);
            } catch (Exception e) {
                throw new BeanInitializationException("Unable to stop bean " + beanName, e);
            }
            ContainerUtil.dispose(bean);
        }
    }
}
