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
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.context.Context;

import org.apache.avalon.excalibur.component.ComponentHandler;
import org.apache.avalon.excalibur.component.RoleManager;
import org.apache.avalon.excalibur.component.LogkitLoggerManager;

import org.apache.cocoon.components.language.generator.CompiledComponent;
import org.apache.cocoon.components.language.programming.Program;

/**
 * This represents program in Java language.
 * It wraps Java Class object.
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: JavaProgram.java,v 1.3 2004/03/05 13:02:48 bdelacretaz Exp $
 */
public class JavaProgram implements Program {

    protected Class program;

    public JavaProgram(Class program) {
        this.program = program;
    }

    public String getName() {
        return program.getName();
    }

    public ComponentHandler getHandler(ComponentManager manager,
                                       Context context,
                                       RoleManager roles,
                                       LogkitLoggerManager logKitManager)
            throws Exception {

        return ComponentHandler.getComponentHandler(
                program,
                new DefaultConfiguration("", "GeneratorSelector"),
                manager, context, roles, logKitManager, null, "N/A");
    }

    public CompiledComponent newInstance() throws Exception {
        return (CompiledComponent)this.program.newInstance();
    }
}
