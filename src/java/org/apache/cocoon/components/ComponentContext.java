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
package org.apache.cocoon.components;

import java.util.Map;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.environment.Environment;

/**
 * This is the {@link Context} implementation for Cocoon components.
 * It extends the {@link DefaultContext} by a special handling for
 * getting objects from the object model and other application information.
 * 
 * @see org.apache.cocoon.components.ContextHelper
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ComponentContext.java,v 1.6 2004/05/16 15:20:48 cziegeler Exp $
 */

public class ComponentContext 
    extends DefaultContext {

    protected static final String OBJECT_MODEL_KEY_PREFIX = ContextHelper.CONTEXT_OBJECT_MODEL + '.';
    
    /**
     * Create a Context with specified data and parent.
     *
     * @param contextData the context data
     * @param parent the parent Context (may be null)
     */
    public ComponentContext(final Map contextData, final Context parent) {
        super( contextData, parent );
    }

    /**
     * Create a Context with specified data.
     *
     * @param contextData the context data
     */
    public ComponentContext(final Map contextData) {
        super( contextData );
    }

    /**
     * Create a Context with specified parent.
     *
     * @param parent the parent Context (may be null)
     */
    public ComponentContext(final Context parent) {
        super( parent );
    }

    /**
     * Create a Context with no parent.
     *
     */
    public ComponentContext() {
        super();
    }

    /**
     * Retrieve an item from the Context.
     *
     * @param key the key of item
     * @return the item stored in context
     * @throws ContextException if item not present
     */
    public Object get( final Object key )
    throws ContextException {
        if ( ContextHelper.CONTEXT_OBJECT_MODEL.equals(key)) {
            final Environment env = CocoonComponentManager.getCurrentEnvironment();
            if ( env == null ) {
                throw new ContextException("Unable to locate " + key + " (No environment available)");
            }
            return env.getObjectModel();
        } else if ( ContextHelper.CONTEXT_SITEMAP_SERVICE_MANAGER.equals(key)) {
            final ComponentManager manager = CocoonComponentManager.getSitemapComponentManager();
            if ( manager == null ) {
                throw new ContextException("Unable to locate " + key + " (No environment available)");
            }
            return new ComponentManagerWrapper(manager);
        }
        if ( key instanceof String ) {
            String stringKey = (String)key;
            if ( stringKey.startsWith(OBJECT_MODEL_KEY_PREFIX) ) {
                final Environment env = CocoonComponentManager.getCurrentEnvironment();
                if ( env == null ) {
                    throw new ContextException("Unable to locate " + key + " (No environment available)");
                }
                final Map objectModel = env.getObjectModel();
                String objectKey = stringKey.substring(OBJECT_MODEL_KEY_PREFIX.length());
                
                Object o = objectModel.get( objectKey );
                if ( o == null ) {
                    final String message = "Unable to locate " + key;
                    throw new ContextException( message );
                }
                return o;
            }
        }
        return super.get( key );
    }

    public static final class ComponentManagerWrapper implements ServiceManager {
        
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
