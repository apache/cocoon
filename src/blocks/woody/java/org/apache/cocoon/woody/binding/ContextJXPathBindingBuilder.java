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
package org.apache.cocoon.woody.binding;

import org.apache.cocoon.woody.util.DomHelper;
import org.w3c.dom.Element;

/**
 * ContextJXPathBindingBuilder provides a helper class for the Factory
 * implemented in {@link JXPathBindingManager} that helps construct the
 * actual {@link ContextJXPathBinding} out of the configuration in the
 * provided configElement which looks like:
 * <pre><code>
 * &lt;wb:context path="<i>xpath expression</i>"&gt;
 *   &lt;!-- in here come the nested child bindings on the sub-context --&gt;
 * &lt;/wb:context&gt;
 * </code></pre>
 *
 * @version CVS $Id: ContextJXPathBindingBuilder.java,v 1.7 2004/03/05 13:02:26 bdelacretaz Exp $
 */
public class ContextJXPathBindingBuilder extends JXPathBindingBuilderBase {

    /**
     * Creates an instance of ContextJXPathBinding with the configured
     * path and nested child bindings from the declarations in the bindingElm
     */
    public JXPathBindingBase buildBinding(Element bindingElm,
        JXPathBindingManager.Assistant assistant) throws BindingException {

        try {
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(bindingElm);
            String xpath = DomHelper.getAttribute(bindingElm, "path");

            JXPathBindingBase[] childBindings = assistant.makeChildBindings(bindingElm);

            ContextJXPathBinding contextBinding = new ContextJXPathBinding(commonAtts, xpath, childBindings);
            return contextBinding;
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building context binding defined at " + DomHelper.getLocation(bindingElm), e);
        }
    }
}
