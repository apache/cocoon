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
 * NewJXPathBindingBuilder provides a helper class for the Factory
 * implemented in {@link JXPathBindingManager} that helps construct the
 * actual {@link NewJXPathBinding} out of the configuration in the
 * provided configElement which looks like:
 * <pre><code>
 * &lt;wb:new id="<i>widget-id</i>"&gt;
 *   &lt;wb:field id="<i>sub-widget-id</i>" path="<i>relative-xpath</i>"
 *       direction="<i>load|save</i>" lenient="<i>true/false</i>" />
 * &lt;/wb:new&gt;
 * </code></pre>
 *
 * @author Timothy Larson
 * @version CVS $Id: NewJXPathBindingBuilder.java,v 1.6 2004/03/09 13:54:06 reinhard Exp $
 */
public class NewJXPathBindingBuilder
    extends JXPathBindingBuilderBase {

    public JXPathBindingBase buildBinding(Element bindingElm, JXPathBindingManager.Assistant assistant)
            throws BindingException {
        try {
            String widgetId = DomHelper.getAttribute(bindingElm, "id");
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(bindingElm);

            JXPathBindingBase[] childBindings = assistant.makeChildBindings(bindingElm);

            NewJXPathBinding newBinding = new NewJXPathBinding(commonAtts, widgetId, childBindings);
            return newBinding;
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building new binding defined at " + DomHelper.getLocation(bindingElm), e);
        }
    }
}
