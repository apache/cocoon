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
import java.util.Map;

import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * Base interface for an environment abstraction
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.11 $ $Date: 2000-09-02 21:12:35 $
 */

public interface Environment extends EntityResolver {

    /**
     * Get the URI to process
     */
    public String getUri();

    /**
     * Get the view to process
     */
    public String getView();

    /**
     * Change the context from uriprefix to context
     */
    public void changeContext(String uriprefix, String context) throws MalformedURLException;

    /**
     * Redirect to the given URL
     */
    public void redirect(String url) throws IOException;

    /**
     * Set the content type of the generated resource
     */
    public void setContentType(String mimeType);

    /**
     * Set the response status code
     */
    public void setStatus(int statusCode); 

    /**
     * Get the output stream where to write the generated resource.
     */
    public OutputStream getOutputStream() throws IOException;

    /**
     * Get the underlying object model
     */
    public Map getObjectModel();

}

