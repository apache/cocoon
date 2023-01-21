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
package org.apache.cocoon.components.language.programming;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.cocoon.components.language.generator.CompiledComponent;
import org.apache.cocoon.xsp.handler.ComponentHandler;

/**
 * This interface states the functionality of a program.
 * For compilable languages this is the wrapper for a Java Class object.
 *
 * @version $Id$
 */
public interface Program {

    /**
     * Get the name of this program.
     */
    String getName();

    /**
     * Get ComponentHandler which holds instances of this program.
     */
    ComponentHandler getHandler(ServiceManager manager,
                                Context context) throws Exception;

    /**
     * Create new instance of the program.
     */
    CompiledComponent newInstance() throws Exception;
}
