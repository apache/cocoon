/*
 * Copyright 2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Constants;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.core.container.ComponentLocatorWrapper;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.sitemap.ComponentLocator;
import org.apache.cocoon.sitemap.Sitemap;
import org.apache.commons.lang.NotImplementedException;

/**
 * This is the core Cocoon component.
 * It can be looked up to get access to various information about the
 * current installation.
 *
 * The core of Cocoon is a singleton object that is created on startup.
 *
 * @version $Id$
 * @since 2.2
 */
public class Core {

    /** The key to lookup the component. */
    public static String ROLE = Core.class.getName();

    /**
     * The cleanup threads that are invoked after the processing of a
     * request is finished.
     */
    private static final ThreadLocal cleanup = new ThreadLocal();

    /** The component context. */
    private final Context context;

    private final Settings settings;

    /**
     * Constructor
     * The core object is created by the {@link CoreUtil} class. Never construct
     * a core object yourself (apart from testing of course)!
     * @param s The settings
     * @param c The context
     */
    public Core(Settings s, Context c) {
        this.settings = s;
        this.context = c;
    }

    /**
     * Add a cleanup task.
     * A cleanup task is run after a request is processed.
     * @param task The task to run.
     */
    public static void addCleanupTask(CleanupTask task) {
        List l = (List)cleanup.get();
        if ( l == null ) {
            l = new ArrayList();
            cleanup.set(l);
        }
        l.add(task);
    }

    /**
     * Invoke all registered cleanup tasks for the current process.
     * This method should not be called directly!
     */
    public static void cleanup() {
        List l = (List)cleanup.get();
        if ( l != null ) {
            final Iterator i = l.iterator();
            while ( i.hasNext() ) {
                final CleanupTask t = (CleanupTask)i.next();
                t.invoke();
            }
            l.clear();
            cleanup.set(null);
        }
    }

    /**
     * The interface for the cleanup task.
     * A cleanup task can be run after a request has been processed.
     */
    public static interface CleanupTask {

        /**
         * Start the cleanup.
         * This method should never raise any exception!
         */
        void invoke();
    }

    /**
     * Return the settings.
     */
    public Settings getSettings() {
        return this.settings;
    }

    /**
     * Update the dynamic settings at runtime.
     * @param dynSettings
     */
    public void update(DynamicSettings dynSettings) {
        throw new NotImplementedException("The update method is not implemented yet.");
    }

    /**
     * Return the environment context object.
     * @return The environment context.
     */
    public org.apache.cocoon.environment.Context getEnvironmentContext() {
        try {
            return (org.apache.cocoon.environment.Context)this.context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        } catch (ContextException ce) {
            throw new CoreResourceNotFoundException("Unable to get the environment object from the context.", ce);
        }
    }

    /**
     * Return the current object model
     * @return The object model.
     */
    public Map getCurrentObjectModel() {
        return ContextHelper.getObjectModel(this.context);
    }

    /**
     * Return the work directory.
     */
    public File getWorkDirectory() {
        try {
            return (File)this.context.get(Constants.CONTEXT_WORK_DIR);
        } catch (ContextException ce) {
            throw new CoreResourceNotFoundException("Unable to get the working directory from the context.", ce);
        }        
    }

    /**
     * Return the upload directory.
     */
    public File getUploadDirectory() {
        try {
            return (File)this.context.get(Constants.CONTEXT_UPLOAD_DIR);
        } catch (ContextException ce) {
            throw new CoreResourceNotFoundException("Unable to get the upload directory from the context.", ce);
        }        
    }

    /**
     * Return the cache directory.
     */
    public File getCacheDirectory() {
        try {
            return (File)this.context.get(Constants.CONTEXT_CACHE_DIR);
        } catch (ContextException ce) {
            throw new CoreResourceNotFoundException("Unable to get the cache directory from the context.", ce);
        }        
    }

    /**
     * Return the current sitemap.
     * @return The current sitemap or null if no request is currently processed
     */
    public Sitemap getCurrentSitemap() {
        Processor p = EnvironmentHelper.getCurrentProcessor();
        if ( p != null ) {
            return SITEMAP;            
        }
        return null;
    }

    private final static Sitemap SITEMAP = new SitemapImpl();

    public final static class SitemapImpl implements Sitemap {

        /**
         * @see org.apache.cocoon.sitemap.Sitemap#getComponentLocator()
         */
        public ComponentLocator getComponentLocator() {
            final ServiceManager m = EnvironmentHelper.getSitemapServiceManager();
            ComponentLocator l = null;
            if ( m != null ) {
                if ( !(m instanceof ComponentLocator) ) {
                    l = new ComponentLocatorWrapper(m);
                } else {
                    l = (ComponentLocator)m;
                }
            }
            return l;
        }

        /**
         * @see org.apache.cocoon.sitemap.Sitemap#getProcessor()
         */
        public Processor getProcessor() {
            return EnvironmentHelper.getCurrentProcessor();
        }

        /**
         * @see org.apache.cocoon.sitemap.Sitemap#getInterpreter(java.lang.String)
         */
        public Interpreter getInterpreter(String language) {
            // TODO We ignore the language for now
            return (Interpreter)this.getProcessor().getAttribute(Interpreter.ROLE);
        }
    }
}
