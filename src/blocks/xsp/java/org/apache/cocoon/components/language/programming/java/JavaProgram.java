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
package org.apache.cocoon.components.language.programming.java;

import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.cocoon.components.ComponentInfo;
import org.apache.cocoon.components.language.generator.CompiledComponent;
import org.apache.cocoon.components.language.programming.Program;
import org.apache.cocoon.core.container.handler.AbstractComponentHandler;
import org.apache.cocoon.core.container.handler.ComponentHandler;

/**
 * This represents program in Java language.
 * It wraps Java Class object.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id$
 */
public class JavaProgram extends AbstractLogEnabled implements Program {

    protected Class program;
    
    protected DefaultConfiguration config;

    public JavaProgram(Class program) {
        this.program = program;
        this.config = new DefaultConfiguration("", "GeneratorSelector");
        // Instruct the core to avoid proxying this class
        this.config.setAttribute("model", ComponentInfo.TYPE_NON_THREAD_SAFE_POOLED);
    }

    public String getName() {
        return program.getName();
    }

    public ComponentHandler getHandler(ServiceManager manager,
                                       Context context)
    throws Exception {
        return AbstractComponentHandler.getComponentHandler(
                program,
                getLogger(),
                context,
                manager,
                this.config);
    }

    public CompiledComponent newInstance() throws Exception {
        return (CompiledComponent)this.program.newInstance();
    }
}
