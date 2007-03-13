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
 * An experimental simple repeater binding that will replace
 * (i.e. delete then re-add all) its content.
 * Based on SimpleRepeater code.
 * <pre>
 * &lt;fb:temp-repeater
 *   id="contacts"
 *   parent-path="contacts"&gt;
 *   &lt;<em>... child bindings ...</em>&gt;
 * &lt;/fb:temp-repeater&gt;
 * </pre>
 *
 * @version $Id$
 */
public class TempRepeaterJXPathBindingBuilder extends JXPathBindingBuilderBase {

    public JXPathBindingBase buildBinding(Element bindingElem,
        JXPathBindingManager.Assistant assistant) throws BindingException {
        try {
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(bindingElem);

            String repeaterId = DomHelper.getAttribute(bindingElem, "id", null);
            String parentPath = DomHelper.getAttribute(bindingElem, "parent-path", null);
            String rowPath = DomHelper.getAttribute(bindingElem, "row-path", null);
            String rowPathInsert = DomHelper.getAttribute(bindingElem, "row-path-insert", rowPath);
            boolean virtualRows = DomHelper.getAttributeAsBoolean(bindingElem, "virtual-rows", false);
            boolean clearOnLoad = DomHelper.getAttributeAsBoolean(bindingElem, "clear-before-load", true);
            boolean deleteIfEmpty = DomHelper.getAttributeAsBoolean(bindingElem, "delete-parent-if-empty", false);

            JXPathBindingBase[] insertBindings = null;
            JXPathBindingBase[] childBindings = null;

            // do inheritance
            TempRepeaterJXPathBinding otherBinding = (TempRepeaterJXPathBinding) assistant.getContext().getSuperBinding();
            if (otherBinding != null) {
                childBindings = otherBinding.getChildBindings();
                insertBindings = otherBinding.getInsertChildBindings();
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
                if (rowPathInsert == null) {
                    rowPathInsert = otherBinding.getRowPathInsert();
                }
                if (!bindingElem.hasAttribute("virtual-rows")) {
                    clearOnLoad = otherBinding.getVirtualRows();
                }
                if (!bindingElem.hasAttribute("clear-before-load")) {
                    clearOnLoad = otherBinding.getClearOnLoad();
                }
                if (!bindingElem.hasAttribute("delete-parent-if-empty")) {
                    deleteIfEmpty = otherBinding.getDeleteIfEmpty();
                }
            }

            Element childWrapElement = DomHelper.getChildElement(bindingElem, BindingManager.NAMESPACE, "on-bind");
            childBindings = assistant.makeChildBindings(childWrapElement,childBindings);

            Element insertWrapElement = DomHelper.getChildElement(bindingElem, BindingManager.NAMESPACE, "on-insert-row");

            if (insertWrapElement != null) {
                insertBindings = assistant.makeChildBindings(insertWrapElement,insertBindings);
            }
            return new TempRepeaterJXPathBinding(commonAtts, repeaterId,
                    parentPath, rowPath, rowPathInsert, virtualRows,
                    clearOnLoad, deleteIfEmpty,
                    new ComposedJXPathBindingBase(
                            JXPathBindingBuilderBase.CommonAttributes.DEFAULT,
                            childBindings),
                    new ComposedJXPathBindingBase(
                            JXPathBindingBuilderBase.CommonAttributes.DEFAULT,
                            insertBindings));
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building temp-repeater binding", e,
                                       DomHelper.getLocationObject(bindingElem));
        }
    }
}
