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
 * @version CVS $Revision: 1.1.2.5 $ $Date: 2000-07-27 21:49:05 $
 */
public class SitemapHandler implements Runnable, Configurable, Composer {

    /** the configuration */
    private Configuration conf = null;

    /** the component manager */
    private ComponentManager manager = null;

    /** the source of this sitemap */
    private File sourceFile = null;

    /** the last error */
    private Exception exception = null;

    /** the managed sitemap */
    private Sitemap sitemap = null;
    private boolean check_reload = true;
 
    /** the regenerating thread */ 
    private Thread regeneration = null; 
    private boolean isRegenerationRunning = false;
    private Environment environment = null;
 
    /** the sitemaps base path */ 
    private String basePath = null; 

    public void setComponentManager (ComponentManager manager) {
        this.manager = manager;
    }

    public void setConfiguration (Configuration conf) {
        this.conf = conf;
    }

    protected SitemapHandler (String source, boolean check_reload) throws FileNotFoundException {
        System.out.println("SitemapHandler: Instantiating sitemap \""+source+"\"");
        this.check_reload = check_reload;
        String s = null;
System.out.println("SitemapHandler: source=\""+source+"\"");
        if (source.charAt(source.length()-1) == File.separatorChar) {
            s = source+"sitemap.xmap";
            this.sourceFile = new File (s);
        } else {
            sourceFile = new File (source);
            if (!sourceFile.isFile()) {
System.out.println("SitemapHandler: source=\""+source+"\" is not a file");
                s = source+File.separatorChar+"sitemap.xmap";
                sourceFile = new File (s);
            }
            if (!sourceFile.canRead()) {
                throw new FileNotFoundException ("file "+s+" not found or cannot be opened for reading");
            }
        }
        System.out.println("SitemapHandler: Instantiatet sitemap \""+sourceFile.getPath()+"\"");
    }

    protected void throwError () 
    throws ProcessingException, SAXException, IOException, InterruptedException {
        System.out.println("SitemapHandler.throwError()");
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
        System.out.println("SitemapHandler.throwError() no Exception to throw");
    }

    protected boolean available () {
        System.out.println("SitemapHandler.available() = "+(sitemap!=null?"true":"false"));
        return (sitemap != null);
    }

    protected boolean hasChanged () {
        System.out.print("SitemapHandler.hasChanged() = ");
        if (sitemap != null) {
            if (check_reload) {
                System.out.println((sitemap.modifiedSince(this.sourceFile.lastModified())?"true":"false"));
                return sitemap.modifiedSince(this.sourceFile.lastModified());
            }
            System.out.println("false");
            return false;
        }
        System.out.println("true");
        return true;
    }

    protected boolean isRegenerating () {
        System.out.print("SitemapHandler.isRegenerating() = "+(isRegenerationRunning?"true":"false"));
        return isRegenerationRunning; 
    }

    protected synchronized void regenerateAsynchroniously (Environment environment) {
        System.out.println("SitemapHandler.regenerateAsynchroniously()");
        if (!this.isRegenerationRunning) {
            isRegenerationRunning = true;
            regeneration = new Thread (this);
            this.environment = environment;
            regeneration.start();
        }
    }

    protected synchronized void regenerate (Environment environment) 
    throws ProcessingException, SAXException, IOException, InterruptedException { 
        System.out.println("SitemapHandler.regenerate()");
        if (!this.isRegenerationRunning) {
            System.out.println("SitemapHandler.regenerate(): regenerating");
            isRegenerationRunning = true;
            regeneration = new Thread (this);
            this.environment = environment;
            regeneration.start();
            regeneration.join();
            throwError();
        } else {
            System.out.println("SitemapHandler.regenerate(): regenerating already in progress");
        }
    }

    public boolean process (Environment environment, OutputStream out) 
    throws Exception {
        System.out.println("SitemapHandler.process()");
        this.throwError();
        return sitemap.process (environment, out);
    }

    /** Generate the Sitemap class */
    public void run() {
        System.out.println("SitemapHandler.run()");

        Sitemap smap = null;
        InputSource inputSource = new InputSource (sourceFile.getPath());
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
            smap = (Sitemap) programGenerator.load(file, markupLanguage, 
                                 programmingLanguage, environment);
            System.out.println ("C2 generateSitemap: generator obtained");
            if (smap instanceof Composer) smap.setComponentManager(this.manager);
            if (smap instanceof Configurable) smap.setConfiguration(this.conf);
            this.sitemap = smap;
            System.out.println ("C2 generateSitemap: generator called");
        } catch (Exception e) {
            synchronized (this.exception) {
                this.exception = e;
            }
        } finally {
            System.out.println("SitemapHandler.run(): finally");
            regeneration = null;
            environment = null;
            isRegenerationRunning = false;
        }
    }

    public void setBasePath (String basePath) {
        this.basePath = basePath;
    }
} 
