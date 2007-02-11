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
 * A simple repeater binding that will replace (i.e. delete then re-add all) its
 * content.
 * <pre>
 * &lt;wb:simple-repeater
 *   id="contacts"
 *   parent-path="contacts"&gt;
 *   &lt;<em>... child bindings ...</em>
 * &lt;/wb:simple-repeater&gt;
 * </pre>
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: SimpleRepeaterJXPathBindingBuilder.java,v 1.11 2004/03/09 13:54:06 reinhard Exp $
 */
public class SimpleRepeaterJXPathBindingBuilder
    extends JXPathBindingBuilderBase {

    public JXPathBindingBase buildBinding(Element bindingElem,
        JXPathBindingManager.Assistant assistant) throws BindingException {
        try {
            CommonAttributes commonAtts =
                JXPathBindingBuilderBase.getCommonAttributes(bindingElem);

            String repeaterId = DomHelper.getAttribute(bindingElem, "id");
            String parentPath = DomHelper.getAttribute(
                    bindingElem, "parent-path");
            String rowPath = DomHelper.getAttribute(bindingElem, "row-path");
            boolean clearOnLoad = DomHelper.getAttributeAsBoolean(
                    bindingElem, "clear-before-load", true);
            boolean deleteIfEmpty = DomHelper.getAttributeAsBoolean(
                    bindingElem, "delete-parent-if-empty", false);

            JXPathBindingBase[] childBindings =
                assistant.makeChildBindings(bindingElem);

            return new SimpleRepeaterJXPathBinding(commonAtts, repeaterId,
                    parentPath, rowPath, clearOnLoad, deleteIfEmpty,
                    new ComposedJXPathBindingBase(
                            JXPathBindingBuilderBase.CommonAttributes.DEFAULT,
                            childBindings));
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException(
                    "Error building repeater binding defined at " +
                    DomHelper.getLocation(bindingElem), e);
        }
    }
}
