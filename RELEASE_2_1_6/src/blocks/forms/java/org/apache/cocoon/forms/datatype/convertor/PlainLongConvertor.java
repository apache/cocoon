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
package org.apache.cocoon.forms.datatype.convertor;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.Locale;

/**
 * Convertor for java.lang.Longs that does not do any (Locale-dependent)
 * formatting. It simply uses String.valueOf() and Long.parseLong().
 *
 * @version CVS $Id$
 */
public class PlainLongConvertor implements Convertor {
    public ConversionResult convertFromString(String value, Locale locale, Convertor.FormatCache formatCache) {
        try {
            return new ConversionResult(new Long(Long.parseLong(value)));
        } catch (NumberFormatException e) {
            return ConversionResult.create("long");
        }
    }

    public String convertToString(Object value, Locale locale, Convertor.FormatCache formatCache) {
        return String.valueOf(value);
    }

    public Class getTypeClass() {
        return Long.class;
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        // intentionally empty
    }
}
