/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.environment;

import java.io.IOException; 
import java.net.MalformedURLException; 

import org.xml.sax.EntityResolver; 
import org.xml.sax.SAXException; 
import org.xml.sax.InputSource; 
 
public interface Environment extends EntityResolver {
    public void changeContext (String uriprefix, String context) 
        throws MalformedURLException;
    public String getView ();
    public String getUri (); 
    public void setContentType (String mimeType); 
}
