/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.aspect.impl;

import java.lang.reflect.Constructor;

import org.apache.cocoon.portal.aspect.AspectDescription;
import org.apache.cocoon.util.ClassUtils;



/**
 * Utility class for aspects
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: AspectUtil.java,v 1.5 2004/03/05 13:02:10 bdelacretaz Exp $
 */
public class AspectUtil { 

    /**
     * Create a new instance
     */
    public static Object createNewInstance(AspectDescription desc) {
        try {
            Class clazz = ClassUtils.loadClass(desc.getClassName());
            if ( clazz.getName().startsWith("java.lang.")) {
                Constructor constructor = clazz.getConstructor(new Class[] {String.class});
                String value = (desc.getDefaultValue() == null ? "0" : desc.getDefaultValue());
                return constructor.newInstance(new String[] {value});
            } else {
                if ( desc.getDefaultValue() != null ) {
                    Constructor constructor = clazz.getConstructor(new Class[] {String.class});
                    return constructor.newInstance(new String[] {desc.getDefaultValue()});
                }
                return clazz.newInstance();
            }
        } catch (Exception ignore) {
            return null;
        }
    }
    
    public static Object convert(AspectDescription desc, Object value) {
        try {
            Class clazz = ClassUtils.loadClass(desc.getClassName());
            if ( clazz.getName().startsWith("java.lang.")) {
                if ( !clazz.equals(value.getClass())) {
                    Constructor constructor = clazz.getConstructor(new Class[] {String.class});
                    return constructor.newInstance(new String[] {value.toString()});
                } else {
                    return value;
                }
            } else {
                if ( !value.getClass().equals(clazz) ) {
                    // FIXME - this is catch by "ignore"
                    throw new RuntimeException("Class of aspect doesn't match description.");
                }
                return value;
            }
        } catch (Exception ignore) {
            // if we can't convert, well we don't do it :)
            return value;
        }        
    }
}
