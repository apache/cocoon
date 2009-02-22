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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.environment.ObjectModelHelper;

/**
 * A <code>Selector</code> that matches a string from within the host parameter
 * of the HTTP request.
 *
 * <p>Configuration:
 * <pre>
 * &lt;map:selector name="host" src="org.apache.cocoon.selection.HostSelector"&gt;
 *   &lt;host name="uk-site" value="www.foo.co.uk"/&gt;
 * &lt;/map:selector&gt;
 * </pre>
 * <p>Usage:
 * <pre>
 * &lt;map:select type="host"&gt;
 *   &lt;map:when test="uk-site"&gt;
 *     &lt;map:transform src="stylesheets/page/uk.xsl"/&gt;
 *   &lt;/map:when&gt;
 *   &lt;map:otherwise&gt;
 *     &lt;map:transform src="stylesheets/page/us.xsl"/&gt;
 *   &lt;/map:otherwise&gt;
 * &lt;/map:select&gt;
 * </pre>
 *
 * @cocoon.sitemap.component.documentation
 * A <code>Selector</code> that matches a string from within the host parameter
 * of the HTTP request.
 *
 * @version $Id$
 */
public class HostSelector extends NamedPatternsSelector {

    public void configure(Configuration conf) throws ConfigurationException {
        configure(conf, "host", "name", "value");
    }

    public boolean select(String expression, Map objectModel, Parameters parameters) {
        // Inform proxies that response varies with the Host header
        ObjectModelHelper.getResponse(objectModel).addHeader("Vary", "Host");

        // Get the host request header
        String host = ObjectModelHelper.getRequest(objectModel).getHeader("Host");
        if (host == null) {
            getLogger().debug("No Host header -- failing.");
            return false;
        }

        return checkPatterns(expression, host, false);
    }
}
