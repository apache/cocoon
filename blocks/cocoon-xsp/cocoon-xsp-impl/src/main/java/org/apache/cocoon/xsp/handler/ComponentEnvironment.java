/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
package org.apache.cocoon.xsp.handler;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * The component enviromnent contains all objects necessary to create
 * a new component; it's just a "container" of objects.
 *
 * @since 2.2
 * @version $Id$
 */
public class ComponentEnvironment {
    public final ServiceManager serviceManager;
    public final Context context;

    public final ClassLoader classLoader;

    public ComponentEnvironment(Context context,
                                ServiceManager serviceManager,
                                ClassLoader classLoader ) {
        this.context = context;
        this.serviceManager = serviceManager;
        this.classLoader = classLoader;
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        return this.classLoader.loadClass(name);
    }
}
