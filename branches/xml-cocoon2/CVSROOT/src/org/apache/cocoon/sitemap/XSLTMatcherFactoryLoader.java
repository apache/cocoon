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

import org.w3c.dom.DocumentFragment;
  
/** 
 * This class is used as a XSLT extension class. It is used by the sitemap 
 * generation stylesheet to load a <code>MatcherFactory</code> to get the 
 * generated source code.
 * 
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a> 
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2000-07-19 22:20:00 $ 
 */ 

public class XSLTMatcherFactoryLoader {

    Hashtable obj = new Hashtable ();

    public String getSource (String level, String matcherFactoryClassname, String pattern, 
        String prefix, DocumentFragment conf) 
    throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {
        MatcherFactory factory = null;
/*
        MatcherFactory factory = (MatcherFactory) obj.get(matcherFactoryClassname);
        if (factory == null) {
*/
            Class cl = this.getClass().getClassLoader().loadClass(matcherFactoryClassname);
            factory = (MatcherFactory) cl.newInstance();
/*
            obj.put (matcherFactoryClassname, factory);
        }
*/
        if ("class".equals(level)) {
            return factory.generateClassLevel (pattern, prefix, conf);
        } else {
            return factory.generateMethodLevel (pattern, prefix, conf);
        } 
    }
}
