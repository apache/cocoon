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
package org.apache.cocoon.blocks;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.blocks.util.CoreUtil;
import org.apache.cocoon.blocks.util.ServletConfigurationWrapper;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.container.CocoonServiceManager;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.core.container.CoreServiceManager;
import org.apache.cocoon.core.container.SingleComponentServiceManager;
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
    private Blocks blocks;
    private String contextURL;

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

    // FIXME The InterBlockServiceManager need access to the BlocksManager,
    // it should preferably just need to access something more component
    // handling specific.
    public void setBlocks(Blocks blocks) {
        this.blocks = blocks;
    }

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        this.blockWiring = new BlockWiring();
        this.blockWiring.setServletContext(this.getServletContext());
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

        this.blockContext =
            new BlockContext(this.getServletContext(), this.blockWiring, this);
        this.contextURL = CoreUtil.getContextURL(this.blockContext, BlockConstants.BLOCK_CONF);
        ServletConfig blockServletConfig =
            new ServletConfigurationWrapper(this.getServletConfig(), this.blockContext);

        Settings settings = CoreUtil.createSettings(blockServletConfig);
        Context newContext =
            CoreUtil.createContext(blockServletConfig, settings, BlockConstants.BLOCK_CONF); 
        Core core = new Core(settings, newContext);
        String confLocation = this.contextURL + "::";

        // Create a service manager for getting components from other blocks
        ServiceManager topServiceManager = new InterBlockServiceManager(this.blockWiring, this.blocks);
        ((InterBlockServiceManager)topServiceManager).enableLogging(this.getLogger());

        this.serviceManager = new SingleComponentServiceManager(topServiceManager, core, Core.ROLE);
        if (!this.blockWiring.isCore()) {
            this.getLogger().debug("Non core Block");
            try {
                this.serviceManager =
                    this.createLocalSourceResolverSM(newContext, this.serviceManager , confLocation);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        
        // Create a service manager with the exposed components of the block
        if (this.blockWiring.getComponentConfiguration() != null) {
            DefaultConfiguration componentConf =
                new DefaultConfiguration("components", confLocation);
            componentConf.addAll(this.blockWiring.getComponentConfiguration());
            this.serviceManager = new CocoonServiceManager(this.serviceManager);
            try {
                LifecycleHelper.setupComponent(this.serviceManager,
                        this.getLogger(),
                        newContext,
                        null,
                        componentConf);
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }

        // Create a servlet for the block
        if (this.blockWiring.hasServlet()) {
            String servletClass = this.blockWiring.getServletClass();
            try {
                this.blockServlet = (Servlet) ClassUtils.newInstance(servletClass);
                LifecycleHelper.setupComponent(this.blockServlet,
                        this.getLogger(),
                        newContext,
                        this.serviceManager,
                        this.blockWiring.getServletConfiguration());
            } catch (Exception e) {
                throw new ServletException(e);
            }
            this.blockServlet.init(blockServletConfig);            
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
    
    /**
     * The exported components of the block. Return null if the block doesn't export components.
     * 
     * @return a ServiceManager containing the blocks exported components
     */
    public ServiceManager getServiceManager() {
        // Check that the block have a local service manager
        if (this.blockWiring.getComponentConfiguration() != null) {
            return this.serviceManager;
        } else {
            return null;
        }
    }

    public Servlet getBlockServlet() {
        return this.blockServlet;
    }

    /**
     * @param newContext
     * @param confLocation
     * @throws Exception 
     * @throws Exception
     */
    protected ServiceManager createLocalSourceResolverSM(Context newContext,
            ServiceManager parentServiceManager, String confLocation) throws Exception {
        // The source resolver must be defined in this service
        // manager, otherwise the root path will be the one from the
        // parent manager, we add a resolver to get it right. If the
        // components section contain includes the CoreComponentManager
        // use the location of the configuration an the parent SourceResolver
        // for resolving the include.
        DefaultConfiguration sourceManagerConf =
            new DefaultConfiguration("components", confLocation);
        // FIXME: Need a local role manager as it is not inherited through the InterBlockServiceManager 
        DefaultConfiguration roleInclude =
            new DefaultConfiguration("include");
        roleInclude.setAttribute("src", "resource://org/apache/cocoon/cocoon.roles");
        sourceManagerConf.addChild(roleInclude);
        DefaultConfiguration resolverConf =
            new DefaultConfiguration("source-resolver");
        sourceManagerConf.addChild(resolverConf);
        ServiceManager sourceResolverSM =
            new CoreServiceManager(parentServiceManager);
        LifecycleHelper.setupComponent(
                sourceResolverSM,
                this.getLogger(),
                newContext,
                null,
                sourceManagerConf);
        return sourceResolverSM;
    }
}
