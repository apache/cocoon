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

package org.apache.cocoon.acting.modular;

import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.acting.ServiceableAction;
import org.apache.cocoon.components.modules.input.InputModule;
import org.apache.cocoon.components.modules.output.OutputModule;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;

import java.util.Iterator;
import java.util.Map;

/** Demo action that uses componentized input / output layer. In order
 * to stop combinatorial explosion of actions, matchers, and selectors
 * they should instead use components to access their inputs and
 * outputs. Available components include request parameters,
 * attributes, headers, and session attributes. Which component to use
 * can be specified upon setup via "input-module" and
 * "output-module" tags through the name attribute.
 *
 * This particular action copies all available parameters obtained
 * from the input component to the output component or, if a specific
 * parameter is specified through parameter-name, just one parameter.
 *
 * @version CVS $Id: TestAction.java,v 1.4 2004/05/24 12:42:44 cziegeler Exp $
 */
public class TestAction extends ServiceableAction 
    implements Configurable, ThreadSafe {

    String INPUT_MODULE_ROLE = InputModule.ROLE;
    String OUTPUT_MODULE_ROLE = OutputModule.ROLE;
    String INPUT_MODULE_SELECTOR = INPUT_MODULE_ROLE+"Selector";
    String OUTPUT_MODULE_SELECTOR = OUTPUT_MODULE_ROLE+"Selector";

    Configuration inputConf = null;
    Configuration outputConf = null;
    String inputName = null;
    String outputName = null;
    String defaultParameterName = null;
    boolean useGetValues = false;

    String inputHint = "request-param"; // default to request parameters
    String outputHint = "request-attr"; // default to request attributes


    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {

        this.inputConf  = config.getChild("input-module");
        this.inputName  = this.inputConf.getAttribute("name", this.inputHint);
        this.outputConf = config.getChild("output-module");
        this.outputName = this.outputConf.getAttribute("name", this.outputHint);
        this.defaultParameterName = config.getChild("parameter-name").getValue(null);
        this.useGetValues = config.getChild("use-getValues").getValueAsBoolean(this.useGetValues);
    }



    /* (non-Javadoc)
     * @see org.apache.cocoon.acting.Action#act(org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map act( Redirector redirector, SourceResolver resolver, Map objectModel, 
                    String source, Parameters param ) throws Exception {

        // general setup
        String parameterName = param.getParameter("parameter-name",this.defaultParameterName);
        boolean useGetValues = param.getParameterAsBoolean("use-getValues",this.useGetValues);
        InputModule input = null;
        OutputModule output = null;
        ComponentSelector inputSelector = null;
        ComponentSelector outputSelector = null;

        try {
            if (getLogger().isDebugEnabled()) getLogger().debug("start...");
            // obtain input and output components
            inputSelector=(ComponentSelector) this.manager.lookup(INPUT_MODULE_SELECTOR); 
            if (inputName != null && inputSelector != null && inputSelector.hasComponent(inputName)){
                input = (InputModule) inputSelector.select(inputName);
            }
            outputSelector=(ComponentSelector) this.manager.lookup(OUTPUT_MODULE_SELECTOR); 
            if (outputName != null && outputSelector != null && outputSelector.hasComponent(outputName)){
                output = (OutputModule) outputSelector.select(outputName);
            }


            if (input != null  && output != null) {
                if (getLogger().isDebugEnabled()) getLogger().debug("got input and output modules");
                // do something

                if (parameterName == null) {
                    if (getLogger().isDebugEnabled()) getLogger().debug("reading all parameter values");
                    // for a test, read all parameters from input and write them to outout
                    // get names first, then (one) value per name
                    Iterator iter = input.getAttributeNames(this.inputConf,objectModel);
                    while (iter.hasNext()) {
                        parameterName = (String) iter.next();
                        Object value = input.getAttribute(parameterName, this.inputConf, objectModel);
                        output.setAttribute(this.outputConf, objectModel, parameterName, value);
                        
                        if (getLogger().isDebugEnabled()) 
                            getLogger().debug("["+parameterName+"] = ["+value+"]");
                    }
                        // ------------------------------------------------------------------------
                } else {

                    if (useGetValues) {
                        // get all existing values
                        Object[] value = input.getAttributeValues(parameterName, this.inputConf, objectModel);
                        output.setAttribute(this.outputConf, objectModel, parameterName, value);
                        
                        if (getLogger().isDebugEnabled()) 
                            for (int i=0; i<value.length; i++)
                                getLogger().debug("["+parameterName+"["+i+"]] = ["+value[i]+"]");
                        // ------------------------------------------------------------------------

                    } else {
                        // get just one parameter value
                        if (getLogger().isDebugEnabled()) 
                            getLogger().debug("reading parameter values for "+parameterName);
                        
                        Object value = input.getAttribute(parameterName, this.inputConf, objectModel);
                        output.setAttribute(this.outputConf, objectModel, parameterName, value);
                        
                        if (getLogger().isDebugEnabled()) getLogger().debug("["+parameterName+"] = ["+value+"]");
                        // ------------------------------------------------------------------------
                    }
                }
                output.commit(this.outputConf,objectModel);
                if (getLogger().isDebugEnabled()) getLogger().debug("done commit");
                // done
            }


        } catch (Exception e) {
            throw e;
        } finally {
            // release components
            if (getLogger().isDebugEnabled()) getLogger().debug("releasing components");
            if (outputSelector != null) {
                if (output != null) 
                    outputSelector.release(output);
                this.manager.release(outputSelector);
            }
            if (inputSelector != null) {
                if (input != null)
                    inputSelector.release(input);
                this.manager.release(inputSelector);
            }
            if (getLogger().isDebugEnabled()) getLogger().debug("... end");
        }
        return EMPTY_MAP;
    }

}
