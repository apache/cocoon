/*
 * $Id: EnumConvertor.java,v 1.1 2003/11/06 22:58:37 ugo Exp $
 */
package org.apache.cocoon.woody.datatype.convertor;

import java.lang.reflect.Method;
import java.util.Locale;

import org.apache.cocoon.woody.datatype.Enum;

/**
 * Description of EnumConvertor.
 */
public class EnumConvertor implements Convertor {

    private String className;
    
    public EnumConvertor(String className) {
        this.className = className;
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.datatype.convertor.Convertor#convertFromString(java.lang.String, java.util.Locale, org.apache.cocoon.woody.datatype.convertor.Convertor.FormatCache)
     */
    public Object convertFromString(
        String value,
        Locale locale,
        FormatCache formatCache) {
        try {
            Method method = getTypeClass().
                getMethod("fromString", new Class[] { String.class, Locale.class});
            return method.invoke(null, new Object[] { value, locale});
        } catch (Exception e) {
            // FIXME: I'd like to throw a o.a.c.ProcessingException here,
            // but unfortunately it's a checked exception.
            // Checked exceptions are evil, aren't they?
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.datatype.convertor.Convertor#convertToString(java.lang.Object, java.util.Locale, org.apache.cocoon.woody.datatype.convertor.Convertor.FormatCache)
     */
    public String convertToString(
        Object value,
        Locale locale,
        FormatCache formatCache) {
        return ((Enum) value).convertToString(locale);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.datatype.convertor.Convertor#getTypeClass()
     */
    public Class getTypeClass() {
        try {
            // FIXME: use the correct class loader.
            return Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            // FIXME: I'd like to throw a o.a.c.ProcessingException here,
            // but unfortunately it's a checked exception.
            // Checked exceptions are evil, aren't they?
            throw new RuntimeException("Class " + className + " not found", e);
        }
    }

}
