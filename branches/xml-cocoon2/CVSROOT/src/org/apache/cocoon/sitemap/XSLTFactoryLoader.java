/***************************************************************************** 
 * Copyright (C) The Apache Software Foundation. All rights reserved.        * 
 * ------------------------------------------------------------------------- * 
 * This software is published under the terms of the Apache Software License * 
 * version 1.1, a copy of which has been included  with this distribution in * 
 * the LICENSE file.                                                         * 
 *****************************************************************************/
  
package org.apache.cocoon.sitemap; 

import java.util.HashMap;

import org.apache.cocoon.CodeFactory;
import org.apache.cocoon.util.ClassUtils;

import org.w3c.dom.DocumentFragment;
  
/** 
 * This class is used as a XSLT extension class. It is used by the sitemap 
 * generation stylesheet to load <code>MatcherFactory</code>s or 
 * <code>SelectorFactory</code>s to get the generated source code.
 * 
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a> 
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-10-08 21:09:26 $ 
 */ 

public class XSLTFactoryLoader {
    
    HashMap obj = new HashMap();

    public String getClassSource(String className, String prefix, String pattern, DocumentFragment conf) 
    throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception { 
        Object factory = obj.get(className);
        if (factory == null) factory = ClassUtils.newInstance(className); 
        obj.put(className, factory); 
        
        if (factory instanceof CodeFactory) {
            return ((CodeFactory) factory).generateClassSource(prefix, pattern, conf);
        }
        
        throw new Exception ("Wrong class \"" + factory.getClass().getName()
                            + "\". Should implement the CodeFactory interface");
    }
                   
    public String getMethodSource(String className, String prefix, String pattern, DocumentFragment conf) 
    throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception { 
        Object factory = obj.get(className);
        if (factory == null) factory = ClassUtils.newInstance(className); 
        obj.put (className, factory); 

        if (factory instanceof CodeFactory) {
            return ((CodeFactory) factory).generateMethodSource(prefix, pattern, conf);
        }
        
        throw new Exception ("Wrong class \"" + factory.getClass().getName()
                            + "\". Should implement the CodeFactory interface");
    }

    public boolean isFactory (String className) {
        boolean result = false;
        try {
            result = ClassUtils.implementsInterface (className, CodeFactory.class.getName());
        } catch (Exception e) {}
        return result;
    }
}
