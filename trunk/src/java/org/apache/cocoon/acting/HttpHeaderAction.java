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
package org.apache.cocoon.acting;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.SourceResolver;

/**
 * This action adds HTTP headers to the response.
 *
 * @author <a href="mailto:balld@apache.org">Donald Ball</a>
 * @version CVS $Id: HttpHeaderAction.java,v 1.4 2004/03/08 13:57:35 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type="Action"
 * @x-avalon.lifestyle type="singleton"
 * @x-avalon.info name="set-header"
 */
public class HttpHeaderAction extends AbstractConfigurableAction {

    /**
     * Stores keys of global configuration.
     */
    private Object[] defaults = {};

    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);
        this.defaults = super.settings.keySet().toArray();
    }

    public Map act(Redirector redirector, SourceResolver resolver,
                   Map objectModel, String source, Parameters parameters)
    throws Exception {
        final Map results = new HashMap();

        final Response response = ObjectModelHelper.getResponse(objectModel);

        // Process local configuration parameters
        final String[] names = parameters.getNames();
        for (int i = 0; i < names.length; i++) {
            response.setHeader(names[i],parameters.getParameter(names[i]));
            results.put(names[i], parameters.getParameter(names[i]));
        }

        // Process global defaults, that are not overridden
        for (int i = 0; i < defaults.length; i++) {
            if (!results.containsKey(this.defaults[i])) {
                response.setHeader((String) this.defaults[i], (String) this.settings.get(defaults[i]));
                results.put(this.defaults[i], this.settings.get(defaults[i]));
            }
        }

        return Collections.unmodifiableMap(results);
    }
}
