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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.configuration.Settings;

/**
 * This is the core Cocoon component.
 * It can be looked up to get access to various information about the
 * current installation.
 *
 * The core of Cocoon is a singleton object that is created on startup.
 *
 * @version SVN $Id$
 * @since 2.2
 */
public class Core
    implements Contextualizable {

    /** Application <code>Context</code> Key for the settings. Please don't
     * use this constant to lookup the settings object. Lookup the core
     * component and use {@link #getSettings()} instead. */
    public static final String CONTEXT_SETTINGS = "settings";

    /**
     * The cleanup threads that are invoked after the processing of a
     * request is finished.
     */
    private static final ThreadLocal cleanup = new ThreadLocal();

    /** The component context. */
    private Context context;

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public static void addCleanupTask(CleanupTask task) {
        List l = (List)cleanup.get();
        if ( l == null ) {
            l = new ArrayList();
            cleanup.set(l);
        }
        l.add(task);
    }

    public static void cleanup() {
        List l = (List)cleanup.get();
        if ( l != null ) {
            final Iterator i = l.iterator();
            while ( i.hasNext() ) {
                final CleanupTask t = (CleanupTask)i.next();
                t.invoke();
            }
            l.clear();
        }
    }

    public static interface CleanupTask {

        void invoke();
    }

    /**
     * Return the settings.
     */
    public Settings getSettings() {
        return getSettings(this.context);
    }

    /**
     * Return the component context.
     * This method allows access to the component context for other components
     * that are not created by an Avalon based container.
     */
    public Context getContext() {
        return this.context;
    }

    /**
     * Return the current settings.
     * Please don't use this method directly, look up the Core component
     * and use {@link #getSettings()} instead.
     * @param context The component context.
     * @return The settings.
     */
    public static final Settings getSettings(Context context) {
        // the settings object is always present
        try {
            return (Settings)context.get(CONTEXT_SETTINGS);
        } catch (ContextException ce) {
            throw new CascadingRuntimeException("Unable to get the settings object from the context.", ce);
        }
    }

}
