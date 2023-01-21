/*
 * Copyright 2004 Outerthought bvba and Schaubroeck nv
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.forms.datatype.convertor;

import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.apache.cocoon.forms.datatype.convertor.ConversionResult;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.outerj.daisy.htmlcleaner.HtmlCleanerTemplate;
import org.outerj.daisy.htmlcleaner.HtmlCleaner;

import java.util.Locale;

/**
 * A CForms convertor doing string-to-string conversion by utilizing
 * the Daisy HtmlCleaner component.
 */
public class HtmlCleaningConvertor implements Convertor {
    HtmlCleanerTemplate template;

    public HtmlCleaningConvertor(HtmlCleanerTemplate template) {
        this.template = template;
    }

    public ConversionResult convertFromString(String value, Locale locale, Convertor.FormatCache formatCache) {
        HtmlCleaner cleaner = template.newHtmlCleaner();
        try {
            String result = cleaner.cleanToString(value);
            return new ConversionResult(result);
        } catch (Exception e) {
            Throwable t = ExceptionUtils.getRootCause(e);
            if (t == null)
                t = e;
            String message = t.getMessage();
            if (message == null)
                message = t.toString();
            ValidationError validationError = new ValidationError(message, false);
            return new ConversionResult(validationError);
        }
    }

    public String convertToString(Object object, Locale locale, Convertor.FormatCache formatCache) {
        return (String)object;
    }

    public Class getTypeClass() {
        return java.lang.String.class;
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        // nothing to say about me
    }
}
