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

import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;

/**
 * Matches a session attribute against a regular expression.
 *
 * <p><b>Global and local configuration</b></p>
 * <tableborder="1">
 * <tr><td><code>attribute-name</code></td><td>String identifying the session attribute</td></tr>
 * </table>
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: RegexpSessionAttributeMatcher.java,v 1.3 2004/03/08 14:02:41 cziegeler Exp $
 */
public class RegexpSessionAttributeMatcher extends AbstractRegexpMatcher
    implements Configurable
{
    private String defaultParam;

    public void configure(Configuration config) throws ConfigurationException {
        this.defaultParam = config.getChild("attribute-name").getValue(null);
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Default attribute-name is = '" + this.defaultParam + "'");
        }
    }

    protected String getMatchString(Map objectModel, Parameters parameters) {

        String paramName = parameters.getParameter("attribute-name", this.defaultParam);
        if (paramName == null) {
            getLogger().warn("No attribute name given. FAILING");
            return null;
        }

        Object result = ObjectModelHelper.getRequest(objectModel).getSession().getAttribute(paramName);
        if (result == null) {
            getLogger().debug("Session attribute '" + paramName + "' not set.");
            return null;
        }

        return result.toString();
    }
}
