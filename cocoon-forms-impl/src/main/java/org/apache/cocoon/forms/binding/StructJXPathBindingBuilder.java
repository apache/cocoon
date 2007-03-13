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
package org.apache.cocoon.forms.binding;

import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.util.Deprecation;

import org.w3c.dom.Element;

/**
 * StructJXPathBindingBuilder provides a helper class for the Factory
 * implemented in {@link JXPathBindingManager} that helps construct the
 * actual {@link StructJXPathBinding} out of the configuration in the
 * provided configElement which looks like:
 * <pre><code>
 * &lt;fb:struct id="<i>widget-id</i>" path="<i>xpath-expression</i>"
 *     direction="<i>load|save</i>" lenient="<i>true|false</i>" &gt;
 *   &lt;fb:field id="<i>sub-widget-id</i>" path="<i>relative-xpath</i>" />
 * &lt;/fb:struct&gt;
 * </code></pre>
 *
 * @version $Id$
 */
public class StructJXPathBindingBuilder extends JXPathBindingBuilderBase {

    public JXPathBindingBase buildBinding(Element bindingElm, JXPathBindingManager.Assistant assistant)
    throws BindingException {
        Deprecation.logger.info("'fb:struct' is deprecated and replaced by 'fb:group' at " + DomHelper.getLocation(bindingElm));
        try {
            String widgetId = DomHelper.getAttribute(bindingElm, "id");
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(bindingElm);
            String xpath = DomHelper.getAttribute(bindingElm, "path");

            JXPathBindingBase[] childBindings = new JXPathBindingBase[0];

            // do inheritance
            StructJXPathBinding otherBinding = (StructJXPathBinding) assistant.getContext().getSuperBinding();
            if (otherBinding != null) {
                childBindings = otherBinding.getChildBindings();
                commonAtts = JXPathBindingBuilderBase.mergeCommonAttributes(otherBinding.getCommonAtts(), commonAtts);

                // FIXME Never happens
                if (xpath == null)
                    xpath = otherBinding.getXPath();
                // FIXME Never happens
                if (widgetId == null)
                    widgetId = otherBinding.getId();
            }

            childBindings = assistant.makeChildBindings(bindingElm,childBindings);

            return new StructJXPathBinding(commonAtts, widgetId, xpath, childBindings);
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building struct binding", e,
                                       DomHelper.getLocationObject(bindingElm));
        }
    }
}
