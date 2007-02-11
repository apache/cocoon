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
package org.apache.cocoon.woody.datatype.convertor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;

import org.apache.avalon.framework.CascadingRuntimeException;

/**
 * A {@link org.apache.cocoon.woody.datatype.convertor.Convertor Convertor}
 * implementation for types implementing Joshua Bloch's
 * <a href="http://developer.java.sun.com/developer/Books/shiftintojava/page1.html#replaceenums">
 * typesafe enum</a> pattern.
 * 
 * @see org.apache.cocoon.woody.datatype.typeimpl.EnumType
 * @version CVS $Id: EnumConvertor.java,v 1.9 2004/03/09 13:54:15 reinhard Exp $
 */
public class EnumConvertor implements Convertor {

    private Class clazz;
    
    /**
     * Construct a new EnumConvertor for a class
     * @param className The package-qualified name of the class implementing
     * the typesafe enum pattern.
     */
    public EnumConvertor(String className) {
        try {
            clazz = Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            throw new CascadingRuntimeException("Class " + className + " not found", e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.datatype.convertor.Convertor#convertFromString(java.lang.String, java.util.Locale, org.apache.cocoon.woody.datatype.convertor.Convertor.FormatCache)
     */
    public Object convertFromString(String value,
									Locale locale,
									FormatCache formatCache) {
        try {
            // If the enum provides a "fromString" method, use it
            try {
                Method method = getTypeClass().
                    getMethod("fromString", new Class[] { String.class, Locale.class});
                return method.invoke(null, new Object[] { value, locale});
            } catch(NoSuchMethodException nsme) {
                // fromString method was not found, try to convert
                // the value to a field via reflection.
                // Strip the class name
                int pos = value.lastIndexOf('.');
                if (pos >= 0) {
                    value = value.substring(pos + 1);
                }
                Class clazz = getTypeClass();
                Field field = clazz.getField(value);
                return field.get(null);
            }
        } catch (Exception e) {
            throw new CascadingRuntimeException("Got exception trying to convert " + value, e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.datatype.convertor.Convertor#convertToString(java.lang.Object, java.util.Locale, org.apache.cocoon.woody.datatype.convertor.Convertor.FormatCache)
     */
    public String convertToString(Object value,
								  Locale locale,
								  FormatCache formatCache) {
        Class clazz = getTypeClass();
        Field fields[] = clazz.getDeclaredFields();
        for (int i = 0 ; i < fields.length ; ++i) {
            try {
                int mods = fields[i].getModifiers();
                if (Modifier.isPublic(mods)
                        && Modifier.isStatic(mods)
                        && Modifier.isFinal(mods)
                        && fields[i].get(null).equals(value)) {
                    return clazz.getName() + "." + fields[i].getName();
                }
            } catch (Exception e) {
                throw new CascadingRuntimeException("Got exception trying to get value of field " + fields[i], e);
            }
        }
        // Fall back on toString
        return value.toString();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.datatype.convertor.Convertor#getTypeClass()
     */
    public Class getTypeClass() {
        return clazz;
    }
}
