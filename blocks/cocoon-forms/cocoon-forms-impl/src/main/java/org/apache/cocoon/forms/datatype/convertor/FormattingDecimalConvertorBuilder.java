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
package org.apache.cocoon.forms.datatype.convertor;

import org.w3c.dom.Element;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.i18n.I18nUtils;

import java.util.Locale;

/**
 * Builds {@link FormattingDecimalConvertor}s.
 *
 * @version $Id$
 */
public class FormattingDecimalConvertorBuilder implements DecimalConvertorBuilder {
    public Convertor build(Element configElement) throws Exception {
        FormattingDecimalConvertor convertor = createConvertor();

        if (configElement == null)
            return convertor;

        String variant = configElement.getAttribute("variant");
        if (!variant.equals("")) {
            if (variant.equals("integer"))
                convertor.setVariant(FormattingDecimalConvertor.INTEGER);
            else if (variant.equals("number"))
                convertor.setVariant(FormattingDecimalConvertor.NUMBER);
            else if (variant.equals("percent"))
                convertor.setVariant(FormattingDecimalConvertor.PERCENT);
            else if (variant.equals("currency"))
                convertor.setVariant(FormattingDecimalConvertor.CURRENCY);
            else
                throw new Exception("Invalid value \"" + variant + "\" for variant attribute at " + DomHelper.getLocation(configElement));
        }

        Element patternsEl = DomHelper.getChildElement(configElement, FormsConstants.DEFINITION_NS, "patterns", false);
        if (patternsEl != null) {
            Element patternEl[] = DomHelper.getChildElements(patternsEl, FormsConstants.DEFINITION_NS, "pattern");
            for (int i = 0; i < patternEl.length; i++) {
                String locale = patternEl[i].getAttribute("locale");
                String pattern = DomHelper.getElementText(patternEl[i]);
                if (pattern.length() == 0)
                    throw new Exception("pattern element does not contain any content at " + DomHelper.getLocation(patternEl[i]));
                if (locale.length() == 0)
                    convertor.setNonLocalizedPattern(pattern);
                else {
                    Locale loc = I18nUtils.parseLocale(locale);
                    convertor.addFormattingPattern(loc, pattern);
                }
            }
        }

        return convertor;
    }

    protected FormattingDecimalConvertor createConvertor() {
        return new FormattingDecimalConvertor();
    }
}
