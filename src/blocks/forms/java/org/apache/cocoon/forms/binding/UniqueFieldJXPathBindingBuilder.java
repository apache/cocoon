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

import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.i18n.I18nUtils;
import org.w3c.dom.Element;

import java.util.Locale;

/**
 * UniqueFieldJXPathBindingBuilder provides a helper class for the Factory
 * implemented in {@link JXPathBindingManager} that helps construct the
 * actual {@link UniqueFieldJXPathBinding} out of the configuration in the
 * provided configElement which looks like:
 * <pre><code>
 * &lt;wb:unique-field id="<i>widget-id</i>" path="<i>xpath-expression</i>"&gt;
 *   &lt;!-- optional convertor of these field --&gt;
 *   &lt;wd:convertor&gt;
 *     &lt;!-- any convertor --&gt;
 *   &lt;/wd:convertor&gt;
 * &lt;/wb:unique-field&gt;
 * </code></pre>
 *
 * @version CVS $Id: UniqueFieldJXPathBindingBuilder.java,v 1.2 2004/03/09 13:08:46 cziegeler Exp $
 */
public class UniqueFieldJXPathBindingBuilder extends JXPathBindingBuilderBase {

    /**
     * Creates an instance of {@link UniqueFieldJXPathBinding} based on the attributes
     * and nested configuration of the provided bindingElm.
     */
    public JXPathBindingBase buildBinding(Element bindingElm, JXPathBindingManager.Assistant assistant) throws BindingException {

        try {
            CommonAttributes commonAtts = JXPathBindingBuilderBase.getCommonAttributes(bindingElm);
            String widgetId = DomHelper.getAttribute(bindingElm, "id");
            String xpath = DomHelper.getAttribute(bindingElm, "path");

            Convertor convertor = null;
            Locale convertorLocale = Locale.US;
            Element convertorEl = DomHelper.getChildElement(bindingElm, Constants.DEFINITION_NS, "convertor");
            if (convertorEl != null) {
                String datatype = DomHelper.getAttribute(convertorEl, "datatype");
                String localeStr = convertorEl.getAttribute("datatype");
                if (!localeStr.equals("")) {
                    convertorLocale = I18nUtils.parseLocale(localeStr);
                }
                convertor = assistant.getDatatypeManager().createConvertor(datatype, convertorEl);
            }

            UniqueFieldJXPathBinding fieldBinding =
                    new UniqueFieldJXPathBinding(commonAtts,
                            widgetId, xpath, convertor, convertorLocale);

            return fieldBinding;
        } catch (BindingException e) {
            throw e;
        } catch (Exception e) {
            throw new BindingException("Error building binding defined at " + DomHelper.getLocation(bindingElm), e);
        }
    }
}
