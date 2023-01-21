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
 * A simple repeater binding that will replace (i.e. delete then re-add all) its
 * content.
 * <pre>
 * &lt;fb:simple-repeater
 *   id="contacts"
 *   parent-path="contacts"&gt;
 *   &lt;<em>... child bindings ...</em>&gt;
 * &lt;/fb:simple-repeater&gt;
 * </pre>
 *
 * @version $Id$
 */
public class SimpleRepeaterJXPathBindingBuilder extends JXPathBindingBuilderBase {

    public JXPathBindingBase buildBinding(Element bindingElem,
                                          JXPathBindingManager.Assistant assistant)
    throws BindingException {
        try {
            CommonAttributes commonAtts =
                JXPathBindingBuilderBase.getCommonAttributes(bindingElem);

            String repeaterId = DomHelper.getAttribute(bindingElem, "id", null);
            String parentPath = DomHelper.getAttribute(
                    bindingElem, "parent-path", null);
            String rowPath = DomHelper.getAttribute(bindingElem, "row-path", null);
            boolean clearOnLoad = DomHelper.getAttributeAsBoolean(
                    bindingElem, "clear-before-load", true);
            boolean deleteIfEmpty = DomHelper.getAttributeAsBoolean(
                    bindingElem, "delete-parent-if-empty", false);

            JXPathBindingBase[] childBindings = null;

            // do inheritance
            SimpleRepeaterJXPathBinding otherBinding = (SimpleRepeaterJXPathBinding) assistant.getContext().getSuperBinding();
            if (otherBinding != null) {
                childBindings = otherBinding.getChildBindings();
                commonAtts = JXPathBindingBuilderBase.mergeCommonAttributes(otherBinding.getCommonAtts(), commonAtts);

                if (parentPath == null) {
                    parentPath = otherBinding.getRepeaterPath();
                }
                if (repeaterId == null) {
                    repeaterId = otherBinding.getId();
                }
                if (rowPath == null) {
                    rowPath = otherBinding.getRowPath();
                }
                if (!bindingElem.hasAttribute("clear-before-load")) {
                    clearOnLoad = otherBinding.getClearOnLoad();
                }
                if (!bindingElem.hasAttribute("delete-parent-if-empty")) {
                    deleteIfEmpty = otherBinding.getDeleteIfEmpty();
                }
            }

            childBindings = assistant.makeChildBindings(bindingElem, childBindings);

            return new SimpleRepeaterJXPathBinding(commonAtts, repeaterId,
                    parentPath, rowPath, clearOnLoad, deleteIfEmpty,
                    new ComposedJXPathBindingBase(
                            JXPathBindingBuilderBase.CommonAttributes.DEFAULT,
                            childBindings));
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building repeater binding", e,
                                       DomHelper.getLocationObject(bindingElem));
        }
    }
}
