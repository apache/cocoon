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
import org.apache.avalon.ComponentSelector;
import org.apache.avalon.Component;
import org.apache.avalon.ComponentManagerException;
import org.apache.avalon.Context;
import org.apache.avalon.Contextualizable;
import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.Composer;
import org.apache.avalon.configuration.ConfigurationException;
import org.apache.avalon.configuration.DefaultConfiguration;
import org.apache.avalon.ThreadSafe;

import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.RoleUtils;
import org.apache.cocoon.Roles;

import org.apache.log.Logger;
import org.apache.avalon.Loggable;

/** Default component manager for Cocoon's non sitemap components.
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-03-16 20:01:50 $
 */
public class CocoonComponentSelector implements Contextualizable, ComponentSelector, Composer, Configurable, ThreadSafe, Loggable {

    protected Logger log;

    /** The application context for components
     */
    protected Context context;

    /** The application context for components
     */
    private ComponentManager manager;

    /** Static component handlers.
     */
    private Map componentMapping;

    /** Static component handlers.
     */
    private Map componentHandlers;

    /** Construct a new default component manager.
     */
    public CocoonComponentSelector() {
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

    public void compose(ComponentManager manager) throws ComponentManagerException {
        if (this.manager == null) {
            this.manager = manager;
        }
    }

    /** Return an instance of a component.
     */
    public Component select( Object hint )
    throws ComponentManagerException {

        CocoonComponentHandler handler = null;
        Component component = null;

        if ( hint == null ) {
            log.error("CocoonComponentManager Attempted to retrieve component with null hint.");
            throw new ComponentManagerException("Attempted to retrieve component with null hint.");
        }

        handler = (CocoonComponentHandler) this.componentHandlers.get(hint);
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

        throw new ComponentManagerException("Could not find the component for hint: " + hint);
    }

    public void configure(Configuration conf) throws ConfigurationException {
        log.debug("CocoonComponentSelector setting up with root element: " + conf.getName());
        Configuration[] instances = conf.getChildren("component-instance");

        for (int i = 0; i < instances.length; i++) {
            Object hint = instances[i].getAttribute("name");
            String className = (String) instances[i].getAttribute("class");

            try {
                this.addComponent(hint, ClassUtils.loadClass(className), instances[i]);
            } catch (Exception e) {
                log.error("CocoonComponentSelector The component instance for \"" + hint + "\" has an invalid class name.", e);
                throw new ConfigurationException("The component instance for '" + hint + "' has an invalid class name.", e);
            }
        }
    }

    public void release(Component component) {
        CocoonComponentHandler handler = (CocoonComponentHandler) this.componentMapping.get(component);
        handler.put(component);
        this.componentMapping.remove(component);
    }

    /** Add a new component to the manager.
     * @param hint the hint name for the new component.
     * @param component the class of this component.
     * @param Configuration the configuration for this component.
     */
    public void addComponent(Object hint, Class component, Configuration config)
    throws ComponentManagerException {
        try {
            CocoonComponentHandler handler = new CocoonComponentHandler(component, config, this.manager, this.context);
            handler.setLogger(this.log);
            handler.init();
            this.componentHandlers.put(hint, handler);
        } catch (Exception e) {
            throw new ComponentManagerException ("Could not set up Component for hint: " + hint, e);
        }
    }

    /** Add a static instance of a component to the manager.
     * @param hint the hint name for the component.
     * @param instance the instance of the component.
     */
    public void addComponentInstance(String hint, Object instance) {
        try {
            CocoonComponentHandler handler = new CocoonComponentHandler((Component) instance);
            handler.setLogger(this.log);
            handler.init();
            this.componentHandlers.put(hint, handler);
        } catch (Exception e) {
            this.log.warn("Could not set up Component for hint: " + hint, e);
        }
    }
}
