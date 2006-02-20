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
package org.apache.cocoon.core.container;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.ComponentInfo;
import org.apache.cocoon.components.Preloadable;
import org.apache.cocoon.core.container.handler.AbstractComponentHandler;
import org.apache.cocoon.core.container.handler.ComponentHandler;
import org.apache.cocoon.util.JMXUtils;

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

    /** The parent selector, if any */
    protected StandaloneServiceSelector parentSelector;

    /** The parent locator, if any */
    protected ServiceManager parentLocator;

    /** The role of this selector. Set in <code>configure()</code>. */
    protected String roleName;

    /** The default key */
    protected String defaultKey;

    /** The application context for components */
    protected Context context;

    /** Static component mapping handlers. */
    protected final Map componentMapping = Collections.synchronizedMap(new HashMap());

    /** Used to map roles to ComponentHandlers. */
    protected final Map componentHandlers = Collections.synchronizedMap(new HashMap());

    /** Is the Manager disposed or not? */
    protected boolean disposed;

    /** Is the Manager initialized? */
    protected boolean initialized;

    /** RoleInfos. */
    protected RoleManager roleManager;

    /** LoggerManager. */
    protected LoggerManager loggerManager;

    protected ComponentEnvironment componentEnv;

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize( final Context context ) {
        this.context = context;
    }

    public void setRoleManager( final RoleManager roles ) {
        this.roleManager = roles;
    }

    /**
     * Configure the LoggerManager.
     */
    public void setLoggerManager( final LoggerManager manager ) {
        this.loggerManager = manager;
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
    protected ComponentHandler getComponentHandler( final String role,
                                                    final Class componentClass,
                                                    final Configuration configuration,
                                                    final ServiceManager serviceManager,
                                                    final ComponentInfo  baseInfo)
    throws Exception {
        if (this.componentEnv == null) {
            this.componentEnv = new ComponentEnvironment(null, getLogger(), this.roleManager,
                    this.loggerManager, this.context, serviceManager);
        }
        // FIXME - we should always get an info here
        ComponentInfo info;
        if ( baseInfo != null ) {
            info = baseInfo.duplicate();
        } else {
            info = new ComponentInfo();
            info.fill(configuration);
            info.setRole(role);
            info.setJmxDomain(JMXUtils.findJmxDomain(info.getJmxDomain(), serviceManager));
            info.setJmxName(JMXUtils.findJmxName(info.getJmxName(), componentClass.getName()));
        }
        info.setConfiguration(configuration);
        info.setServiceClassName(componentClass.getName());

        return AbstractComponentHandler.getComponentHandler(role,
                                                     this.componentEnv,
                                                     info);
    }

    protected void addComponent(String className,
                                String role,
                                Configuration configuration,
                                ComponentInfo info) 
    throws ConfigurationException {
        // check for old excalibur class names - we only test against the selector
        // implementation
        if ( "org.apache.cocoon.components.ExtendedComponentSelector".equals(className)) {
            className = DefaultServiceSelector.class.getName();
        }

        try {
            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( "Adding component (" + role + " = " + className + ")" );
            }
            // FIXME - use different classloader
            final Class clazz = this.getClass().getClassLoader().loadClass( className );
            this.addComponent( role, clazz, configuration, info );
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

        ComponentHandler handler = (ComponentHandler)this.componentHandlers.get( key );

        // Retrieve the instance of the requested component
        if( null == handler ) {
            // Doesn't exist here : try in parent selector
            if ( this.parentSelector != null ) {
                return this.parentSelector.select(key);                
            }
            final String message = this.roleName
                + ": service selector could not find the component for key [" + key + "]";
            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( message );
            }
            throw new ServiceException( key, message );
        }

        Object component = null;

        try {
            component = handler.get();
        } catch( final ServiceException ce ) {
            //rethrow
            throw ce;
        } catch( final Exception e ) {
            final String message = this.roleName
                + ": service selector could not access the component for key [" + key + "]";

            if( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug( message, e );
            }
            throw new ServiceException( key, message, e );
        }

        if( null == component ) {
            // Doesn't exist here : try in parent selector
            if ( this.parentSelector != null ) {
                component = this.parentSelector.select(key);
            } else {
                final String message = this.roleName
                    + ": service selector could not find the component for key [" + key + "]";
                if( this.getLogger().isDebugEnabled() ) {
                    this.getLogger().debug( message );
                }
                throw new ServiceException( key, message );
            }
        }

        this.componentMapping.put( component, handler );
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
            ComponentHandler handler = (ComponentHandler)this.componentHandlers.get( key );
            exists = (handler != null);
        } catch( Throwable t ) {
            // We can safely ignore all exceptions
        }

        if ( !exists && this.parentSelector != null ) {
            exists = this.parentSelector.isSelectable( key );
        }
        return exists;
    }

    /**
     * @see org.apache.avalon.framework.service.ServiceSelector#release(java.lang.Object)
     */
    public void release( final Object component ) {
        if( null == component ) {
            return;
        }

        // Was it selected on the parent ?
        if ( this.parentSelector != null &&
             this.parentSelector.canRelease(component) ) {
            this.parentSelector.release(component);

        } else {
            final ComponentHandler handler =
                (ComponentHandler)this.componentMapping.get( component );

            if( null == handler ) {
                this.getLogger().warn( "Attempted to release a " + component.getClass().getName()
                    + " but its handler could not be located." );
                return;
            }

            // ThreadSafe components will always be using a ThreadSafeComponentHandler,
            //  they will only have a single entry in the m_componentMapping map which
            //  should not be removed until the ComponentLocator is disposed.  All
            //  other components have an entry for each instance which should be
            //  removed.
            if( !handler.isSingleton() ) {
                // Remove the component before calling put.  This is critical to avoid the
                //  problem where another thread calls put on the same component before
                //  remove can be called.
                this.componentMapping.remove( component );
            }

            try {
                handler.put( component );
            } catch( Exception e ) {
                if( this.getLogger().isDebugEnabled() ) {
                    this.getLogger().debug( "Error trying to release component", e );
                }
            }
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
        this.roleName = getRoleName(config);

        // Get default key
        this.defaultKey = config.getAttribute(this.getDefaultKeyAttributeName(), null);

        // Add components
        String compInstanceName = getComponentInstanceName();

        Configuration[] instances = config.getChildren();

        for (int i = 0; i < instances.length; i++) {

            Configuration instance = instances[i];
            ComponentInfo info = null;

            String key = instance.getAttribute("name").trim();

            String classAttr = instance.getAttribute(getClassAttributeName(), null);
            String className;

            if (compInstanceName == null) {
                // component-instance implicitly defined by the presence of the 'class' attribute
                if (classAttr == null) {
                    info = this.roleManager.getDefaultServiceInfoForKey(roleName, instance.getName());
                    className = info.getServiceClassName();
                } else {
                    className = classAttr.trim();
                }

            } else {
                // component-instances names explicitly defined
                if (compInstanceName.equals(instance.getName())) {
                    className = (classAttr == null) ? null : classAttr.trim();
                } else {
                    info = this.roleManager.getDefaultServiceInfoForKey(roleName, instance.getName());
                    className = info.getServiceClassName();
                }
            }

            if (className == null) {
                String message = "Unable to determine class name for component named '" + key +
                    "' at " + instance.getLocation();

                getLogger().error(message);
                throw new ConfigurationException(message);
            }

            this.addComponent( className, key, instance, info );
        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() 
    throws Exception {
        this.initialized = true;

        List keys = new ArrayList( this.componentHandlers.keySet() );

        for( int i = 0; i < keys.size(); i++ ) {
            final Object key = keys.get( i );
            final ComponentHandler handler =
                (ComponentHandler)this.componentHandlers.get( key );

            try {
                handler.initialize();
            } catch( Exception e ) {
                if( this.getLogger().isDebugEnabled() ) {
                    this.getLogger().debug( "Caught an exception trying to initialize "
                        + "of the component handler.", e );
                }
            }

        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        Iterator keys = this.componentHandlers.keySet().iterator();
        List keyList = new ArrayList();

        while( keys.hasNext() ) {
            Object key = keys.next();
            ComponentHandler handler =
                (ComponentHandler)this.componentHandlers.get( key );

            handler.dispose();

            keyList.add( key );
        }

        keys = keyList.iterator();

        while( keys.hasNext() ) {
            this.componentHandlers.remove( keys.next() );
        }

        keyList.clear();

        if ( this.parentLocator != null ) {
            this.parentLocator.release( this.parentSelector );
            this.parentLocator = null;
            this.parentSelector = null;
        }

        this.disposed = true;
    }

    /** Add a new component to the manager.
     * @param key the key for the new component.
     * @param component the class of this component.
     * @param configuration the configuration for this component.
     */
    public void addComponent( final String key,
                              final Class component,
                              final Configuration configuration,
                              final ComponentInfo info)
    throws ServiceException {
        if( this.initialized ) {
            throw new ServiceException( key,
                "Cannot add components to an initialized service selector" );
        }

        try {
            final ComponentHandler handler = getComponentHandler( null,
                                                                  component,
                                                                  configuration,
                                                                  this.serviceManager,
                                                                  info);

            handler.initialize();
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
        return null;
    }

    /**
     * Get the name of the attribute giving the class name of a component.
     * The default here is "class", but this can be overriden in subclasses.
     *
     * @return "<code>class</code>", but can be changed by subclasses
     */
    protected String getClassAttributeName() {
        return "class";
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
        String name = config.getAttribute("role", null);
        if (name == null && this.roleManager != null) {
            name = this.roleManager.getRoleForName(config.getName());
        }

        return name;
    }

    /**
     * Set the ComponentLocatorImpl that allows access to a possible
     * parent of this selector
     * @param locator
     * @throws ServiceException
     */
    void setParentLocator(ServiceManager locator, String role)
    throws ServiceException {
        if (this.parentSelector != null) {
            throw new ServiceException(null, "Parent selector is already set");
        }
        this.parentLocator = locator;

        if (locator != null && locator.hasService(role)) {
            // Get the parent, unwrapping it as far as needed
            Object parent = locator.lookup(role);
            
            if (parent instanceof StandaloneServiceSelector) {
                this.parentSelector = (StandaloneServiceSelector)parent;
            } else {
                throw new IllegalArgumentException("Parent selector is not an extended component selector (" + parent + ")");
            }
        }
    }

    protected boolean canRelease(Object component) {
        if ( this.parentSelector != null &&
             this.parentSelector.canRelease(component) ) {
            return true;
        }
        return this.componentMapping.containsKey( component );
    }

    /**
     * A special factory that sets the RoleManager and LoggerManager after service()
     */
    public static class Factory extends ComponentFactory {

        private final String role;

        public Factory(ComponentEnvironment env, ComponentInfo info, String role) 
        throws Exception {
            super(env, info);
            this.role = role;
        }

        protected void setupObject(Object obj)
        throws Exception {
            final StandaloneServiceSelector component = (StandaloneServiceSelector)obj;

            ContainerUtil.enableLogging(component, this.environment.logger);
            ContainerUtil.contextualize(component, this.environment.context);
            ContainerUtil.service(component, this.environment.serviceManager);

            component.setLoggerManager(this.environment.loggerManager);
            component.setRoleManager(this.environment.roleManager);

            ServiceManager manager = this.environment.serviceManager;
            if (manager instanceof CoreServiceManager) {
                // Can it be something else?
                component.setParentLocator( ((CoreServiceManager)manager).parentManager, this.role);
            }

            ContainerUtil.configure(component, this.serviceInfo.getConfiguration());
            ContainerUtil.initialize(component);
            ContainerUtil.start(component);
        }
    }
}
