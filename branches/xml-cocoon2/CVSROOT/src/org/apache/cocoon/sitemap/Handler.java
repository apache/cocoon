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

import java.net.URL;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.apache.cocoon.Processor;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.language.generator.ProgramGenerator;
import org.apache.cocoon.components.language.generator.CompiledComponent;
import org.apache.cocoon.components.pipeline.StreamPipeline;
import org.apache.cocoon.components.pipeline.EventPipeline;
import org.apache.cocoon.components.url.URLFactory;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.Roles;

import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.component.Composable;
import org.apache.avalon.component.ComponentManager;
import org.apache.avalon.context.Contextualizable;
import org.apache.avalon.context.Context;
import org.apache.avalon.component.Component;
import org.apache.avalon.Disposable;
import org.apache.avalon.logger.AbstractLoggable;
import org.apache.avalon.logger.Loggable;

/**
 * Handles the manageing and stating of one <code>Sitemap</code>
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.25 $ $Date: 2001-04-20 20:50:14 $
 */
public class Handler extends AbstractLoggable implements Runnable, Configurable, Composable, Contextualizable, Processor, Disposable {
    private Context context;

    /** the configuration */
    private Configuration conf;

    /** the component manager */
    private ComponentManager manager;

    /** the source of this sitemap */
    private String source;
    private File sourceFile;

    /** the URLFactory */
    private URLFactory urlFactory;

    /** the last error */
    private Exception exception;

    /** the managed sitemap */

    private Sitemap sitemap = null;
    private boolean check_reload = true;

    /** the regenerating thread */
    private Thread regeneration;
    private boolean isRegenerationRunning = false;
    private Environment environment;

    /** the sitemaps base path */
    private String basePath;

    public void compose (ComponentManager manager) {
        this.manager = manager;
        try {
            urlFactory = (URLFactory) manager.lookup(Roles.URL_FACTORY);
        } catch (Exception e) {
            getLogger().error ("cannot obtain URLFactory", e);
        }
    }

    public void configure (Configuration conf) {
        this.conf = conf;
    }

    public void contextualize (Context context) {
        this.context = context;
    }

    protected Handler (String source, boolean check_reload)
    throws FileNotFoundException {
        this.check_reload = check_reload;
        this.source = source;
    }

    protected boolean available () {
        return (sitemap != null);
    }

    protected boolean hasChanged () {
        if (available()) {
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
        String s;
        if (this.source.charAt(this.source.length() - 1) == '/') {
            s = this.source + "sitemap.xmap";
        } else {
            s = this.source;
        }
        this.sourceFile = new File(urlFactory.getURL(environment.resolveEntity(null, s).getSystemId()).getFile());
        if (!this.sourceFile.canRead()) {
            throw new FileNotFoundException ("file " + this.sourceFile.toString() + " not found or cannot be opened for reading");
        }
        if (!this.isRegenerationRunning) {
            isRegenerationRunning = true;
            regeneration = new Thread (this);
            this.environment = environment;
            regeneration.start();
        }
    }

    protected synchronized void regenerate (Environment environment)
    throws Exception {
        getLogger().debug("Beginning sitemap regeneration");
        regenerateAsynchronously(environment);
        if (regeneration != null)
            regeneration.join();
    }

    public boolean process (Environment environment)
    throws Exception {
        checkSanity();
        return sitemap.process(environment);
    }

    public boolean process (Environment environment, StreamPipeline pipeline, EventPipeline eventPipeline)
    throws Exception {
        checkSanity();
        return sitemap.process(environment, pipeline, eventPipeline);
    }

    private void checkSanity () throws Exception {
        throwEventualException();
        if (sitemap == null) {
            getLogger().fatalError("Sitemap is not set for the Handler!!!!");
            throw new RuntimeException("The Sitemap is null, this should never be!");
        }
    }

    public void setBasePath (String basePath) {
        this.basePath = basePath;
    }

    /** Generate the Sitemap class */
    public void run() {
        Sitemap smap;

        String markupLanguage = "sitemap";
        String programmingLanguage = "java";

        ProgramGenerator programGenerator = null;
        try {
            /* FIXME: Workaround -- set the logger XSLTFactoryLoader used to generate source
             * within the sitemap generation phase.
             * Needed because we never have the opportunity to handle the lifecycle of the
             * XSLTFactoryLoader, since it is created by the Xalan engine.
             */
            XSLTFactoryLoader.setLogger(getLogger());

            programGenerator = (ProgramGenerator) this.manager.lookup(Roles.PROGRAM_GENERATOR);
            smap = (Sitemap) programGenerator.load(this.sourceFile, markupLanguage, programmingLanguage, environment);

            if (this.sitemap != null) {
               programGenerator.release((CompiledComponent) this.sitemap);
            }

            this.sitemap = smap;
            getLogger().debug("Sitemap regeneration complete");

            if (this.sitemap != null) {
                getLogger().debug("The sitemap has been successfully compiled!");
            } else {
                getLogger().debug("No errors, but the sitemap has not been set.");
            }
        } catch (Throwable t) {
            getLogger().error("Error compiling sitemap", t);

            if (t instanceof Exception) {
              this.exception = (Exception) t;
            }
        } finally {
            if(programGenerator != null) this.manager.release((Component) programGenerator);
            this.regeneration = null;
            this.environment = null;
            this.isRegenerationRunning = false;
        }
    }

    public void throwEventualException() throws Exception {
        if (this.exception != null) throw this.exception;
    }

    public Exception getException() {
        return this.exception;
    }

    /**
     * dispose
     */
    public void dispose() {
        if(urlFactory != null) manager.release((Component)urlFactory);
    }
}
