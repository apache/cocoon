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
package org.apache.cocoon.components.treeprocessor;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: SimpleSelectorProcessingNode.java,v 1.5 2004/07/15 12:49:50 sylvain Exp $
 */

public abstract class SimpleSelectorProcessingNode extends SimpleParentProcessingNode
    implements Serviceable, Disposable {

    private String selectorRole;
    
    /** ServiceManager */
    private ServiceManager manager;
    
    /** Selector where to get components from */
    protected ServiceSelector selector;
    
    /** The underlying component, if it's threadsafe. Null otherwise */
    private Object threadSafeComponent;

    public SimpleSelectorProcessingNode(String selectorRole, String componentType) {
        super(componentType);
        this.selectorRole = selectorRole;
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.selector = (ServiceSelector)manager.lookup(selectorRole);
        
        // Pre-lookup the associated component, and cache it if it's threadsafe
        Object component = this.selector.select(this.getType());
        if (component instanceof ThreadSafe) {
            this.threadSafeComponent = component;
        } else {
            this.selector.release(component);
        }
    }
    
    protected boolean hasThreadSafeComponent() {
        return this.threadSafeComponent != null;
    }
    
    protected Object getThreadSafeComponent() {
        return this.threadSafeComponent;
    }

//    public void setSelector(ServiceSelector selector) {
//        this.selector = selector;
//    }

//    /**
//     * Tests if the component designated by this node using the selector and component name
//     * is <code>ThreadSafe</code>, and return it if true.
//     * <p>
//     * Note : this method must be called <i>after</i> <code>setSelector()</code>.
//     */
//    protected Object getThreadSafeComponent() throws ServiceException {
//        return getThreadSafeComponent(this.componentName);
//    }
//
//    /**
//     * Tests if the component designated by this node using the selector and component name
//     * is <code>ThreadSafe</code>, and return it if true.
//     * <p>
//     * Note : this method must be called <i>after</i> <code>setSelector()</code>.
//     * @throws ServiceException
//     */
//    protected Object getThreadSafeComponent(String name) throws ServiceException {
//        Object component = this.selector.select(name);
//        if (component instanceof ThreadSafe) {
//            return component;
//        } else {
//            this.selector.release(component);
//            return null;
//        }
//    }
    
    public void dispose() {
        this.selector.release(this.threadSafeComponent);
        this.manager.release(this.selector);
        this.selector = null;
        this.manager = null;
    }
}
