/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.environment.commandline;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileOutputStream;

import java.util.Map;

import java.net.MalformedURLException;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.environment.AbstractEnvironment;

public class FileSavingEnvironment extends AbstractEnvironment {

    private String contentType;
    private OutputStream stream;
    
    public FileSavingEnvironment(String uri, File context, Map links, OutputStream stream)
    throws MalformedURLException {
        super(uri, null, context);
        this.stream = stream;
        this.objectModel.put(Cocoon.LINK_OBJECT, links);
    }

    /**
     * Redirect the client to a new URL
     */
    public void redirect(String newURL) throws IOException {
        // FIXME (SM) What do we do here?
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


