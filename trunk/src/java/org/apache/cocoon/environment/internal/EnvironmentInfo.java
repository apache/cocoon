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
package org.apache.cocoon.environment.internal;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Processor;
import org.apache.cocoon.environment.Environment;

/**
 * Experimental code for cleaning up the environment handling
 * This is an internal class, and it might change in an incompatible way over time.
 * For developing your own components/applications based on Cocoon, you shouldn't 
 * really need it.
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: EnvironmentInfo.java,v 1.2 2004/03/08 14:02:47 cziegeler Exp $
 * @since 2.2
 */
public class EnvironmentInfo {
    
    public final Processor      processor;
    public final int            oldStackCount;
    public final ServiceManager manager;
    public final Environment    environment;
    
    public EnvironmentInfo(Processor processor, 
                           int oldStackCount,
                           ServiceManager manager,
                           Environment    environment) {
        this.processor = processor;
        this.oldStackCount = oldStackCount;
        this.manager = manager;
        this.environment = environment;
    }
}

