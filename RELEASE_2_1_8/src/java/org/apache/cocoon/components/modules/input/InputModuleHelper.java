/*
 * Copyright 2004 The Apache Software Foundation.
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

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: InputModuleHelper.java,v 1.2 2004/04/30 23:07:55 joerg Exp $
 */
public class InputModuleHelper {
    
    // TODO consolidate code with AbstractMetaModule to use this class as delegate

    protected final static String INPUT_MODULE_SELECTOR = InputModule.ROLE+"Selector";

    /* Operation codes */
    private final static int OP_GET = 0;
    private final static int OP_VALUES = 1;
    private final static int OP_NAMES = 2;

    
    private Map inputModules;
    private ComponentManager componentManager;
    private ComponentSelector componentInputSelector;
    private ServiceManager serviceManager;
    private ServiceSelector serviceInputSelector;
    
    /**
     * Get the input module
     */
    private InputModule getInputModule(String name)
    throws CascadingRuntimeException {
        if ( this.inputModules == null ) {
            throw new RuntimeException("ModuleHelper is not setup correctly.");
        }
        InputModule module = (InputModule) this.inputModules.get(name);
        if ( module == null ) {
            try {
                if ( this.componentManager != null ) {
                    if (this.componentInputSelector.hasComponent(name)) {
                        module = (InputModule) this.componentInputSelector.select(name);
                    }                
                } else {
                    if (this.serviceInputSelector.isSelectable(name)) {
                        module = (InputModule) this.serviceInputSelector.select(name);
                    }                            
                }
            } catch (Exception e) {
                throw new CascadingRuntimeException("Unable to lookup input module " + name, e);
            }
            if ( module == null ) {
                throw new RuntimeException("No such InputModule: "+name);
            }
            this.inputModules.put(name, module);
        }
        return module;
    }
    
    /**
     * Capsules use of an InputModule. Does all the lookups and so
     * on. Returns either an Object, an Object[], or an Iterator,
     * depending on the method called i.e. the op specified. The
     * second module is preferred and has an non null name. If an
     * exception is encountered, a warn message is printed and null is
     * returned.
     * @param op an <code>int</code> value encoding the desired operation
     * @param name a <code>String</code> value holding the name of the
     * InputModule
     * @param attr a <code>String</code> value holding the name of the
     * attribute to return. Is disregarded when attribute names is
     * requested.
     * @param objectModel a <code>Map</code> value holding the current
     * ObjectModel
     * @return an <code>Object</code> value
     * @exception CascadingRuntimeException if an error occurs. The real
     * exception can be obtained with <code>getCause</code>.
     */
    private Object get(int op, String name, String attr, Map objectModel, Configuration conf) throws CascadingRuntimeException {

        Object value = null;
        final InputModule input = this.getInputModule(name);

        try {

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

        } catch (Exception e) {
            throw new CascadingRuntimeException("Error accessing attribute '"+attr+"' from input module '"+name+"'. "+e.getMessage(), e);
        }

        return value;
    }

    private Object get(int op, String name, String attr, Map objectModel) throws RuntimeException {
        return get(op, name, attr, objectModel, null);
    }



    /**
     * Initializes the instance for first use. Stores references to
     * component manager and component selector in instance 
     *
     * @param manager a <code>ComponentManager</code> value
     * @exception RuntimeException if an error occurs
     * @deprecated Use the {@link #setup(ServiceManager)} method instead
     */
    public void setup(ComponentManager manager) throws RuntimeException {

        this.inputModules = new HashMap();
        this.componentManager = manager;
        try {
            this.componentInputSelector = (ComponentSelector) this.componentManager.lookup(INPUT_MODULE_SELECTOR); 
        } catch (Exception e) {
            throw new CascadingRuntimeException("Could not obtain selector for InputModule.",e);
        }
    }

    /**
     * Initializes the instance for first use. Stores references to
     * service manager and service selector in instance 
     * 
     * @param manager a <code>ServiceManager</code> value
     * @exception RuntimeException if an error occurs
     */
    public void setup(ServiceManager manager) throws RuntimeException {

        this.inputModules = new HashMap();
        this.serviceManager = manager;
        try {
            this.serviceInputSelector = (ServiceSelector) this.serviceManager.lookup(INPUT_MODULE_SELECTOR); 
        } catch (Exception e) {
            throw new CascadingRuntimeException("Could not obtain selector for InputModule.",e);
        }
    }


