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
package org.apache.cocoon.selection;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;

import java.util.Map;

/**
 * Tests a specific browser pattern against the requesting user-agent.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: BrowserSelector.java,v 1.2 2004/03/05 13:02:57 bdelacretaz Exp $
 */

public class BrowserSelector extends NamedPatternsSelector {

    public void configure(Configuration conf) throws ConfigurationException {
        configure(conf, "browser", "name", "useragent");
    }

    public boolean select(String expression, Map objectModel, Parameters parameters) {
        // Inform proxies that response varies with the user-agent header
        ObjectModelHelper.getResponse(objectModel).addHeader("Vary", "User-Agent");

        // Get the user-agent request header
        String userAgent = ObjectModelHelper.getRequest(objectModel).getHeader("User-Agent");
        if (userAgent == null) {
            getLogger().debug("No User-Agent header -- failing.");
            return false;
        }

        return checkPatterns(expression, userAgent);
    }
}
