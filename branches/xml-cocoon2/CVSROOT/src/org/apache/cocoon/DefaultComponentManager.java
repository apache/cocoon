/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Iterator;

import org.apache.avalon.ComponentManager;
import org.apache.avalon.Component;
import org.apache.avalon.ComponentManagerException;
import org.apache.avalon.ComponentNotFoundException;
import org.apache.avalon.ComponentNotAccessibleException;
import org.apache.avalon.Context;
import org.apache.avalon.Contextualizable;
import org.apache.avalon.SingleThreaded;
import org.apache.avalon.ThreadSafe;
import org.apache.avalon.Poolable;
import org.apache.avalon.Recyclable;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.Composer;
import org.apache.avalon.ConfigurationException;
import org.apache.avalon.DefaultConfiguration;

import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.RoleUtils;
import org.apache.cocoon.util.ComponentPool;
import org.apache.cocoon.util.ComponentPoolController;
import org.apache.cocoon.CocoonComponentSelector;

import org.apache.log.Logger;
import org.apache.avalon.Loggable;

/** Default component manager for Cocoon's non sitemap components.
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a>
 * @version CVS $Revision: 1.1.2.21 $ $Date: 2001-02-22 17:10:20 $
 */
public class DefaultComponentManager implements ComponentManager, Loggable, Configurable, Contextualizable {

    protected Logger log;

    /** The application context for components
     */
    private Context context;

    /** Hashmap of all components which this ComponentManager knows about.
     */
    private Map components;

    /** Static component instances.
     */
    private Map instances;

    /** Configurations for components.
     */
    private Map configurations;

    /** Component pools. */
    private Map pools;

    /** Construct a new default component manager.
     */
    public DefaultComponentManager() {
        // Setup the maps.
        components = Collections.synchronizedMap(new HashMap());
        configurations = Collections.synchronizedMap(new HashMap());
        pools = Collections.synchronizedMap(new HashMap());
        instances = Collections.synchronizedMap(new HashMap());
    }

    public void setLogger(Logger logger) {
        if (this.log == null) {
            this.log = logger;
        }
    }

    public void contextualize(Context context) {
        if (this.context == null) {
            this.context = context;
        }
    }

