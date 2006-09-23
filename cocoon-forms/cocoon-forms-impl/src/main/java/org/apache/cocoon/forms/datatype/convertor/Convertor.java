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

import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;

import java.util.Locale;

/**
 * The purpose of a Convertor is to convert between Object and String
 * representations of the same thing. For example, convert
 * {@link Long}s or {@link java.util.Date Date}s to strings, and vice
 * versa. The behaviour of this conversion process can depend on the
 * user's Locale.
 *
 * <p>If you need to do mass conversion of a lot of values, it may be
 * beneficial to pass a {@link FormatCache} object to the convert methods.
 * Some convertors need to build and configure parser or formatting objects,
 * which can be expensive if it needs to be done repeatedly.
 *
 * @version $Id$
 */
public interface Convertor {
    
    /**
     * Converts string representation into the object of convertor's type.
     *
     * @param formatCache can be null if not needed
     */
    ConversionResult convertFromString(String value, Locale locale, FormatCache formatCache);

    String convertToString(Object value, Locale locale, FormatCache formatCache);

    Class getTypeClass();

    /**
     * Generates a bit of information about this convertor (optional).
     */
    void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException;

    public interface FormatCache {
        public Object get();
        public void store(Object object);
    }
}
