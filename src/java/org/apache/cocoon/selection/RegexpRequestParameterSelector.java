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

import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.environment.ObjectModelHelper;

/**
 * <p>The {@link RegexpRequestParameterSelector} class defines a selector matching
 * specific request parameters to configured regular-expression patterns.</p> 
 *
 * <p>The configuration of an {@link RegexpRequestParameterSelector} follows exactly
 * what has been outlined in {@link AbstractRegexpSelector} regarting regular
 * expression patterns, and additionally it requires an extra configuration element
 * specifying the request parameter whose value needs to be matched:</p>
 * 
 * <pre>
 * &lt;map:components&gt;
 *   ...
 *   &lt;map:selectors default="..."&gt;
 *     &lt;map:selector name="..." src="org.apache.cocoon.selection...."&gt;
 *       &lt;pattern name="empty"&gt;^$&lt;/pattern&gt;
 *       &lt;pattern name="number"&gt;^[0-9]+$&lt;/pattern&gt;
 *       &lt;pattern name="string"&gt;^.+$&lt;/pattern&gt;
 *       &lt;parameter-name&gt;...&lt;/parameter-name&gt;
 *     &lt;/map:selector&gt;
 *  &lt;/map:selectors&gt;
 * &lt;/map:components&gt;
 * </pre>
 * 
 * <p>If not configured, or if it needs to be overriddent, the parameter name can
 * also be specified as a <code>&lt;map:parameter&nbsp;.../&gt;</code> inside the
 * pipeline itself.</p>
 * 
 * @version CVS $Revision: 1.1 $
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 */
public class RegexpRequestParameterSelector extends AbstractRegexpSelector {

    /** <p>The name of the parameter to work on.</p> */
    protected String parameterName;

    /**
     * <p>Create a new {@link RegexpRequestParameterSelector} instance.</p>
     */
    public RegexpRequestParameterSelector() {
        super();
    }

    /**
     * <p>Configure this instance parsing all regular expression patterns and
     * storing the parameter name upon which selection occurs.</p>
     * 
     * @param configuration the {@link Configuration} instance where configured
     *                      patterns are defined.
     * @throws ConfigurationException if one of the regular-expression to configure
     *                                could not be compiled.
     */
    public void configure(Configuration configuration)
    throws ConfigurationException {
        super.configure(configuration);
        this.parameterName = configuration.getChild("parameter-name").getValue(null);
    }

    /**
     * <p>Return the value of the parameter identified by the configured parameter
     * name, if any.</p>
     * 
     * @param objectModel the Cocoon object model.
     * @param parameters the {@link Parameters} associated with the pipeline.
     * @return the value of the configured request parameter or <b>null</b>.
     */
    public Object getSelectorContext(Map objectModel, Parameters parameters) {
        String name = parameters.getParameter("parameter-name", this.parameterName);
        if (name == null) {
            this.getLogger().warn("No parameter name given -- failing.");
            return null;
        }
        return ObjectModelHelper.getRequest(objectModel).getParameter(name);
    }
}
