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

import java.util.Locale;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.i18n.I18nUtils;

import org.w3c.dom.Element;

/**
 * A simple multi field binding that will replace (i.e. delete then re-add all) its
 * content.
 * <pre><code>
 * &lt;fb:multi-value id="<i>widget-id</i>"
 *   parent-path="<i>xpath-expression</i>"
 *   row-path="<i>xpath-expression</i>"&gt;
 *   &lt;!-- optional child binding to be executed upon 'save' of changed value --&gt;
 *   &lt;fb:on-update&gt;
 *     &lt;!-- any childbinding --&gt;
 *   &lt;/fb:on-update&gt;
 * &lt;/fb:multi-value&gt;
 * </code></pre>
 *
 * @version $Id$
 */
public class MultiValueJXPathBindingBuilder extends JXPathBindingBuilderBase {

    public JXPathBindingBase buildBinding(Element bindingElem,
                                          JXPathBindingManager.Assistant assistant)
    throws BindingException {

        try {
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(bindingElem);

            String multiValueId = DomHelper.getAttribute(bindingElem, "id", null);
            String parentPath = DomHelper.getAttribute(bindingElem, "parent-path", null);
            String rowPath = DomHelper.getAttribute(bindingElem, "row-path", null);

            Convertor convertor = null;
            Locale convertorLocale = Locale.US;
            Element convertorEl = DomHelper.getChildElement(bindingElem, FormsConstants.DEFINITION_NS, "convertor");
            if (convertorEl != null) {
                String datatype = DomHelper.getAttribute(convertorEl, "datatype", null);
                String localeStr = DomHelper.getAttribute(convertorEl, "locale", null);
                if (localeStr != null) {
                    convertorLocale = I18nUtils.parseLocale(localeStr);
                }

                convertor = assistant.getDatatypeManager().createConvertor(datatype, convertorEl);
            }

            MultiValueJXPathBinding existingBinding = (MultiValueJXPathBinding) assistant.getContext().getSuperBinding();
            JXPathBindingBase[] existingBindings = new JXPathBindingBase[0];
            if (existingBinding != null) {
                commonAtts = JXPathBindingBuilderBase.mergeCommonAttributes(existingBinding.getCommonAtts(), commonAtts);
                existingBindings = existingBinding.getUpdateBinding().getChildBindings();

                if (multiValueId == null)
                    multiValueId = existingBinding.getId();
                if (parentPath == null)
                    parentPath = existingBinding.getMultiValuePath();
                if (rowPath == null)
                    rowPath = existingBinding.getRowPath();

                if (convertor == null) {
                    convertor = existingBinding.getConvertor();
                    convertorLocale = existingBinding.getLocale();
                }
            }

            Element updateWrapElement =
                DomHelper.getChildElement(bindingElem, BindingManager.NAMESPACE, "on-update");
            JXPathBindingBase[] updateBindings = assistant.makeChildBindings(updateWrapElement,existingBindings);


            return new MultiValueJXPathBinding( commonAtts, multiValueId, parentPath, rowPath,
                                                updateBindings, convertor, convertorLocale);
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building multi value binding", e,
                                       DomHelper.getLocationObject(bindingElem));
        }
    }
}
