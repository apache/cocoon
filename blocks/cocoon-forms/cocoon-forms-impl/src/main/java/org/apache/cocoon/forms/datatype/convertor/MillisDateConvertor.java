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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.Locale;
import java.util.Date;

/**
 * A Convertor for Date objects which uses the number of milliseconds since
 * January 1, 1970, 00:00:00 GMT as string representation.
 *
 * @version $Id$
 */
public class MillisDateConvertor implements Convertor {
    public ConversionResult convertFromString(String value, Locale locale, Convertor.FormatCache formatCache) {
        try {
            long date = Long.parseLong(value);
            return new ConversionResult(new Date(date));
        } catch (NumberFormatException e) {
            return ConversionResult.create("date");
        }
    }

    public String convertToString(Object value, Locale locale, Convertor.FormatCache formatCache) {
        Date date = (Date)value;
        return String.valueOf(date.getTime());
    }

    public Class getTypeClass() {
        return Date.class;
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        // intentionally empty
    }
}
