/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import org.xml.sax.SAXException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;

/**
 * Handles the manageing and stating of one <code>Sitemap</code>
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-07-20 21:57:16 $
 */
public class SitemapHandler implements Runnable {

    /** the source of this sitemap */
    private File source = null;
    private long changeDate = -1L;

    /** the last error */
    private Exception exception = null;

    /** the managed sitemap */
    private SitemapProcessor sitemap = null;

    /** the regenerating thread */
    private Thread regeneration = null;

    protected SitemapHandler (String source) throws FileNotFoundException {
        File f = null;
        String s = null;
        if (source.charAt(source.length()-1) == '/') {
            s = source+"sitemap.xmap";
            f = new File (s);
        } else {
            f = new File (source);
            if (!f.isFile()) {
                s = source+File.separatorChar+"sitemap.xmap";
                f = new File (s);
            }
            if (!f.canRead()) {
                throw new FileNotFoundException ("file "+s+" not found or cannot be opened for reading");
            }
        }
        this.source = f;
        changeDate = f.lastModified();
    }

    protected void throwError () throws Exception {
        if (exception != null) {
            throw exception;
        }
    }

    protected boolean available () {
        return (sitemap != null);
    }

    protected boolean hasChanged () {
        if (sitemap != null) {
            return sitemap.modifiedSince(this.changeDate);
        }
        return true;
    }
/*
    protected boolean modifiedSince () {
        if (sitemap != null) {
            return sitemap.modifiedSince(this.changeDate);
        }
        return true;
    }
*/
    protected boolean isRegenerating () {
        return regeneration.isAlive();
    }

    protected void regenerateAsynchroniously () {
        if (!isRegenerating()) {
            regeneration = new Thread (this);
            regeneration.start();
        }
    }

    protected void regenerate () throws InterruptedException {
        if (!isRegenerating()) {
            regeneration = new Thread (this);
            regeneration.start();
            regeneration.join();
        }
    }

    public boolean process (Environment environment, OutputStream out) 
    throws ProcessingException, SAXException, IOException {
        return sitemap.process (environment, out);
    }

    public void run() {
    }
} 
