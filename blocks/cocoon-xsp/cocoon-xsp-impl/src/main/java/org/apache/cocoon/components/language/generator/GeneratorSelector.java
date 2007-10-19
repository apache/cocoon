/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.components.classloader.ClassLoaderManager;
import org.apache.cocoon.components.language.programming.Program;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.xsp.handler.AbstractComponentHandler;
import org.apache.cocoon.xsp.handler.ComponentHandler;

/**
 * This interface is the common base of all Compiled Components.  This
 * includes Sitemaps and XSP Pages
 *
 * @version $Id$
 */
public class GeneratorSelector extends AbstractLogEnabled
                               implements ThreadSafe, Contextualizable, Serviceable {

    public static final String ROLE = "org.apache.cocoon.components.language.generator.ServerPages";

    private Context context;
    
    private ServiceManager serviceManager;
    
    /** Static component mapping handlers. */
    protected final Map componentMapping = Collections.synchronizedMap(new HashMap());

    /** Used to map roles to ComponentHandlers. */
    protected final Map componentHandlers = Collections.synchronizedMap(new HashMap());
    
    protected ClassLoaderManager classManager;


    public void contextualize(Context context) {
        this.context = context;
    }
    
    public void service(ServiceManager manager) throws ServiceException {
        this.serviceManager = manager;

        this.classManager = (ClassLoaderManager) manager.lookup(ClassLoaderManager.ROLE);

        final Settings settings = (Settings) manager.lookup(Settings.ROLE);
        try {
            this.classManager.addDirectory(new File(settings.getWorkDirectory()));
        } catch (Exception e) {
            throw new ServiceException(ROLE, "Could not add repository to ClassLoaderManager", e);
        }
    }

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
            final ComponentHandler handler =
                    generator.getHandler(newManager, this.context);
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
        ComponentHandler handler = (ComponentHandler) this.componentHandlers.remove(hint);
        if (handler != null) {
            handler.dispose();
            this.classManager.reinstantiate();
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Removing " + handler.getClass().getName() + " for " + hint);
            }
        }
    }

    public void dispose() {
        this.serviceManager.release(this.classManager);

        synchronized(this) {
            Iterator keys = this.componentHandlers.keySet().iterator();
            List keyList = new ArrayList();

            while (keys.hasNext()) {
                Object key = keys.next();
                AbstractComponentHandler handler =
                        (AbstractComponentHandler) this.componentHandlers.get(key);

                handler.dispose();

                keyList.add(key);
            }

            keys = keyList.iterator();
            while (keys.hasNext()) {
                this.componentHandlers.remove(keys.next());
            }

            keyList.clear();
        }
    }
}
