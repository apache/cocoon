/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.cocoon.components.modules.input;

import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * AbstractMetaModule gives you the infrastructure for easily
 * deploying more "meta" InputModules i.e. InputModules that are
 * composed of other InputModules.  In order to get at the Logger, use
 * getLogger().
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @author <a href="mailto:jefft@apache.org">Jeff Turner</a>
 * @version CVS $Id: AbstractMetaModule.java,v 1.9 2004/04/28 18:13:38 haul Exp $
 */
public abstract class AbstractMetaModule extends AbstractInputModule
    implements Composable, Disposable {

    /** The component manager instance */
    protected ComponentManager manager;

    /** The cached InputModule-Selector */
    protected ComponentSelector inputSelector = null;

    /** The cached default InputModule */
    protected InputModule input = null;

    /** The default InputModule name / shorthand. Defaults to 'request-param' */
    protected String defaultInput = "request-param"; // default to request parameters

    /** The default InputModule configuration */
    protected Configuration inputConf = null;  // will become an empty configuration object
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


    /**
     * Set the current <code>ComponentManager</code> instance used by this
     * <code>Composable</code>.
     */
    public void compose(ComponentManager manager) throws ComponentException {

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
                this.inputSelector=(ComponentSelector) this.manager.lookup(INPUT_MODULE_SELECTOR); 
                if (this.inputSelector != null && this.inputSelector instanceof ThreadSafe) {
                    
                    if (this.defaultInput != null) {
                        this.input = obtainModule(this.defaultInput);
                    }
                    
                } else if (!(this.inputSelector instanceof ThreadSafe) ) {
                    this.manager.release(this.inputSelector);
                    this.inputSelector = null;
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
            if (this.input != null)
                this.inputSelector.release(this.input);
            this.manager.release(this.inputSelector);
        }
    }


    /**
     * Obtain a permanent reference to an InputModule.
     */
    protected InputModule obtainModule(String type) {
        ComponentSelector inputSelector = this.inputSelector;
        InputModule module = null;
        try {
            if (inputSelector == null) 
                inputSelector=(ComponentSelector) this.manager.lookup(INPUT_MODULE_SELECTOR); 

            if (inputSelector.hasComponent(type)){
                
                if (type != null && inputSelector.hasComponent(type))
                    module = (InputModule) inputSelector.select(type);
                
                if (!(module instanceof ThreadSafe) ) {
                    inputSelector.release(module);
                    module = null;
                }
            }
            if (type != null && module == null)
                if (getLogger().isWarnEnabled())
                    getLogger().warn("A problem occurred setting up '" + type
                                     +"': Selector is "+(inputSelector!=null?"not ":"")
                                     +"null, Component is "
                                     +(inputSelector!=null && inputSelector.hasComponent(type)?"known":"unknown"));
            
        } catch (ComponentException ce) {
            if (getLogger().isWarnEnabled())
                getLogger().warn("Could not obtain selector for InputModules: "+ce.getMessage());
        } finally {
            if (this.inputSelector == null) 
                this.manager.release(inputSelector);
            // FIXME: Is it OK to keep a reference to the module when we release the selector?
        }

        return module;
    }


    /**
     * release a permanent reference to an InputModule.
     */
    protected void releaseModule(InputModule module) {
        ComponentSelector inputSelector = this.inputSelector;
        if (module != null) {
            try {
                // FIXME: Is it OK to release a module when we have released the selector before?
                if (inputSelector == null) 
                    inputSelector=(ComponentSelector) this.manager.lookup(INPUT_MODULE_SELECTOR); 
                
                inputSelector.release(module);
                module = null;
                
            } catch (ComponentException ce) {
                if (getLogger().isWarnEnabled())
                    getLogger().warn("Could not obtain selector for InputModules: "+ce.getMessage());
            } finally {
                if (this.inputSelector == null) 
                    this.manager.release(inputSelector);
            }
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
     * @param op Operation to perform ({@link OP_GET}, {@link OP_NAMES}, {@link OP_VALUES}).
     *
     * @return Either an Object, an Object[], or an Iterator, depending on <code>op</code> param.
     */ 
    private Object get(int op, String attr, Map objectModel,
                         InputModule staticMod, String staticModName, Configuration staticModConf,
                         InputModule dynamicMod, String dynamicModName, Configuration dynamicModConf)
        throws ConfigurationException {

        ComponentSelector cs = this.inputSelector;
        Object value = null;
        String name = null;
        InputModule input = null;
        Configuration conf = null;
        boolean release = false;

        try {
            if (cs == null) {
                try {
                cs = (ComponentSelector) this.manager.lookup(INPUT_MODULE_SELECTOR);
                } catch (ComponentException e) {
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
                if (cs.hasComponent(name)) {
                    release = true;
                    try {
                    input = (InputModule) cs.select(name);
                    } catch (ComponentException e) {
                        throw new ConfigurationException(
                                "MetaModule unable to create "+
                                (useDynamic ? "dynamically" : "statically")+
                                " specified internal module '"+name+"'", e);
                    }
                } else {
                    if (getLogger().isWarnEnabled())
                        getLogger().warn("No such InputModule: "+name);
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
