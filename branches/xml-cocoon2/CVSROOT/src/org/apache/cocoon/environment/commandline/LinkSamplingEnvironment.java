/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
 
package org.apache.cocoon.environment.commandline;

import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;


import java.net.MalformedURLException;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.Main;
import org.apache.cocoon.environment.AbstractEnvironment;

public class LinkSamplingEnvironment extends AbstractEnvironment {

    private ByteArrayOutputStream stream;
    
    public LinkSamplingEnvironment(String uri, File context) 
    throws MalformedURLException, IOException {
        super(uri, Cocoon.LINK_VIEW, context);
        this.stream = new ByteArrayOutputStream();
    }

    /** 
     * Set the ContentType 
     */ 
    public void setContentType(String contentType) {
        if (!Cocoon.LINK_CONTENT_TYPE.equals(contentType)) {
            // fixme (SM) this static method sucks!!
            Main.warning("The link MIMEtype doesn't match. A probable error occurred into the pipeline");
        }
    }
 
    /** 
     * Get the OutputStream 
     */ 
    public OutputStream getOutputStream() throws IOException {
        return this.stream;
    }

    /**
     * Redirect the client to a new URL
     */
    public void redirect(String newURL) throws IOException {
        // FIXME (SM) What do we do here?
    }

    /** 
     * Indicates if other links are present.
     */ 
    public Collection getLinks() throws IOException {

        // FIXME (SM) I'm sure there is a much faster and less memory consuming
        // way of doing this, but I'm lazy and I don't care at this point.
        // Anyway the parsing is very easy:
        // + http://host/to-be-processed
        // - http://host/not-to-be-processed
        // with +/- at char(0), space at char(1), url starting at char(2)
         
        ArrayList list = new ArrayList();
        BufferedReader buffer = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(stream.toByteArray())));
        while (true) {
            String line = buffer.readLine();
            if (line == null) break;
            if (line.charAt(0) == '+') {
                list.add(line.substring(2));
            }
        }
        return list;
    }
}
