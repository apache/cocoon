/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.language.programming.javascript;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.cocoon.components.language.generator.CompiledComponent;
import org.apache.cocoon.components.language.programming.Program;
import org.apache.cocoon.core.container.spring.avalon.ComponentInfo;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.xsp.handler.AbstractComponentHandler;
import org.apache.cocoon.xsp.handler.ComponentHandler;

/**
 * This class represents program in the Javascript language.
 *
 * @version $Id$
 */
public class JavascriptProgram extends AbstractLogEnabled
                               implements Program {

    protected File file;
    protected Class clazz;
    protected DefaultConfiguration config;


    public JavascriptProgram(File file, Class clazz, Collection dependecies) {
        DefaultConfiguration child;

        this.file = file;
        this.clazz = clazz;

        config = new DefaultConfiguration("", "GeneratorSelector");
        // Instruct the core to avoid proxying this class
        config.setAttribute("model", ComponentInfo.MODEL_POOLED);
        child = new DefaultConfiguration("file", "");
        child.setValue(file.toString());
        config.addChild(child);

        for (Iterator i = dependecies.iterator(); i.hasNext(); ) {
            child = new DefaultConfiguration("dependency", "");
            child.setValue(i.next().toString());
            config.addChild(child);
        }
    }

    public String getName() {
        return file.toString();
    }

    public ComponentHandler getHandler(ServiceManager manager,
                                       Context context)
    throws Exception {
        return AbstractComponentHandler.getComponentHandler(clazz,
                                                            context,
                                                            manager,
                                                            config);
    }

    public CompiledComponent newInstance() throws Exception {
        CompiledComponent instance = (CompiledComponent) clazz.newInstance();
        if (instance instanceof Configurable) {
            ((Configurable) instance).configure(config);
        }

        return instance;
    }
}
