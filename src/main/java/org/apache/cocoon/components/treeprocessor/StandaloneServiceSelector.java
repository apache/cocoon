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
package org.apache.cocoon.components.treeprocessor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.core.container.spring.ComponentInfo;
import org.apache.cocoon.util.ClassUtils;

/**
 * This is the selector used to select/create node builders.
 *
 * @version $Id$
 * @since 2.2
 */
public class StandaloneServiceSelector
    extends AbstractLogEnabled
    implements ServiceSelector,
               Serviceable,
               Configurable,
               Disposable,
               Initializable,
               Contextualizable {

    /** The application context for components
     */
    protected ServiceManager serviceManager;

    /** The role of this selector. Set in <code>configure()</code>. */
    protected String roleName;

    /** The default key */
    protected String defaultKey;

    /** The application context for components */
    protected Context context;

    /** Used to map roles to component infos. */
    protected final Map componentInfos = Collections.synchronizedMap(new HashMap());

    /** All singletons. */
    protected final Map singletons = Collections.synchronizedMap(new HashMap());

    /** Is the Manager disposed or not? */
    protected boolean disposed;

    /** Is the Manager initialized? */
    protected boolean initialized;

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize( final Context context ) {
        this.context = context;
    }

    /**
     * Obtain a new ComponentHandler for the specified component. 
     * 
     * @param role the component's role.
     * @param componentClass Class of the component for which the handle is
     *                       being requested.
     * @param configuration The configuration for this component.
     *
     * @throws Exception If there were any problems obtaining a ComponentHandler
     */
    protected ComponentInfo getComponentInfo( final Class componentClass,
                                              final Configuration configuration)
    throws Exception {
        final ComponentInfo info = new ComponentInfo();
        info.fill(configuration);
        info.setConfiguration(configuration);
        info.setComponentClassName(componentClass.getName());
        if ( ThreadSafe.class.isAssignableFrom( componentClass ) ) {
            info.setModel(ComponentInfo.MODEL_SINGLETON);
        } else {
            info.setModel(ComponentInfo.MODEL_PRIMITIVE);
        }
        return info;
    }

    protected void addComponent(String        role,
                                String        className,
                                Configuration configuration) 
    throws ConfigurationException {
        if( this.initialized ) {
            throw new ConfigurationException( 
                "Cannot add components to an initialized service selector: " + role );
        }
        try {
            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( "Adding component (" + role + " = " + className + ")" );
            }

            final Class clazz = this.getClass().getClassLoader().loadClass( className );

            final ComponentInfo info = getComponentInfo( clazz,
                                                         configuration);

            this.componentInfos.put( role, info );

        } catch( final ClassNotFoundException cnfe ) {
            final String message = "Could not get class (" + className + ") for role "
                                 + role + " at " + configuration.getLocation();

            throw new ConfigurationException( message, cnfe );
        } catch( final ServiceException ce ) {
            final String message = "Cannot setup class "+ className + " for role " + role
                                 + " at " + configuration.getLocation();
            throw new ConfigurationException( message, ce );
        } catch( final Exception e ) {
            final String message = "Unexpected exception when setting up role " + role + " at " + configuration.getLocation();
            throw new ConfigurationException( message, e );
        }        
    }

    /**
     * @see org.apache.avalon.framework.service.ServiceSelector#select(java.lang.Object)
     */
    public Object select( Object hint )
    throws ServiceException {
        final String key;
        if (hint == null) {
            key = this.defaultKey;
        } else {
            key = hint.toString();
        }

        if( !this.initialized ) {
            if( this.getLogger().isWarnEnabled() ) {
                this.getLogger().warn( "Selecting a component on an uninitialized service selector "
                    + "with key [" + key + "]" );
            }
        }

        if( this.disposed ) {
            throw new IllegalStateException(
                "You cannot select a component from a disposed service selector." );
        }

        Object component = this.singletons.get(key);
        if ( component == null ) {
            final ComponentInfo info = (ComponentInfo)this.componentInfos.get( key );

            // Retrieve the instance of the requested component
            if( null == info ) {
                final String message = this.roleName
                   + ": service selector could not find the component for key [" + key + "]";
                throw new ServiceException( key, message );
            }
            try {
                component = this.createComponent(info);
            } catch (ServiceException se) {
                throw se;
            } catch (Exception e) {
                throw new ServiceException(key, "Unable to create new component.", e);
            }
        }

        return component;
    }

    /**
     * @see org.apache.avalon.framework.service.ServiceSelector#isSelectable(java.lang.Object)
     */
    public boolean isSelectable( Object hint ) {
        final String key;
        if (hint == null) {
            key = this.defaultKey;
        } else {
            key = hint.toString();
        }

        if( !this.initialized ) return false;
        if( this.disposed ) return false;

        boolean exists = false;

        try {
            Object component = this.componentInfos.get( key );
            exists = (component != null);
        } catch( Throwable t ) {
            // We can safely ignore all exceptions
        }

        return exists;
    }

    /**
     * @see org.apache.avalon.framework.service.ServiceSelector#release(java.lang.Object)
     */
    public void release( final Object component ) {
        if ( !(component instanceof ThreadSafe) ) {
            ContainerUtil.dispose(component);
        }
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service( final ServiceManager componentManager )
    throws ServiceException {
        this.serviceManager = componentManager;
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure( final Configuration config )
    throws ConfigurationException {
        this.roleName = config.getAttribute("role", null);

        // Get default key
        this.defaultKey = config.getAttribute("default", null);

        final Configuration[] instances = config.getChildren();

        for (int i = 0; i < instances.length; i++) {

            final Configuration instance = instances[i];
            String key = instance.getAttribute("name").trim();

            String classAttr = instance.getAttribute("builder", null);
            String className;

            // component-instances names explicitly defined
            if ("node".equals(instance.getName())) {
                className = (classAttr == null) ? null : classAttr.trim();
            } else {
                className = null;
            }

            if (className == null) {
                String message = "Unable to determine class name for component named '" + key +
                    "' at " + instance.getLocation();

                throw new ConfigurationException(message);
            }

            this.addComponent( key, className, instance );
        }
    }

    /**
     * Create a new component.
     */
    protected Object createComponent(ComponentInfo info)
    throws Exception {
        final Object component = ClassUtils.newInstance(info.getComponentClassName());
        ContainerUtil.enableLogging(component, this.getLogger());
        ContainerUtil.contextualize(component, this.context);
        ContainerUtil.service(component, this.serviceManager);
        ContainerUtil.configure(component, info.getConfiguration());
        if ( component instanceof Parameterizable ) {
            ContainerUtil.parameterize(component, Parameters.fromConfiguration(info.getConfiguration()));
        }
        ContainerUtil.initialize(component);
        return component;
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() 
    throws Exception {
        this.initialized = true;

        final Iterator i = this.componentInfos.entrySet().iterator();
        while ( i.hasNext() ) {
            final Map.Entry entry = (Map.Entry)i.next();
            final ComponentInfo info = (ComponentInfo)entry.getValue();
            if ( info.getModel() == ComponentInfo.MODEL_SINGLETON ) {
                this.singletons.put(entry.getKey(), this.createComponent(info));
            }
        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        final Iterator iter = this.singletons.values().iterator();
        while( iter.hasNext() ) {
            final Object current = iter.next();
            ContainerUtil.dispose(current);
        }
        this.singletons.clear();
        this.componentInfos.clear();
        this.disposed = true;
    }
}
