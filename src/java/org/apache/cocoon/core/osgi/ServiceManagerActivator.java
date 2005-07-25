/*
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.cocoon.core.osgi;

import java.net.URL;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.container.ComponentContext;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

public class ServiceManagerActivator implements BundleActivator {


//  Registering services must go through the ServiceFactory class wrapping the componentHandler
    
    private OSGiCoreServiceManager manager;

    public void start(final BundleContext ctx) throws Exception {
        
        // Create a logger manager that delegates to OSGi
        // FIXME: have the maximum level as a property of the bundle
        LoggerManager logManager = new OSGiLoggerManager(ctx, LogService.LOG_DEBUG);

        // Create a parent manager that will lookup registered OSGi services
        ServiceManager parentManager = new OSGiServiceManager(ctx);
        
        // Create a regular manager
        this.manager = new OSGiCoreServiceManager(parentManager, this.getClass().getClassLoader(), ctx) {
        };
        
        //---- LogEnabled
        this.manager.enableLogging(logManager.getDefaultLogger());
        
        //---- Contextualizable
        DefaultContext avalonCtx = new ComponentContext();
        // Context entries defined in CocoonServlet/CoreUtil:
        // "servlet-config"
        // "servlet-context"
        // ContextHelper.CONTEXT_ROOT_URL
        // Constants.CONTEXT_ENVIRONMENT_CONTEXT
        // Constants.CONTEXT_WORK_DIR
        // Constants.CONTEXT_UPLOAD_DIR
        // Constants.CONTEXT_CACHE_DIR
        // Constants.CONTEXT_CONFIG_URL
        // Constants.CONTEXT_DEFAULT_ENCODING
        // Constants.CONTEXT_CLASS_LOADER
        // Core.ROLE (???)
        // Constants.CONTEXT_CLASSPATH

        this.manager.contextualize(avalonCtx);
        
        //---- LoggerManager
        this.manager.setLoggerManager(logManager);
        
        //---- RoleManager
        // No parent role manager
        this.manager.setRoleManager(null);
        
        //---- Configurable
        // Create a configuration object with one include directive. ECM++ will do the rest!
        DefaultConfiguration config = new DefaultConfiguration("cocoon");
        DefaultConfiguration include = new DefaultConfiguration("include");
        URL confURL = ctx.getBundle().getResource("/BLOCK-INF/block.xconf");
        include.setAttribute("src", confURL.toExternalForm());
        config.addChild(include);
        
        this.manager.configure(config);
        
        //---- Initializable
        this.manager.initialize();

    }

    public void stop(BundleContext ctx) throws Exception {
        // Dispose the ServiceManager
        this.manager.dispose();
    }
}
