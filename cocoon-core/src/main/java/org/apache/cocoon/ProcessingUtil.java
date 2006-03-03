/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.environment.internal.EnvironmentHelper;

/**
 * This is an utility class for processing Cocoon requests.
 *
 * $Id$
 */
public class ProcessingUtil {

    /** Bean name for the Avalon context. */
    public static String CONTEXT_ROLE = "org.apache.avalon.framework.context.Context";

    /** Bean name for the logger. */
    public static String LOGGER_ROLE = "org.apache.avalon.framework.logger.Logger";

    /** Bean name for the settings. */
    public static String SETTINGS_ROLE = Settings.class.getName();

    /** Bean name for the service manager. */
    public static String SERVICE_MANAGER_ROLE = "org.apache.avalon.framework.service.ServiceManager";

    /**
     * Avoid construction.
     */
    private ProcessingUtil() {}

    /**
     * The cleanup threads that are invoked after the processing of a
     * request is finished.
     */
    private static final ThreadLocal cleanup = new ThreadLocal();

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
     * Get the current sitemap component manager.
     * This method return the current sitemap component manager. This
     * is the manager that holds all the components of the currently
     * processed (sub)sitemap.
     */
    static public ServiceManager getSitemapServiceManager() {
        return EnvironmentHelper.getSitemapServiceManager(); 
    }
}
