/***************************************************************************** 
 * Copyright (C) The Apache Software Foundation. All rights reserved.        * 
 * ------------------------------------------------------------------------- * 
 * This software is published under the terms of the Apache Software License * 
 * version 1.1, a copy of which has been included  with this distribution in * 
 * the LICENSE file.                                                         * 
 *****************************************************************************/ 
package org.apache.cocoon.selection; 

import org.w3c.dom.DocumentFragment;
 
/** 
 * Interface a class has to implement that produces java source code 
 * representing logic for a <code>Selector</code>s select method. The 
 * returned source code will be directly integrated into a method of the 
 * generated sitemap code. 
 * This <code>SelectorFactory</code>s generate method will be called during 
 * sitemap code generation.
 * 
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a> 
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-07-19 22:19:58 $ 
 */ 

public interface SelectorFactory {
    public String generateClassLevel (String test, String prefix, DocumentFragment conf) throws Exception;
    public String generateMethodLevel (String test, String prefix, DocumentFragment conf) throws Exception;
}
