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

import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.RoleUtils;
import org.apache.cocoon.Roles;

import org.apache.log.Logger;
import org.apache.avalon.Loggable;

/** Default component manager for Cocoon's non sitemap components.
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-03-16 19:54:03 $
 */
public class CocoonComponentManager implements ComponentManager, Loggable, Configurable, Contextualizable {

    protected Logger log;

    /** The application context for components
     */
    private Context context;

    /** Static component handlers.
     */
    private Map componentMapping;

    /** Static component handlers.
     */
    private Map componentHandlers;

    /** Construct a new default component manager.
     */
    public CocoonComponentManager() {
        // Setup the maps.
        componentHandlers = Collections.synchronizedMap(new HashMap());
        componentMapping = Collections.synchronizedMap(new HashMap());
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

        CocoonComponentHandler handler = null;
        Component component = null;

        if ( role == null ) {
            log.error("CocoonComponentManager Attempted to retrieve component with null role.");
            throw new ComponentManagerException("Attempted to retrieve component with null role.");
        }

        handler = (CocoonComponentHandler) this.componentHandlers.get(role);
        // Retrieve the instance of the requested component
        if ( handler != null ) {
            try {
                component = handler.get();
            } catch (Exception e) {
                throw new ComponentManagerException("Could not access the Component for you", e);
            }
        }

        if (component != null) {
            this.componentMapping.put(component, handler);
            return component;
        }

        Class componentClass = null;
        Configuration config = new DefaultConfiguration("", "-");

        try {
            componentClass = ClassUtils.loadClass(RoleUtils.defaultClass(role));
        } catch (Exception e) {
            log.error("CocoonComponentManager Could not find component for role: " + role, e);
            throw new ComponentManagerException("Could not find component for role: " + role, e);
        }

        try {
            handler = new CocoonComponentHandler(componentClass, config, this, this.context);
            handler.setLogger(this.log);
            handler.init();

            this.componentHandlers.put(role, handler);
            component = handler.get();
            this.componentMapping.put(component, handler);

            return component;
        } catch (Exception e) {
            throw new ComponentManagerException("Could not access the component for role: " + role, e);
        }
    }

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
                log.debug("Adding component (" + role + " = " + className + ")");
                this.addComponent(role,ClassUtils.loadClass(className),e[i]);
            } catch ( Exception ex ) {
                log.error("Could not load class " + className, ex);
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

    public void release(Component component) {
        CocoonComponentHandler handler = (CocoonComponentHandler) this.componentMapping.get(component);
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
            handler.setLogger(this.log);
            handler.init();
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
            handler.setLogger(this.log);
            handler.init();
            this.componentHandlers.put(role, handler);
        } catch (Exception e) {
            this.log.warn("Could not set up Component for role: " + role, e);
        }
    }
}
