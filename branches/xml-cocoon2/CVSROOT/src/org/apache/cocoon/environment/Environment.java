/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.environment;

import java.io.IOException; 
import java.io.OutputStream; 
import java.net.MalformedURLException; 
import java.util.Dictionary; 

import org.xml.sax.EntityResolver; 
import org.xml.sax.SAXException; 

/**
 * Base interface for an environment abstraction 
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2000-08-31 14:56:34 $
 */

public interface Environment extends EntityResolver {
    // Sitemap methods
    public String getUri (); 
    public void changeContext (String uriprefix, String context) 
        throws MalformedURLException;
    public void redirect (String url) throws IOException;

    // Request methods
    public String getView ();

    // Response methods
    public void setContentType (String mimeType); 
    public void setStatus (int statusCode); 
    public OutputStream getOutputStream() throws IOException; 

    // Object model
    public Dictionary getObjectModel(); 
  }
