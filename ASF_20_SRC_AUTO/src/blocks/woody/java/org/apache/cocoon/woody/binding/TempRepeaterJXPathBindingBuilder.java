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
 * An experimental simple repeater binding that will replace
 * (i.e. delete then re-add all) its content.
 * Based on SimpleRepeater code.
 * <pre>
 * &lt;wb:temp-repeater
 *   id="contacts"
 *   parent-path="contacts"&gt;
 *   &lt;<em>... child bindings ...</em>
 * &lt;/wb:temp-repeater&gt;
 * </pre>
 *
 * @author Timothy Larson
 * @version CVS $Id: TempRepeaterJXPathBindingBuilder.java,v 1.6 2004/03/05 13:02:27 bdelacretaz Exp $
 */
public class TempRepeaterJXPathBindingBuilder
    extends JXPathBindingBuilderBase {

    public JXPathBindingBase buildBinding(Element bindingElem,
        JXPathBindingManager.Assistant assistant) throws BindingException {
        try {
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(bindingElem);

            String repeaterId = DomHelper.getAttribute(bindingElem, "id");
            String parentPath = DomHelper.getAttribute(bindingElem,
                    "parent-path");
            String rowPath = DomHelper.getAttribute(bindingElem, "row-path");
            String rowPathInsert = DomHelper.getAttribute(bindingElem,
                    "row-path-insert", rowPath);
            boolean virtualRows = DomHelper.getAttributeAsBoolean(
                    bindingElem, "virtual-rows", false);
            boolean clearOnLoad = DomHelper.getAttributeAsBoolean(
                    bindingElem, "clear-before-load", true);
            boolean deleteIfEmpty = DomHelper.getAttributeAsBoolean(
                    bindingElem, "delete-parent-if-empty", false);

            Element childWrapElement = DomHelper.getChildElement(
                    bindingElem, BindingManager.NAMESPACE, "on-bind");
            JXPathBindingBase[] childBindings =
                    assistant.makeChildBindings(childWrapElement);

            Element insertWrapElement = DomHelper.getChildElement(bindingElem,
                    BindingManager.NAMESPACE, "on-insert-row");
            JXPathBindingBase[] insertBindings = null;
            if (insertWrapElement != null) {
                insertBindings =
                    assistant.makeChildBindings(insertWrapElement);
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
            throw new BindingException(
                    "Error building temp-repeater binding defined at " +
                    DomHelper.getLocation(bindingElem), e);
        }
    }
}
