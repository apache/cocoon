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
import java.net.URL;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Constants;
import org.apache.cocoon.blocks.util.CoreUtil;
import org.apache.cocoon.blocks.util.ServletConfigurationWrapper;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.container.CocoonServiceManager;
import org.apache.cocoon.components.container.ComponentContext;
import org.apache.cocoon.core.container.CoreServiceManager;
import org.apache.cocoon.environment.http.HttpContext;

/**
 * @version $Id$
 */
public class BlockManager
    extends HttpServlet
    implements Block, Configurable, Contextualizable, Disposable, Initializable, LogEnabled { 

    public static String ROLE = BlockManager.class.getName();

    private Logger logger;
    private Context context;
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

    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public void configure(Configuration config)
        throws ConfigurationException {
        this.config = config;
    }

    public void initialize() throws Exception {
        this.blockWiring = new BlockWiring();
        this.blockWiring.setServletContext(this.getServletContext());
        LifecycleHelper.setupComponent(this.blockWiring,
                                       this.getLogger(),
                                       null,
                                       null,
                                       this.config);    

        getLogger().debug("Initializing new Block Manager: " + this.blockWiring.getId());

        this.blockContext =
            new BlockContext(this.getServletContext(), this.blockWiring, this);
        this.contextURL = CoreUtil.getContextURL(this.blockContext, BlockConstants.BLOCK_CONF);
        Context newContext = this.getAvalonContext();
        String confLocation = this.contextURL + "::";

        ServletConfig blockServletConfig =
            new ServletConfigurationWrapper(this.getServletConfig(), this.blockContext);
        if (this.blockWiring.isCore()) {
            this.getLogger().debug("Block with core=true");
            CoreUtil coreUtil = new CoreUtil(blockServletConfig, BlockConstants.BLOCK_CONF);
            this.serviceManager = coreUtil.getServiceManager();
       } else {
            // Create a service manager for getting components from other blocks
            ServiceManager topServiceManager = new InterBlockServiceManager(this.blockWiring, this.blocks);
            ((InterBlockServiceManager)topServiceManager).enableLogging(this.getLogger());

            this.serviceManager =
                this.createLocalSourceResolverSM(newContext, topServiceManager, confLocation);
        }
        // FIXME this.settings = (Settings) this.serviceManager.lookup(Core.ROLE);
        
        // Create a service manager with the exposed components of the block
        if (this.blockWiring.getComponentConfiguration() != null) {
            DefaultConfiguration componentConf =
                new DefaultConfiguration("components", confLocation);
            componentConf.addAll(this.blockWiring.getComponentConfiguration());
            this.serviceManager = new CocoonServiceManager(this.serviceManager);
            LifecycleHelper.setupComponent(this.serviceManager,
                    this.getLogger(),
                    newContext,
                    null,
                    componentConf);
        }

        // Create a processor for the block
        if (this.blockWiring.getProcessorConfiguration() != null) {
            this.blockServlet = new SitemapServlet();
            this.blockServlet.init(blockServletConfig);
            LifecycleHelper.setupComponent(this.blockServlet,
                    this.getLogger(),
                    newContext,
                    this.serviceManager,
                    this.blockWiring.getProcessorConfiguration());    
            
        }
    }

    public void dispose() {
    }
    
    protected final Logger getLogger() {
        return this.logger;
    }

    /**
     * @throws Exception
     */
    protected Context getAvalonContext() throws Exception {
        ComponentContext newContext = new ComponentContext(this.context);
        // A block is supposed to be an isolated unit so it should not have
        // any direct access to the global root context
        newContext.put(ContextHelper.CONTEXT_ROOT_URL, new URL(this.contextURL));
        newContext.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT, new HttpContext(this.blockContext));
        newContext.makeReadOnly();
        
        return newContext;
    }

    /**
     * @param newContext
     * @param confLocation
     * @throws Exception
     */
    protected ServiceManager createLocalSourceResolverSM(Context newContext, ServiceManager parentServiceManager, String confLocation) throws Exception {
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

    // Block methods

    // The blocks manager should not be available within a block so I
    // didn't want to make it part of the parent manager. But this is
    // a little bit clumsy. Question is what components, if any, the
    // blocks should have in common.
    public void setBlocks(Blocks blocks) {
        this.blocks = blocks;
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
    
    // Servlet methods

        public void init(ServletConfig servletConfig) throws ServletException {
            super.init(servletConfig);
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
    
    public Servlet getBlockServlet() {
        return this.blockServlet;
    }
}
