/*
 * Copyright 1999-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.validation.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Startable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.validation.SchemaParser;
import org.apache.cocoon.components.validation.Validator;

/**
 * <p>The default implementation of the {@link Validator} interface.</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class DefaultValidator implements Validator, LogEnabled, ThreadSafe,
Contextualizable, Serviceable, Configurable, Initializable, Disposable {
    
    /** <p>The default shorthand code to use in subcomponent configurations.</p> */
    public static final String DEFAULT_SHORTHAND = "schema-parser";
    /** <p>The default subcomponent {@link Class} instance.</p> */
    public static final Class DEFAULT_CLASS = SchemaParser.class;

    /** <p>The shorthand code to use in subcomponents configurations.</p> */
    private final String shorthand;
    /** <p>The {@link Class} of the subcomponents selected by this instance.</p> */
    private final Class componentClass;
    /** <p>The {@link Set} of all instantiated components.</p> */
    private final Set components;
    /** <p>A {@link Map} associating names with component instances.</p> */
    private final Map selections;

    /** <p>The configured {@link Logger} instance.</p> */
    private Logger logger = null;
    /** <p>The configured {@link Context} instance.</p> */
    private Context context = null;
    /** <p>The configured {@link ServiceManager} instance.</p> */
    private ServiceManager manager = null;
    /** <p>The configured {@link Configuration} instance.</p> */
    private Configuration conf = null;

    /**
     * <p>Create a new {@link DefaultValidator} instance.</p>
     */
    public DefaultValidator() {
        this(null, null);
    }

    /**
     * <p>Create a new {@link DefaultValidator} instance.</p>
     *
     * @param shorthand the shorthand code to use in subcomponents configurations.
     * @param componentClass the {@link Class} of the subcomponents selected by this.
     */
    public DefaultValidator(String shorthand, Class componentClass) {
        this.shorthand = shorthand == null? DEFAULT_SHORTHAND: shorthand;
        this.componentClass = componentClass == null? DEFAULT_CLASS: componentClass;
        this.components = Collections.synchronizedSet(new HashSet());
        this.selections = Collections.synchronizedMap(new HashMap());
    }

    /**
     * <p>Enable logging.</p>
     */
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    /**
     * <p>Contextualize this instance.</p>
     */
    public void contextualize(Context context)
    throws ContextException {
        this.context = context;
    }

    /**
     * <p>Specify the {@link ServiceManager} available to this instance.</p>
     */
    public void service(ServiceManager manager)
    throws ServiceException {
        this.manager = manager;
    }

    /**
     * <p>Configure this instance.</p>
     */
    public void configure(Configuration conf)
    throws ConfigurationException {
        this.conf = conf;
    }

    /**
     * <p>Initialize this instance.</p>
     * 
     * <p>Required components lookup and sub-components initialization will occur
     * when this method is called.</p>
     */
    public void initialize()
    throws Exception {
        this.logger.debug("Initializing " + this.getClass().getName());

        if (this.logger == null) throw new IllegalStateException("Null logger");
        if (this.context == null) throw new IllegalStateException("Null context");
        if (this.manager == null) throw new IllegalStateException("Null manager");
        if (this.conf == null) throw new IllegalStateException("Null configuration");
        
        Configuration configurations[] = this.conf.getChildren(this.shorthand);
        this.logger.debug("Configuring " + configurations.length + " schema parsers"
                          + " from " + this.conf.getLocation());
        for (int x = 0; x < configurations.length; x++) try {
            Configuration configuration = configurations[x];
            String className = configuration.getAttribute("class");
            String selectionKey = configuration.getAttribute("name");
            this.logger.debug("Configuring schema parser " + selectionKey + " as "
                              + className + " from " + configuration.getLocation());

            Class clazz;
            try {
                clazz = Class.forName(className);
            } catch (Exception exception) {
                String message = "Unable to load class " + className;
                throw new ConfigurationException(message, configuration, exception);
            }

            if (!this.componentClass.isAssignableFrom(clazz)) {
                String message = "Class " + className + " does not represent a "
                                 + this.componentClass.getName();
                throw new ConfigurationException(message, configuration);
            }

            Object component;
            try {
                component = clazz.newInstance();
            } catch (Exception exception) {
                String message = "Unable to instantiate SchemaParser " + className;
                throw new ConfigurationException(message, configuration, exception);
            }

            this.components.add(this.setupComponent(component, configuration));
            this.selections.put(selectionKey, component);
            this.logger.debug("SchemaParser " + selectionKey + " class" + className);
            if (component instanceof SchemaParser) {
                SchemaParser parser = (SchemaParser) component;
                String grammars[] = parser.getSupportedGrammars();
                if (grammars != null) {
                    for (int k = 0; k < grammars.length; k++) {
                        if (this.selections.containsKey(grammars[k])) continue;
                        this.selections.put(grammars[k], component);
                        this.logger.debug("SchemaParser " + selectionKey +
                                          "provides grammar " + grammars[k]);
                    }
                }
            }
        } catch (Exception exception) {
            this.logger.warn("Exception creating schema parsers", exception);

            Iterator iterator = this.components.iterator();
            while (iterator.hasNext()) try {
                this.decommissionComponent(iterator.next());
            } catch (Exception nested) {
                this.logger.fatalError("Error decommissioning component", nested);
            }

            if (exception instanceof ConfigurationException) {
                throw exception;
            } else {
                Configuration configuration = configurations[x];
                String message = "Unable to setup SchemaParser declared at ";
                message += configuration.getLocation();
                throw new ConfigurationException(message, configuration, exception);
            }
        }
        this.logger.debug("Configured successfully");
    }

    /**
     * <p>Select the subcomponent managed by this instance associated wit the
     * specified key.</p>
     */
    public Object select(Object key)
    throws ServiceException {
        if (this.isSelectable(key)) return this.selections.get(key);
        throw new ServiceException((String) key, "Schema parser not configured");
    }

    /**
     * <p>Ensure that a subcomponent is selectable for the specified key.</p>
     */
    public boolean isSelectable(Object key) {
        return this.selections.containsKey((String) key);
    }

    /**
     * <p>Release a previously selected subcomponent instance.</p>
     */
    public void release(Object object) {
        // Nothing to do over here...
    }

    /**
     * <p>Dispose of this instance.</p>
     * 
     * <p>All sub-components initialized previously will be disposed of when this
     * method is called.</p>
     */
    public void dispose() {
        Iterator iterator = this.components.iterator();
        while (iterator.hasNext()) try {
            this.decommissionComponent(iterator.next());
        } catch (Exception exception) {
            this.logger.fatalError("Error decommissioning component", exception);
        }
    }
    
    /**
     * <p>Manage the instantiation lifecycle of a specified component.</p>
     */
    private Object setupComponent(Object component, Configuration configuration)
    throws Exception {
        boolean initialized = false;
        boolean started = false;

        try {
            if (component instanceof LogEnabled)
                    ((LogEnabled) component).enableLogging(this.logger);
    
            if (component instanceof Contextualizable)
                    ((Contextualizable) component).contextualize(this.context);
    
            if (component instanceof Serviceable)
                    ((Serviceable) component).service(this.manager);
            
            if (component instanceof Configurable)
                    ((Configurable) component).configure(configuration);
    
            if (component instanceof Parameterizable)
                    ((Parameterizable) component).parameterize(
                            Parameters.fromConfiguration(configuration));
    
            if (component instanceof Initializable)
                    ((Initializable) component).initialize();
            initialized = true;
    
            if (component instanceof Startable)
                ((Startable) component).start();
            started = true;

            return component;

        } catch (Exception exception) {
            if ((started) && (component instanceof Startable)) try {
                ((Startable) component).stop();
            } catch (Exception nested) {
                this.logger.fatalError("Error stopping component", nested);
            }
            if ((initialized) && (component instanceof Disposable)) try {
                ((Disposable) component).dispose();
            } catch (Exception nested) {
                this.logger.fatalError("Error disposing component", nested);
            }
            throw exception;
        }
    }

    /**
     * <p>Manage the distruction lifecycle of a specified component.</p>
     */
    private void decommissionComponent(Object component)
    throws Exception {
        try {
            if (component instanceof Startable) ((Startable) component).stop();
        } finally {
            if (component instanceof Disposable) ((Disposable) component).dispose();
        }
    }
}