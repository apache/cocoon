/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.ComponentManager;
import org.apache.avalon.Component;
import org.apache.avalon.ComponentManagerException;
import org.apache.avalon.Context;
import org.apache.avalon.Contextualizable;
import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.Composer;
import org.apache.avalon.configuration.ConfigurationException;
import org.apache.avalon.configuration.DefaultConfiguration;
import org.apache.avalon.Disposable;
import org.apache.avalon.Initializable;
import org.apache.avalon.AbstractLoggable;

import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.RoleUtils;
import org.apache.cocoon.Roles;

/** Default component manager for Cocoon's non sitemap components.
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a>
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2001-03-19 17:08:37 $
 */
public class CocoonComponentManager extends AbstractLoggable implements ComponentManager, Configurable, Contextualizable, Disposable {

    /** The application context for components
     */
    private Context context;

    /** Static component mapping handlers.
     */
    private Map componentMapping;

    /** Static component handlers.
     */
    private Map componentHandlers;

    /** Is the Manager disposed or not? */
    private boolean disposed = false;

    /** Construct a new default component manager.
     */
    public CocoonComponentManager() {
        // Setup the maps.
        componentHandlers = Collections.synchronizedMap(new HashMap());
        componentMapping = Collections.synchronizedMap(new HashMap());
    }

    /** Set up the Component's Context.
     */
    public void contextualize(Context context) {
        if (this.context == null) {
            this.context = context;
        }
    }

    /** Properly dispose of the Child handlers.
     */
    public synchronized void dispose() {
        this.disposed = true;

        Iterator keys = this.componentHandlers.keySet().iterator();
        List keyList = new ArrayList();

        while (keys.hasNext()) {
            Object key = keys.next();
            CocoonComponentHandler handler = (CocoonComponentHandler)
                this.componentHandlers.get(key);

            handler.dispose();
            keyList.add(key);
        }

        keys = keyList.iterator();

        while (keys.hasNext()) {
            this.componentHandlers.remove(keys.next());
        }

        keyList.clear();
    }

    /**
     * Return an instance of a component based on a Role.  The Role is usually the Interface's
     * Fully Qualified Name(FQN)--unless there are multiple Components for the same Role.  In that
     * case, the Role's FQN is appended with "Selector", and we return a ComponentSelector.
     */
    public Component lookup( String role )
    throws ComponentManagerException {

        if (disposed) throw new IllegalStateException("You cannot lookup components on a disposed ComponentManager");

        CocoonComponentHandler handler = null;
        Component component = null;

        if ( role == null ) {
            getLogger().error("CocoonComponentManager Attempted to retrieve component with null role.");
            throw new ComponentManagerException("Attempted to retrieve component with null role.");
        }

        handler = (CocoonComponentHandler) this.componentHandlers.get(role);
        // Retrieve the instance of the requested component
        if ( handler == null ) {
            getLogger().debug("Could not find ComponentHandler, attempting to create one for role: " + role);
            Class componentClass = null;
            Configuration config = new DefaultConfiguration("", "-");

            try {
                componentClass = ClassUtils.loadClass(RoleUtils.defaultClass(role));

                handler = new CocoonComponentHandler(componentClass, config, this, this.context);
                handler.setLogger(getLogger());
                handler.init();
            } catch (Exception e) {
                getLogger().error("CocoonComponentManager Could not find component for role: " + role, e);
                throw new ComponentManagerException("Could not find component for role: " + role, e);
            }

            this.componentHandlers.put(role, handler);
        }

        try {
            component = handler.get();
        } catch (IllegalStateException ise) {
            handler.init();

            try {
                component = handler.get();
            } catch (Exception ee) {
                throw new ComponentManagerException("Could not access the Component for you", ee);
            }
        } catch (Exception e) {
            throw new ComponentManagerException("Could not access the Component for you", e);
        }

        this.componentMapping.put(component, handler);
        return component;
    }

    /**
     * Configure the ComponentManager.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        // Set components

        Configuration[] e = conf.getChildren("component");
        for (int i = 0; i < e.length; i++) {
            String type = e[i].getAttribute("type", "");
            String role = e[i].getAttribute("role", "");
            String className = e[i].getAttribute("class", "");

            if (! "".equals(type)) {
                role = RoleUtils.lookup(type);
            }

            if ("".equals(className)) {
                className = RoleUtils.defaultClass(role);
            }

            try {
                getLogger().debug("Adding component (" + role + " = " + className + ")");
                this.addComponent(role,ClassUtils.loadClass(className),e[i]);
            } catch ( Exception ex ) {
                getLogger().error("Could not load class " + className, ex);
                throw new ConfigurationException("Could not get class " + className
                    + " for role " + role, ex);
            }
        }

        Iterator r = RoleUtils.shorthandNames();
        while (r.hasNext()) {
            Configuration co = conf.getChild((String) r.next(), false);

            if (co != null) {
                String role = RoleUtils.lookup(co.getName());
                String className = co.getAttribute("class", "");

                if ("".equals(className)) {
                    className = RoleUtils.defaultClass(role);
                }

                try {
                    getLogger().debug("Adding component (" + role + " = " + className + ")");
                    this.addComponent(role, ClassUtils.loadClass(className), co);
                } catch ( Exception ex ) {
                    getLogger().error("Could not load class " + className, ex);
                    throw new ConfigurationException("Could not get class " + className
                        + " for role " + role, ex);
                }
            }
        }
    }

    /**
     * Release a Component.  This implementation makes sure it has a handle on the propper
     * ComponentHandler, and let's the ComponentHandler take care of the actual work.
     */
    public void release(Component component) {
        if (component == null) return;
        CocoonComponentHandler handler = (CocoonComponentHandler) this.componentMapping.get(component);
        if (handler == null) return;
        handler.put(component);
        this.componentMapping.remove(component);
    }

    /** Add a new component to the manager.
     * @param role the role name for the new component.
     * @param component the class of this component.
     * @param Configuration the configuration for this component.
     */
    public void addComponent(String role, Class component, Configuration config)
    throws ComponentManagerException {
        try {
            CocoonComponentHandler handler = new CocoonComponentHandler(component, config, this, this.context);
            handler.setLogger(getLogger());
            this.componentHandlers.put(role, handler);
        } catch (Exception e) {
            throw new ComponentManagerException ("Could not set up Component for role: " + role, e);
        }
    }

    /** Add a static instance of a component to the manager.
     * @param role the role name for the component.
     * @param instance the instance of the component.
     */
    public void addComponentInstance(String role, Object instance) {
        try {
            CocoonComponentHandler handler = new CocoonComponentHandler((Component) instance);
            handler.setLogger(getLogger());
            this.componentHandlers.put(role, handler);
        } catch (Exception e) {
            getLogger().warn("Could not set up Component for role: " + role, e);
        }
    }
}
