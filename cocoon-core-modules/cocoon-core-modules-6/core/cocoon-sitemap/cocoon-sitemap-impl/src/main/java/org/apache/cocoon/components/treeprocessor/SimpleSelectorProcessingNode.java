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
package org.apache.cocoon.components.treeprocessor;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * Base class for processing nodes that are based on a component in a Selector (act, match, select, etc).
 *
 * @version $Id$
 */
public abstract class SimpleSelectorProcessingNode extends SimpleParentProcessingNode
    implements Serviceable, Disposable {

    private String selectorRole;
    
    /** ServiceManager */
    private ServiceManager manager;
    
    /** Selector where to get components from */
    private ServiceSelector selector;
    
    /** The underlying component, if it's threadsafe. Null otherwise */
    private Object threadSafeComponent;

    public SimpleSelectorProcessingNode(String selectorRole, String componentType) {
        super(componentType);
        this.selectorRole = selectorRole;
    }

    public void service(ServiceManager avalonManager) throws ServiceException {
        this.manager = avalonManager;
        this.selector = (ServiceSelector)this.manager.lookup(selectorRole);
        
        // Pre-lookup the associated component, and cache it if it's threadsafe
        Object component = this.selector.select(this.getType());
        if (component instanceof ThreadSafe) {
            this.threadSafeComponent = component;
        } else {
            this.selector.release(component);
        }
    }
    
    /**
     * Get the component to be used by this node. That component may be cached for faster
     * execution if it's ThreadSafe. In any case, a call to {@link #releaseComponent(Object)} must
     * be done to release the component if needed.
     * 
     * @return the component to use
     * @throws ServiceException if component lookup fails
     */
    protected Object getComponent() throws ServiceException {
        if (this.threadSafeComponent != null) {
            return this.threadSafeComponent;
        }
        return this.selector.select(this.componentName);
    }
    
    /**
     * Release the component used by this node (does nothing if it's the cached
     * ThreadSafe component)
     * 
     * @param obj the component
     */
    protected void releaseComponent(Object obj) {
        if (obj != this.threadSafeComponent) {
            this.selector.release(obj);
        }
    }
    
    public void dispose() {
        this.selector.release(this.threadSafeComponent);
        this.manager.release(this.selector);
        this.selector = null;
        this.manager = null;
    }
}
