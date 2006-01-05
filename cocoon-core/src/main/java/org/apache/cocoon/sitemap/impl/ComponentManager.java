/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.sitemap.impl;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.sitemap.ComponentLocator;


/**
 * This is the connection between the Cocoon core components
 * and an optional application/sitemap container.
 *
 * It acts as a service manager and as a component locator at the same time.
 * A component manager is initialized with both, a service manager for a sitemap
 * and an optional component locator for the sitemap. Each operation (lookup etc.)
 * is first performed on the component locator. If the locator does not have
 * the component in question, the service manager is asked.
 *
 * @since 2.2
 * @version $Id$
 */
public class ComponentManager implements ServiceManager, ComponentLocator {

    final protected ServiceManager serviceManager;
    final protected ComponentLocator componentLocator;

    public ComponentManager(final ServiceManager sm, final ComponentLocator cl) {
        this.serviceManager = sm;
        this.componentLocator = cl;
    }

    /**
     * @see org.apache.avalon.framework.service.ServiceManager#hasService(java.lang.String)
     */
    public boolean hasService(String key) {
        boolean result = false;
        if ( this.componentLocator != null ) {
            result = this.componentLocator.hasComponent(key);
        }
        if ( !result ) {
            result = this.serviceManager.hasService(key);
        }
        return result;
    }

    /**
     * @see org.apache.cocoon.sitemap.ComponentLocator#release(java.lang.Object)
     * @see org.apache.avalon.framework.service.ServiceManager#release(java.lang.Object)
     */
    public void release(Object component) {
        // FIXME - we should optimize this
        if ( this.componentLocator != null ) {
            this.componentLocator.release(component);
        }
        this.serviceManager.release(component);
    }

    /**
     * @see org.apache.cocoon.sitemap.ComponentLocator#hasComponent(java.lang.String)
     */
    public boolean hasComponent(String key) {
        return this.hasService(key);
    }

    /**
     * @see org.apache.avalon.framework.service.ServiceManager#lookup(java.lang.String)
     */
    public Object lookup(String key) 
    throws ServiceException {
        try {
            return this.doLookup(key);
        } catch (ProcessingException se) {
            throw new ServiceException("ComponentLocator", 
                                       "Unable to lookup component for key: " + key, se);
        }
    }

    /**
     * @see org.apache.cocoon.sitemap.ComponentLocator#getComponent(java.lang.String)
     */
    public Object getComponent(String key) throws ProcessingException {
        try {
            return this.doLookup(key);
        } catch (ServiceException se) {
            throw new ProcessingException("Unable to lookup component for key: " + key, se);
        }
    }

    protected Object doLookup(String key)
    throws ProcessingException, ServiceException {
        Object component = null;
        if ( this.componentLocator != null ) {
            if ( this.componentLocator.hasComponent(key) ) {
                component = this.componentLocator.getComponent(key);
            }
        }
        if ( component == null && this.serviceManager.hasService(key) ) {
            component = this.serviceManager.lookup(key);
        }
        return component;
    }
    
    public ServiceManager getServiceManager() {
        if ( this.serviceManager instanceof ComponentManager ) {
            return ((ComponentManager)this.serviceManager).getServiceManager();
        }
        return this.serviceManager;
    }

}
