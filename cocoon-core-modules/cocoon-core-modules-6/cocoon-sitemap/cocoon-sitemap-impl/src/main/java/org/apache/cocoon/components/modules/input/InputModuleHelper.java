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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.service.ServiceManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @version $Id$
 */
public class InputModuleHelper {
    
    // TODO consolidate code with AbstractMetaModule to use this class as delegate

    /* Operation codes */
    private final static int OP_GET = 0;
    private final static int OP_VALUES = 1;
    private final static int OP_NAMES = 2;

    
    private Map inputModules;
    private ServiceManager serviceManager;
    
    /**
     * Get the input module
     */
    private InputModule getInputModule(String name)
    throws InputModuleException {
        if ( this.inputModules == null ) {
            throw new InputModuleInitializationException("ModuleHelper is not setup correctly.");
        }
        InputModule module = (InputModule) this.inputModules.get(name);
        if ( module == null ) {
            final String role = InputModule.ROLE + '/' + name;
            try {
                if (this.serviceManager.hasService(role) ) {
                    module = (InputModule) this.serviceManager.lookup(role);
                }
            } catch (Exception e) {
                throw new InputModuleNotFoundException("Unable to lookup input module " + name, e);
            }
            if ( module == null ) {
                throw new InputModuleNotFoundException("No such InputModule: "+name);
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
     * @throws InputModuleException if an error occurs. The real
     * exception can be obtained with <code>getCause</code>.
     */
    private Object get(int op, String name, String attr, Map objectModel, Configuration conf) throws InputModuleException {

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
            throw new InputModuleAttributeException("Error accessing attribute '"+attr+"' from input module '"+name+"'. "+e.getMessage(), e);
        }

        return value;
    }

    private Object get(int op, String name, String attr, Map objectModel) throws InputModuleException {
        return get(op, name, attr, objectModel, null);
    }



    /**
     * Initializes the instance for first use. Stores references to
     * service manager and service selector in instance 
     * 
     * @param manager a <code>ServiceManager</code> value
     * @throws InputModuleException if an error occurs
     */
    public void setup(ServiceManager manager) throws InputModuleException {
        this.inputModules = new HashMap();
        this.serviceManager = manager;
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
     * @throws InputModuleException if an error occurs
     */
    public Object getAttribute(Map objectModel, Configuration conf, String module, String name, Object deflt) throws InputModuleException {

        Object result = this.get(OP_GET, module, name, objectModel, conf);
        if (result == null) result = deflt;
        return result;
    }

    /**
     * Get a single attribute value from a module.  Same as {@link
     * #getAttribute(Map, Configuration, String, String, Object)} with a
     * <code>null</code> configuration.
     */
    public Object getAttribute(Map objectModel, String module, String name, Object deflt) throws InputModuleException {
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
     * @throws InputModuleException if an error occurs
     */
    public Object[] getAttributeValues(Map objectModel, Configuration conf, String module, String name, Object[] deflt) throws InputModuleException {

        Object[] result = (Object[]) this.get(OP_VALUES, module, name, objectModel, conf);
        if (result == null) result = deflt;
        return result;
    }

    /**
     * Get an array of values from a module. Same as
     * {@link #getAttributeValues(Map, Configuration, String, String, Object[])}
     * with a <code>null</code> configuration.
     */
    public Object[] getAttributeValues(Map objectModel, String module, String name, Object[] deflt) throws InputModuleException {
        return getAttributeValues(objectModel, null, module, name, deflt);
    }

    /**
     * Get an iterator to a collection of attribute names from a
     * module.
     *
     * @param objectModel a <code>Map</code> value
     * @param module the module's name
     * @return an <code>Iterator</code> value
     * @throws InputModuleException if an error occurs
     */
    public Iterator getAttributeNames(Map objectModel, Configuration conf, String module) throws InputModuleException {

        return (Iterator) this.get(OP_NAMES, module, null, objectModel);
    }

    /**  Get an iterator to a collection of attribute names from a module. Same
     * as {@link #getAttributeNames(Map, Configuration, String)} with a
     * <code>null</code> configuration.
     */
    public Iterator getAttributeNames(Map objectModel, String module) throws InputModuleException {
        return getAttributeNames(objectModel, (Configuration)null, module);
    }

    /**
     * Releases all obtained module references.
     *
     * @throws InputModuleException if an error occurs
     */
    public void releaseAll() throws InputModuleException {
        if ( this.inputModules != null ) {
            if ( this.serviceManager != null ) {
                try {
                    final Iterator iter = this.inputModules.keySet().iterator();
                    while (iter.hasNext()) {
                        this.serviceManager.release(this.inputModules.get(iter.next()));
                    }
                    this.serviceManager = null;
                } catch (Exception e) {
                    throw new InputModuleDestructionException("Could not release InputModules.",e);
                }
                
            }
            this.inputModules = null;
        }
    }
    
}
