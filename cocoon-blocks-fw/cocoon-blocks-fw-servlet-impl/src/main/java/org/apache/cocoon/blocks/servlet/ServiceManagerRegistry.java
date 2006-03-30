/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.blocks.servlet;

import org.apache.avalon.framework.service.ServiceManager;

/**
 * Interface for ditributed service management.
 * 
 * WARNING: It is rather specific for the ECM way of managing component and will probably
 * be replaced with a more OSGi friendly strategy.
 * 
 * In ECM the service manager contains component handlers while the OSGi service registry
 * contains objects or factories. I didn't find any nice way to combine these strategies in
 * one interface.
 * 
 * @version $Id$
 */
public interface ServiceManagerRegistry extends ServiceManager {
    
    /**
     * Register what service manager that contains the component bound to a certain role
     * @param role the component role
     * @param manager the service manager containing the component
     */
    public void registerServiceManager(String role, ServiceManager manager);
}
