/***************************************************************************** 
 * Copyright (C) The Apache Software Foundation. All rights reserved.        * 
 * ------------------------------------------------------------------------- * 
 * This software is published under the terms of the Apache Software License * 
 * version 1.1, a copy of which has been included  with this distribution in * 
 * the LICENSE file.                                                         * 
 *****************************************************************************/ 
 
package org.apache.cocoon; 

import org.apache.avalon.ConfigurationException;

import org.w3c.dom.DocumentFragment;
 
/** 
 * Interface a class has to implement that produces java source code 
 * representing logic for class methods. The 
 * returned source code will be directly integrated into a method of the 
 * generated sitemap code. 
 * This <code>CodeFactory</code>s generate method will be called during 
 * sitemap code generation.
 * 
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a> 
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-09-06 23:22:21 $ 
 */ 

public interface CodeFactory {
    public String generateClassSource (String test, String prefix, DocumentFragment conf) 
    throws ConfigurationException;

    public String generateMethodSource (String test, String prefix, DocumentFragment conf) 
    throws ConfigurationException;
}
