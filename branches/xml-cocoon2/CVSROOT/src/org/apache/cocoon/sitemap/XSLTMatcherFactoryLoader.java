/***************************************************************************** 
 * Copyright (C) The Apache Software Foundation. All rights reserved.        * 
 * ------------------------------------------------------------------------- * 
 * This software is published under the terms of the Apache Software License * 
 * version 1.1, a copy of which has been included  with this distribution in * 
 * the LICENSE file.                                                         * 
 *****************************************************************************/ 
package org.apache.cocoon.sitemap; 

import org.apache.cocoon.matching.MatcherFactory;

import org.w3c.dom.DocumentFragment;
  
/** 
 * This class is used as a XSLT extension class. It is used by the sitemap 
 * generation stylesheet to load a <code>MatcherFactory</code> to get the 
 * generated source code.
 * 
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a> 
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-07-17 21:06:14 $ 
 */ 

public class XSLTMatcherFactoryLoader {

    public String getSource (String matcherFactoryClassname, String pattern, DocumentFragment conf) 
    throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception {
        Class cl = this.getClass().getClassLoader().loadClass(matcherFactoryClassname);
        MatcherFactory factory = (MatcherFactory) cl.newInstance();
        return factory.generate (pattern, conf);
    }
}
