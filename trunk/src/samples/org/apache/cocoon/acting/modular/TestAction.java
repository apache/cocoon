/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: TestAction.java,v 1.4 2004/02/10 05:55:18 stefano Exp $
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


    public void configure(Configuration config) throws ConfigurationException {

        this.inputConf  = config.getChild("input-module");
        this.inputName  = this.inputConf.getAttribute("name", this.inputHint);
        this.outputConf = config.getChild("output-module");
        this.outputName = this.outputConf.getAttribute("name", this.outputHint);
        this.defaultParameterName = config.getChild("parameter-name").getValue(null);
        this.useGetValues = config.getChild("use-getValues").getValueAsBoolean(this.useGetValues);
    }



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
                    outputSelector.release((ComponentSelector) output);
                this.manager.release(outputSelector);
            }
            if (inputSelector != null) {
                if (input != null)
                    inputSelector.release((ComponentSelector) input);
                this.manager.release(inputSelector);
            }
            if (getLogger().isDebugEnabled()) getLogger().debug("... end");
        }
        return EMPTY_MAP;
    }

}
