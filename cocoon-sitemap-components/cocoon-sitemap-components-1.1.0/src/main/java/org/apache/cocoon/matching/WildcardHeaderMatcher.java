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
package org.apache.cocoon.matching;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.environment.ObjectModelHelper;

import java.util.Map;

/**
 * Matches a request header (e.g. "referer") against a wildcard expression.
 *
 * <p><b>Global and local configuration</b></p>
 * <table border="1">
 * <tr><td><code>header-name</code></td><td>Name of the request header to
 * match against</td></tr>
 * </table>
 *
 * @cocoon.sitemap.component.documentation
 * Matches a request header (e.g. "referer") against a wildcard expression.
 *
 * @version $Id$
 */
public class WildcardHeaderMatcher extends AbstractWildcardMatcher
                                   implements Configurable {

    private String defaultParam;

    public void configure(Configuration config) throws ConfigurationException {
        // Check old name
        this.defaultParam = config.getChild("parameter-name").getValue(null);
        if (defaultParam != null) {
            getLogger().warn("'parameter-name' is deprecated. Please use 'header-name'");
        }
        // Load with new one
        this.defaultParam = config.getChild("header-name").getValue(this.defaultParam);
    }

    protected String getMatchString(Map objectModel, Parameters parameters) {

        // Check old name
        String paramName = parameters.getParameter("parameter-name", null);
        if (paramName != null) {
            getLogger().warn("'parameter-name' is deprecated. Please use 'header-name'");
        } else {
            paramName = this.defaultParam;
        }

        // Load with new one.
        paramName = parameters.getParameter("header-name", paramName);

        if (paramName == null) {
            getLogger().warn("No header name given. FAILING");
            return null;
        }

        String result = ObjectModelHelper.getRequest(objectModel).getHeader(paramName);
        if (result == null) {
            getLogger().debug("Header '" + paramName + "' not set.");
        }

        return result;
    }
}