    /**
     * Get a single attribute value from a module. Uses cached
     * reference if existing.
     *
     * @param objectModel a <code>Map</code> value
     * @param conf a <code>Configuration</code> containing the module dynamic configuration (aka modeConf)
     * @param module a <code>String</code> value holding the module name
     * @param name a <code>String</code> value holding the attribute name
     * @param deflt an <code>Object</code> value holding a default value
     * @return an <code>Object</code> value
     * @exception RuntimeException if an error occurs
     */
    public Object getAttribute(Map objectModel, Configuration conf, String module, String name, Object deflt) throws RuntimeException {

        Object result = this.get(OP_GET, module, name, objectModel, conf);
        if (result == null) result = deflt;
        return result;
    }

    /**
     * Get a single attribute value from a module.  Same as {@link
     * #getAttribute(Map, Configuration, String, String, Object)} with a
     * <code>null</code> configuration.
     */
    public Object getAttribute(Map objectModel, String module, String name, Object deflt) throws RuntimeException {
        return getAttribute(objectModel, null, module, name, deflt);
    }


    /**
     * Get an array of values from a module. Uses cached reference if
     * existing.
     *
     * @param objectModel a <code>Map</code> value
     * @param conf a <code>Configuration</code> containing the module dynamic configuration (aka modeConf)
     * @param module a <code>String</code> value holding the module name
     * @param name a <code>String</code> value holding the attribute name
     * @param deflt an <code>Object[]</code> value holding a default value
     * @return an <code>Object[]</code> value
     * @exception RuntimeException if an error occurs
     */
    public Object[] getAttributeValues(Map objectModel, Configuration conf, String module, String name, Object[] deflt) throws RuntimeException {

        Object[] result = (Object[]) this.get(OP_VALUES, module, name, objectModel, conf);
        if (result == null) result = deflt;
        return result;
    }

    /**
     * Get an array of values from a module. Same as
     * {@link #getAttributeValues(Map, Configuration, String, String, Object[])}
     * with a <code>null</code> configuration.
     */
    public Object[] getAttributeValues(Map objectModel, String module, String name, Object[] deflt) throws RuntimeException {
        return getAttributeValues(objectModel, null, module, name, deflt);
    }


    /**
     * Get an iterator to a collection of attribute names from a
     * module.
     *
     * @param objectModel a <code>Map</code> value
     * @param module the module's name
     * @return an <code>Iterator</code> value
     * @exception RuntimeException if an error occurs
     */
    public Iterator getAttributeNames(Map objectModel, Configuration conf, String module) throws RuntimeException {

        return (Iterator) this.get(OP_NAMES, module, null, objectModel);
    }

    /**  Get an iterator to a collection of attribute names from a module. Same
     * as {@link #getAttributeNames(Map, Configuration, String)} with a
     * <code>null</code> configuration.
     */
    public Iterator getAttributeNames(Map objectModel, String module) throws RuntimeException {
        return getAttributeNames(objectModel, (Configuration)null, module);
    }



    /**
     * Releases all obtained module references.
     *
     * @exception RuntimeException if an error occurs
     */
    public void releaseAll() throws RuntimeException {

        if ( this.inputModules != null ) {
            // test for component manager
            if ( this.componentManager != null ) {
                try {
                    Iterator iter = this.inputModules.keySet().iterator();
                    while (iter.hasNext()) {
                        this.componentInputSelector.release((InputModule) this.inputModules.get(iter.next()));
                    }
                    this.inputModules = null;
                    this.componentManager.release(this.componentInputSelector);
                    this.componentManager = null;
                    this.inputModules = null;
                } catch (Exception e) {
                    throw new CascadingRuntimeException("Could not release InputModules.",e);
                }
                
            }
            if ( this.serviceManager != null ) {
                try {
                    Iterator iter = this.inputModules.keySet().iterator();
                    while (iter.hasNext()) {
                        this.serviceInputSelector.release(this.inputModules.get(iter.next()));
                    }
                    this.inputModules = null;
                    this.serviceManager.release(this.serviceInputSelector);
                    this.serviceManager = null;
                    this.inputModules = null;
                } catch (Exception e) {
                    throw new CascadingRuntimeException("Could not release InputModules.",e);
                }
                
            }
        }
    }
    
}
