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

import java.util.Locale;

import org.apache.cocoon.forms.datatype.convertor.ConversionResult;
import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * The CharConvertor converts a String to a Character object and viceversa.
 * 
 * <p>Converting from char to String, returns a String only containing the input character
 * Converting from String to char, returns a Character object created with the first char of the input String</p>
 * 
 */
public class CharConvertor implements Convertor {

    public ConversionResult convertFromString(String value, Locale locale, Convertor.FormatCache formatCache) {
        Character c = new Character(value.charAt(0));
        return new ConversionResult(c);
    }

    public String convertToString(Object value, Locale locale, Convertor.FormatCache formatCache) {
        Character c = (Character)value;
        if (c.charValue() == 0) return "";
        return c.toString();
    }

    public Class getTypeClass() {
        return Character.class;
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        // intentionally empty
    }
}