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
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * This modules allows to "chain" several other modules. If a module
 * returns "null" as attribute value, the next module in the chain is
 * queried until either a value can be obtained or the end of the
 * chain is reached.
 *
 * <p>A typical example would be to "chain" request parameters,
 * session attributes, and constants in this order. This way, an
 * application could have a default skin that could be overridden by a
 * user in her/his profile stored in the session. In addition, the
 * user could request a different skin through passing a request
 * parameter.</p>
 *
 * <p>Usage:</p>
 *
 * <p> Any number of &lt;input-module/&gt; blocks may appear in the
 * component configuration. The @name attribute is used as the name of
 * the requested input module. The complete &lt;input-module/&gt;
 * block is passed at run-time to the module and thus can contain any
 * configuration data for that particular module.</p>
 *
 * <p>Configuration:</p>
 *
 * <p>It can be controlled whether it returns a flat or a deep view,
 * i.e. whether only values from the first module are returned if
 * non-null or they are merged with values from other modules
 * <code>&lt;all-values&gt;true&lt;/all-values&gt;</code>. The same is
 * possible for the attribute names
 * (<code>&lt;all-names/&gt;</code>). In addition, empty strings could
 * be treated the same as null values
 * (<code>&lt;empty-as-null/&gt;</code>).</p>
 *
 * @version $Id$
 */
public class ChainMetaModule extends AbstractMetaModule implements ThreadSafe {

    private ModuleHolder[] inputs;

    private boolean emptyAsNull = false;
    private boolean allNames = false;
    private boolean allValues = false;

    public ChainMetaModule() {
        this.defaultInput = null;
    }

    /**
     * @see org.apache.cocoon.components.modules.input.AbstractInputModule#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        Configuration[] confs = config.getChildren("input-module");
        if (confs.length > 0) {
            this.inputs = new ModuleHolder[confs.length];
            int j = 0;
            for (int i=0; i<confs.length; i++) {
                ModuleHolder module = new ModuleHolder();
                module.name = confs[i].getAttribute("name",null);
                if (module.name == null) {
                    if (getLogger().isErrorEnabled())
                        getLogger().error("No name attribute for module configuration. Skipping.");
                    continue;
                }
                module.config = confs[i];
                this.inputs[j]=module;
                j++;
            }
        }
        this.emptyAsNull = config.getChild("empty-as-null").getValueAsBoolean(this.emptyAsNull);
        this.allNames = config.getChild("all-names").getValueAsBoolean(this.allNames);
        this.allValues = config.getChild("all-values").getValueAsBoolean(this.allValues);
    }

    /**
     * @see org.apache.cocoon.components.modules.input.AbstractMetaModule#lazy_initialize()
     */
    public synchronized void lazy_initialize() {
        if ( !this.initialized ) {
            super.lazy_initialize();
            // obtain input modules
            for (int i=0; i<this.inputs.length; i++) {
                if (this.inputs[i].name != null) 
                    this.inputs[i].input = obtainModule(this.inputs[i].name);
            }
        }
    }

    /**
     * @see org.apache.cocoon.components.modules.input.AbstractMetaModule#dispose()
     */
    public void dispose() {
        if (this.inputSelector != null) {
            for (int i=0; i<this.inputs.length; i++) {
                this.inputSelector.release(this.inputs[i].input);
            }
            this.inputs = null;
        }
        super.dispose();
    }

