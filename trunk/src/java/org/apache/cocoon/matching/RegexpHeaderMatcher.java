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
 * Matches a request header (e.g. "referer") against a regular expression.
 *
 * <p><b>Global and local configuration</b></p>
 * <table border="1">
 * <tr><td><code>header-name</code></td><td>Name of the request header to
 * match against</td></tr>
 * </table>
 *
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: RegexpHeaderMatcher.java,v 1.3 2004/03/08 14:02:41 cziegeler Exp $
 */

public class RegexpHeaderMatcher extends AbstractRegexpMatcher
    implements Configurable
{
    private String defaultParam;

    public void configure(Configuration config) throws ConfigurationException {

        this.defaultParam = config.getChild("header-name").getValue(null);
    }

    protected String getMatchString(Map objectModel, Parameters parameters) {

        String paramName = parameters.getParameter("header-name", this.defaultParam);
        if (paramName == null) {
            getLogger().warn("No header name given. FAILING");
            return null;
        }

        String result = ObjectModelHelper.getRequest(objectModel).getHeader(paramName);
        if (result == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Header '" + paramName + "' not set.");
            }
        }

        return result;
    }
}
