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
package org.apache.cocoon.components.language.generator;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.classloader.ClassLoaderManager;
import org.apache.cocoon.components.language.programming.Program;
import org.apache.cocoon.core.container.AbstractComponentHandler;
import org.apache.cocoon.core.container.CocoonServiceSelector;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This interface is the common base of all Compiled Components.  This
 * includes Sitemaps and XSP Pages
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id$
 */
public class GeneratorSelector 
extends CocoonServiceSelector  {

    public static String ROLE = "org.apache.cocoon.components.language.generator.ServerPages";

    protected ClassLoaderManager classManager;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) 
    throws ServiceException {
        super.service(manager);

        this.classManager = (ClassLoaderManager) manager.lookup(ClassLoaderManager.ROLE);

        try {
            this.classManager.addDirectory((File) this.context.get(Constants.CONTEXT_WORK_DIR));
        } catch (Exception e) {
            throw new ServiceException(ROLE, "Could not add repository to ClassLoaderManager", e);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceSelector#select(java.lang.Object)
     */
    public Object select(Object hint) throws ServiceException {

        AbstractComponentHandler handler = (AbstractComponentHandler) this.componentHandlers.get(hint);
        if (handler == null) {
            throw new ServiceException(ROLE, "Could not find component for hint: " + hint);
        }

        try {
            Object component = handler.get();
            componentMapping.put(component, handler);
            return component;
        } catch (Exception ce) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Could not access component for hint: " + hint, ce);
            }
            throw new ServiceException(ROLE, "Could not access component for hint: " + hint, ce);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.ServiceSelector#release(java.lang.Object)
     */
    public void release(Object component) {
        AbstractComponentHandler handler = (AbstractComponentHandler)componentMapping.remove(component);
        if (handler != null) {
            try {
                handler.put(component);
            } catch (Exception e) {
                getLogger().error("Error trying to release component", e);
            }
        }
    }

    public void addGenerator(ServiceManager newManager,
                             Object hint, Program generator)
    throws Exception {
        try {
            final AbstractComponentHandler handler =
                    generator.getHandler(newManager, this.context );
            handler.initialize();
            this.componentHandlers.put(hint, handler);
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Adding " + generator.getName() + " for " + hint);
            }
        } catch(final Exception e) {
            // Error will be logged by caller. This is for debug only
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Could not set up Component for hint: " + hint, e);
            }
            throw e;
        }
    }

    public void removeGenerator(Object hint) {
        AbstractComponentHandler handler = (AbstractComponentHandler) this.componentHandlers.remove(hint);
        if (handler != null) {
            handler.dispose();
            this.classManager.reinstantiate();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Removing " + handler.getClass().getName() + " for " + hint);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        this.serviceManager.release(this.classManager);

        synchronized(this) {
            Iterator keys = this.componentHandlers.keySet().iterator();
            List keyList = new ArrayList();

            while(keys.hasNext()) {
                Object key = keys.next();
                AbstractComponentHandler handler =
                    (AbstractComponentHandler)this.componentHandlers.get(key);

                handler.dispose();

                keyList.add(key);
            }

            keys = keyList.iterator();

            while(keys.hasNext()) {
                this.componentHandlers.remove(keys.next());
            }

            keyList.clear();
        }

        super.dispose();
    }
}
