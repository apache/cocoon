/*
 * Copyright 2001,2004 The Apache Software Foundation.
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

package org.apache.cocoon.util;

import org.apache.commons.lang.BooleanUtils;

/**
 * Derived from org.apache.commons.jex.util.TypeUtils
 */
public class TypeUtils {

    public static Object convert(Object value, Class toType) {
        if (value == null)
            return value;

        Class fromType = value.getClass();

        if (toType == Object.class || fromType.equals(toType)
                || toType.isAssignableFrom(fromType))
            return value;

        else if (value instanceof Boolean)
            return doConvert((Boolean) value, toType);

        else if (value instanceof Number)
            return doConvert((Number) value, toType);

        else if (value instanceof String)
            return doConvert((String) value, toType);

        else
            throw new RuntimeException(message(value, toType));
    }

    protected static Object doConvert(Boolean value, Class toType) {
        if (toType == String.class)
            return value.toString();

        else if (Number.class.isAssignableFrom(toType))
            return doConvert(BooleanUtils.toIntegerObject(value), toType);

        else
            throw new RuntimeException(message(Boolean.class, toType));
    }

    protected static Object doConvert(Number value, Class toType) {
        if (toType == String.class)
            return value.toString();

        else if (toType == Boolean.class)
            return value.doubleValue() == 0.0 ? Boolean.FALSE : Boolean.TRUE;

        else if (toType == Byte.class)
            return new Byte(value.byteValue());

        else if (toType == Short.class)
            return new Short(value.shortValue());

        else if (toType == Integer.class)
            return new Integer(value.intValue());

        else if (toType == Long.class)
            return new Long(value.longValue());

        else if (toType == Float.class)
            return new Float(value.floatValue());

        else if (toType == Double.class)
            return new Double(value.doubleValue());

        else
            throw new RuntimeException(message(value, toType));
    }

    protected static Object doConvert(String value, Class toType) {
        if (toType == Boolean.class)
            return BooleanUtils.toBooleanObject(value);

        else if (toType == Byte.class)
            return new Byte(value);

        else if (toType == Short.class)
            return new Short(value);

        else if (toType == Integer.class)
            return new Integer(value);

        else if (toType == Long.class)
            return new Long(value);

        else if (toType == Float.class)
            return new Float(value);

        else if (toType == Double.class)
            return new Double(value);

        else
            throw new RuntimeException(message(value, toType));
    }

    public static boolean toBoolean(Object object) {
        Boolean value = (Boolean) convert(object, Boolean.class);
        return value == null ? false : value.booleanValue();
    }

    public static byte toByte(Object object) {
        Byte value = (Byte) convert(object, Byte.class);
        return value == null ? 0 : value.byteValue();
    }

    public static char toChar(Object object) {
        Character value = (Character) convert(object, Character.class);
        return value == null ? 0 : value.charValue();
    }

    public static double toDouble(Object object) {
        Double value = (Double) convert(object, Double.class);
        return value == null ? 0 : value.doubleValue();
    }

    public static float toFloat(Object object) {
        Float value = (Float) convert(object, Float.class);
        return value == null ? 0 : value.floatValue();
    }

    public static int toInt(Object object) {
        Integer value = (Integer) convert(object, Integer.class);
        return value == null ? 0 : value.intValue();
    }

    public static long toLong(Object object) {
        Long value = (Long) convert(object, Long.class);
        return value == null ? 0 : value.longValue();
    }

    public static short toShort(Object object) {
        Short value = (Short) convert(object, Short.class);
        return value == null ? 0 : value.shortValue();
    }

    // Helper functions

    protected static String message(Object value, Class toType) {
        return value.getClass().getName() + " -> " + toType.getName()
                + " conversion not supported";
    }

}