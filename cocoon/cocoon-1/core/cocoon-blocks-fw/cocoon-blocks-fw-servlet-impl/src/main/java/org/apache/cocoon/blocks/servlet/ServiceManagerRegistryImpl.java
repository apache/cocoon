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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;

/**
 * @version $Id$
 */
public class ServiceManagerRegistryImpl implements ServiceManagerRegistry {

    private Map serviceManagerMapping = Collections.synchronizedMap(new HashMap());
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.blocks.ServiceManagerRegistry#registerServiceManager(java.lang.String, org.apache.avalon.framework.service.ServiceManager)
     */
    public void registerServiceManager(String role, ServiceManager manager) {
        this.serviceManagerMapping.put(role, manager);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceManager#lookup(java.lang.String)
     */
    public Object lookup(String role) throws ServiceException {
        ServiceManager manager = (ServiceManager) this.serviceManagerMapping.get(role);
        if (manager == null) {
            throw new ServiceException(role, "Could not find any manager in connected blocks that contains the role");
        }
        return manager.lookup(role);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceManager#hasService(java.lang.String)
     */
    public boolean hasService(String role) {
        ServiceManager manager = (ServiceManager) this.serviceManagerMapping.get(role);
        return manager != null && manager.hasService(role);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceManager#release(java.lang.Object)
     */
    public void release(Object role) {
        ServiceManager manager = (ServiceManager) this.serviceManagerMapping.get(role);
        if (manager != null)
            manager.release(role);
    }

}
