/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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
package org.apache.cocoon.portal.aspect.impl;

import java.lang.reflect.Constructor;

import org.apache.cocoon.portal.aspect.AspectDescription;
import org.apache.cocoon.util.ClassUtils;



/**
 * Utility class for aspects
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: AspectUtil.java,v 1.4 2003/12/11 09:56:58 cziegeler Exp $
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