    /** Return an instance of a component.
     */
    public Component lookup( String role )
    throws ComponentManagerException {

        Component component;

        if ( role == null ) {
            log.error("DefaultComponentManager Attempted to retrieve component with null role.");
            throw new ComponentNotFoundException("Attempted to retrieve component with null role.");
        }

        // Retrieve the instance of the requested component
        component = (Component) this.instances.get(role);

        if ( component != null ) {
            return component;
        }

        // Retrieve the class of the requested component.
        Class componentClass = (Class)this.components.get(role);

        if (componentClass == null) {
            try {
                componentClass = ClassUtils.loadClass(RoleUtils.defaultClass(role));
            } catch (Exception e) {
                log.error("DefaultComponentManager Could not find component for role '" + role + "'.", e);
                throw new ComponentNotFoundException("Could not find component for role '" + role + "'.", e);
            }

            this.components.put(role, componentClass);

            if (Configurable.class.isAssignableFrom(componentClass)) {
                this.configurations.put(role, new DefaultConfiguration("", "-"));
            }
        }

        if ( !Component.class.isAssignableFrom(componentClass) ) {
            log.error("DefaultComponentManager Component with role '" + role + "' (" + componentClass.getName() + ")does not implement Component.");
            throw new ComponentNotAccessibleException(
                "Component with role '" + role + "' (" + componentClass.getName() + ")does not implement Component.",
                null
            );
        }

        // Work out what class of component we're dealing with.
        if ( ThreadSafe.class.isAssignableFrom(componentClass)) {
            component = getThreadsafeComponent(role, componentClass);
        } else if ( Poolable.class.isAssignableFrom(componentClass) ) {
            component = getPooledComponent(role, componentClass);
        } else if ( SingleThreaded.class.isAssignableFrom(componentClass) ) {
            try {
                component = (Component)componentClass.newInstance();
            } catch ( InstantiationException e ) {
                log.error("DefaultComponentManager Could not access class " + componentClass.getName(), e);
                throw new ComponentNotAccessibleException(
                    "Could not instantiate component " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            } catch ( IllegalAccessException e ) {
                log.error("DefaultComponentManager Could not access class " + componentClass.getName(), e);
                throw new ComponentNotAccessibleException(
                    "Could not access class " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            }
            setupComponent(role, component);
        } else {
            /* The component doesn't implement any of the Avalon marker
             * classes, treat as normal.
             */
            try {
                component = (Component)componentClass.newInstance();
            } catch ( InstantiationException e ) {
                log.error("DefaultComponentManager Could not instantiate component " + componentClass.getName(), e);
                throw new ComponentNotAccessibleException(
                    "Could not instantiate component " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            } catch ( IllegalAccessException e ) {
                log.error("DefaultComponentManager Could not access class " + componentClass.getName(), e);
                throw new ComponentNotAccessibleException(
                    "Could not access class " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            }
            setupComponent(role, component);
        }

        return component;
    }

    public void configure(Configuration conf) throws ConfigurationException {
        // Set components

        Iterator e = conf.getChildren("component");
        while (e.hasNext()) {
            Configuration co = (Configuration) e.next();
            String type = co.getAttribute("type", "");
            String role = co.getAttribute("role", "");
            String className = co.getAttribute("class", "");

            if (! "".equals(type)) {
                role = RoleUtils.lookup(type);
            }

            if ("".equals(className)) {
                className = RoleUtils.defaultClass(role);
            }

            try {
                log.debug("Adding component (" + role + " = " + className + ")");
                this.addComponent(role,ClassUtils.loadClass(className),co);
            } catch ( Exception ex ) {
                log.error("Could not load class " + className, ex);
                throw new ConfigurationException("Could not get class " + className
                    + " for role " + role, ex);
            }
        }

        e = RoleUtils.shorthandNames();
        while (e.hasNext()) {
            Configuration co = conf.getChild((String) e.next());
            if (! co.getLocation().equals("-")) {
                String role = RoleUtils.lookup(co.getName());
                String className = co.getAttribute("class", "");

                if (className.equals("")) {
                    className = RoleUtils.defaultClass(role);
                }

                try {
                    log.debug("Adding component (" + role + " = " + className + ")");
                    this.addComponent(role, ClassUtils.loadClass(className), co);
                } catch ( Exception ex ) {
                    log.error("Could not load class " + className, ex);
                    throw new ConfigurationException("Could not get class " + className
                        + " for role " + role, ex);
                }
            }
        }
    }

    /** Retrieve an instance of a threadsafe component.
     * @param componentClass the class to retrieve an instance of.
     * @return and instance of the component.
     */
    private Component getThreadsafeComponent(String role, Class componentClass)
    throws ComponentManagerException {

        Component retVal;

        try {
            retVal = (Component) componentClass.newInstance();

            this.setupComponent(role, retVal);
            this.instances.put(role, retVal);
        } catch (Exception e) {
            log.error("Could not set up the Component for role: " + role, e);
            throw new ComponentNotAccessibleException("Could not set up the Component for role: " + role, e);
        }

        return retVal;
    }

    /** Return an instance of a component from its associated pool.
     * @param componentClass the class of the component of which we need an instance.
     */
    private Component getPooledComponent(String role, Class componentClass)
    throws ComponentManagerException {
        ComponentPool pool = (ComponentPool)pools.get(componentClass);

        if ( pool == null ) {
            try {
                log.debug("Creating new component pool for " + componentClass.getName() + ".");
                ComponentFactory cf = new ComponentFactory(componentClass, (Configuration)configurations.get(role), this, this.context);
                cf.setLogger(this.log);

                pool = new ComponentPool(cf, new ComponentPoolController());
                pool.setLogger(this.log);
                pool.init();
            } catch (Exception e) {
                log.error("Could not create pool for component " + componentClass.getName(), e);
                throw new ComponentNotAccessibleException(
                    "Could not create pool for component " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            }
            pools.put(componentClass, pool);
        }

        Component component;
        try {
            component = (Component)pool.get();
        } catch ( Exception e ) {
            log.error("Could not retrieve component " + componentClass.getName(), e);
            throw new ComponentNotAccessibleException(
                "Could not retrieve component " + componentClass.getName() + " due to a " +
                e.getClass().getName() + ": " + e.getMessage(),
                e
            );
        }

        return component;
    }

    public void release(Component component) {
        if (component instanceof Poolable) {
            ComponentPool pool = (ComponentPool) pools.get(component.getClass());

            if (pool != null) {
                pool.put((Poolable) component);
            }
        } else if (component instanceof Recyclable) {
            ((Recyclable) component).recycle();
        }
    }

    /** Configure a new component.
     * @param c the component to configure.
     */
    private void setupComponent(String role, Component c)
    throws ComponentManagerException {

        if ( c instanceof Contextualizable ) {
            ((Contextualizable)c).contextualize(this.context);
        }

        if ( c instanceof Loggable ) {
            ((Loggable)c).setLogger(this.log);
        }

        if ( c instanceof Configurable ) {
            try {
                ((Configurable)c).configure(
                    (Configuration)this.configurations.get(role)
                );
            } catch (ConfigurationException e) {
                log.error("Could not configure component " + c.getClass().getName(), e);
                throw new ComponentNotAccessibleException(
                    "Could not configure component " + c.getClass().getName() + ".",
                    e
                );
            }
        }

        if ( c instanceof Composer ) {
            ((Composer)c).compose(this);
        }
    }

    /** Add a new component to the manager.
     * @param role the role name for the new component.
     * @param component the class of this component.
     * @param Configuration the configuration for this component.
     */
    protected void addComponent(String role, Class component, Configuration config)
    throws ConfigurationException,
           ComponentManagerException {

        this.components.put(role,component);
        if ( config != null ) {
            this.configurations.put(role, config);
        }
      }

    /** Add a static instance of a component to the manager.
     * @param role the role name for the component.
     * @param instance the instance of the component.
     */
    public void addComponentInstance(String role, Object instance) {
        this.instances.put(role,instance);
    }
}
