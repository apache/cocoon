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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import org.apache.cocoon.components.Preloadable;
import org.apache.cocoon.core.container.spring.ComponentInfo;
import org.apache.cocoon.util.ClassUtils;

/**
 * Default component selector for Cocoon's components.
 *
 * @version $Id$
 * @since 2.2
 */
public class StandaloneServiceSelector
    extends AbstractLogEnabled
    implements Preloadable,
               ServiceSelector,
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

    /** Used to map roles to ComponentHandlers. */
    protected final Map componentHandlers = Collections.synchronizedMap(new HashMap());

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
     * @param serviceManager The service manager which will be managing the Component.
     *
     * @throws Exception If there were any problems obtaining a ComponentHandler
     */
    protected ComponentInfo getComponentHandler( final String role,
                                                    final Class componentClass,
                                                    final Configuration configuration,
                                                    final ServiceManager serviceManager)
    throws Exception {
        ComponentInfo info;
        info = new ComponentInfo();
        info.fill(configuration);
        info.setRole(role);
        info.setConfiguration(configuration);
        info.setComponentClassName(componentClass.getName());
        info.setModel(ComponentInfo.MODEL_SINGLETON);
        return info;
    }

    protected void addComponent(String className,
                                String role,
                                Configuration configuration) 
    throws ConfigurationException {
        try {
            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( "Adding component (" + role + " = " + className + ")" );
            }
            // FIXME - use different classloader
            final Class clazz = this.getClass().getClassLoader().loadClass( className );
            this.addComponent( role, clazz, configuration );
        } catch( final ClassNotFoundException cnfe ) {
            final String message = "Could not get class (" + className + ") for role "
                                 + role + " at " + configuration.getLocation();

            if( this.getLogger().isErrorEnabled() ) {
                this.getLogger().error( message, cnfe );
            }

            throw new ConfigurationException( message, cnfe );
        } catch( final ServiceException ce ) {
            final String message = "Cannot setup class "+ className + " for role " + role
                                 + " at " + configuration.getLocation();

            if( this.getLogger().isErrorEnabled() ) {
                this.getLogger().error( message, ce );
            }

            throw new ConfigurationException( message, ce );
        } catch( final Exception e ) {
            final String message = "Unexpected exception when setting up role " + role + " at " + configuration.getLocation();
            if( this.getLogger().isErrorEnabled() ) {
                this.getLogger().error( message, e );
            }
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

        Object component = this.componentHandlers.get( key );

        // Retrieve the instance of the requested component
        if( null == component ) {
            final String message = this.roleName
                + ": service selector could not find the component for key [" + key + "]";
            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( message );
            }
            throw new ServiceException( key, message );
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
            Object component = this.componentHandlers.get( key );
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
        // nothing to do as we only serve singletons
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
        this.roleName = getRoleName(config);

        // Get default key
        this.defaultKey = config.getAttribute(this.getDefaultKeyAttributeName(), null);

        // Add components
        String compInstanceName = getComponentInstanceName();

        Configuration[] instances = config.getChildren();

        for (int i = 0; i < instances.length; i++) {

            Configuration instance = instances[i];
            String key = instance.getAttribute("name").trim();

            String classAttr = instance.getAttribute(getClassAttributeName(), null);
            String className;

            // component-instances names explicitly defined
            if (compInstanceName.equals(instance.getName())) {
                className = (classAttr == null) ? null : classAttr.trim();
            } else {
                className = null;
            }

            if (className == null) {
                String message = "Unable to determine class name for component named '" + key +
                    "' at " + instance.getLocation();

                getLogger().error(message);
                throw new ConfigurationException(message);
            }

            this.addComponent( className, key, instance );
        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() 
    throws Exception {
        this.initialized = true;

        List keys = new ArrayList( this.componentHandlers.keySet() );
        final Map components = new HashMap();

        for( int i = 0; i < keys.size(); i++ ) {
            final Object key = keys.get( i );
            final ComponentInfo handler =
                (ComponentInfo)this.componentHandlers.get( key );

            try {
                Object component = ClassUtils.newInstance(handler.getComponentClassName());
                ContainerUtil.enableLogging(component, this.getLogger());
                ContainerUtil.contextualize(component, this.context);
                ContainerUtil.service(component, this.serviceManager);
                ContainerUtil.configure(component, handler.getConfiguration());
                if ( component instanceof Parameterizable ) {
                    ContainerUtil.parameterize(component, Parameters.fromConfiguration(handler.getConfiguration()));
                }
                ContainerUtil.initialize(component);
                components.put(key, component);
            } catch( Exception e ) {
                if( this.getLogger().isDebugEnabled() ) {
                    this.getLogger().debug( "Caught an exception trying to initialize "
                        + "of the component handler.", e );
                }
            }
        }
        this.componentHandlers.clear();
        this.componentHandlers.putAll(components);
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        Iterator iter = this.componentHandlers.values().iterator();
        while( iter.hasNext() ) {
            final Object current = iter.next();
            ContainerUtil.dispose(current);
        }
        this.componentHandlers.clear();
        this.disposed = true;
    }

    /** Add a new component to the manager.
     * @param key the key for the new component.
     * @param component the class of this component.
     * @param configuration the configuration for this component.
     */
    public void addComponent( final String key,
                              final Class component,
                              final Configuration configuration)
    throws ServiceException {
        if( this.initialized ) {
            throw new ServiceException( key,
                "Cannot add components to an initialized service selector" );
        }

        try {
            final ComponentInfo handler = getComponentHandler( null,
                                                               component,
                                                               configuration,
                                                               this.serviceManager);

            this.componentHandlers.put( key, handler );

            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug(
                    "Adding " + component.getName() + " for key [" + key + "]" );
            }
        } catch (ServiceException se) {
            throw se;
        } catch( final Exception e ) {
            final String message =
                "Could not set up component for key [ " + key + "]";
            if( this.getLogger().isErrorEnabled() ) {
                this.getLogger().error( message, e );
            }

            throw new ServiceException(key, message, e );
        }
    }

    /**
     * Get the name for component-instance elements (i.e. components not defined
     * by their role shortcut. If <code>null</code>, any element having a 'class'
     * attribute will be considered as a component instance.
     * <p>
     * The default here is to return <code>null</code>, and subclasses can redefine
     * this method to return particular values.
     *
     * @return <code>null</code>, but can be changed by subclasses
     */
    protected String getComponentInstanceName() {
        return "node";
    }

    /**
     * Get the name of the attribute giving the class name of a component.
     * The default here is "class", but this can be overriden in subclasses.
     *
     * @return "<code>class</code>", but can be changed by subclasses
     */
    protected String getClassAttributeName() {
        return "builder";
    }

    /**
     * Get the name of the attribute giving the default key to use if
     * none is given. The default here is "default", but this can be
     * overriden in subclasses. If this method returns <code>null</code>,
     * no default key can be specified.
     *
     * @return "<code>default</code>", but can be changed by subclasses
     */
    protected String getDefaultKeyAttributeName() {
        return "default";
    }

    /**
     * Get the role name for this selector. This is called by <code>configure()</code>
     * to set the value of <code>this.roleName</code>.
     *
     * @return the role name, or <code>null<code> if it couldn't be determined.
     */
    protected String getRoleName(Configuration config) {
        // Get the role for this selector
        return config.getAttribute("role", null);
    }
}
