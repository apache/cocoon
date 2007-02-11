/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: EnumConvertor.java,v 1.6 2003/11/16 10:56:30 ugo Exp $
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
