/*
 * Copyright 2006 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring;

import java.io.IOException;
import java.util.Stack;

import javax.servlet.ServletContext;

import org.apache.cocoon.configuration.Settings;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.scope.RequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.context.support.XmlWebApplicationContext;

/**
 * Own implementation of a {@link XmlWebApplicationContext} which is configured with
 * a base url specifying the root directory for this web application context.
 *
 * @since 2.2
 * @version $Id$
 */
public class CocoonWebApplicationContext extends XmlWebApplicationContext implements Container {

    /** The name of the request attribute containing the current bean factory. */
    public static final String WEB_APPLICATION_CONTEXT_REQUEST_ATTRIBUTE = CocoonWebApplicationContext.class.getName();

    private static final String WEB_APPLICATION_CONTEXT_STACK_REQUEST_ATTRIBUTE = CocoonWebApplicationContext.class.getName() + "/Stack";

    /** The base url (already postfixed with a '/'). */
    protected final String baseUrl;

    /** The class loader for this context (or null). */
    protected final ClassLoader classLoader;

    /** The bean definition for this context. */
    protected final String beanDefinition;

    public CocoonWebApplicationContext(ClassLoader           classloader,
                                       WebApplicationContext parent,
                                       String                url,
                                       String                rootDefinition) {
        this.setParent(parent);
        this.setClassLoader(classloader);
        this.setServletContext(parent.getServletContext());
        if ( url.endsWith("/") ) {
            this.baseUrl = url;
        } else {
            this.baseUrl = url + '/';
        }
        this.classLoader = (classloader != null ? classloader : ClassUtils.getDefaultClassLoader());
        this.beanDefinition = rootDefinition;
        this.refresh();
    }

    /**
     * @see org.springframework.web.context.support.XmlWebApplicationContext#loadBeanDefinitions(org.springframework.beans.factory.xml.XmlBeanDefinitionReader)
     */
    protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws BeansException, IOException {
        if ( this.beanDefinition != null ) {
            reader.loadBeanDefinitions(new ByteArrayResource(this.beanDefinition.getBytes("utf-8")));
        }
        super.loadBeanDefinitions(reader);
    }

    /**
     * @see org.springframework.web.context.support.AbstractRefreshableWebApplicationContext#getResourceByPath(java.lang.String)
     */
    protected Resource getResourceByPath(String path) {
        // only if the path does not start with a "/" and is not a url
        // we assume it is relative
        if ( path != null && !path.startsWith("/") && !ResourceUtils.isUrl(path) ) {
            return super.getResourceByPath(this.baseUrl + path);
        }
        return super.getResourceByPath(path);
    }

    /**
     * A child application context has no default configuration.
     * @see org.springframework.web.context.support.XmlWebApplicationContext#getDefaultConfigLocations()
     */
    protected String[] getDefaultConfigLocations() {
        return new String[0];
    }

    /**
     * Notify about entering this context.
     * @param attributes The request attributes.
     * @return A handle which should be passed to {@link #leavingContext(RequestAttributes, Object)}.
     */
    public Object enteringContext(RequestAttributes attributes) {
        final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        final Object oldContext = attributes.getAttribute(WEB_APPLICATION_CONTEXT_REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        if ( oldContext != null ) {
            Stack stack = (Stack)attributes.getAttribute(WEB_APPLICATION_CONTEXT_STACK_REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
            if ( stack == null ) {
                stack = new Stack();
                attributes.setAttribute(WEB_APPLICATION_CONTEXT_STACK_REQUEST_ATTRIBUTE, stack, RequestAttributes.SCOPE_REQUEST);
            }
            stack.push(oldContext);
        }
        attributes.setAttribute(WEB_APPLICATION_CONTEXT_REQUEST_ATTRIBUTE, this, RequestAttributes.SCOPE_REQUEST);
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
        final Stack stack = (Stack)attributes.getAttribute(WEB_APPLICATION_CONTEXT_STACK_REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        if ( stack == null ) {
            attributes.removeAttribute(WEB_APPLICATION_CONTEXT_REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        } else {
            final Object oldContext = stack.pop();
            attributes.setAttribute(WEB_APPLICATION_CONTEXT_REQUEST_ATTRIBUTE, oldContext, RequestAttributes.SCOPE_REQUEST);
            if ( stack.size() == 0 ) {
                attributes.removeAttribute(WEB_APPLICATION_CONTEXT_STACK_REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
            }
        }
    }

    /**
     * Return the current web application context.
     * @param servletContext The servlet context.
     * @param attributes     The request attributes.
     * @return The web application context.
     */
    public static WebApplicationContext getCurrentContext(ServletContext servletContext,
                                                          RequestAttributes attributes) {
        WebApplicationContext parentContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        if (attributes.getAttribute(WEB_APPLICATION_CONTEXT_REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST) != null) {
            parentContext = (CocoonWebApplicationContext) attributes
                    .getAttribute(WEB_APPLICATION_CONTEXT_REQUEST_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        }
        return parentContext;
    }

    /**
     * @see org.apache.cocoon.core.container.spring.Container#getSettings()
     */
    public Settings getSettings() {
        return (Settings)this.getBean(Settings.ROLE);
    }
}
