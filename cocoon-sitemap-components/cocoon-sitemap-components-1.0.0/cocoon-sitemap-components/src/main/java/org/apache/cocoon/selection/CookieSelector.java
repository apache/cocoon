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
import javax.servlet.http.Cookie;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * A <code>Selector</code> that matches a string against a configurable cookie's value.
 *
 * <p><b>Global and local configuration</b></p>
 * <table border="1">
 * <tr>
 *	<td><code>cookie-name</code></td>
 *	<td>Name of the cookie whose value to match against</td>
 *	</tr>
 * </table>
 *
 * @cocoon.sitemap.component.documentation
 * A <code>Selector</code> that matches a string against a configurable cookie's value.
 *
 * @version $Id$
 */
public class CookieSelector extends AbstractLogEnabled
                            implements Configurable, Selector, ThreadSafe {

    protected String defaultName;


    public void configure(Configuration config) throws ConfigurationException {
        this.defaultName = config.getChild("cookie-name").getValue(null);
    }

    public boolean select(String expression, Map objectModel, Parameters parameters) {

        String name = parameters.getParameter("cookie-name", this.defaultName);
        if (name == null) {
            getLogger().warn("No cookie name given -- failing.");
            return false;
        }

        Cookie[] cookies = ObjectModelHelper.getRequest(objectModel).getCookies();
        if (cookies == null) {
            getLogger().debug("Cookie '" + name + "' not set -- failing");
            return false;
        }

        // TODO: this is not optimized
        String value = null;
        for (int i = 0; i < cookies.length; i++) {
            if (cookies[i].getName().equals(name)) {
                value = cookies[i].getValue();
                break;
            }
        }

        if (value == null) {
            getLogger().debug("Cookie '" + name + "' not set -- failing");
            return false;
        }

        return value.equals(expression);
    }
}
