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

import org.apache.avalon.excalibur.component.ComponentHandler;
import org.apache.avalon.excalibur.component.ExcaliburComponentSelector;
import org.apache.avalon.excalibur.component.LogkitLoggerManager;
import org.apache.avalon.excalibur.component.RoleManager;
import org.apache.avalon.excalibur.logger.LogKitManager;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.context.Context;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.classloader.ClassLoaderManager;
import org.apache.cocoon.components.language.programming.Program;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This interface is the common base of all Compiled Components.  This
 * includes Sitemaps and XSP Pages
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="mailto:vgritsenko@apache.org">Vadim Gritsenko</a>
 * @version CVS $Id: GeneratorSelector.java,v 1.5 2004/03/05 13:02:47 bdelacretaz Exp $
 */
public class GeneratorSelector extends ExcaliburComponentSelector implements Disposable {

    public static String ROLE = "org.apache.cocoon.components.language.generator.ServerPages";

    private ClassLoaderManager classManager;

    /** The component manager */
    protected ComponentManager manager;

    private LogkitLoggerManager logKitManager;

    protected Context context;

    protected RoleManager roles;

    protected Map componentHandlers = new HashMap();

    /** Dynamic component handlers mapping. */
    private Map componentMapping = new HashMap();


    public void contextualize(Context context) {
        super.contextualize(context);
        this.context = context;
    }

    public void setRoleManager(RoleManager roleMgr) {
        super.setRoleManager(roleMgr);
        this.roles = roleMgr;
    }

    /**
     * Configure the LogKitManager
     */
    public void setLogKitManager( final LogKitManager logkit ) {
        super.setLogKitManager(logkit);
        if( null == this.logKitManager ) {
             this.logKitManager = new LogkitLoggerManager( null, logkit );
        }
    }

    /**
     * Configure the LoggerManager.
     */
    public void setLoggerManager( final LoggerManager logkit ) {
        super.setLoggerManager(logkit);
        if( null ==  this.logKitManager ) {
             this.logKitManager = new LogkitLoggerManager( logkit, null );
        }
    }

    public void compose (ComponentManager manager) throws ComponentException {
        super.compose(manager);
        this.manager = manager;

        try {
            this.classManager = (ClassLoaderManager) manager.lookup(ClassLoaderManager.ROLE);
        } catch (ComponentException cme) {
            throw new ComponentException(ClassLoaderManager.ROLE, "GeneratorSelector", cme);
        }

        try {
            this.classManager.addDirectory((File) this.m_context.get(Constants.CONTEXT_WORK_DIR));
        } catch (Exception e) {
            throw new ComponentException(ROLE, "Could not add repository to ClassLoaderManager", e);
        }
    }

    public Component select(Object hint) throws ComponentException {

        ComponentHandler handler = (ComponentHandler) this.componentHandlers.get(hint);
        if (handler == null) {
            throw new ComponentException(ROLE, "Could not find component for hint: " + hint);
        }

        try {
            Component component = handler.get();
            componentMapping.put(component, handler);
            return component;
        } catch (Exception ce) {
            if (getLogger().isDebugEnabled())
                getLogger().debug("Could not access component for hint: " + hint, ce);
            throw new ComponentException(ROLE, "Could not access component for hint: " + hint, ce);
        }
    }

    public void release(Component component) {
        ComponentHandler handler = (ComponentHandler)componentMapping.remove(component);
        if (handler != null) {
            try {
                handler.put(component);
            } catch (Exception e) {
                getLogger().error("Error trying to release component", e);
            }
        }
    }

    public void addGenerator(ComponentManager newManager,
                                Object hint, Program generator)
            throws Exception {
        try {
            final ComponentHandler handler =
                    generator.getHandler(newManager, this.context, this.roles, this.logKitManager);
            handler.enableLogging(getLogger());
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
                getLogger().debug("Removing " + handler.getClass().getName() + " for " + hint.toString());
            }
        }
    }

    public void dispose() {
        this.manager.release(this.classManager);

        synchronized(this) {
            Iterator keys = this.componentHandlers.keySet().iterator();
            List keyList = new ArrayList();

            while(keys.hasNext()) {
                Object key = keys.next();
                ComponentHandler handler =
                    (ComponentHandler)this.componentHandlers.get(key);

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
