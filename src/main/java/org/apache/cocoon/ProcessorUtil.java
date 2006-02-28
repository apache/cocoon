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

/**
 * This is an utility class for processing Cocoon requests.
 *
 * $Id$
 */
public class ProcessorUtil {

    /**
     * Avoid construction.
     */
    private ProcessorUtil() {}

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

}
