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
package org.apache.cocoon.matching;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.environment.ObjectModelHelper;

import java.util.Map;

/**
 * Matches a request parameter against a wildcard expression.
 *
 * <p><b>Global and local configuration</b></p>
 * <table border="1">
 * <tr><td><code>parameter-name</code></td><td>Name of the request parameter to
 * match against</td></tr>
 * </table>
 *
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: WildcardRequestParameterMatcher.java,v 1.3 2004/03/05 13:02:56 bdelacretaz Exp $
 */
public class WildcardRequestParameterMatcher extends AbstractWildcardMatcher
    implements Configurable
{
    private String defaultParam;

    public void configure(Configuration config) throws ConfigurationException {
        this.defaultParam = config.getChild("parameter-name").getValue(null);
    }

    protected String getMatchString(Map objectModel, Parameters parameters) {

        String paramName = parameters.getParameter("parameter-name", this.defaultParam);
        if (paramName == null) {
            getLogger().warn("No parameter name given. FAILING");
            return null;
        }

        String result = ObjectModelHelper.getRequest(objectModel).getParameter(paramName);
        if (result == null) {
            getLogger().debug("Parameter '" + paramName + "' not set.");
        }

        return result;
    }
}
