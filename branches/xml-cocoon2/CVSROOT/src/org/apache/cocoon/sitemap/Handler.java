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

import org.apache.cocoon.Processor;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.language.generator.ProgramGenerator;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.Roles;

import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.Composer;
import org.apache.avalon.ComponentManager;

/**
 * Handles the manageing and stating of one <code>Sitemap</code>
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2000-10-19 14:44:19 $
 */
public class Handler implements Runnable, Configurable, Composer, Processor {

    /** the configuration */
    private Configuration conf;

    /** the component manager */
    private ComponentManager manager;

    /** the parent sitemap component manager */
    private ComponentManager parentSitemapComponentManager;

    /** the source of this sitemap */
    private File sourceFile;

    /** the last error */
    private Exception exception;

    /** the managed sitemap */
    private Sitemap sitemap;
    private boolean check_reload = true;
 
    /** the regenerating thread */ 
    private Thread regeneration;
    private boolean isRegenerationRunning = false;
    private Environment environment;
 
    /** the sitemaps base path */ 
    private String basePath;

    public void compose (ComponentManager manager) {
        this.manager = manager;
    }

    public void configure (Configuration conf) {
        this.conf = conf;
    }

    protected Handler (ComponentManager sitemapComponentManager, String source, boolean check_reload)
    throws FileNotFoundException {
        this.parentSitemapComponentManager = sitemapComponentManager;
        this.check_reload = check_reload;
        String s = null;
        if (source.charAt(source.length() - 1) == File.separatorChar) {
            s = source + "sitemap.xmap";
            this.sourceFile = new File (s);
        } else {
            this.sourceFile = new File (source);
            if (!this.sourceFile.isFile()) {
                s = source + File.separatorChar + "sitemap.xmap";
                this.sourceFile = new File (s);
            }
        }
        if (!this.sourceFile.canRead()) {
            throw new FileNotFoundException ("file " + s + " not found or cannot be opened for reading");
        }
    }

    protected boolean available () {
        return (sitemap != null);
    }

    protected boolean hasChanged () {
        if (sitemap != null) {
            if (check_reload) {
                return sitemap.modifiedSince(this.sourceFile.lastModified());
            }
            return false;
        }
        return true;
    }

    protected boolean isRegenerating () {
        return isRegenerationRunning; 
    }

    protected synchronized void regenerateAsynchronously (Environment environment)
    throws Exception { 
        if (!this.isRegenerationRunning) {
            isRegenerationRunning = true;
            regeneration = new Thread (this);
            this.environment = environment;
            regeneration.start();
        }
    }

    protected synchronized void regenerate (Environment environment) 
    throws Exception { 
        regenerateAsynchronously(environment);
        if (regeneration != null)
            regeneration.join();
    }

    public boolean process (Environment environment) 
    throws Exception {
        throwEventualException();
        return sitemap.process(environment);
    }

    public void setBasePath (String basePath) {
        this.basePath = basePath;
    }

    /** Generate the Sitemap class */
    public void run() {
        Sitemap smap;
        InputSource inputSource = new InputSource (sourceFile.getPath());
        String systemId = inputSource.getSystemId();

        File file = new File(systemId);

        String markupLanguage = "sitemap";
        String programmingLanguage = "java";

        try {
            ProgramGenerator programGenerator = (ProgramGenerator) this.manager.lookup(Roles.PROGRAM_GENERATOR);
            smap = (Sitemap) programGenerator.load(file, markupLanguage, programmingLanguage, environment);
            smap.setParentSitemapComponentManager (this.parentSitemapComponentManager);
            if (smap instanceof Composer) smap.compose(this.manager);
            if (smap instanceof Configurable) smap.configure(this.conf);
            this.sitemap = smap;
        } catch (Exception e) {
            this.exception = e;
        } finally {
            this.regeneration = null;
            this.environment = null;
            this.isRegenerationRunning = false;
        }
    }
    
    public void throwEventualException() throws Exception {
        if (this.exception != null) throw this.exception;
    }
} 
