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

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.Cocoon;
import org.apache.cocoon.Processor;
import org.apache.cocoon.core.BootstrapEnvironment;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.container.CoreServiceManager;
import org.osgi.framework.BundleContext;

/**
 * @version $Id$
 * @since 2.2
 */
public class CoreBlockActivator extends ServiceManagerActivator {

    private Core core;
    private Processor processor;

    public void start(final BundleContext ctx) throws Exception {
        Thread.currentThread().setContextClassLoader(CoreBlockActivator.class.getClassLoader());
        BootstrapEnvironment env = new OSGiBootstrapEnvironment(ctx);
        env.log("OSGiBootstrapEnvironment created");
        OSGICoreUtil coreUtil = new OSGICoreUtil(env);
        env.log("CoreUtil created");
        this.core = coreUtil.getCore();
        this.processor = coreUtil.createCocoon();

        super.start(ctx);
    }

    public void stop(BundleContext ctx) throws Exception {
        super.stop(ctx);
    }

    protected Context getContext() throws Exception {
        return this.core.getContext();
    }

    /**
     * This method may be overwritten by subclasses to provide an own
     * configuration
     */
//     protected Configuration getConfiguration() {
//         DefaultConfiguration config = new DefaultConfiguration("cocoon", "CoreBlockActivator");
//         return config;
//     }

    /**
     * This method may be overwritten by subclasses to add aditional
     * components.
     */
    protected void addComponents(CoreServiceManager manager) 
    throws ServiceException, ConfigurationException {
        manager.addInstance(Core.ROLE, this.core);
        manager.addInstance(Cocoon.class.getName(), this.processor);
    }
}
