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
package org.apache.cocoon.selection;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;

import java.util.Map;

/**
 * A <code>Selector</code> that matches a string against a configurable request parameter's value.
 *
 * <p><b>Global and local configuration</b></p>
 * <table border="1">
 * <tr><td><code>parameter-name</code></td><td>Name of the request
 * parameter whose value to match against</td></tr>
 * </table>
 *
 * @cocoon.sitemap.component.documentation
 * A <code>Selector</code> that matches a string against a configurable request parameter's value.
 *
 * @version $Id$
 */
public class RequestParameterSelector extends AbstractSwitchSelector
  implements Configurable {

    protected String defaultName;

    public void configure(Configuration config) throws ConfigurationException {
        this.defaultName = config.getChild("parameter-name").getValue(null);
    }

    public Object getSelectorContext(Map objectModel, Parameters parameters) {
        
        String name = parameters.getParameter("parameter-name", this.defaultName);

        if (name == null) {
            getLogger().warn("No parameter name given -- failing.");
            return null;
        }

        return ObjectModelHelper.getRequest(objectModel).getParameter(name);
    }

    public boolean select(String expression, Object selectorContext) {
        if (selectorContext == null) {
            getLogger().debug("Request parameter '" + selectorContext + "' not set -- failing.");
            return false;
        }

        return selectorContext.equals(expression);
    }
}
