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

import org.apache.avalon.ComponentManager;
import org.apache.avalon.ComponentSelector;
import org.apache.avalon.Component;
import org.apache.avalon.ComponentNotFoundException;
import org.apache.avalon.ComponentNotAccessibleException;
import org.apache.avalon.SingleThreaded;
import org.apache.avalon.ThreadSafe;
import org.apache.avalon.Poolable;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.Composer;
import org.apache.avalon.ConfigurationException;

import org.apache.cocoon.util.ComponentPool;
import org.apache.cocoon.util.ComponentPoolController;

/** Default component manager for Cocoon's non sitemap components.
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-10-23 19:29:52 $
 */
public class CocoonComponentSelector implements ComponentSelector, Composer {
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

        /** Parent Component Manager */
    private ComponentManager manager;


    /** Construct a new default component manager.
     */
    public CocoonComponentSelector() {
        // Setup the maps.
        components = Collections.synchronizedMap(new HashMap());
        threadSafeInstances = Collections.synchronizedMap(new HashMap());
        configurations = Collections.synchronizedMap(new HashMap());
        pools = Collections.synchronizedMap(new HashMap());
        instances = Collections.synchronizedMap(new HashMap());
    }

    /** Implement Composer interface
     */
    public void compose(ComponentManager manager) {
        if (this.manager == null) {
            this.manager = manager;
        }
    }

    /** Return an instance of a component.
     */
    public Component select( Object hint ) throws
        ComponentNotFoundException, ComponentNotAccessibleException {

        Component component;

        if ( hint == null ) {
            throw new ComponentNotFoundException("Attempted to retrieve component will null roll.");
        }

        // Retrieve the class of the requested component.
        Class componentClass = (Class)this.components.get(hint);

        if ( componentClass == null ) {
            component = (Component)this.instances.get(hint);
            if ( component == null ) {
                throw new ComponentNotFoundException("Could not find component for hint '" + hint.toString() + "'.");
            } else {
                // we found an individual instance of a component.
                return component;
            }
        }

        if ( !Component.class.isAssignableFrom(componentClass) ) {
            throw new ComponentNotAccessibleException(
                "Component with hint '" + hint.toString() + "' (" + componentClass.getName() + ")does not implement Component.",
                null
            );
        }

        // Work out what class of component we're dealing with.
        if ( ThreadSafe.class.isAssignableFrom(componentClass) ) {
            component = getThreadsafeComponent(componentClass);
        } else if ( Poolable.class.isAssignableFrom(componentClass) ) {
            component = getPooledComponent(componentClass);
        } else if ( SingleThreaded.class.isAssignableFrom(componentClass) ) {
            try {
                component = (Component)componentClass.newInstance();
            } catch ( InstantiationException e ) {
                throw new ComponentNotAccessibleException(
                    "Could not instantiate component " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            } catch ( IllegalAccessException e ) {
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
                component = (Component)componentClass.newInstance();
            } catch ( InstantiationException e ) {
                throw new ComponentNotAccessibleException(
                    "Could not instantiate component " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            } catch ( IllegalAccessException e ) {
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
    private Component getThreadsafeComponent(Class componentClass) {
        Component component = (Component)threadSafeInstances.get(componentClass);

        if ( component == null ) {
            try {
                component = (Component)componentClass.newInstance();
            } catch ( InstantiationException e ) {
                throw new ComponentNotAccessibleException(
                    "Failed to instantiate component " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            } catch ( IllegalAccessException e ) {
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
    private Component getPooledComponent(Class componentClass) throws ComponentNotAccessibleException {
        ComponentPool pool = (ComponentPool)pools.get(componentClass);

        if ( pool == null ) {
            try {
                pool = new ComponentPool(
                    new ComponentFactory(componentClass, (Configuration)configurations.get(componentClass), this.manager),
                    new ComponentPoolController()
                    );
            } catch (Exception e) {
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
    private void setupComponent(Component c) throws ComponentNotAccessibleException {
        if ( c instanceof Configurable ) {
            try {
                ((Configurable)c).configure(
                    (Configuration)this.configurations.get(c.getClass())
                );
            } catch (ConfigurationException e) {
                throw new ComponentNotAccessibleException(
                    "Could not configure component " + c.getClass().getName() + ".",
                    e
                );
            }
        }

        if ( c instanceof Composer ) {
            ((Composer)c).compose(this.manager);
        }
    }

    /** Add a new component to the manager.
     * @param hint the hint for the new component.
     * @param component the class of this component.
     * @param Configuration the configuration for this component.
     */
    public void addComponent(Object hint, Class component, Configuration config) {
        this.components.put(hint,component);
        if ( config != null ) {
            this.configurations.put(component,config);
        }
    }

    /** Add a static instance of a component to the manager.
     * @param hint the hint name for the component.
     * @param instance the instance of the component.
     */
    public void addComponentInstance(Object hint, Object instance) {
        this.instances.put(hint,instance);
    }
}
