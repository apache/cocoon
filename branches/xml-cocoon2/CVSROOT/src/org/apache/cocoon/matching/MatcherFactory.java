/***************************************************************************** 
 * Copyright (C) The Apache Software Foundation. All rights reserved.        * 
 * ------------------------------------------------------------------------- * 
 * This software is published under the terms of the Apache Software License * 
 * version 1.1, a copy of which has been included  with this distribution in * 
 * the LICENSE file.                                                         * 
 *****************************************************************************/ 
package org.apache.cocoon.matching; 

import org.apache.avalon.ConfigurationException;

import org.w3c.dom.DocumentFragment;
 
/** 
 * Interface a class has to implement that produces java source code 
 * representing logic for a <code>Matcher</code>s match method. The 
 * returned source code will be directly integrated into a method of the 
 * generated sitemap code. 
 * This <code>MatcherFactory</code>s generate method will be called during 
 * sitemap code generation.
 * 
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a> 
 * @version CVS $Revision: 1.1.2.5 $ $Date: 2000-07-27 21:49:02 $ 
 */ 

public interface MatcherFactory {
    public String generateClassSource (String prefix, String pattern, DocumentFragment conf) 
    throws ConfigurationException;

    public String generateMethodSource (String prefix, String pattern, DocumentFragment conf)
    throws ConfigurationException;
}
