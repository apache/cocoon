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

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.language.generator.ProgramGenerator;
import org.apache.cocoon.environment.Environment;

import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.Composer;
import org.apache.avalon.ComponentManager;

/**
 * Handles the manageing and stating of one <code>Sitemap</code>
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-07-22 20:41:57 $
 */
public class SitemapHandler implements Runnable, Configurable, Composer {

    /** the configuration */
    private Configuration conf = null;

    /** the component manager */
    private ComponentManager manager = null;

    /** the source of this sitemap */
    private File source = null;
    private long changeDate = -1L;

    /** the last error */
    private Exception exception = null;

    /** the managed sitemap */
    private Sitemap sitemap = null;
 
    /** the regenerating thread */ 
    private Thread regeneration = null; 
 
    /** the sitemaps base path */ 
    private String basePath = null; 

    public void setComponentManager (ComponentManager manager) {
        this.manager = manager;
    }

    public void setConfiguration (Configuration conf) {
        this.conf = conf;
    }

    protected SitemapHandler (String source) throws FileNotFoundException {
        System.out.println("SitemapHandler: Instantiating sitemap \""+source+"\"");
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
        System.out.println("SitemapHandler: Instantiatet sitemap \""+f.getPath()+"\"");
    }

    protected void throwError () 
    throws ProcessingException, SAXException, IOException, InterruptedException {
        Exception e = exception;
        exception = null;
        if (e instanceof ProcessingException) {
            throw (ProcessingException) exception;
        } else if (e instanceof SAXException) {
            throw (SAXException) exception;
        } else if (e instanceof IOException) {
            throw (IOException) exception;
        } else if (e instanceof InterruptedException) {
            throw (InterruptedException) exception;
        } else if (e != null) {
            throw new ProcessingException ("Unknown Exception raised: "
                                         + exception.toString());
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

    protected boolean isRegenerating () {
        if (regeneration == null)
            return false;
        return regeneration.isAlive();
    }

    protected void regenerateAsynchroniously () {
        if (!isRegenerating()) {
            regeneration = new Thread (this);
            regeneration.start();
        }
    }

    protected void regenerate () 
    throws ProcessingException, SAXException, IOException, InterruptedException { 
        if (!isRegenerating()) {
            regeneration = new Thread (this);
            regeneration.start();
            regeneration.join();
            throwError();
        }
    }

    public boolean process (Environment environment, OutputStream out) 
    throws ProcessingException, SAXException, IOException, InterruptedException {
        this.throwError();
        return sitemap.process (environment, out);
    }

    /** Generate the Sitemap class */
    public void run() {
/*
    private void generateSitemap (String sitemapName) 
            throws java.net.MalformedURLException, IOException, 
                   org.apache.cocoon.ProcessingException {
*/

        InputSource inputSource = new InputSource (source.getPath());
        String systemId = inputSource.getSystemId();
        System.out.println ("C2 generateSitemap: "+systemId);

        File file = new File(systemId);

        String markupLanguage = "sitemap";
        String programmingLanguage = "java";

        ProgramGenerator programGenerator = null;

        System.out.println ("C2 generateSitemap: obtaining programGenerator");
        programGenerator = (ProgramGenerator) this.manager.getComponent("program-generator");
        System.out.println ("C2 generateSitemap: programGenerator obtained");

        System.out.println ("C2 generateSitemap: obtaining generator");
        try {
            sitemap = (Sitemap) programGenerator.load(file, markupLanguage, programmingLanguage);
            System.out.println ("C2 generateSitemap: generator obtained");
            if (sitemap instanceof Composer) sitemap.setComponentManager(this.manager);
            if (sitemap instanceof Configurable) sitemap.setConfiguration(this.conf);
            sitemap.setBasePath (basePath);
            System.out.println ("C2 generateSitemap: generator called");
        } catch (Exception e) {
            synchronized (this.exception) {
                this.exception = e;
            }
        } finally {
            regeneration = null;
        }
    }

    public void setBasePath (String basePath) {
        this.basePath = basePath;
    }
} 
