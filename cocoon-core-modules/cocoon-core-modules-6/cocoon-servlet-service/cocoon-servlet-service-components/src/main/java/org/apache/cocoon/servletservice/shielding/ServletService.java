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
package org.apache.cocoon.servletservice.shielding;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.servletservice.ServletServiceContext;
import org.apache.cocoon.servletservice.util.ServletConfigurationWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * <p>
 * This class is only used as a base class for the
 * {@link ShieldingServletService}. For non shielded servlet services, the
 * {@link org.apache.cocoon.servletservice.spring.ServletFactoryBean} should be
 * used.
 * </p>
 * <p>
 * TODO: Make the shielding work together with the ServletBeanFactory.
 * </p>
 *
 * @version $Id$
 * @since 1.0.0
 */
public class ServletService extends HttpServlet
    implements ApplicationContextAware, ServletContextAware, BeanNameAware,
    InitializingBean, DisposableBean {

    private ServletServiceContext servletServiceContext;
    private String embeddedServletClass;
    private Servlet embeddedServlet;
    private ServletContext servletContext;
    private String beanName;
    private ApplicationContext parentContainer;

    public ServletService() {
        this.servletServiceContext = new ServletServiceContext();
    }

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig servletConfig) throws ServletException {
        this.servletServiceContext.setServletContext(servletConfig.getServletContext());

        // create a sub container that resolves paths relative to the block
        // context rather than the parent context and make it available in
        // a context attribute
        if (this.parentContainer == null)
            this.parentContainer =
                WebApplicationContextUtils.getRequiredWebApplicationContext(servletConfig.getServletContext());
        GenericWebApplicationContext container = new GenericWebApplicationContext();
        container.setParent(this.parentContainer);
        container.setServletContext(this.servletServiceContext);
        container.refresh();
        this.servletServiceContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, container);

        // create a servlet config based on the block servlet context
        ServletConfig blockServletConfig =
            new ServletConfigurationWrapper(servletConfig, this.servletServiceContext) {

                // FIXME: The context should get the init parameters from the
                // config rather than the opposite way around.
                public String getInitParameter(String name) {
                    return super.getServletContext().getInitParameter(name);
                }

                public Enumeration getInitParameterNames() {
                    return super.getServletContext().getInitParameterNames();
                }
            };

        super.init(blockServletConfig);

        // create and initialize the embedded servlet
        this.embeddedServlet = createEmbeddedServlet(this.embeddedServletClass, blockServletConfig);
        this.embeddedServlet.init(blockServletConfig);
        this.servletServiceContext.setServlet(this.embeddedServlet);
    }

    /**
     * Creates and initializes the embedded servlet
     * @param string
     * @throws ServletException
     */
    protected Servlet createEmbeddedServlet(String embeddedServletClassName, ServletConfig servletConfig)
    throws ServletException {
        try {
            return (Servlet) this.getClass().getClassLoader().loadClass(embeddedServletClassName).newInstance();
        } catch (Exception e) {
            throw new ServletException("Loading class for embedded servlet failed " + embeddedServletClassName, e);
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        RequestDispatcher dispatcher =
            this.servletServiceContext.getRequestDispatcher(request.getPathInfo());
        dispatcher.forward(request, response);
    }

    public void destroy() {
        this.embeddedServlet.destroy();
        super.destroy();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.parentContainer = applicationContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public void setMountPath(String mountPath) {
        this.servletServiceContext.setMountPath(mountPath);
    }

    public String getMountPath() {
        return this.servletServiceContext.getMountPath();
    }

    /**
     * The path to the blocks resources relative to the servlet context URL,
     * must start with an '/'.
     * @param blockContextURL
     */
    // FIXME: would like to throw an exception if the form of the url is faulty,
    // what is the preferred way of handling faulty properties in Spring?
    public void setBlockContextURL(String blockContextURL) {
        this.servletServiceContext.setContextPath(blockContextURL);
    }

    public void setServletClass(String servletClass) {
        this.embeddedServletClass = servletClass;
    }

    public void setProperties(Map properties) {
        this.servletServiceContext.setInitParams(properties);
    }

    public void setConnections(Map connections) {
        this.servletServiceContext.setConnections(connections);
    }

    public void afterPropertiesSet() throws Exception {

        // Create a servlet config object based on the servlet context
        // from the webapp container
        ServletConfig servletConfig = new ServletConfig() {

            public String getInitParameter(String parameter) {
                return ServletService.this.servletContext.getInitParameter(parameter);
            }

            public Enumeration getInitParameterNames() {
                return ServletService.this.servletContext.getInitParameterNames();
            }

            public ServletContext getServletContext() {
                return ServletService.this.servletContext;
            }

            public String getServletName() {
                return ServletService.this.beanName;
            }

        };
        this.init(servletConfig);
    }
}
