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

import java.util.Locale;

import org.apache.cocoon.i18n.I18nUtils;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.datatype.convertor.Convertor;
import org.apache.cocoon.woody.util.DomHelper;
import org.w3c.dom.Element;

/**
 * RepeaterJXPathBindingBuilder provides a helper class for the Factory
 * implemented in {@link JXPathBindingManager} that helps construct the
 * actual {@link RepeaterJXPathBinding} out of the configuration in the
 * provided configElement which looks like:
 * <pre><code>
 * &lt;wb:repeater
 *   id="contacts"
 *   parent-path="contacts"
 *   row-path="contact"
 *   unique-row-id="id"
 *   unique-path="@id"   &gt;
 *
 *   &lt;wb:on-bind&gt;
 *      &lt;!-- nested bindings executed on updates AND right after the insert --&gt;
 *   &lt;/wb:on-bind&gt;
 *
 *   &lt;wb:on-delete-row&gt;
 *      &lt;!-- nested bindings executed on deletion of row --&gt;
 *   &lt;/wb:on-delete-row&gt;
 *
 *   &lt;wb:on-insert-row&gt;
 *      &lt;!-- nested bindings executed to prepare the insertion of a row --&gt;
 *   &lt;/wb:on-insert-row&gt;
 *
 * &lt;/wb:repeater&gt;
 * </code></pre>
 *
 * @version CVS $Id: RepeaterJXPathBindingBuilder.java,v 1.16 2004/03/09 13:54:07 reinhard Exp $
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
            String uniqueRowId =
                DomHelper.getAttribute(bindingElm, "unique-row-id", null);
            String uniqueRowIdPath =
                DomHelper.getAttribute(bindingElm, "unique-path", null);

            Convertor convertor = null;
            Locale convertorLocale = Locale.US;
            Element convertorEl =
                DomHelper.getChildElement(bindingElm,
                        Constants.WD_NS, "convertor");
            if (convertorEl != null) {
                String datatype =
                    DomHelper.getAttribute(convertorEl, "datatype");
                String localeStr = convertorEl.getAttribute("datatype");
                if (!localeStr.equals("")) {
                    convertorLocale = I18nUtils.parseLocale(localeStr);
                }
                convertor =
                    assistant.getDatatypeManager().createConvertor(datatype,
                            convertorEl);
            }

            Element childWrapElement = DomHelper.getChildElement(bindingElm,
                    BindingManager.NAMESPACE, "on-bind");
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
            }
            /* New <wb:unique-row> child element builder */
            Element uniqueFieldWrapElement = DomHelper.getChildElement(bindingElm,
                    BindingManager.NAMESPACE, "unique-row");
            JXPathBindingBase[] uniqueFieldBinding = null;
            if (uniqueFieldWrapElement != null) {
                uniqueFieldBinding = assistant.makeChildBindings(uniqueFieldWrapElement);
            } else if (uniqueRowId == null || uniqueRowIdPath == null) {
                throw new BindingException(
                      "RepeaterBinding misses '<unique-row>' child definition. " +
                      DomHelper.getLocation(bindingElm));
            } else {
                if (this.getLogger().isInfoEnabled()) {
                this.getLogger().info("<wb:repeater>: The attributes 'unique-row-id' and " +
                        "'unique-path' are deprecated. Use <unique-row> child element instead." +
                        " Located at " + DomHelper.getLocation(bindingElm));
                }
            }

            RepeaterJXPathBinding repeaterBinding =
                new RepeaterJXPathBinding(commonAtts, repeaterId, parentPath,
                        rowPath, rowPathForInsert, uniqueRowId,
                        uniqueRowIdPath, convertor, convertorLocale,
                        childBindings, insertBinding, deleteBindings, uniqueFieldBinding);
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
