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
package org.apache.cocoon.servletservice.spring;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.cocoon.servletservice.Mountable;
import org.apache.cocoon.servletservice.ServletServiceContext;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.DefaultIntroductionAdvisor;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 *
 * @version $Id$
 */
public class ServletFactoryBean implements FactoryBean, ApplicationContextAware,
                                           ServletContextAware, BeanNameAware {

    private ApplicationContext parentContainer;
    private ServletContext servletContext;
    private String beanName;

    private Servlet embeddedServlet;

    private String mountPath;
    private String contextPath;

    private Map initParams;
    private Map contextParams;
    private Map connections;

    private ServletServiceContext servletServiceContext;


    public ServletFactoryBean() {
    }

    public void init() throws Exception {
        this.servletServiceContext = new ServletServiceContext();
        this.servletServiceContext.setServletContext(this.servletContext);

        this.servletServiceContext.setMountPath(this.mountPath);
        this.servletServiceContext.setContextPath(this.contextPath);

        this.servletServiceContext.setInitParams(this.initParams);
        this.servletServiceContext.setAttributes(this.contextParams);
        this.servletServiceContext.setConnections(connections);

        // create a sub container that resolves paths relative to the block
        // context rather than the parent context and make it available in
        // a context attribute
        if (this.parentContainer == null) {
            this.parentContainer =
                    WebApplicationContextUtils.getRequiredWebApplicationContext(this.servletContext);
        }

        GenericWebApplicationContext container = new GenericWebApplicationContext();
        container.setParent(this.parentContainer);
        container.setServletContext(this.servletServiceContext);
        container.refresh();
        this.servletServiceContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, container);

        // create a servlet config based on the block servlet context
        ServletConfig blockServletConfig =
                new ServletConfig() {

                    public String getInitParameter(String name) {
                        return ServletFactoryBean.this.servletServiceContext.getInitParameter(name);
                    }

                    public Enumeration getInitParameterNames() {
                        return ServletFactoryBean.this.servletServiceContext.getInitParameterNames();
                    }

                    public ServletContext getServletContext() {
                        return ServletFactoryBean.this.servletServiceContext;
                    }

                    public String getServletName() {
                        return ServletFactoryBean.this.beanName;
                    }
                };

        // create and initialize the embeded servlet
        this.embeddedServlet.init(blockServletConfig);
        this.servletServiceContext.setServlet(this.embeddedServlet);
    }

    public void destroy() {
        this.embeddedServlet.destroy();
    }

    /* (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.parentContainer = applicationContext;
    }

    /* (non-Javadoc)
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    /**
     * @param embeddedServlet the embeddedServlet to set
     */
    public void setEmbeddedServlet(Servlet embeddedServlet) {
        this.embeddedServlet = embeddedServlet;
    }

    /**
     * @param mountPath
     */
    public void setMountPath(String mountPath) {
        this.mountPath = mountPath;
    }

    /**
     * The path to the blocks resources relative to the servlet context URL,
     * must start with an '/'.
     * @param contextPath
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * @param initParams
     */
    public void setInitParams(Map initParams) {
        this.initParams = initParams;
    }

    /**
     * @param contextParams the contextParams to set
     */
    public void setContextParams(Map contextParams) {
        this.contextParams = contextParams;
    }

    /**
     * @param connections
     */
    public void setConnections(Map connections) {
        this.connections = connections;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception {
        ProxyFactory proxyFactory = new ProxyFactory(this.embeddedServlet);
        proxyFactory.addAdvice(new ServiceInterceptor());
        if (this.mountPath != null) {
            proxyFactory.addAdvisor(new MountableMixinAdvisor());
        }
        return proxyFactory.getProxy();
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class getObjectType() {
        return this.embeddedServlet != null ? this.embeddedServlet.getClass() : null;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }

    private class ServiceInterceptor implements MethodInterceptor {

        /* (non-Javadoc)
         * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
         */
        public Object invoke(MethodInvocation invocation) throws Throwable {
            if ("service".equals(invocation.getMethod().getName())) {
                Object[] arguments = invocation.getArguments();
                HttpServletRequest request = (HttpServletRequest) arguments[0];
                HttpServletResponse response = (HttpServletResponse) arguments[1];
                RequestDispatcher dispatcher =
                        ServletFactoryBean.this.servletServiceContext.getRequestDispatcher(request.getPathInfo());
                dispatcher.forward(request, response);
                return null;
            } else if ("init".equals(invocation.getMethod().getName())) {
                // The embedded servlet is initialized by this factory bean, ignore other containers
                return null;                
            } else if ("destroy".equals(invocation.getMethod().getName())) {
                // The embedded servlet is destroyed up by this factory bean, ignore other containers
                return null;
            }

            return invocation.proceed();
        }
    }

    private class MountableMixin extends DelegatingIntroductionInterceptor
                                 implements Mountable {

        public String getMountPath() {
            return ServletFactoryBean.this.mountPath;
        }
    }

    private class MountableMixinAdvisor extends DefaultIntroductionAdvisor {

        public MountableMixinAdvisor() {
            super(new MountableMixin(), Mountable.class);
        }
    }
}
