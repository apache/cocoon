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
import org.apache.avalon.AbstractLoggable;
import org.apache.avalon.Disposable;
import org.apache.avalon.ThreadSafe;

import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.RoleUtils;
import org.apache.cocoon.Roles;

/** Default component manager for Cocoon's non sitemap components.
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a>
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2001-03-19 19:55:52 $
 */
public class CocoonComponentSelector extends AbstractLoggable implements Contextualizable, ComponentSelector, Composer, Configurable, ThreadSafe, Disposable {

    /** The application context for components
     */
    protected Context context;

    /** The application context for components
     */
    private ComponentManager manager;

    /** Dynamic component handlers mapping.
     */
    private Map componentMapping;

    /** Static configuraiton object.
     */
    private Configuration conf = null;

    /** Static component handlers.
     */
    private Map componentHandlers;

    private boolean disposed = false;

    /** Construct a new default component manager.
     */
    public CocoonComponentSelector() {
        // Setup the maps.
        componentHandlers = Collections.synchronizedMap(new HashMap());
        componentMapping = Collections.synchronizedMap(new HashMap());
    }

    /** Provide the application Context.
     */
    public void contextualize(Context context) {
        if (this.context == null) {
            this.context = context;
        }
    }

    /** Compose the ComponentSelector so that we know what the parent ComponentManager is.
     */
    public void compose(ComponentManager manager) throws ComponentManagerException {
        if (this.manager == null) {
            this.manager = manager;
        }
    }

    /**
     * Properly dispose of all the ComponentHandlers.
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
     * Return an instance of a component based on a hint.  The Composer has already selected the
     * role, so the only part left it to make sure the Component is handled.
     */
    public Component select( Object hint )
    throws ComponentManagerException {

        if (disposed) throw new IllegalStateException("You cannot select a Component from a disposed ComponentSelector");

        CocoonComponentHandler handler = null;
        Component component = null;

        if ( hint == null ) {
            getLogger().error(this.getName() + ": CocoonComponentSelector Attempted to retrieve component with null hint.");
            throw new ComponentManagerException("Attempted to retrieve component with null hint.");
        }

        handler = (CocoonComponentHandler) this.componentHandlers.get(hint);
        // Retrieve the instance of the requested component
        if ( handler == null ) {
            throw new ComponentManagerException(this.getName() + ": CocoonComponentSelector could not find the component for hint: " + hint);
        }

        try {
            component = handler.get();
        } catch (Exception e) {
            throw new ComponentManagerException(this.getName() + ": CocoonComponentSelector could not access the Component for you", e);
        }

        if (component == null) {
            throw new ComponentManagerException(this.getName() + ": CocoonComponentSelector could not find the component for hint: " + hint);
        }

        this.componentMapping.put(component, handler);
        return component;
    }

    /**
     * Default Configuration handler for ComponentSelector.
     */
    public void configure(Configuration conf) throws ConfigurationException {
        this.conf = conf;
        getLogger().debug("CocoonComponentSelector setting up with root element: " + conf.getName());
        Configuration[] instances = conf.getChildren("component-instance");

        for (int i = 0; i < instances.length; i++) {
            Object hint = instances[i].getAttribute("name").trim();
            String className = (String) instances[i].getAttribute("class").trim();

            try {
                this.addComponent(hint, ClassUtils.loadClass(className), instances[i]);
            } catch (Exception e) {
                getLogger().error("CocoonComponentSelector The component instance for \"" + hint + "\" has an invalid class name.", e);
                throw new ConfigurationException("The component instance for '" + hint + "' has an invalid class name.", e);
            }
        }
    }

    /**
     * Release the Component to the propper ComponentHandler.
     */
    public void release(Component component) {
        if (component == null) return;
        CocoonComponentHandler handler = (CocoonComponentHandler) this.componentMapping.get(component);
        if (handler == null) return;
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
            handler.setLogger(getLogger());
            handler.init();
            this.componentHandlers.put(hint, handler);
            getLogger().debug("Adding " + component.getName() + " for " + hint.toString());
        } catch (Exception e) {
            getLogger().error("Could not set up Component for hint: " + hint, e);
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
            handler.setLogger(getLogger());
            handler.init();
            this.componentHandlers.put(hint, handler);
            getLogger().debug("Adding " + instance.getClass().getName() + " for " + hint.toString());
        } catch (Exception e) {
            getLogger().error("Could not set up Component for hint: " + hint, e);
        }
    }

    private static final String DEFAULT_NAME = "UnnamedSelector";

    /**
     * Return this selector's configuration name or a default name if no such
     * configuration was provided. This accounts for the case when a static
     * component instance has been added through
     * <code>addComponentInstance</code> with no associated configuration
     */
   private String getName() {
     if (this.conf != null) {
       return this.conf.getName();
     }

     return DEFAULT_NAME;
   }
}
