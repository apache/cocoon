package org.apache.cocoon.woody.datatype.convertor;

import java.util.Locale;

/**
 * Convertor for java.lang.Boolean's.
 */
public class PlainBooleanConvertor implements Convertor {
    public Object convertFromString(String value, Locale locale, Convertor.FormatCache formatCache) {
        return Boolean.valueOf(value);
    }

    public String convertToString(Object value, Locale locale, Convertor.FormatCache formatCache) {
        return value.toString();
    }

    public Class getTypeClass() {
        return Boolean.class;
    }
}
