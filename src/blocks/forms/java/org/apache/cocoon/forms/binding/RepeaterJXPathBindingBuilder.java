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
package org.apache.cocoon.forms.binding;

import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * RepeaterJXPathBindingBuilder provides a helper class for the Factory
 * implemented in {@link JXPathBindingManager} that helps construct the
 * actual {@link RepeaterJXPathBinding} out of the configuration in the
 * provided configElement which looks like:
 * <pre><code>
 * &lt;fb:repeater
 *   id="contacts"
 *   parent-path="contacts"
 *   row-path="contact"
 *   row-path-insert="new-contact"  &gt;
 *
 *   &lt;fb:identity&gt;
 *      &lt;!-- nested bindings that map the 'identity' of the items --&gt;
 *   &lt;/fb:identity&gt;
 *
 *   &lt;fb:on-bind&gt;
 *      &lt;!-- nested bindings executed on updates AND right after the insert --&gt;
 *   &lt;/fb:on-bind&gt;
 *
 *   &lt;fb:on-delete-row&gt;
 *      &lt;!-- nested bindings executed on deletion of row --&gt;
 *   &lt;/fb:on-delete-row&gt;
 *
 *   &lt;fb:on-insert-row&gt;
 *      &lt;!-- nested bindings executed to prepare the insertion of a row --&gt;
 *   &lt;/fb:on-insert-row&gt;
 *
 * &lt;/fb:repeater&gt;
 * </code></pre>
 *
 * @version CVS $Id: RepeaterJXPathBindingBuilder.java,v 1.4 2004/04/01 12:59:57 mpo Exp $
 */
public class RepeaterJXPathBindingBuilder extends JXPathBindingBuilderBase {

    /**
     * Creates an instance of {@link RepeaterJXPathBinding} according to the
     * attributes and nested comfiguration elements of the bindingElm.
     *
     * @param bindingElm
     * @param assistant
     * @return JXPathBindingBase
     */
    public JXPathBindingBase buildBinding(Element bindingElm,
            JXPathBindingManager.Assistant assistant) throws BindingException {

        try {
            CommonAttributes commonAtts =
                JXPathBindingBuilderBase.getCommonAttributes(bindingElm);

            String repeaterId = DomHelper.getAttribute(bindingElm, "id");
            String parentPath =
                DomHelper.getAttribute(bindingElm, "parent-path");
            String rowPath = DomHelper.getAttribute(bindingElm, "row-path");
            String rowPathForInsert =
                DomHelper.getAttribute(bindingElm, "row-path-insert", rowPath);

            Element childWrapElement =
                DomHelper.getChildElement(bindingElm, BindingManager.NAMESPACE, "on-bind");
            if (childWrapElement == null) {
                throw new BindingException(
                      "RepeaterBinding misses '<on-bind>' child definition. " +
                      DomHelper.getLocation(bindingElm));
            }
            JXPathBindingBase[] childBindings =
                assistant.makeChildBindings(childWrapElement);

            Element deleteWrapElement = DomHelper.getChildElement(bindingElm,
                    BindingManager.NAMESPACE, "on-delete-row");
            JXPathBindingBase[] deleteBindings = null;
            if (deleteWrapElement != null) {
                deleteBindings =
                    assistant.makeChildBindings(deleteWrapElement);
            }

            Element insertWrapElement = DomHelper.getChildElement(bindingElm,
                    BindingManager.NAMESPACE, "on-insert-row");
            JXPathBindingBase insertBinding = null;
            if (insertWrapElement != null) {
                insertBinding =
                    assistant.makeChildBindings(insertWrapElement)[0];
                    // TODO: we now safely take only the first element here,
                    // but we should in fact send out a warning to the log 
                    // if more were available!
            }

            Element identityWrapElement = DomHelper.getChildElement(bindingElm,
                    BindingManager.NAMESPACE, "identity");
            JXPathBindingBase[] identityBinding = null;
            if (identityWrapElement != null) {
                identityBinding =
                    assistant.makeChildBindings(identityWrapElement);
            }

            RepeaterJXPathBinding repeaterBinding =
                new RepeaterJXPathBinding(commonAtts, repeaterId, parentPath,
                        rowPath, rowPathForInsert, 
                        childBindings, insertBinding, deleteBindings, identityBinding);
            return repeaterBinding;
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException(
                    "Error building repeater binding defined at " +
                    DomHelper.getLocation(bindingElm), e);
        }
    }
}
