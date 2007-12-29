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

import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * A <code>Selector</code> that matches a string against a configurable
 * request header, e.g. "referer".
 *
 * <p><b>Global and local configuration</b></p>
 * <table border="1">
 * <tr><td><code>header-name</code></td><td>Name of the request header to
 * match against</td></tr>
 * </table>
 *
 * @cocoon.sitemap.component.documentation
 * A <code>Selector</code> that matches a string against a configurable
 * request header, e.g. "referer".
 *
 * @version $Id$
 */
public class HeaderSelector extends AbstractLogEnabled
                            implements Configurable, ThreadSafe, Selector {

    protected String defaultName;


    public void configure(Configuration config) throws ConfigurationException {
        // Check old name
        this.defaultName = config.getChild("parameter-name").getValue(null);
        if (defaultName != null) {
            getLogger().warn("'parameter-name' is deprecated. Please use 'header-name'");
        }
        // Load with new one
        this.defaultName = config.getChild("header-name").getValue(this.defaultName);
    }

    public boolean select(String expression, Map objectModel, Parameters parameters) {
        // Check old name
        String name = parameters.getParameter("parameter-name", null);
        if (name != null) {
            getLogger().warn("'parameter-name' is deprecated. Please use 'header-name'");
        } else {
            name = this.defaultName;
        }

        // Load with new one.
        name = parameters.getParameter("header-name", name);

        if (name == null) {
            getLogger().warn("No header name given -- failing.");
            return false;
        }

        String value = ObjectModelHelper.getRequest(objectModel).getHeader(name);
        if (value == null) {
            getLogger().debug("Header '" + name + "' not set -- failing.");
            return false;
        }

        return value.equals(expression);
    }
}
