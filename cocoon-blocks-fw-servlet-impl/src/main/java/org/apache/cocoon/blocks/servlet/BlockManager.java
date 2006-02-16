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
package org.apache.cocoon.blocks.servlet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.blocks.Block;
import org.apache.cocoon.blocks.BlockCallStack;
import org.apache.cocoon.blocks.BlockContext;
import org.apache.cocoon.blocks.ServiceManagerRegistry;
import org.apache.cocoon.blocks.util.ServletConfigurationWrapper;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.util.ClassUtils;

/**
 * @version $Id$
 */
public class BlockManager
    extends HttpServlet
    implements Block, Configurable, LogEnabled { 

    public static String ROLE = BlockManager.class.getName();

    private Logger logger;
    private Configuration config;
    private ServiceManager serviceManager;

    private Servlet blockServlet;
    private BlockWiring blockWiring;
    private BlockContext blockContext;
    private ServiceManagerRegistry serviceManagerRegistry;

    private URL contextURL;

    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    public void configure(Configuration config)
        throws ConfigurationException {
        this.config = config;
    }

    protected final Logger getLogger() {
        return this.logger;
    }

    public void setServiceManagerRegistry(ServiceManagerRegistry serviceManagerRegistry) {
        this.serviceManagerRegistry = serviceManagerRegistry;
    }
    
    public void setContextURL(URL contextURL) {
        this.contextURL = contextURL;
    }

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        this.blockWiring = new BlockWiring();
        this.blockWiring.setContextURL(this.contextURL);
        try {
            LifecycleHelper.setupComponent(this.blockWiring,
                                           this.getLogger(),
                                           null,
                                           null,
                                           this.config);
        } catch (Exception e) {
            throw new ServletException(e);
        }    

        getLogger().debug("Initializing new Block Manager: " + this.blockWiring.getId());

        this.blockContext = new BlockContext(this.getServletContext());
        this.blockContext.setContextURL(this.contextURL);
        this.blockContext.setMountPath(this.blockWiring.getMountPath());
        this.blockContext.setConnections(this.blockWiring.getConnections());
        this.blockContext.setProperties(this.blockWiring.getProperties());
        ServletConfig blockServletConfig =
            new ServletConfigurationWrapper(this.getServletConfig(), this.blockContext);

        // Set up the component manager of the block
        try {
            // FIXME make the component manager class configurable
            String serviceManagerClassName = "org.apache.cocoon.container.ECMBlockServiceManager";
            this.serviceManager = (ServiceManager) ClassUtils.newInstance(serviceManagerClassName);

            // FIXME Don't know if this is the way to go, but I didn't feel like introducing interfaces
            // for something that a DI container could take care of
            Class serviceManagerClass = Class.forName(serviceManagerClassName);
            Method method = serviceManagerClass.getMethod("setServletConfig", new Class[]{ServletConfig.class});
            method.invoke(this.serviceManager, new Object[]{blockServletConfig});
            
            LifecycleHelper.setupComponent(this.serviceManager,
                    this.getLogger(),
                    null,
                    this.serviceManagerRegistry,
                    this.blockWiring.getComponentConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }

        // Create a servlet for the block
        if (this.blockWiring.hasServlet()) {
            String servletClass = this.blockWiring.getServletClass();
            try {
                this.blockServlet = (Servlet) ClassUtils.newInstance(servletClass);
                LifecycleHelper.setupComponent(this.blockServlet,
                        this.getLogger(),
                        null,
                        this.serviceManager,
                        this.blockWiring.getServletConfiguration());
            } catch (Exception e) {
                throw new ServletException(e);
            }
            this.blockServlet.init(blockServletConfig);
            
            this.blockContext.setServlet(this.blockServlet);
        }
    }
    
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Request to the own block
        try {
            // It is important to set the current block context each time
            // a new block is entered, this is used for the block
            // protocol
            BlockCallStack.enterBlock(this.blockContext);
            this.blockServlet.service(request, response);
        } finally {
            BlockCallStack.leaveBlock();
        }
    }

    public String getServletInfo() {
        return "BlockManager";
    }

    public void destroy() {
        super.destroy();
    }
    
    public ServletContext getBlockContext() {
        return this.blockContext;
    }
}
