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
import org.apache.avalon.SingleThreaded;
import org.apache.avalon.ThreadSafe;
import org.apache.avalon.Poolable;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.Composer;
import org.apache.avalon.ConfigurationException;

import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.ComponentPool;
import org.apache.cocoon.util.ComponentPoolController;
import org.apache.cocoon.CocoonComponentSelector;

import org.apache.log.Logger;
import org.apache.log.LogKit;

/** Default component manager for Cocoon's non sitemap components.
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a>
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2001-01-05 16:20:59 $
 */
public class DefaultComponentManager implements ComponentManager {

    protected Logger log = LogKit.getLoggerFor("cocoon");

    /** Hashmap of all components which this ComponentManager knows about.
     */
    private Map components;

    /** Thread safe instances.
     */
    private Map threadSafeInstances;

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
        threadSafeInstances = Collections.synchronizedMap(new HashMap());
        configurations = Collections.synchronizedMap(new HashMap());
        pools = Collections.synchronizedMap(new HashMap());
        instances = Collections.synchronizedMap(new HashMap());
    }

    /** Return an instance of a component.
     */
    public Component lookup( String role ) throws
        ComponentManagerException {

        Component component;

        if ( role == null ) {
            log.error("Attempted to retrieve a component with a null Role");
            throw new ComponentNotFoundException("Attempted to retrieve component will null roll.");
        }

        // Retrieve the class of the requested component.
        Class componentClass = (Class)this.components.get(role);

        if ( componentClass == null ) {
            component = (Component)this.instances.get(role);
            if ( component == null ) {
                log.error(role + " could not be found");
                throw new ComponentNotFoundException("Could not find component for role '" + role + "'.");
            } else {
                // we found an individual instance of a component.
				log.debug("DefaultComponentManager returned instance for role " + role + ".");
                return component;
            }
        }

        if ( !Component.class.isAssignableFrom(componentClass) ) {
            log.error("The object found is not a Component");
            throw new ComponentNotAccessibleException(
                "Component with role '" + role + "' (" + componentClass.getName() + ")does not implement Component.",
                null
            );
        }

        // Work out what class of component we're dealing with.
        if ( ThreadSafe.class.isAssignableFrom(componentClass) ) {
			log.debug("DefaultComponentManager using threadsafe instance of " + componentClass.getName() + " for role " + role + ".");
            component = getThreadsafeComponent(componentClass);
        } else if ( Poolable.class.isAssignableFrom(componentClass) ) {
            log.debug("DefaultComponentManager using poolable instance of "
                + componentClass.getName() + " for role " + role + "."
            );
            component = getPooledComponent(componentClass);
        } else if ( SingleThreaded.class.isAssignableFrom(componentClass) ) {
            try {
                log.debug("DefaultComponentManager using new instance of single threaded component "
                    + componentClass.getName() + "for role " + role + "."
                );
                component = (Component)componentClass.newInstance();
            } catch ( InstantiationException e ) {
                log.error("Could not create new instance of SingleThreaded " + role, e);
                throw new ComponentNotAccessibleException(
                    "Could not instantiate component " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            } catch ( IllegalAccessException e ) {
                log.error("Could not access class " + componentClass.getName(), e);
                throw new ComponentNotAccessibleException(
                    "Could not access class " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            }
            setupComponent(component);
        } else {
            /* The component doesn't implement any of the Avalon marker
             * classes, treat as normal.
             */
            try {
                log.debug("DefaultComponentManager using new instance of unmarked component "
                    + componentClass.getName() + " for role " + role + "."
                );
                component = (Component)componentClass.newInstance();
            } catch ( InstantiationException e ) {
                log.error("Could not create new instance of class " + componentClass.getName(), e);
                throw new ComponentNotAccessibleException(
                    "Could not instantiate component " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            } catch ( IllegalAccessException e ) {
                log.error("Could not access class " + componentClass.getName(), e);
                throw new ComponentNotAccessibleException(
                    "Could not access class " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            }
            setupComponent(component);
        }

        return component;
    }

    /** Retrieve an instance of a threadsafe component.
     * @param componentClass the class to retrieve an instance of.
     * @return and instance of the component.
     */
    private Component getThreadsafeComponent(Class componentClass)
    throws ComponentManagerException {
        Component component = (Component)threadSafeInstances.get(componentClass);

        if ( component == null ) {
            try {
                component = (Component)componentClass.newInstance();
            } catch ( InstantiationException e ) {
                log.error("Failed to instantiate component " + componentClass.getName(), e);
                throw new ComponentNotAccessibleException(
                    "Failed to instantiate component " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            } catch ( IllegalAccessException e ) {
                log.error("Could not access component " + componentClass.getName(), e);
                throw new ComponentNotAccessibleException(
                    "Could not access component " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            }
            setupComponent(component);
            threadSafeInstances.put(componentClass,component);
        }
        return component;
    }

    /** Return an instance of a component from its associated pool.
     * @param componentClass the class of the component of which we need an instance.
     */
    private Component getPooledComponent(Class componentClass)
    throws ComponentManagerException {
        ComponentPool pool = (ComponentPool)pools.get(componentClass);

        if ( pool == null ) {
            try {
                log.debug("Creating new component pool for " + componentClass.getName() + ".");
                pool = new ComponentPool(
                    new ComponentFactory(componentClass, (Configuration)configurations.get(componentClass), this),
                    new ComponentPoolController()
                    );
            } catch (Exception e) {
                log.error("Could not create pool for component " + componentClass.getName(), e);
                throw new ComponentNotAccessibleException(
                    "Could not create pool for component " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            }
            pools.put(componentClass,pool);
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

    /** Configure a new component.
     * @param c the component to configure.
     */
    private void setupComponent(Component c)
    throws ComponentManagerException {
        if ( c instanceof Configurable ) {
            try {
                ((Configurable)c).configure(
                    (Configuration)this.configurations.get(c.getClass())
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
    public void addComponent(String role, Class component, Configuration config)
    throws ConfigurationException,
           ComponentManagerException {
        if (component.equals(CocoonComponentSelector.class)) {
            CocoonComponentSelector selector = new CocoonComponentSelector();
            Iterator instances = config.getChildren("component-instance");

            selector.compose(this);

            while (instances.hasNext()) {
                Configuration current = (Configuration) instances.next();
                Object hint = current.getAttribute("name");
                String className = (String) current.getAttribute("class");
            try {
                    selector.addComponent(hint, ClassUtils.loadClass(className), current);
                } catch (Exception e) {
                    log.error("The component instance for \"" + hint + "\" has an invalid class name.", e);
                    throw new ConfigurationException("The component instance for '" + hint + "' has an invalid class name.", e);
                }
            }

            this.addComponentInstance(role, selector);
            return;
        }

        this.components.put(role,component);
        if ( config != null ) {
            this.configurations.put(component,config);
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
