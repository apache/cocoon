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
import org.apache.avalon.ComponentSelector;
import org.apache.avalon.Component;
import org.apache.avalon.ComponentManagerException;
import org.apache.avalon.ComponentNotFoundException;
import org.apache.avalon.ComponentNotAccessibleException;
import org.apache.avalon.Recyclable;
import org.apache.avalon.Context;
import org.apache.avalon.Contextualizable;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;
import org.apache.avalon.SingleThreaded;
import org.apache.avalon.ThreadSafe;
import org.apache.avalon.Poolable;
import org.apache.avalon.Disposable;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.Composer;
import org.apache.avalon.ConfigurationException;

import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.ComponentPool;
import org.apache.cocoon.util.ComponentPoolController;

import org.apache.log.Logger;
import org.apache.avalon.Loggable;

/** Default component manager for Cocoon's non sitemap components.
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a>
 * @version CVS $Revision: 1.1.2.27 $ $Date: 2001-03-03 23:35:22 $
 */
public class CocoonComponentSelector implements Contextualizable, ComponentSelector, Composer, Configurable, ThreadSafe, Loggable {
    protected Logger log;
    /** Hashmap of all components which this ComponentManager knows about.
     */
    protected Map components;

    /** The app Context */
    protected Context context;

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
        configurations = Collections.synchronizedMap(new HashMap());
        pools = Collections.synchronizedMap(new HashMap());
        instances = Collections.synchronizedMap(new HashMap());
    }

    public void setLogger(Logger logger) {
        if (this.log == null) {
            this.log = logger;
        }
    }

    /** Implement Composer interface
     */
    public void compose(ComponentManager manager)
    throws ComponentManagerException {
        if (this.manager == null) {
            this.manager = manager;
        }
    }

    public void contextualize(Context context) {
      if (this.context == null) {
          this.context = context;
      }
    }

    /** Return an instance of a component.
     */
    public Component select( Object hint )
    throws ComponentManagerException {

        Component component;

        if ( hint == null ) {
            log.error("CocoonComponentSelector Attempted to retrieve component with null hint.");
            throw new ComponentNotFoundException("Attempted to retrieve component with null hint.");
        }

        // Retrieve the instance of the requested component
        component = (Component) this.instances.get(hint);

        if ( component != null ) {
            return component;
        }

        // Retrieve the class of the requested component.
        Class componentClass = (Class)this.components.get(hint);
        if (componentClass == null) {
            log.error("CocoonComponentSelector Could not find component for hint '" + hint.toString() + "'.");
            throw new ComponentNotFoundException("Could not find component for hint '" + hint.toString() + "'.");
        }

        if ( !Component.class.isAssignableFrom(componentClass) ) {
            log.error("CocoonComponentSelector Component with hint '" + hint.toString() + "' (" + componentClass.getName() + ")does not implement Component.");
            throw new ComponentNotAccessibleException(
                "Component with hint '" + hint.toString() + "' (" + componentClass.getName() + ")does not implement Component.",
                null
            );
        }

        // Work out what class of component we're dealing with.
        if ( ThreadSafe.class.isAssignableFrom(componentClass)) {
            component = getThreadsafeComponent(hint, componentClass);
        } else if ( Poolable.class.isAssignableFrom(componentClass) ) {
            component = getPooledComponent(hint, componentClass);
        } else if ( SingleThreaded.class.isAssignableFrom(componentClass) ) {
            try {
                component = (Component)componentClass.newInstance();
            } catch ( InstantiationException e ) {
                log.error("CocoonComponentSelector Could not access class " + componentClass.getName(), e);
                throw new ComponentNotAccessibleException(
                    "Could not instantiate component " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            } catch ( IllegalAccessException e ) {
                log.error("CocoonComponentSelector Could not access class " + componentClass.getName(), e);
                throw new ComponentNotAccessibleException(
                    "Could not access class " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            }
            setupComponent(hint, component);
        } else {
            /* The component doesn't implement any of the Avalon marker
             * classes, treat as normal.
             */
            try {
                component = (Component)componentClass.newInstance();
            } catch ( InstantiationException e ) {
                log.error("CocoonComponentSelector Could not instantiate component " + componentClass.getName(), e);
                throw new ComponentNotAccessibleException(
                    "Could not instantiate component " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            } catch ( IllegalAccessException e ) {
                log.error("CocoonComponentSelector Could not access class " + componentClass.getName(), e);
                throw new ComponentNotAccessibleException(
                    "Could not access class " + componentClass.getName() + ": " + e.getMessage(),
                    e
                );
            }
            setupComponent(hint, component);
        }

        return component;
    }

    private Component getThreadsafeComponent(Object hint, Class component)
    throws ComponentManagerException {

        Component retVal;

        try {
            retVal = (Component) component.newInstance();

            this.setupComponent(hint, retVal);
            this.instances.put(hint, retVal);
        } catch (Exception e) {
            log.error("Could not set up the Component for hint: " + String.valueOf(hint), e);
            throw new ComponentNotAccessibleException("Could not set up the Component for hint: " + String.valueOf(hint), e);
        }

        return retVal;
    }

    public void configure(Configuration conf) throws ConfigurationException {
        log.debug("CocoonComponentSelector setting up with root element: " + conf.getName());
        Iterator instances = conf.getChildren("component-instance");

        while (instances.hasNext()) {
            Configuration current = (Configuration) instances.next();
            Object hint = current.getAttribute("name");
            String className = (String) current.getAttribute("class");

            try {
                this.addComponent(hint, ClassUtils.loadClass(className), current);
            } catch (Exception e) {
                log.error("CocoonComponentSelector The component instance for \"" + hint + "\" has an invalid class name.", e);
                throw new ConfigurationException("The component instance for '" + hint + "' has an invalid class name.", e);
            }
        }
    }

    /** Return an instance of a component from its associated pool.
     * @param componentClass the class of the component of which we need an instance.
     */
    private Component getPooledComponent(Object hint, Class componentClass)
    throws ComponentManagerException {
        ComponentPool pool = (ComponentPool)pools.get(componentClass);

        if ( pool == null ) {
            try {
                log.debug("Creating new pool for:" + componentClass);
                ComponentFactory cf = new ComponentFactory(componentClass, (Configuration)configurations.get(hint), this.manager, this.context);
                cf.setLogger(this.log);

                pool = new ComponentPool(cf);
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
            log.error("Could not retrieve component ", e);
            throw new ComponentNotAccessibleException(
                "Could not retrieve component " + componentClass.getName() + " due to a " +
                e.getClass().getName() + ": " + e.getMessage(),
                e
            );
        }

        return component;
    }

    public void release(Component component) {
        if (
            component instanceof Disposable
            && ! ( component instanceof Poolable )
            && ! ( component instanceof ThreadSafe)
        ) {
            try {
                ((Disposable) component).dispose();
            } catch (Exception e) {
                this.log.warn(
                    "Could not dispose of instance of component " + component.getClass().getName() + ".",
                    e
                );
            }
        }
        
        if (component instanceof Poolable) {
            ComponentPool pool = (ComponentPool) pools.get(component.getClass());

            if (pool != null) {
                pool.put((Poolable) component);
            } else {
                log.debug("Could not find pool for:" + component.getClass());
            }
        }

    }

    /** Configure a new component.
     * @param c the component to configure.
     */
    private void setupComponent(Object hint, Component c)
    throws ComponentManagerException {

        if ( c instanceof Contextualizable ) {
            ((Contextualizable)c).contextualize(this.context);
        }

        if ( c instanceof Loggable ) {
            ((Loggable)c).setLogger(this.log);
        }

        if ( c instanceof Composer ) {
            ((Composer)c).compose(this.manager);
        }

        if ( c instanceof Configurable ) {
            try {
                ((Configurable)c).configure(
                    (Configuration)this.configurations.get(hint)
                );
            } catch (ConfigurationException e) {
                log.error("CocoonComponentSelector Could not configure component " + c.getClass().getName(), e);
                throw new ComponentNotAccessibleException(
                    "Could not configure component " + c.getClass().getName() + ".",
                    e
                );
            }
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
            this.configurations.put(hint, config);
        }
    }

    /** Add a static instance of a component to the manager.
     * @param hint the hint name for the component.
     * @param instance the instance of the component.
     */
    public void addComponentInstance(Object hint, Component instance) {
        this.instances.put(hint,instance);
    }
}
