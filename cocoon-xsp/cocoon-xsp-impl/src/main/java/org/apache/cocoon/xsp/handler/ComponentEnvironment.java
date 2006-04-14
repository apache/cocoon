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
package org.apache.cocoon.xsp.handler;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * The component enviromnent contains all objects necessary to create
 * a new component; it's just a "container" of objects.
 *
 * @version $Id$
 * @since 2.2
 */
public class ComponentEnvironment {

    public final ServiceManager serviceManager;
    public final Context context;
    public final Logger logger;

    public ComponentEnvironment(Logger logger,
                                Context context,
                                ServiceManager serviceManager) {

        this.logger = logger;
        this.context = context;
        this.serviceManager = serviceManager;
    }

    public Class loadClass(String name) throws ClassNotFoundException {
        return this.getClass().getClassLoader().loadClass(name);
    }
}
