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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.components.language.markup.xsp.XSPModuleHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple helper action to allow passing sitemap variables to InputModules.
 * Sitemap evaluation of input modules using the curly bracket syntax e.g.
 * {defaults:skin} suffers from the fact that it is not
 * possible to use a sitemap variable as part of the invocation like
 * {defaults:{1})}. This action takes three parameters, the name
 * of the input module, the attribute name, and whether to call getAttribute() or
 * getAttributeValues(). Thus the above becomes
 * <pre>
 *   &lt;map:act type="inputmodule"&gt;
 *     &lt;map:parameter name="module" value="defaults"/&gt;
 *     &lt;map:parameter name="attribute" value="{1}"/&gt;
 *     &lt;map:parameter name="single-value" value="false"/&gt;
 * 
 *     &lt;!-- do something with the result: "{1}" --&gt;
 * 
 *   &lt;/map:act&gt;
 * </pre> 
 * The action invokes the 
 * {@link org.apache.cocoon.components.modules.input.InputModule#getAttributeValues(String, Configuration, Map) getAttributeValues()}
 * method and returns all results numbered from "1". If no result exists,
 * "null" is returned and the nested block is skipped.
 * The name of the input module to use may be preconfigured when
 * declaring the action in your sitemap:
 * <pre>
 *     &lt;map:action name="inputmodule" 
 *                    src="org.apache.cocoon.acting.InputModuleAction" 
 *                    logger="sitemap.action.inputmodule"&gt;
 *        &lt;module&gt;defaults&lt;/module&gt;
 *        &lt;single-value&gt;false&lt;/single-value&gt;
 *     &lt;/map:action&gt;
 * </pre>
 * 
 * 
 * @see org.apache.cocoon.components.modules.input.InputModule
 * 
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: InputModuleAction.java,v 1.4 2004/03/05 13:02:43 bdelacretaz Exp $
 */
public class InputModuleAction extends ConfigurableServiceableAction {

    /* (non-Javadoc)
     * @see org.apache.cocoon.acting.Action#act(org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map act(Redirector redirector, SourceResolver resolver,
                   Map objectModel, String source, Parameters parameters)
        throws Exception {

        HashMap map = null;
        Configuration conf = null;
        String module = parameters.getParameter("module", (String) this.settings.get("module"));
        String attrib =
            parameters.getParameter("attribute", (String) this.settings.get("attribute"));
        boolean single =
            parameters.getParameterAsBoolean(
                "single-value",
                ((Boolean) this.settings.get("single-value")).booleanValue());

        if (module != null && attrib != null) {
            XSPModuleHelper mhelper = new XSPModuleHelper();
            mhelper.setup(manager);
            Object[] result = null;
            if (!single) {
                result = mhelper.getAttributeValues(objectModel, conf, module, attrib, null);
            } else {
                Object tmp = mhelper.getAttribute(objectModel, conf, module, attrib, null);
                if (tmp != null){
                    result = new Object[1];
                    result[0] = tmp;
                }
            }
            mhelper.releaseAll();

            if (result != null && result.length != 0) {
                map = new HashMap();
                for (int i = 0; i < result.length; i++) {
                    map.put(Integer.toString(i), result[i]);
                }
            }
        } else {
            if (getLogger().isErrorEnabled()) {
                getLogger().error(
                    "Parameter is missing: module=" + module + " attribute=" + attrib);
            }
        }
        return map;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration conf) throws ConfigurationException {
        super.configure(conf);
        boolean result = false;
        String tmp = (String) this.settings.get("single-value", "false");
        result = tmp.equalsIgnoreCase("true") || tmp.equalsIgnoreCase("yes");
        this.settings.put("single-value", new Boolean(result));
    }

}
