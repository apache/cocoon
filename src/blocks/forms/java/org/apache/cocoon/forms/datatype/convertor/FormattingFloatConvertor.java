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

import org.outerj.i18n.DecimalFormat;

import java.util.Locale;
import java.text.ParseException;

/**
 * A Convertor for {@link Float}s backed by the
 * {@link java.text.DecimalFormat DecimalFormat} class.
 *
 * <p>This class is mostly the same as the {@link FormattingDecimalConvertor},
 * so see there for more information.
 *
 * @version CVS $Id: FormattingFloatConvertor.java,v 1.2 2004/03/18 11:44:58 bruno Exp $
 */
public class FormattingFloatConvertor extends FormattingDecimalConvertor {

    public FormattingFloatConvertor() {
        super();
    }

    public Object convertFromString(String value, Locale locale, Convertor.FormatCache formatCache) {
        DecimalFormat decimalFormat = getDecimalFormat(locale, formatCache);
        try {
            Number decimalValue = decimalFormat.parse(value);
            if (decimalValue instanceof Float)
                return decimalValue;
            else
                return new Float(decimalValue.floatValue());
        } catch (ParseException e) {
            return null;
        }
    }

    public Class getTypeClass() {
        return Float.class;
    }
}