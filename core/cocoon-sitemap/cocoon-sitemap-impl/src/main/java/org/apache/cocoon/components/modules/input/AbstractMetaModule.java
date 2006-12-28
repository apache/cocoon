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

package org.apache.cocoon.components.modules.input;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * AbstractMetaModule gives you the infrastructure for easily
 * deploying more "meta" InputModules i.e. InputModules that are
 * composed of other InputModules.  In order to get at the Logger, use
 * getLogger().
 *
 * @version $Id$
 */
public abstract class AbstractMetaModule extends AbstractInputModule
    implements Serviceable {

    /** The service manager instance */
    protected ServiceManager manager;

    /** The cached InputModule-Selector */
    protected ServiceSelector inputSelector;

    /** The cached default InputModule */
    protected InputModule input;

    /** The default InputModule name / shorthand. Defaults to 'request-param' */
    protected String defaultInput = "request-param"; // default to request parameters

    /** The default InputModule configuration */
    protected Configuration inputConf;  // will become an empty configuration object
                                        // during configure() so why bother here...
    
    /** Is this instance initialized? */
    protected boolean initialized = false;

    /* Constants */

    protected final static String INPUT_MODULE_SELECTOR = InputModule.ROLE+"Selector";

    /* Operation codes */
    private final static int OP_GET = 0;
    private final static int OP_VALUES = 1;
    private final static int OP_NAMES = 2;
    private final static String[] OPNAME = new String[] {"GET_VALUE", "GET_VALUES", "GET_NAMES"};


    public void service(ServiceManager manager) throws ServiceException {
        this.manager=manager;
    }

    /**
     * Initialize the meta module with exactly one other input
     * module. Since "meta" modules require references to components
     * of the same role, initialization cannot be done in initialize()
     * when also implementing ThreadSafe since at that point the
     * component selector is not yet initialized it would trigger the
     * creation of a new one resulting in an endless loop of
     * initializations. Therefore, every module needs to call this
     * method when it first requires access to another module if the
     * module itself has not been initialized. Override this method
     * and dispose() to keep references to more than one module.
     */
    public synchronized void lazy_initialize() {
        try {
            // obtain input modules
            if (!this.initialized) {
                this.inputSelector = (ServiceSelector)this.manager.lookup(INPUT_MODULE_SELECTOR); 
                if (this.defaultInput != null) {
                    this.input = obtainModule(this.defaultInput);
                }
                
                this.initialized = true;
            }
        } catch (Exception e) {
            if (getLogger().isWarnEnabled()) 
                getLogger().error("A problem occurred setting up input modules :'" + e.getMessage(), e);
        }
    }


    /**
     * Dispose exactly one cached InputModule. To work on more than
     * one, override this method and initialize().
     */
    public void dispose() {
        if (this.inputSelector != null) {
            this.inputSelector.release(this.input);
            this.input = null;
            this.manager.release(this.inputSelector);
            this.inputSelector = null;
        }
        this.manager = null;
    }

    /**
     * Obtain a permanent reference to an InputModule.
     */
    protected InputModule obtainModule(String type) {
        // check whether the input selector has been looked up yet
        // the contract of this class requries this, but we check anyway
        if ( this.inputSelector == null ) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("A problem occurred setting up '" + type
                                 +"': Selector is null");
            }
            return null;
        }
        InputModule module = null;
        try {
            if ( this.inputSelector.isSelectable(type) ){
                
                module = (InputModule) inputSelector.select(type);
                
                if (!(module instanceof ThreadSafe) ) {
                    if (getLogger().isWarnEnabled()) {
                        getLogger().warn("A problem occurred setting up '" + type
                                        +"': Component is not thread safe.");
                    }
                    this.inputSelector.release(module);
                    module = null;
                }
            } else {
                if (getLogger().isWarnEnabled()) {
                    getLogger().warn("A problem occurred setting up '" + type
                                     +"': Component is unknown.");
                }
            }
            
        } catch (ServiceException ce) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("Could not obtain InputModules: "+ce.getMessage(), ce);
            }
        }

        return module;
    }


    /**
     * release a permanent reference to an InputModule.
     */
    protected void releaseModule(InputModule module) {
        if (module != null) {
            this.inputSelector.release(module);
        }
    }

    /**
     * Get names of available attributes in the specified (usually statically
     * assigned) Input Module.
     * @see InputModule#getAttributeNames(Configuration, Map)
     */
    protected Iterator getNames(Map objectModel, 
                                InputModule staticMod, String staticModName, Configuration staticModConf) 
        throws ConfigurationException {

        return (Iterator) this.get(OP_NAMES, null, objectModel, staticMod, staticModName, staticModConf, null, null, null);
    }

    /**
     * Get names of available attributes in one of the specified Input Modules
     * (static or dynamic, dynamic preferred).  Dynamic IM may be
     * <code>null</code>.
     * @see InputModule#getAttributeNames(Configuration, Map)
     */
     protected Iterator getNames(Map objectModel, 
                                InputModule staticMod, String staticModName, Configuration staticModConf,
                                InputModule dynamicMod, String dynamicModName, Configuration dynamicModConf)
        throws ConfigurationException {

        return (Iterator) this.get(OP_NAMES, null, objectModel, staticMod, staticModName, staticModConf, dynamicMod, dynamicModName, dynamicModConf);
    }

    protected Object getValue(String attr, Map objectModel, ModuleHolder holder) throws ConfigurationException{
        return this.getValue(attr, objectModel, holder.input, holder.name, holder.config);
    }

    protected Object getValue(String attr, Map objectModel, ModuleHolder staticHolder, ModuleHolder dynamicHolder) throws ConfigurationException{
        return this.getValue(attr, objectModel, staticHolder.input, staticHolder.name, dynamicHolder.config);
    }

    protected Object[] getValues(String attr, Map objectModel, ModuleHolder holder) throws ConfigurationException{
        return this.getValues(attr, objectModel, holder.input, holder.name, holder.config);
    }

    protected Object[] getValues(String attr, Map objectModel, ModuleHolder staticHolder, ModuleHolder dynamicHolder) throws ConfigurationException{
        return this.getValues(attr, objectModel, staticHolder.input, staticHolder.name, dynamicHolder.config);
    }

    /**
     * Get an attribute's value from a (usually statically assigned) Input
     * Module.
     * @see InputModule#getAttribute(String, Configuration, Map)
     */
     protected Object getValue(String attr, Map objectModel, 
                              InputModule staticMod, String staticModName, Configuration staticModConf)
        throws ConfigurationException {

        return this.get(OP_GET, attr, objectModel, staticMod, staticModName, staticModConf, null, null, null);
    }


    /**
     * Get attribute's value in one of the specified Input Modules 
     * (static or dynamic, dynamic preferred).  Dynamic IM may be
     * <code>null</code>.
     * @see InputModule#getAttribute(String, Configuration, Map)
     */
     protected Object getValue(String attr, Map objectModel, 
                              InputModule staticMod, String staticModName, Configuration staticModConf,
                              InputModule dynamicMod, String dynamicModName, Configuration dynamicModConf)
        throws ConfigurationException {

        return this.get(OP_GET, attr, objectModel, staticMod, staticModName, staticModConf, dynamicMod, dynamicModName, dynamicModConf);
    }

    /**
     * Get an attribute's values from a (usually statically assigned) Input
     * Module.
     * @see InputModule#getAttributeValues(String, Configuration, Map)
     */
     protected Object[] getValues(String attr, Map objectModel, 
                                 InputModule staticMod, String staticModName, Configuration staticModConf)
        throws ConfigurationException {

        return (Object[]) this.get(OP_VALUES, attr, objectModel, staticMod, staticModName, staticModConf, null, null, null);
    }

    /**
     * Get attribute's values in one of the specified Input Modules 
     * (static or dynamic, dynamic preferred).  Dynamic IM may be
     * <code>null</code>.
     * @see InputModule#getAttributeValues(String, Configuration, Map)
     */
     protected Object[] getValues(String attr, Map objectModel, 
                                 InputModule staticMod, String staticModName, Configuration staticModConf,
                                 InputModule dynamicMod, String dynamicModName, Configuration dynamicModConf)
        throws ConfigurationException {

        return (Object[]) this.get(OP_VALUES, attr, objectModel, staticMod, staticModName, staticModConf, dynamicMod, dynamicModName, dynamicModConf);
    }


    /**
     * Encapsulates use of an InputModule. Does all the lookups and so on.  
     * The second module (dynamic) is preferred if it has an non null name. If
     * an exception is encountered, a warn message is printed and null is
     * returned.
     * @param op Operation to perform ({@link #OP_GET}, {@link #OP_NAMES}, {@link #OP_VALUES}).
     *
     * @return Either an Object, an Object[], or an Iterator, depending on <code>op</code> param.
     */ 
    private Object get(int op, String attr, Map objectModel,
                         InputModule staticMod, String staticModName, Configuration staticModConf,
                         InputModule dynamicMod, String dynamicModName, Configuration dynamicModConf)
    throws ConfigurationException {

        ServiceSelector cs = this.inputSelector;
        Object value = null;
        String name = null;
        InputModule input = null;
        Configuration conf = null;
        boolean release = false;

        try {
            if (cs == null) {
                try {
                cs = (ServiceSelector) this.manager.lookup(INPUT_MODULE_SELECTOR);
                } catch (ServiceException e) {
                    throw new ConfigurationException("Could not find MetaModule's module selector", e);
                }
            }

            boolean useDynamic;
            if (dynamicMod == null && dynamicModName == null) {
                useDynamic = false;
                input = staticMod;
                name = staticModName;
                conf = staticModConf;
            } else {
                useDynamic = true;
                input = dynamicMod;
                name = dynamicModName;
                conf = dynamicModConf;
            }
        
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("MetaModule performing op "+OPNAME[op]+" on " + 
                        (useDynamic?"dynamically":"statically") + " " +
                        (input==null?"created":"assigned") +
                        " module '"+name+"', using config "+dynamicModConf);
            }

            if (input == null) {
                if (cs.isSelectable(name)) {
                    release = true;
                    try {
                        input = (InputModule) cs.select(name);
                    } catch (ServiceException e) {
                        throw new ConfigurationException(
                                "MetaModule unable to create "+
                                (useDynamic ? "dynamically" : "statically")+
                                " specified internal module '"+name+"'", e);
                    }
                } else {
                    throw new ConfigurationException("MetaModule: No such InputModule: "+name);
                }
            }

            switch (op) {
            case OP_GET:    
                value = input.getAttribute(attr, conf, objectModel);
                break;
            case OP_VALUES:
                value = input.getAttributeValues(attr, conf, objectModel);
                break;
            case OP_NAMES:
                value = input.getAttributeNames(conf, objectModel);
                break;
            }

            if (getLogger().isDebugEnabled())
                getLogger().debug("using "+name+" as "+input+" for "+op+" ("+attr+") and "+conf+" gives "+value);
            
        } finally {         
            if (release)
                cs.release(input);

            if (this.inputSelector == null)
                this.manager.release(cs);
        }

        return value;
    }
                              
}
