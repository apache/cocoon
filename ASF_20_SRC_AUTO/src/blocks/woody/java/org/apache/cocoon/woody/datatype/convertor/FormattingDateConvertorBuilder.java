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
package org.apache.cocoon.woody.datatype.convertor;

import org.w3c.dom.Element;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.i18n.I18nUtils;

import java.text.DateFormat;
import java.util.Locale;

/**
 * Builds {@link FormattingDateConvertor}s.
 *
 * @version CVS $Id: FormattingDateConvertorBuilder.java,v 1.3 2004/03/05 13:02:28 bdelacretaz Exp $
 */
public class FormattingDateConvertorBuilder implements ConvertorBuilder {
    public Convertor build(Element configElement) throws Exception {
        FormattingDateConvertor convertor = new FormattingDateConvertor();

        if (configElement == null)
            return convertor;

        String style = configElement.getAttribute("style");
        if (!style.equals("")) {
            if (style.equals("short"))
                convertor.setStyle(DateFormat.SHORT);
            else if (style.equals("medium"))
                convertor.setStyle(DateFormat.MEDIUM);
            else if (style.equals("long"))
                convertor.setStyle(DateFormat.LONG);
            else if (style.equals("full"))
                convertor.setStyle(DateFormat.FULL);
            else
                throw new Exception("Invalid value \"" + style + "\" for style attribute at " + DomHelper.getLocation(configElement));
        }

        String variant = configElement.getAttribute("variant");
        if (!variant.equals("")) {
            if (variant.equals("date"))
                convertor.setVariant(FormattingDateConvertor.DATE);
            else if (variant.equals("time"))
                convertor.setVariant(FormattingDateConvertor.TIME);
            else if (variant.equals("datetime"))
                convertor.setVariant(FormattingDateConvertor.DATE_TIME);
            else
                throw new Exception("Invalid value \"" + variant + "\" for variant attribute at " + DomHelper.getLocation(configElement));
        }

        Element patternsEl = DomHelper.getChildElement(configElement, Constants.WD_NS, "patterns", false);
        if (patternsEl != null) {
            Element patternEl[] = DomHelper.getChildElements(patternsEl, Constants.WD_NS, "pattern");
            for (int i = 0; i < patternEl.length; i++) {
                String locale = patternEl[i].getAttribute("locale");
                String pattern = DomHelper.getElementText(patternEl[i]);
                if (pattern.equals(""))
                    throw new Exception("pattern element does not contain any content at " + DomHelper.getLocation(patternEl[i]));
                if (locale.equals(""))
                    convertor.setNonLocalizedPattern(pattern);
                else {
                    Locale loc = I18nUtils.parseLocale(locale);
                    convertor.addFormattingPattern(loc, pattern);
                }
            }
        }

        return convertor;
    }
}
