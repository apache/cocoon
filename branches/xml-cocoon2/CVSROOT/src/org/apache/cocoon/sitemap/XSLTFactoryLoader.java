/***************************************************************************** 
 * Copyright (C) The Apache Software Foundation. All rights reserved.        * 
 * ------------------------------------------------------------------------- * 
 * This software is published under the terms of the Apache Software License * 
 * version 1.1, a copy of which has been included  with this distribution in * 
 * the LICENSE file.                                                         * 
 *****************************************************************************/ 
package org.apache.cocoon.sitemap; 

import java.util.Hashtable;

import org.apache.cocoon.matching.MatcherFactory;
import org.apache.cocoon.selection.SelectorFactory;

import org.w3c.dom.DocumentFragment;
  
/** 
 * This class is used as a XSLT extension class. It is used by the sitemap 
 * generation stylesheet to load <code>MatcherFactory</code>s or 
 * <code>SelectorFactory</code>s to get the generated source code.
 * 
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a> 
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-07-27 22:08:46 $ 
 */ 

public class XSLTFactoryLoader {
    Hashtable obj = new Hashtable ();

    public XSLTFactoryLoader () {
    }

    public String getClassSource (String className, String prefix, String pattern, DocumentFragment conf) 
    throws ClassNotFoundException, InstantiationException,  
           IllegalAccessException, Exception { 
        Object factory = obj.get(className); 
        if (factory == null) { 
            Class cl = this.getClass().getClassLoader().loadClass(className); 
            factory = cl.newInstance(); 
        } 
        obj.put (className, factory); 
        if (factory instanceof MatcherFactory)
            return ((MatcherFactory)factory).generateClassSource (prefix, pattern, conf);
        else if (factory instanceof SelectorFactory)
            return ((SelectorFactory)factory).generateClassSource (prefix, pattern, conf);
        throw new Exception ("wrong class \""+factory.getClass().getName()
                            +"\". Should be a MatcherFactory or SelectorFactory");
    }
                   
    public String getMethodSource (String className, String prefix, String pattern, DocumentFragment conf) 
    throws ClassNotFoundException, InstantiationException,  
           IllegalAccessException, Exception { 
        Object factory = obj.get(className); 
        if (factory == null) { 
            Class cl = this.getClass().getClassLoader().loadClass(className); 
            factory = cl.newInstance(); 
        } 
        obj.put (className, factory); 
        if (factory instanceof MatcherFactory)
            return ((MatcherFactory)factory).generateMethodSource (prefix, pattern, conf);
        else if (factory instanceof SelectorFactory)
            return ((SelectorFactory)factory).generateMethodSource (prefix, pattern, conf);
        throw new Exception ("wrong class \""+factory.getClass().getName()
                            +"\". Should be a MatcherFactory or SelectorFactory");
    }
}
