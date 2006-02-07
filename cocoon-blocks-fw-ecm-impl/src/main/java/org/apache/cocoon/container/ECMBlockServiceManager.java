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
package org.apache.cocoon.container;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.blocks.BlockConstants;
import org.apache.cocoon.blocks.ServiceManagerRegistry;
import org.apache.cocoon.components.ComponentInfo;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.container.CocoonServiceManager;
import org.apache.cocoon.components.source.impl.ContextSourceFactory;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.core.container.CoreServiceManager;
import org.apache.cocoon.core.container.RoleManager;
import org.apache.cocoon.core.container.RoleManagerOwner;
import org.apache.cocoon.core.servlet.CoreUtil;
import org.apache.cocoon.core.source.SimpleSourceResolver;
import org.apache.excalibur.source.SourceFactory;
import org.apache.excalibur.source.impl.ResourceSourceFactory;
import org.apache.excalibur.source.impl.URLSourceFactory;

/**
 * @version $Id$
 */
public class ECMBlockServiceManager
implements ServiceManager, LogEnabled, Configurable, Serviceable, Initializable, RoleManagerOwner {

    ServletConfig servletConfig;
    
    Logger logger;
    Configuration configuration;
    ServiceManager parentServiceManager;
    ServiceManager serviceManager;
    ServiceManagerRegistry serviceManagerRegistry;
    
    // Wiring

    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;        
    }

    // Life cycle methods
    
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    public void configure(Configuration configuration) throws ConfigurationException {
        this.configuration = configuration; 
    }

    public void service(ServiceManager parentServiceManager) throws ServiceException {
        this.parentServiceManager = parentServiceManager;
        this.serviceManagerRegistry = (ServiceManagerRegistry) parentServiceManager;
    }

    public void initialize() throws Exception {
        ServletContext servletContext = this.servletConfig.getServletContext();

        Settings settings = CoreUtil.createSettings(this.servletConfig);
        Context newContext =
            CoreUtil.createContext(this.servletConfig, settings, BlockConstants.BLOCK_CONF); 
        String contextURL = CoreUtil.getContextURL(servletContext, BlockConstants.BLOCK_CONF);
        String confLocation = contextURL + "::";

        CocoonServiceManager coreServicemanager =
            new CocoonServiceManager(this.parentServiceManager);
        LifecycleHelper.setupComponent(
                this.serviceManager,
                this.getLogger(),
                newContext,
                null,
                null,
                false);
        
        // The Core object is needed for the Sitemap and various other Cocoon components
        Core core = new Core(settings, newContext);
        coreServicemanager.addInstance(Core.ROLE, core);
        
        // For the first block that is setup, source factories need to be setup before
        // starting to configure service managers that includes other configuration files
        // TODO This is really complex and should be simplified
        String resourceSourceSelector = SourceFactory.ROLE + "/resource";
        if (!this.parentServiceManager.hasService(resourceSourceSelector)) {
            ResourceSourceFactory resourceFactory = new ResourceSourceFactory();
            resourceFactory.enableLogging(this.getLogger());
            coreServicemanager.addInstance(resourceSourceSelector, resourceFactory);
        }
        String urlSourceSelector = SourceFactory.ROLE + "/*";
        if (!this.parentServiceManager.hasService(urlSourceSelector)) {
            URLSourceFactory urlFactory = new URLSourceFactory();
            urlFactory.enableLogging(getLogger());
            coreServicemanager.addInstance(urlSourceSelector, urlFactory);
        }
        String contextSourceSelector = SourceFactory.ROLE + "/context";
        if (!this.parentServiceManager.hasService(contextSourceSelector)) {
            SimpleSourceResolver sourceResolver = new SimpleSourceResolver();
            LifecycleHelper.setupComponent(
                    sourceResolver,
                    this.getLogger(),
                    newContext,
                    null,
                    null);
            ContextSourceFactory contextFactory = new ContextSourceFactory();
            LifecycleHelper.setupComponent(
                    contextFactory,
                    this.getLogger(),
                    newContext,
                    new SimpleSourceResolver.SimpleServiceManager(sourceResolver),
                    null);
            coreServicemanager.addInstance(contextSourceSelector, contextFactory);
        }
        
        coreServicemanager.initialize();
        this.serviceManager = coreServicemanager;

        // The source resolver must be defined in this service
        // manager, otherwise the root path will be the one from the
        // parent manager, we add a resolver to get it right. If the
        // components section contain the CoreComponentManager, use the
        // location of the configuration and the parent SourceResolver
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
            new CoreServiceManager(this.serviceManager);
        LifecycleHelper.setupComponent(
                sourceResolverSM,
                this.getLogger(),
                newContext,
                null,
                sourceManagerConf);
        
        this.serviceManager =
            sourceResolverSM;
        
        // Create a service manager with the exposed components of the block and register
        // the roles in the global registry
        if (this.configuration != null) {
            DefaultConfiguration componentConf =
                new DefaultConfiguration("components", confLocation);
            componentConf.addAll(this.configuration);
            this.serviceManager =
                new CocoonServiceManager(this.serviceManager) {

                    /* (non-Javadoc)
                     * @see org.apache.cocoon.components.container.CocoonServiceManager#addComponent(java.lang.String, java.lang.String, org.apache.avalon.framework.configuration.Configuration, org.apache.cocoon.components.ComponentInfo)
                     */
                    public void addComponent(String role, String className, Configuration config, ComponentInfo info) throws ConfigurationException {
                        super.addComponent(role, className, config, info);

                        if (configuration.getAttributeAsBoolean("exported", true)) {
                            ECMBlockServiceManager.this.serviceManagerRegistry.registerServiceManager(role, this);
                        }
                    }

                    /* (non-Javadoc)
                     * @see org.apache.cocoon.core.container.CoreServiceManager#addInstance(java.lang.String, java.lang.Object)
                     */
                    public void addInstance(String role, Object instance) throws ServiceException {
                        super.addInstance(role, instance);
                        ECMBlockServiceManager.this.serviceManagerRegistry.registerServiceManager(role, this);
                    }
                
            };
            LifecycleHelper.setupComponent(this.serviceManager,
                    this.getLogger(),
                    newContext,
                    null,
                    componentConf);
        }
    }
    
    // service manager methods
    public Object lookup(String role) throws ServiceException {
        return this.serviceManager.lookup(role);
    }

    public boolean hasService(String role) {
        return this.serviceManager.hasService(role);
    }

    public void release(Object component) {
        this.serviceManager.release(component);
    }

    // logger

    public RoleManager getRoleManager() {
        return ((RoleManagerOwner)this.serviceManager).getRoleManager();
    }

    private Logger getLogger() {
        return this.logger;
    }
}