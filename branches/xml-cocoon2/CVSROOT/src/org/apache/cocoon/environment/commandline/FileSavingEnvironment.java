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
import java.net.MalformedURLException;
import org.apache.cocoon.environment.AbstractEnvironment;

public class FileSavingEnvironment extends AbstractEnvironment {

    private File destDir;
    private String extention;

    public FileSavingEnvironment(String uri, File context, File destDir)
    throws MalformedURLException {
        super(uri, null, context);
        this.destDir = destDir;
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
        this.extention = getExtention(contentType);
    }

    /**
     * Get the OutputStream
     */
    public OutputStream getOutputStream() throws IOException {
        File file = new File(destDir, uri + "." + extention);
        return new FileOutputStream(file);
    }
    
    private String getExtention(String contentType) {
        if ("text/html".equals(contentType)) {
            return "html";
        } else if ("application/pdf".equals(contentType)) {
            return "pdf";
        } else {
            return "unknown";
        }
    }
}


