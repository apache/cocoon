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
import org.apache.avalon.framework.configuration.DefaultConfiguration;

import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;

/**
 * This class sets up the configuration used by the ParentComponentManager sample.
 * The class also holds a reference to the initial context in which the configuration
 * is available.
 * <p>
 * The configuration is bound to <code>org/apache/cocoon/samples/parentcm/ParentCMConfiguration</code>.
 *
 * @author <a href="mailto:leo.sutic@inspireinfrastructure.com">Leo Sutic</a>
 * @version CVS $Id: Configurator.java,v 1.2 2004/03/10 09:54:05 cziegeler Exp $
 */
public class Configurator  {

    /**
     * The Excalibur in-memory JNDI directory. Since the directory doesn't
     * provide any persistence we must keep a reference to the initial context
     * as a static member to avoid passing it around.
     */
    public static Context initialContext = null;

    static {
        try {
            //
            // Create a new role.
            //
            DefaultConfiguration config = new DefaultConfiguration("roles", "");
            DefaultConfiguration timeComponent = new DefaultConfiguration("role", "roles");
            timeComponent.addAttribute("name", Time.ROLE);
            timeComponent.addAttribute("default-class", TimeComponent.class.getName());
            timeComponent.addAttribute("shorthand", "samples-parentcm-time");
            config.addChild(timeComponent);

            //
            // Bind it - get an initial context.
            //
            Hashtable environment = new Hashtable();
            environment.put(Context.INITIAL_CONTEXT_FACTORY, MemoryInitialContextFactory.class.getName());
            initialContext = new InitialContext(environment);

            //
            // Create subcontexts and bind the configuration.
            //
            Context ctx = initialContext.createSubcontext("org");
            ctx = ctx.createSubcontext("apache");
            ctx = ctx.createSubcontext("cocoon");
            ctx = ctx.createSubcontext("samples");
            ctx = ctx.createSubcontext("parentcm");
            ctx.rebind("ParentCMConfiguration", config);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}

