/***************************************************************************** 
 * Copyright (C) The Apache Software Foundation. All rights reserved.        * 
 * ------------------------------------------------------------------------- * 
 * This software is published under the terms of the Apache Software License * 
 * version 1.1, a copy of which has been included  with this distribution in * 
 * the LICENSE file.                                                         * 
 *****************************************************************************/ 
package org.apache.cocoon.matching; 
 
/** 
 * This class generates source code which matches a specific browser pattern
 * for request URIs
 * 
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a> 
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-07-11 23:46:41 $ 
 */ 

public class BrowserMatcherFactory implements MatcherFactory {
    public String generate (String test_expression) {
        return "return \"BrowserMatcher\"";
    }
}
