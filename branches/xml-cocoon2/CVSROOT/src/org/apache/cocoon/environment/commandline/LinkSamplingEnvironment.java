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
import java.io.ByteArrayOutputStream;

import java.net.MalformedURLException;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.environment.AbstractEnvironment;

public class LinkSamplingEnvironment extends AbstractEnvironment {

    private LineLister links;
    
    public LinkSamplingEnvironment(String uri) 
    throws MalformedURLException {
        super(uri, Cocoon.LINK_VIEW, "");
        this.links = new LineLister();
    }

    /** 
     * Set the ContentType 
     */ 
    public void setContentType(String contentType) {
        if (!Cocoon.LINK_CONTENT_TYPE.equals(contentType)) {
            throw new RuntimeException("The link MIME type doesn't match."
                + " Make sure you used the appropriate 'link' serialier");
        }
    }
 
    /** 
     * Get the OutputStream 
     */ 
    public OutputStream getOutputStream() throws IOException {
        return this.links;
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
    public Collection getLinks() {
        return this.links.list();
    }
    
    /**
     * This class parses the output stream and generates a list
     * out of the lines received.
     */
    class LineLister extends ByteArrayOutputStream {

        private List links = Collections.synchronizedList(new ArrayList());
        
        public void write(int c) {
            super.write(c);
            if (c == '\n') {
                synchronized (links) {
                    links.add(this.toString());
                    this.reset();
                }
            }
        }
        
        public Collection list() {
            synchronized (links) {
                return links;
            }
        }
    }
}
