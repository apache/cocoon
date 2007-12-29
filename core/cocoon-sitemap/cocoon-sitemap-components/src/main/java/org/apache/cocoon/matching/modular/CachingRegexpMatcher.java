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
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.components.modules.input.InputModule;

import org.apache.cocoon.matching.AbstractRegexpMatcher;

import java.util.Map;

/**
 * Matches against a regular expression. Needs an input module to
 * obtain value to match against.
 *
 * <p><b>Global and local configuration</b></p>
 * <table border="1">
 * <tr><td><code>input-module</code></td><td>Name of the input module used to obtain the value</td></tr>
 * <tr><td><code>parameter-name</code></td><td>Name of the parameter to match * against</td></tr>
 * </table>
 *
 * @cocoon.sitemap.component.documentation
 * Matches against a regular expression. Needs an input module to
 * obtain value to match against.
 *
 * @version $Id$
 */
public class CachingRegexpMatcher extends AbstractRegexpMatcher
                                  implements Configurable,  Initializable, Serviceable,
                                             Disposable {

    /** The service manager instance */
    protected ServiceManager manager;

    private String defaultParam;
    private String defaultInput = "request-param"; // default to request parameters
    private Configuration inputConf = null; // will become an empty configuration object
                                            // during configure() so why bother here...
    String INPUT_MODULE_ROLE = InputModule.ROLE;
    String INPUT_MODULE_SELECTOR = INPUT_MODULE_ROLE+"Selector";

    private boolean initialized = false;
    private InputModule input;
    private ServiceSelector inputSelector;

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager=manager;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {

        this.defaultParam = config.getChild("parameter-name").getValue(null);
        this.inputConf = config.getChild("input-module");
        this.defaultInput = this.inputConf.getAttribute("name",this.defaultInput);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() {
        try {
            // obtain input module
            this.inputSelector=(ServiceSelector) this.manager.lookup(INPUT_MODULE_SELECTOR); 
            if (this.defaultInput != null && 
                this.inputSelector != null && 
                this.inputSelector.isSelectable(this.defaultInput)
                ){
                this.input = (InputModule) this.inputSelector.select(this.defaultInput);
                if (!(this.input instanceof ThreadSafe && this.inputSelector instanceof ThreadSafe) ) {
                    this.inputSelector.release(this.input);
                    this.manager.release(this.inputSelector);
                    this.input = null;
                    this.inputSelector = null;
                }
                this.initialized = true;
            } else {
                if (getLogger().isErrorEnabled())
                    getLogger().error("A problem occurred setting up '" + this.defaultInput 
                                      + "': Selector is "+(this.inputSelector!=null?"not ":"")
                                      +"null, Component is "
                                      +(this.inputSelector!=null&&this.inputSelector.isSelectable(this.defaultInput)?"known":"unknown"));
            }
        } catch (Exception e) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("A problem occurred setting up '" + this.defaultInput + "': " + e.getMessage());
        }
    }



    public void dispose() {

        if (!this.initialized) 
            if (getLogger().isErrorEnabled()) 
                getLogger().error("Uninitialized Component! FAILING");
        else 
            if (this.inputSelector != null) {
                if (this.input != null)
                    this.inputSelector.release(this.input);
                this.manager.release(this.inputSelector);
            }
    }



    protected String getMatchString(Map objectModel, Parameters parameters) {

        String paramName = parameters.getParameter("parameter-name", this.defaultParam);
        String inputName = parameters.getParameter("input-module", this.defaultInput);

        if (!this.initialized) {
            if (getLogger().isErrorEnabled()) 
                getLogger().error("Uninitialized Component! FAILING");
            return null;
        }
        if (paramName == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No parameter name given. Trying to Continue");
        }
        if (inputName == null) {
            if (getLogger().isWarnEnabled()) 
                getLogger().warn("No input module given. FAILING");
            return null;
        }

        Object result = null;

        if (this.input != null && inputName.equals(this.defaultInput)) {
            // input module is thread safe
            // thus we still have a reference to it
            try {
                if (this.input != null) {
                    result = this.input.getAttribute(paramName, this.inputConf, objectModel);
                }
            } catch (Exception e) {
                if (getLogger().isWarnEnabled()) 
                    getLogger().warn("A problem occurred acquiring Parameter '" + paramName 
                                      + "' from '" + inputName + "': " + e.getMessage());
            }
        } else {
            // input was not thread safe
            // so acquire it again
            ServiceSelector iputSelector = null;
            InputModule iput = null;
            try {
                // obtain input module
                iputSelector=(ServiceSelector) this.manager.lookup(INPUT_MODULE_SELECTOR); 
                if (iputSelector != null && iputSelector.isSelectable(inputName)) {
                    iput = (InputModule) iputSelector.select(inputName);
                }
                if (iput != null) {
                    result = iput.getAttribute(paramName, this.inputConf, objectModel);
                }
            } catch (Exception e) {
                if (getLogger().isWarnEnabled()) 
                    getLogger().warn("A problem occurred acquiring Parameter '" + paramName 
                                     + "' from '" + inputName + "': " + e.getMessage());
            } finally {
                // release components
                if (iputSelector != null) {
                    if (iput != null)
                        iputSelector.release(iput);
                    this.manager.release(iputSelector);
                }
            }
        }

        if (result instanceof String) {
            return (String) result;
        } else {
            return result.toString();
        }
    }
}
