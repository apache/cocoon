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
package org.apache.cocoon.components.container;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;

/**
 * This wrapps a component manager as a service manager
 * @since 2.2
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ComponentManagerWrapper.java,v 1.1 2004/05/25 07:28:24 cziegeler Exp $
 */
public final class ComponentManagerWrapper implements ServiceManager {
    
    protected final ComponentManager manager;
    
    public ComponentManagerWrapper(ComponentManager m) {
        this.manager = m;
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceManager#hasService(java.lang.String)
     */
    public boolean hasService(String role) {
        return this.manager.hasComponent(role);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceManager#lookup(java.lang.String)
     */
    public Object lookup(String role) throws ServiceException {
        try {
            Object o = this.manager.lookup(role);
            if ( o instanceof ComponentSelector ) {
                o = new ComponentSelectorWrapper((ComponentSelector)o);
            }
            return o;
        } catch (ComponentException ce) {
            throw new ServiceException("ComponentManagerWrapper", "Unable to lookup component: " + role, ce);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceManager#release(java.lang.Object)
     */
    public void release(Object c) {
        if ( c instanceof ComponentSelectorWrapper ) {
            c = ((ComponentSelectorWrapper)c).getComponent();
        }
        this.manager.release((Component)c);
    }

    public static final class ComponentSelectorWrapper implements ServiceSelector {
        
        protected final ComponentSelector selector;
        
        public ComponentSelectorWrapper(ComponentSelector s) {
            this.selector = s;
        }

        /* (non-Javadoc)
         * @see org.apache.avalon.framework.service.ServiceSelector#isSelectable(java.lang.Object)
         */
        public boolean isSelectable(Object role) {
            return this.selector.hasComponent(role);
        }
        
        /* (non-Javadoc)
         * @see org.apache.avalon.framework.service.ServiceSelector#release(java.lang.Object)
         */
        public void release(Object role) {
            this.selector.release((Component)role);
        }
        
        /* (non-Javadoc)
         * @see org.apache.avalon.framework.service.ServiceSelector#select(java.lang.Object)
         */
        public Object select(Object role) throws ServiceException {
            try {
                Object o = this.selector.select(role);
                if ( o instanceof ComponentSelector ) {
                    o = new ComponentSelectorWrapper((ComponentSelector)o);
                }
                return o;
            } catch (ComponentException ce) {
                throw new ServiceException("ComponentServiceWrapper", "Unable to lookup component: " + role, ce);
            }
        }
        
        public ComponentSelector getComponent() {
            return this.selector;
        }
        
    }
}


