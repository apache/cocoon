/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: InputModuleAction.java,v 1.3 2003/10/25 18:06:19 joerg Exp $
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
