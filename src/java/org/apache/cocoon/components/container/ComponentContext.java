/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.components.container;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.internal.EnvironmentHelper;

import java.util.Map;

/**
 * This is the {@link Context} implementation for Cocoon components.
 * It extends the {@link DefaultContext} by a special handling for
 * getting objects from the object model and other application information.
 * 
 * @see org.apache.cocoon.components.ContextHelper
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ComponentContext.java,v 1.6 2004/03/08 13:57:37 cziegeler Exp $
 */

public class ComponentContext
    extends DefaultContext {

    protected static final String OBJECT_MODEL_KEY_PREFIX = ContextHelper.CONTEXT_OBJECT_MODEL + '.';

    /**
     * Create a Context with specified data and parent.
     *
     * @param contextData the context data
     * @param parent the parent Context (may be null)
     */
    public ComponentContext(final Map contextData, final Context parent) {
        super( contextData, parent );
    }

    /**
     * Create a Context with specified data.
     *
     * @param contextData the context data
     */
    public ComponentContext(final Map contextData) {
        super( contextData );
    }

    /**
     * Create a Context with specified parent.
     *
     * @param parent the parent Context (may be null)
     */
    public ComponentContext(final Context parent) {
        super( parent );
    }

    /**
     * Create a Context with no parent.
     *
     */
    public ComponentContext() {
        super();
    }

    /**
     * Retrieve an item from the Context.
     *
     * @param key the key of item
     * @return the item stored in context
     * @throws ContextException if item not present
     */
    public Object get( final Object key )
    throws ContextException {
        if ( ContextHelper.CONTEXT_OBJECT_MODEL.equals(key)) {
            final Environment env = EnvironmentHelper.getCurrentEnvironment();
            if ( env == null ) {
                throw new ContextException("Unable to locate " + key + " (No environment available)");
            }
            return env.getObjectModel();
        } else if ( ContextHelper.CONTEXT_SITEMAP_SERVICE_MANAGER.equals(key)) {
            final Object manager = EnvironmentHelper.getSitemapServiceManager();
            if ( manager == null ) {
                throw new ContextException("Unable to locate " + key + " (No environment available)");
            }
            return manager;
        }
        if ( key instanceof String ) {
            String stringKey = (String)key;
            if ( stringKey.startsWith(OBJECT_MODEL_KEY_PREFIX) ) {
                final Environment env = EnvironmentHelper.getCurrentEnvironment();
                if ( env == null ) {
                    throw new ContextException("Unable to locate " + key + " (No environment available)");
                }
                final Map objectModel = env.getObjectModel();
                String objectKey = stringKey.substring(OBJECT_MODEL_KEY_PREFIX.length());

                Object o = objectModel.get( objectKey );
                if ( o == null ) {
                    final String message = "Unable to locate " + key;
                    throw new ContextException( message );
                }
                return o;
            }
        }
        return super.get( key );
    }

}
