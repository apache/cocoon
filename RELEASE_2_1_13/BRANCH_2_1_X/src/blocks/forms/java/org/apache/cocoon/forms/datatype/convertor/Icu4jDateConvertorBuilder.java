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

import java.util.Locale;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.i18n.I18nUtils;
import org.w3c.dom.Element;

import com.ibm.icu.text.DateFormat;

/**
 * Builds {@link Icu4jDateConvertor}s.
 *
 * @version $Id$
 */
public class Icu4jDateConvertorBuilder implements ConvertorBuilder {
    public Convertor build(Element configElement) throws Exception {
        Icu4jDateConvertor convertor = new Icu4jDateConvertor();

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
            if (variant.equals(Icu4jDateConvertor.DATE) ||
                    variant.equals(Icu4jDateConvertor.TIME) ||
                    variant.equals(Icu4jDateConvertor.DATE_TIME)) {
                convertor.setVariant(variant);
            } else {
                throw new Exception("Invalid value \"" + variant + "\" for variant attribute at " + DomHelper.getLocation(configElement));
            }
        }

        String lenient = configElement.getAttribute("lenient");
        if (!lenient.equals("")) {
            if (lenient.equals("false") || lenient.equals("no")) {
                convertor.setLenient(false);
            } else if (lenient.equals("true") || lenient.equals("yes")) {
                convertor.setLenient(true);
            } else {
                throw new Exception("Invalid value \"" + lenient + "\" for lenient attribute at " + DomHelper.getLocation(configElement));
            }
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
}
