/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.Stack;

import javax.servlet.ServletContext;

import org.apache.cocoon.configuration.Settings;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.scope.RequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * 
 * @version $Id$
 */
public class Container {

    /** The name of the request attribute containing the current bean factory. */
    public static final String CONTAINER_REQUEST_ATTRIBUTE = Container.class.getName();

    protected static final String CONTAINER_STACK_REQUEST_ATTRIBUTE = Container.class.getName() + "/Stack";

    protected ClassLoader classLoader;
    protected BeanFactory beanFactory;

    public Container(BeanFactory beanFactory, ClassLoader classLoader) {
        this.beanFactory = beanFactory;
        this.classLoader = classLoader;
    }

    protected static Container ROOT_CONTAINER;

    /**
     * Return the current web application context.
     * @param servletContext The servlet context.
     * @param attributes     The request attributes.
     * @return The web application context.
     */
    public static Container getCurrentContainer(ServletContext servletContext,
                                                RequestAttributes attributes) {
        if (attributes.getAttribute(CONTAINER_REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST) != null) {
            return (Container) attributes
                    .getAttribute(CONTAINER_REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        }
        if ( ROOT_CONTAINER == null ) {
            final WebApplicationContext parentContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
            ROOT_CONTAINER = new Container(parentContext, parentContext.getClassLoader());
        }
        return ROOT_CONTAINER;
    }

    /**
     * Notify about entering this context.
     * @param attributes The request attributes.
     * @return A handle which should be passed to {@link #leavingContext(RequestAttributes, Object)}.
     */
    public Object enteringContext(RequestAttributes attributes) {
        final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        final Object oldContext = attributes.getAttribute(CONTAINER_REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        if ( oldContext != null ) {
            Stack stack = (Stack)attributes.getAttribute(CONTAINER_STACK_REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
            if ( stack == null ) {
                stack = new Stack();
                attributes.setAttribute(CONTAINER_STACK_REQUEST_ATTRIBUTE, stack, RequestAttributes.SCOPE_REQUEST);
            }
            stack.push(oldContext);
        }
        attributes.setAttribute(CONTAINER_REQUEST_ATTRIBUTE, this, RequestAttributes.SCOPE_REQUEST);
        Thread.currentThread().setContextClassLoader(this.classLoader);
        return oldClassLoader;
    }

    /**
     * Notify about leaving this context.
     * @param attributes The request attributes.
     * @param handle     The returned handle from {@link #enteringContext(RequestAttributes)}.
     */
    public void leavingContext(RequestAttributes attributes, Object handle) {
        Thread.currentThread().setContextClassLoader((ClassLoader)handle);
        final Stack stack = (Stack)attributes.getAttribute(CONTAINER_STACK_REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        if ( stack == null ) {
            attributes.removeAttribute(CONTAINER_REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        } else {
            final Object oldContext = stack.pop();
            attributes.setAttribute(CONTAINER_REQUEST_ATTRIBUTE, oldContext, RequestAttributes.SCOPE_REQUEST);
            if ( stack.size() == 0 ) {
                attributes.removeAttribute(CONTAINER_STACK_REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
            }
        }
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    public Settings getSettings() {
        return (Settings)this.beanFactory.getBean(Settings.ROLE);
    }

    public BeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    public void shutdown() {
        if ( this.beanFactory instanceof ConfigurableApplicationContext ) {
            ((ConfigurableApplicationContext)this.beanFactory).close();
        } else if ( this.beanFactory instanceof ConfigurableBeanFactory ) {
            ((ConfigurableBeanFactory)this.beanFactory).destroySingletons();
        }
    }
}
