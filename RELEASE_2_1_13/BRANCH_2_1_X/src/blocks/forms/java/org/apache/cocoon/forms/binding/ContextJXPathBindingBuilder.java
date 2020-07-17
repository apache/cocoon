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

import org.w3c.dom.Element;

/**
 * ContextJXPathBindingBuilder provides a helper class for the Factory
 * implemented in {@link JXPathBindingManager} that helps construct the
 * actual {@link ContextJXPathBinding} out of the configuration in the
 * provided configElement which looks like:
 * <pre><code>
 * &lt;fb:context path="<i>xpath expression</i>"&gt;
 *   &lt;!-- in here come the nested child bindings on the sub-context --&gt;
 * &lt;/fb:context&gt;
 * </code></pre>
 * <p>The <code>fb:context</code> element can have an optional <code>factory</code>
 * attribute, whose value, if present, must be the name of a class extending
 * {@link org.apache.commons.jxpath.AbstractFactory}. If this attribute is present,
 * an instance of the named class is registered with the JXPath context and can be used to
 * create an object corresponding to the path of the <code>fb:context</code> element
 * upon save, if needed.</p>
 *
 * @version $Id$
 */
public class ContextJXPathBindingBuilder extends JXPathBindingBuilderBase {

    /**
     * Creates an instance of ContextJXPathBinding with the configured
     * path and nested child bindings from the declarations in the bindingElm
     */
    public JXPathBindingBase buildBinding(Element bindingElm,
                                          JXPathBindingManager.Assistant assistant)
    throws BindingException {

        try {
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(bindingElm);
            String xpath = DomHelper.getAttribute(bindingElm, "path", null);
            String factory = DomHelper.getAttribute(bindingElm, "factory", null);

            JXPathBindingBase[] childBindings = new JXPathBindingBase[0];

            // do inheritance
            ContextJXPathBinding otherBinding = (ContextJXPathBinding) assistant.getContext().getSuperBinding();
            if (otherBinding != null) {
                childBindings = otherBinding.getChildBindings();
                commonAtts = JXPathBindingBuilderBase.mergeCommonAttributes(otherBinding.getCommonAtts(), commonAtts);

                if (xpath == null) {
                    xpath = otherBinding.getXPath();
                }
                if (factory == null) {
                    factory = otherBinding.getFactoryClassName();
                }

            }

            childBindings = assistant.makeChildBindings(bindingElm,childBindings);

            return new ContextJXPathBinding(commonAtts, xpath, factory, childBindings);
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building context binding",
                                       DomHelper.getLocationObject(bindingElm));
        }
    }
}
