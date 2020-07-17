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
 * ValueJXPathBindingBuilder provides a helper class for the Factory
 * implemented in {@link JXPathBindingManager} that helps construct the
 * actual {@link ValueJXPathBinding} out of the configuration in the
 * provided configElement which looks like:
 * <pre><code>
 * &lt;fb:value id="<i>widget-id</i>" path="<i>xpath-expression</i>"&gt;
 *   &lt;!-- optional child binding to be executed upon 'save' of changed value --&gt;
 *   &lt;fb:on-update&gt;
 *     &lt;!-- any childbinding --&gt;
 *   &lt;/fb:on-update&gt;
 * &lt;/fb:value&gt;
 * </code></pre>
 *
 * @version $Id$
 */
public class ValueJXPathBindingBuilder extends JXPathBindingBuilderBase {

    /**
     * Creates an instance of {@link ValueJXPathBinding} based on the attributes
     * and nested configuration of the provided bindingElm.
     */
    public JXPathBindingBase buildBinding(Element bindingElm, JXPathBindingManager.Assistant assistant)
    throws BindingException {

        try {
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(bindingElm);
            String xpath = DomHelper.getAttribute(bindingElm, "path", null);
            String widgetId = DomHelper.getAttribute(bindingElm, "id", null);

            Convertor convertor = null;
            Locale convertorLocale = Locale.US;
            Element convertorEl = DomHelper.getChildElement(bindingElm, FormsConstants.DEFINITION_NS, "convertor");
            if (convertorEl != null) {
                String datatype = DomHelper.getAttribute(convertorEl, "datatype");
                String localeStr = DomHelper.getAttribute(convertorEl, "locale", null);
                if (localeStr != null) {
                    convertorLocale = I18nUtils.parseLocale(localeStr);
                }

                convertor = assistant.getDatatypeManager().createConvertor(datatype, convertorEl);
            }

            // do inheritance
            ValueJXPathBinding otherBinding = (ValueJXPathBinding) assistant.getContext().getSuperBinding();
            JXPathBindingBase[] existingUpdateBindings = null;
            if (otherBinding != null) {
                commonAtts = JXPathBindingBuilderBase.mergeCommonAttributes(otherBinding.getCommonAtts(), commonAtts);

                if (xpath == null) {
                    xpath = otherBinding.getXPath();
                }
                if (widgetId == null) {
                    widgetId = otherBinding.getId();
                }
                if (convertor == null) {
                    convertor = otherBinding.getConvertor();
                }
                if (convertorLocale == null) {
                    convertorLocale = otherBinding.getConvertorLocale();
                }
                if (convertorLocale == null) {
                    convertorLocale = otherBinding.getConvertorLocale();
                }

                existingUpdateBindings = otherBinding.getUpdateBinding().getChildBindings();
            }

            Element updateWrapElement =
                DomHelper.getChildElement(bindingElm, BindingManager.NAMESPACE, "on-update");
            JXPathBindingBase[] updateBindings = assistant.makeChildBindings(updateWrapElement,existingUpdateBindings);

            return new ValueJXPathBinding(commonAtts, widgetId, xpath, updateBindings,
                    convertor, convertorLocale);
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building binding", e,
                                       DomHelper.getLocationObject(bindingElm));
        }
    }
}
