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

import org.apache.cocoon.forms.datatype.Sex;

import junit.framework.TestCase;

/**
 * Test case for the {@link EnumConvertor} class.
 * 
 * @version CVS $Id: EnumConvertorTestCase.java,v 1.3 2004/05/06 14:59:45 bruno Exp $
 */
public class EnumConvertorTestCase extends TestCase {

    public EnumConvertorTestCase(String name) {
        super(name);
    }

    /**
     * Test the {@link EnumConvertor#convertFromString(java.lang.String, java.util.Locale, org.apache.cocoon.forms.datatype.convertor.Convertor.FormatCache)
     * method.
     */
    public void testConvertFromString() {
        EnumConvertor convertor = new EnumConvertor("org.apache.cocoon.forms.datatype.Sex");
        ConversionResult conversionResult = convertor.convertFromString
            (Sex.class.getName() + ".FEMALE", Locale.getDefault(), null);
        assertSame("Returned sex must be FEMALE", Sex.FEMALE, conversionResult.getResult());
    }
    
    /**
     * Test the {@link EnumConvertor##convertToString(java.lang.Object, java.util.Locale, org.apache.cocoon.forms.datatype.convertor.Convertor.FormatCache)
     * method.
     */
    public void testConvertToString() {
        EnumConvertor convertor = new EnumConvertor("org.apache.cocoon.forms.datatype.Sex");
        assertEquals("Converted value must match string",
                Sex.class.getName() + ".MALE",
                convertor.convertToString
                    (Sex.MALE, Locale.getDefault(), null));
    }
}
