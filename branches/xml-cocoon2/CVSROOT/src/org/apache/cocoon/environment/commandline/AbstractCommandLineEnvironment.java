/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.environment.commandline;

import java.io.File;
import java.io.OutputStream;
import java.io.IOException;

import java.net.MalformedURLException;

import org.apache.cocoon.environment.AbstractEnvironment;

/**
 * This environment is used to save the requested file to disk.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-10-06 21:25:27 $
 */

public abstract class AbstractCommandLineEnvironment extends AbstractEnvironment {

    protected String contentType;
    protected OutputStream stream;
    
    public AbstractCommandLineEnvironment(String uri, String view, File context, OutputStream stream)
    throws MalformedURLException {
        super(uri, view, context);
        this.stream = stream;
    }

    /**
     * Redirect the client to a new URL
     */
    public void redirect(String newURL) throws IOException {
        throw new RuntimeException (this.getClass().getName() + ".redirect(String url) method not yet implemented!");
    }
    
    /**
     * Set the ContentType
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Set the ContentType
     */
    public String getContentType() {
        return this.contentType;
    }

    /**
     * Get the OutputStream
     */
    public OutputStream getOutputStream() throws IOException {
        return this.stream;
    }
}


