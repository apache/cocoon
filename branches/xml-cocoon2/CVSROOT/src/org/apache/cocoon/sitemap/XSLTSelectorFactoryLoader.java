/***************************************************************************** 
 * Copyright (C) The Apache Software Foundation. All rights reserved.        * 
 * ------------------------------------------------------------------------- * 
 * This software is published under the terms of the Apache Software License * 
 * version 1.1, a copy of which has been included  with this distribution in * 
 * the LICENSE file.                                                         * 
 *****************************************************************************/ 
package org.apache.cocoon.sitemap; 

import java.util.Hashtable;

import org.apache.cocoon.selection.SelectorFactory;

import org.w3c.dom.DocumentFragment;
  
/** 
 * This class is used as a XSLT extension class. It is used by the sitemap 
 * generation stylesheet to load a <code>SelectorFactory</code> to get the 
 * generated source code.
 * 
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a> 
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2000-07-20 21:57:19 $ 
 */ 

public class XSLTSelectorFactoryLoader {

    Hashtable obj = new Hashtable ();

    public String getSource (String level, String selectorFactoryClassname, String test, 
            String prefix, DocumentFragment conf) 
    throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {
        SelectorFactory factory = (SelectorFactory ) obj.get(selectorFactoryClassname);
        if (factory == null) {
            Class cl = this.getClass().getClassLoader().loadClass(selectorFactoryClassname);
            factory = (SelectorFactory) cl.newInstance();
            obj.put (selectorFactoryClassname, factory);
        }
        if ("class".equals(level)) {
            return factory.generateClassLevel (test, prefix, conf);
        } else {
            return factory.generateMethodLevel (test, prefix, conf);
        } 
    }
}
