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

import org.apache.cocoon.components.language.generator.CompiledComponent;
import org.apache.cocoon.components.language.programming.Program;
import org.apache.cocoon.core.container.AbstractComponentHandler;

/**
 * This represents program in Java language.
 * It wraps Java Class object.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id$
 */
public class JavaProgram extends AbstractLogEnabled implements Program {

    protected Class program;

    public JavaProgram(Class program) {
        this.program = program;
    }

    public String getName() {
        return program.getName();
    }

    public AbstractComponentHandler getHandler(ServiceManager manager,
                                       Context context)
    throws Exception {

        return AbstractComponentHandler.getComponentHandler(
                program,
                new DefaultConfiguration("", "GeneratorSelector"),
                manager, context, getLogger(), null, null);
    }

    public CompiledComponent newInstance() throws Exception {
        return (CompiledComponent)this.program.newInstance();
    }
}
