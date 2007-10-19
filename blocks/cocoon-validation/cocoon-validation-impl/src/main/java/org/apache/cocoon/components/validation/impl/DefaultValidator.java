/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.util.Iterator;
import java.util.Map;

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
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.components.validation.SchemaParser;
import org.apache.cocoon.util.avalon.CLLoggerWrapper;

/**
 * <p>The default implementation of the {@link org.apache.cocoon.components.validation.Validator}
 * interface provides core management for a number of {@link SchemaParser} instances.</p>
 * 
 * <p>Given the simplicity of this implementation, only {@link SchemaParser}s
 * implementing the {@link ThreadSafe} interface can be managed, and they can be
 * accessed directly (via its name) through the methods specified by the
 * {@link ServiceSelector} interface.</p>
 *
 * <p>That said, normally selection would occur using the methods declared by the
 * {@link AbstractValidator} class and implemented here.</p>
 *
 * @version $Id$
 */
public class DefaultValidator extends AbstractValidator
                              implements ServiceSelector, ThreadSafe, Contextualizable,
                                         Initializable, Disposable, Configurable {

    /** <p>A {@link Map} associating {@link SchemaParser}s with their names.</p> */
    private final Map components = Collections.synchronizedMap(new HashMap());
    /** <p>A {@link Map} associating component names with grammars.</p> */
    private final Map grammars = Collections.synchronizedMap(new HashMap());

    /** <p>The configured {@link Context} instance.</p> */
    private Context context;

    /** <p>The configured {@link Configuration} instance.</p> */
    private Configuration conf;


    /**
     * <p>Create a new {@link DefaultValidator} instance.</p>
     */
    public DefaultValidator() {
        super();
    }

    /**
     * <p>Contextualize this instance.</p>
     */
    public void contextualize(Context context)
    throws ContextException {
        this.context = context;
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
     */
    public void initialize()
    throws Exception {
        getLogger().debug("Initializing " + this.getClass().getName());

        if (this.context == null) throw new IllegalStateException("Null context");
        if (this.manager == null) throw new IllegalStateException("Null manager");
        if (this.conf == null) throw new IllegalStateException("Null configuration");

        Configuration configurations[] = this.conf.getChildren("schema-parser");
        getLogger().debug("Configuring " + configurations.length + " schema parsers"
                          + " from " + this.conf.getLocation());

        /* Iterate through all the sub-confiuration instances */
        for (int x = 0; x < configurations.length; x++) try {
            final Configuration configuration = configurations[x];
            final String className = configuration.getAttribute("class");
            final String selectionKey = configuration.getAttribute("name");
            
            /* Check that we don't have a duplicate schema parser name in configs */
            if (this.components.containsKey(selectionKey)) {
                String message = "Duplicate schema parser \"" + selectionKey + "\"";
                throw new ConfigurationException(message, configuration);
            }

            /* Dump some debugging information, just in case */
            getLogger().debug("Configuring schema parser " + selectionKey + " as "
                              + className + " from " + configuration.getLocation());

            /* Try to load and instantiate the SchemaParser */
            final SchemaParser schemaParser;
            try {
                /* Load the class */
                final Class clazz = Class.forName(className);

                /* ClassCastExceptions normally don't come with messages (darn) */
                if (! SchemaParser.class.isAssignableFrom(clazz)) {
                    String message = "Class " + className + " doesn't implement the "
                                     + SchemaParser.class.getName() + " interface";
                    throw new ConfigurationException(message, configuration);
                }

                /* We only support ThreadSafe SchemaParser instances */
                if (! ThreadSafe.class.isAssignableFrom(clazz)) {
                    String message = "Class " + className + " doesn't implement the "
                                     + ThreadSafe.class.getName() + " interface";
                    throw new ConfigurationException(message, configuration);
                }

                /* Instantiate and set up the new SchemaParser */
                schemaParser = (SchemaParser) clazz.newInstance();
                setupComponent(selectionKey, schemaParser, configuration);

            } catch (ConfigurationException exception) {
                throw exception;
            } catch (Exception exception) {
                String message = "Unable to instantiate SchemaParser " + className;
                throw new ConfigurationException(message, configuration, exception);
            }

            /* Store this instance (and report about it) */
            this.components.put(selectionKey, schemaParser);
            getLogger().debug("SchemaParser \"" + selectionKey + "\" instantiated" +
                              " from class " + className);

            /* Analyze the grammars provided by the current SchemaParser */
            String grammars[] = schemaParser.getSupportedGrammars();
            if (grammars == null) continue;

            /* Iterate through the grammars and store them (default lookup) */
            for (int k = 0; k < grammars.length; k++) {
                if (this.grammars.containsKey(grammars[k])) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("SchemaParser \"" + selectionKey + "\" " +
                                          "supports grammar \"" + grammars[k] +
                                          "\" but is not the default provider");
                    }
                    continue;
                }

                /* Noone yet supports this grammar, make this the default */
                this.grammars.put(grammars[k], selectionKey);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("SchemaParser \"" + selectionKey + "\" is the "
                                      + "default grammar provider for "+grammars[k]);
                }
            }

        } catch (Exception exception) {
            /* Darn, we had an exception instantiating one of the components */
            exception.printStackTrace();
            getLogger().fatal("Exception creating schema parsers", exception);

            /* Dispose all previously stored component instances */
            Iterator iterator = this.components.values().iterator();
            while (iterator.hasNext()) try {
                this.decommissionComponent(iterator.next());
            } catch (Exception nested) {
                getLogger().fatal("Error decommissioning component", nested);
            }

            /* Depending on the exception type, re-throw it or wrap it */
            if (exception instanceof ConfigurationException) {
                throw exception;
            } else {
                Configuration configuration = configurations[x];
                String message = "Unable to setup SchemaParser declared at ";
                message += configuration.getLocation();
                throw new ConfigurationException(message, configuration, exception);
            }
        }
    }

    /**
     * <p>Dispose of this instance.</p>
     * 
     * <p>All sub-components initialized previously will be disposed of when this
     * method is called.</p>
     */
    public void dispose() {
        Iterator iterator = this.components.values().iterator();
        while (iterator.hasNext()) try {
            this.decommissionComponent(iterator.next());
        } catch (Exception exception) {
            getLogger().fatal("Error decommissioning component", exception);
        }
    }

    /* =========================================================================== */
    /* IMPLEMENTATION OF METHODS SPECIFIED BY THE ABSTRACTVALIDATOR CLASS          */
    /* =========================================================================== */

    /**
     * <p>Attempt to acquire a {@link SchemaParser} interface able to understand
     * the grammar language specified.</p>
     * 
     * @param grammar the grammar language that must be understood by the returned
     *                {@link SchemaParser}
     * @return a {@link SchemaParser} instance or <b>null</b> if none was found able
     *         to understand the specified grammar language.
     */
    protected SchemaParser lookupParserByGrammar(String grammar) {
        if (this.grammars.containsKey(grammar)) {
            return this.lookupParserByName((String) this.grammars.get(grammar));
        }
        return null;
    }

    /**
     * <p>Attempt to acquire a {@link SchemaParser} interface associated with the
     * specified instance name.</p>
     * 
     * @param name the name associated with the {@link SchemaParser} to be returned.
     * @return a {@link SchemaParser} instance or <b>null</b> if none was found.
     */
    protected SchemaParser lookupParserByName(String name) {
        if (this.isSelectable(name)) try {
            return (SchemaParser) this.select(name);
        } catch (ServiceException exception) {
            return null;
        }
        return null;
    }

    /**
     * <p>Release a previously acquired {@link SchemaParser} instance back to its
     * original component manager.</p>
     * 
     * <p>This method is supplied in case solid implementations of this class relied
     * on the {@link org.apache.avalon.framework.service.ServiceManager} to manage
     * {@link SchemaParser}s instances.</p>
     * 
     * @param parser the {@link SchemaParser} whose instance is to be released.
     */
    protected void releaseParser(SchemaParser parser) {
        this.release(parser);
    }

    /* =========================================================================== */
    /* IMPLEMENTATION OF THE METHODS SPECIFIED BY THE SERVICESELECTOR INTERFACE    */
    /* =========================================================================== */

    /**
     * <p>Select a subcomponent ({@link SchemaParser}) associated with the specified
     * selection key (its configured &quot;name&quot;).</p>
     */
    public Object select(Object selectionKey)
    throws ServiceException {
        /* Look up for the specified component and return it if found */
        if ( this.components.containsKey(selectionKey)) {
            return this.components.get(selectionKey);
        }

        /* Fail miserably */
        String message = "No component associated with " + selectionKey;
        throw new ServiceException((String) selectionKey, message);
    }

    /**
     * <p>Check whether a subcomponent ({@link SchemaParser}) associated with the
     * specified selection key (its configured &quot;name&quot;) is selectable in
     * this {@link ServiceSelector} instance.</p>
     */
    public boolean isSelectable(Object selectionKey) {
        return this.components.containsKey(selectionKey);
    }

    /**
     * <p>Release a subcomponent ({@link SchemaParser}) instance previously selected
     * from this {@link ServiceSelector} instance.</p>
     */
    public void release(Object component) {
        // We don't need to do anything  in this method.
    }

    /* =========================================================================== */
    /* SUBCOMPONENTS (SCHEMA PARSERS) LIFECYCLE MANAGEMENT METHODS                 */
    /* =========================================================================== */

    /**
     * <p>Manage the instantiation lifecycle of a specified component.</p>
     */
    private Object setupComponent(String name, Object component, Configuration conf)
    throws Exception {
        boolean initialized = false;
        boolean started = false;

        try {
            // All SchemaParsers are now using commons logging. This is legacy support code.
            if (component instanceof LogEnabled) {
                ((LogEnabled) component).enableLogging(new CLLoggerWrapper(getLogger()));
            }
    
            if (component instanceof Contextualizable) {
                ((Contextualizable) component).contextualize(this.context);
            }

            if (component instanceof Serviceable) {
                ((Serviceable) component).service(this.manager);
            }

            if (component instanceof Configurable) {
                ((Configurable) component).configure(conf);
            }

            if (component instanceof Parameterizable)   {
                Parameters parameters = Parameters.fromConfiguration(conf); 
                ((Parameterizable) component).parameterize(parameters);
            }
    
            if (component instanceof Initializable) {
                ((Initializable) component).initialize();
                initialized = true;
            }

            if (component instanceof Startable) {
                ((Startable) component).start();
                started = true;
            }

            return component;

        } catch (Exception exception) {
            if ((started) && (component instanceof Startable)) try {
                ((Startable) component).stop();
            } catch (Exception nested) {
                getLogger().fatal("Error stopping component", nested);
            }
            if ((initialized) && (component instanceof Disposable)) try {
                ((Disposable) component).dispose();
            } catch (Exception nested) {
                getLogger().fatal("Error disposing component", nested);
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