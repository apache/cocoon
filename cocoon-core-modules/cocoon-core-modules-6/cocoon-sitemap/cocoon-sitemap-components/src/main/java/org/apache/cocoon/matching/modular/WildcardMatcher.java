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
package org.apache.cocoon.matching.modular;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.matching.AbstractWildcardMatcher;

import java.util.Map;

/**
 * Matches against a wildcard expression. Needs an input module to
 * obtain value to match against.
 *
 * <p><b>Global and local configuration</b></p>
 * <table border="1">
 * <tr><td><code>input-module</code></td><td>Name of the input module used to obtain the value</td></tr>
 * <tr><td><code>parameter-name</code></td><td>Name of the parameter to match * against</td></tr>
 * </table>
 *
 * @cocoon.sitemap.component.documentation
 * Matches against a wildcard expression. Needs an input module to
 * obtain value to match against.
 *
 * @version $Id$
 */
public class WildcardMatcher extends AbstractWildcardMatcher
                             implements Configurable, Serviceable {

    /** The service manager instance */
    protected ServiceManager manager;

    private String defaultParam;
    private String defaultInput = "request-param"; // default to request parameters
    private Configuration inputConf = null; // will become an empty configuration object
                                            // during configure() so why bother here...
    String INPUT_MODULE_ROLE = InputModule.ROLE;
    String INPUT_MODULE_SELECTOR = INPUT_MODULE_ROLE+"Selector";

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager=manager;
    }

    public void configure(Configuration config) throws ConfigurationException {
        this.defaultParam = config.getChild("parameter-name").getValue(null);
        this.inputConf = config.getChild("input-module");
        this.defaultInput = this.inputConf.getAttribute("name",this.defaultInput);
    }

    protected String getMatchString(Map objectModel, Parameters parameters) {

        String paramName = parameters.getParameter("parameter-name", this.defaultParam);
        String inputName = parameters.getParameter("input-module", this.defaultInput);

        if (paramName == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No parameter name given. Trying to continue");
        }
        if (inputName == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No input module given. FAILING");
            return null;
        }

        InputModule input = null;
        ServiceSelector inputSelector = null;
        Object result = null;

        // one could test whether the input module is ThreadSafe and
        // keep a reference for that instance. Then one would need
        // to implement Disposable in order to release it at EOL
        // That would probably speed up things a lot. Especially, since
        // matchers are invoked very often.
        // Perhaps a CachingWildcardMatcher ?

        try {
            // obtain input module
            inputSelector=(ServiceSelector) this.manager.lookup(INPUT_MODULE_SELECTOR); 
            if (inputSelector != null && inputSelector.isSelectable(inputName)) {
                input = (InputModule) inputSelector.select(inputName);
            }
            if (input != null) {
                result = input.getAttribute(paramName, this.inputConf, objectModel);
            }
        } catch (Exception e) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("A problem occurred acquiring Parameter '" + paramName 
                                 + "' from '" + inputName + "': " + e.getMessage());
        } finally {
            // release components
            if (inputSelector != null) {
                if (input != null)
                    inputSelector.release(input);
                this.manager.release(inputSelector);
            }
        }

        if (getLogger().isDebugEnabled())
            getLogger().debug(" using "+inputName+" obtained value "+result);

        if (result instanceof String) {
            return (String) result;
        } else {
            return result.toString();
        }
    }
}
