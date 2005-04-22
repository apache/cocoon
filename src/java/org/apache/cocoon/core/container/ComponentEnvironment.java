/* 
 * Copyright 2002-2005 The Apache Software Foundation
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
package org.apache.cocoon.core.container;

import java.io.InputStream;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.ComponentInfo;
import org.apache.cocoon.configuration.ConfigurationBuilder;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.Settings;

/**
 * The component enviromnent contains all objects necessary to create
 * a new component; it's just a "container" of objects.
 *
 * @version SVN $Id:  123716 2004-12-30 14:16:00Z vgritsenko $
 */
public class ComponentEnvironment {

    public final ServiceManager serviceManager;
    public final Context context;
    public final Logger logger;
    public final RoleManager roleManager;
    public final LoggerManager loggerManager;
    private final ClassLoader classLoader;
    private Core core;

    public ComponentEnvironment(ClassLoader classLoader, Logger logger, RoleManager roleManager, LoggerManager loggerManager,
            Context context, ServiceManager serviceManager) {

        // Find a class loader
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = this.getClass().getClassLoader();
            }            
        }

        this.classLoader = classLoader;
        this.logger = logger;
        this.roleManager = roleManager;
        this.loggerManager = loggerManager;
        this.context = context;
        this.serviceManager = serviceManager;
        try {
            this.core = (Core)this.context.get(Core.ROLE);
        } catch (ContextException ignore) {
            // this can never happen
        }
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        return this.classLoader.loadClass(name);
    }
    
    public ComponentInfo loadComponentInfo(String name) 
    throws Exception {
        final StringBuffer bu = new StringBuffer(name);
        bu.append(".xconf");
        ComponentInfo ci = null;
        final InputStream is = this.classLoader.getResourceAsStream(bu.toString());
        if ( is != null ) {
            final Settings settings = this.core.getSettings();
            final ConfigurationBuilder cb = new ConfigurationBuilder(settings);
            final Configuration conf = cb.build(is);
            ci = new ComponentInfo();
            ci.fill(conf);
        }
        return ci;
    }
}
