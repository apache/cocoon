/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.samples.parentcm;

import org.apache.avalon.excalibur.naming.memory.MemoryInitialContextFactory;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.DefaultServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

import javax.naming.Context;
import java.util.Hashtable;

/**
 * A sample parent component manager. This manager will lookup the configuration object
 * given by the initialization parameter in JNDI, use it to configure an ExcaliburComponentManager
 * and delegate any requests to it.
 *
 * @author <a href="mailto:leo.sutic@inspireinfrastructure.com">Leo Sutic</a>
 * @version CVS $Id: ParentComponentManager.java,v 1.3 2004/03/10 09:54:05 cziegeler Exp $
 */
public class ParentComponentManager implements ServiceManager, LogEnabled, Initializable {

    /**
     * Our logger.
     */
    private Logger logger;

    /**
     * The JNDI name where the component manager configuration can be found.
     */
    private final String jndiName;

    /**
     * The delegate that will be configured and provide the
     * functionality for this component manager.
     */
    private final ServiceManager delegate;

    public ParentComponentManager(final String jndiName) {
        this.jndiName = jndiName;

        // Initialize it here so we can let it be final.
        this.delegate = new DefaultServiceManager();
    }

    public boolean hasService(final String role) {
        return delegate.hasService(role);
    }

    /**
     * Initializes the CM by looking up the configuration object and using it to
     * configure the delegate.
     */
    public void initialize() throws Exception {
        this.logger.debug("Looking up component manager configuration at : " + this.jndiName);

        Hashtable environment = new Hashtable();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, MemoryInitialContextFactory.class.getName());

        //
        // Yes, this is cheating, but the Excalibur in-memory naming provider
        // is transient. That is, it doesn't store objects persistently and
        // is more like a HashMap.
        //
        // Should be:
        // Context initialContext = new InitialContext(environment);
        //
        Context initialContext = Configurator.initialContext;

        Configuration config = (Configuration) initialContext.lookup(this.jndiName);

        // We ignore the setRoleManager call, as ExcaliburComponentManager handles that
        // in configure().
        ContainerUtil.enableLogging(delegate, logger);
        ContainerUtil.contextualize(delegate, new DefaultContext());
        ContainerUtil.configure(delegate, config);
        ContainerUtil.initialize(delegate);

        this.logger.debug("Component manager successfully initialized.");
    }

    public Object lookup(final String role) throws ServiceException {
        return this.delegate.lookup(role);
    }

    public void release(final Object component) {
        this.delegate.release(component);
    }

    /**
     * Provide component with a logger.
     *
     * @param logger the logger
     */
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }
}

