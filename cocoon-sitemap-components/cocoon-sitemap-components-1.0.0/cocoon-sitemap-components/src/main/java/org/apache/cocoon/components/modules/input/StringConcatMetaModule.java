/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.components.modules.input;

import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

/**
 * <p>StringConcatMetaModule concats strings from configured module chain. It assumes that all modules have string attributes, on the contrary, it calls toString() method.<br>
 * If null is returned by some module {@link RuntimeException} will be thrown.</p>
 * <p>For configuration of input module chain take a look at example on {@link ChainMetaModule}.</p>
 */
public class StringConcatMetaModule extends AbstractMetaModule {

    private ModuleHolder[] inputs;

    public StringConcatMetaModule() {
        this.defaultInput = null;
    }

    /**
     * @param configurations
     *            List of configuration objects for input modules
     * @return List of {@link ModuleHolder} objects obtained from
     *         configurations. If configuration does not have {@code name}
     *         attribute it will be skipped and error logged.
     * @throws ConfigurationException
     */
    protected ModuleHolder[] getFilteredInputModules(Configuration[] configurations) throws ConfigurationException {
        ModuleHolder[] moduleList = new ModuleHolder[configurations.length];
        int j = 0;
        for (int i = 0; i < configurations.length; i++) {
            if (configurations[i].getAttribute("name", null) == null) {
                if (getLogger().isErrorEnabled())
                    getLogger().error("No name attribute for module configuration. Skipping.");
                continue;
            }
            moduleList[j] = new ModuleHolder(configurations[i].getAttribute("name"), configurations[i]);
            j++;
        }
        return moduleList;
    }

    /**
     * @param dynamicConfigurations
     * @return It returns list of modules. If {@code dynamicConfigurations} does
     *         not contain valid list of configuration (e.g. is null) then
     *         static list is returned. In other case list is obtained from
     *         dynamicConfigurations.
     * @throws ConfigurationException
     */
    protected ModuleHolder[] getInputModules(Configuration[] dynamicConfigurations) throws ConfigurationException {
        return (dynamicConfigurations != null && dynamicConfigurations.length != 0) ? getFilteredInputModules(dynamicConfigurations) : inputs;
    }

    /**
     * @see org.apache.cocoon.components.modules.input.AbstractInputModule#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        this.inputs = getFilteredInputModules(config.getChildren("input-module"));
    }

    public Object getAttribute(String name, Configuration modeConf, Map objectModel) throws ConfigurationException {
        ModuleHolder[] inputModules = getInputModules(modeConf != null ? modeConf.getChildren("input-module") : null);
        Object rawValue;
        String value = "";

        for (int i = 0; i < inputModules.length; i++) {
            rawValue = getValue(name, objectModel, inputModules[i]);
            if (rawValue == null)
                throw new RuntimeException("Module " + inputModules[i].name + "returned null as attribute " + name);
            value = value + rawValue;
        }

        return value;
    }
}
