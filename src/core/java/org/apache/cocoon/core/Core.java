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
import org.apache.cocoon.configuration.Settings;

/**
 * The Core
 * 
 * @version SVN $Id$
 */
public class Core {

    /** Application <code>Context</code> Key for the settings @since 2.2 */
    public static final String CONTEXT_SETTINGS = "settings";

    private static final ThreadLocal cleanup = new ThreadLocal();
    
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
     * Return the current response
     * @param context The component context
     * @return The response
     * @since 2.2
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