    /**
     * @see org.apache.cocoon.components.modules.input.AbstractInputModule#getAttributeValues(java.lang.String, org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Object[] getAttributeValues( String attr, Configuration modeConf, Map objectModel ) 
    throws ConfigurationException {
        if (!this.initialized) {
            this.lazy_initialize();
        }

        // obtain correct configuration objects
        // default vs dynamic
        Configuration[] inputConfigs = null;
        boolean allValues = this.allValues;
        boolean emptyAsNull = this.emptyAsNull;
        if (modeConf!=null && modeConf.getChildren().length > 0) {
            inputConfigs = modeConf.getChildren("input-module");
            emptyAsNull = modeConf.getChild("empty-as-null").getValueAsBoolean(emptyAsNull);
            allValues = modeConf.getChild("all-values").getValueAsBoolean(allValues);
            if (inputConfigs.length == 0) inputConfigs = null;
        }

        Object[] value = null;
        boolean debug = getLogger().isDebugEnabled();
        List values = null;
        if (allValues) values = new ArrayList();

        if (inputConfigs == null) {
            // static configuration branch
            int i = 0;
            while (i < this.inputs.length && (value == null || allValues)) {
                if (this.inputs[i].name != null) {
                    value = getValues(attr, objectModel, this.inputs[i].input, this.inputs[i].name, this.inputs[i].config);
                    if (emptyAsNull && value != null && value.length == 0) value = null;
                    if (emptyAsNull && value != null && value.length == 1 && 
                        value[0] instanceof String && value[0].equals("")) value = null;
                    if (debug) getLogger().debug("read from "+this.inputs[i].name+" attribute "+attr+" as "+value);
                    if (allValues && value != null) values.addAll(Arrays.asList(value));
                }
                i++;
            }
        } else {
            // run-time configuration branch
            int i = 0;
            while (i < inputConfigs.length && (value == null || allValues)) {
                String name = inputConfigs[i].getAttribute("name",null);
                if (name != null) {
                    value = getValues(attr, objectModel, null, name, inputConfigs[i]);
                    if (emptyAsNull && value != null && value.length == 0) value = null;
                    if (emptyAsNull && value != null && value.length == 1 && 
                        value[0] instanceof String && value[0].equals("")) value = null;
                    if (debug) getLogger().debug("read from "+name+" attribute "+attr+" as "+value);
                    if (allValues && value != null) values.addAll(Arrays.asList(value));
                }
                i++;
            }
        }
        if (debug) getLogger().debug("result chaining for "+attr+" is "+(allValues? values.toArray() : value));
        return (allValues? values.toArray() : value);
    }

    private void addIterator(Collection col, Iterator iter) {
        while (iter != null && iter.hasNext())
            col.add(iter.next());
    }

    /**
     * @see org.apache.cocoon.components.modules.input.AbstractInputModule#getAttributeNames(org.apache.avalon.framework.configuration.Configuration, java.util.Map)
     */
    public Iterator getAttributeNames( Configuration modeConf, Map objectModel ) 
    throws ConfigurationException {
        if (!this.initialized) {
            this.lazy_initialize();
        }

        // obtain correct configuration objects
        // default vs dynamic
        Configuration[] inputConfigs = null;
        boolean emptyAsNull = this.emptyAsNull;
        boolean allNames = this.allNames;
        if (modeConf!=null && modeConf.getChildren().length > 0) {
            inputConfigs = modeConf.getChildren("input-module");
            emptyAsNull = modeConf.getChild("empty-as-null").getValueAsBoolean(emptyAsNull);
            allNames = modeConf.getChild("all-names").getValueAsBoolean(allNames);
            if (inputConfigs.length == 0) inputConfigs = null;
        }

        Iterator value = null;
        Collection values = null;
        if (allNames) values = new ArrayList();
        boolean debug = getLogger().isDebugEnabled();

        if (inputConfigs == null) {
            // static configuration branch
            int i = 0;
            while (i < this.inputs.length && (value == null || allNames)) {
                if (this.inputs[i].name != null) {
                    value = getNames(objectModel, this.inputs[i].input, this.inputs[i].name, this.inputs[i].config);
                    if (debug) getLogger().debug("read from "+this.inputs[i].name+" AttributeNames as "+value);
                    if (allNames && value != null) addIterator(values, value);
                }
                i++;
            }
        } else {
            // run-time configuration branch
            int i = 0;
            while (i < inputConfigs.length && value == null) {
                String name = inputConfigs[i].getAttribute("name",null);
                if (name != null) {
                    value = getNames(objectModel, null, name, inputConfigs[i]);
                    if (debug) getLogger().debug("read from "+name+" AttributeNames as "+value);
                    if (allNames && value != null) addIterator(values, value);
                }
                i++;
            }
        }
        if (debug) getLogger().debug("result chaining names is "+(allNames? values.iterator() : value));
        return (allNames? values.iterator() : value);
     }


    public Object getAttribute( String attr, Configuration modeConf, Map objectModel ) 
    throws ConfigurationException {
        Object[] values = this.getAttributeValues(attr,modeConf,objectModel);
        if (getLogger().isDebugEnabled()) getLogger().debug("result chaining single for "+attr+" is "+(values != null? values[0] : "null"));
        return (values != null? values[0] : null);
    }
}
