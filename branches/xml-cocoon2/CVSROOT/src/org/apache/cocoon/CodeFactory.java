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
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-10-25 16:02:15 $ 
 */ 

public interface CodeFactory {
    public String generateParameterSource (DocumentFragment conf)
    throws ConfigurationException;

    public String generateClassSource (String test, String prefix, DocumentFragment conf) 
    throws ConfigurationException;

    public String generateMethodSource (DocumentFragment conf) 
    throws ConfigurationException;
}
