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
 * @version CVS $Revision: 1.1.2.18 $ $Date: 2001-04-20 11:27:11 $
 */

public interface Environment extends EntityResolver {

    /**
     * Get the URI to process
     */
    String getURI();

    /**
     * Get the view to process
     */
    String getView();

    /**
     * Get the action to process
     */
    String getAction();

    /**
     * Change the context from uriprefix to context
     */
    void changeContext(String uriprefix, String context) throws Exception;

    /**
     * Redirect to the given URL
     */
    void redirect(boolean sessionmode, String url) throws IOException;

    /**
     * Set the content type of the generated resource
     */
    void setContentType(String mimeType);

    /**
     * Set the length of the generated content
     */
    void setContentLength(int length);

    /**
     * Set the response status code
     */
    void setStatus(int statusCode);

    /**
     * Get the output stream where to write the generated resource.
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * Get the underlying object model
     */
    Map getObjectModel();

    /**
     * Push a new URI for processing
     */
    void pushURI(String uri);

    /**
     * Pop last pushed URI
     */
    String popURI();

    /**
     * Check if the response has been modified since the same
     * "resource" was requested.
     * The caller has to test if it is really the same "resource"
     * which is requested.
     * @result true if the response is modified or if the
     *         environment is not able to test it
     */
    boolean isResponseModified(long lastModified);

    /**
     * Mark the response as not modified.
     */
    void setResponseIsNotModified();
}

