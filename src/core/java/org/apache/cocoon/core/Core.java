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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Constants;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.util.ClassUtils;

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

    private final Settings settings;
    
    public Core() {
        this.settings = null;
    }

    public Core(Settings s) {
        this.settings = s;
    }

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
        // return this.settings;
    }

    /**
     * Return the component context.
     * This method allows access to the component context for other components
     * that are not created by an Avalon based container.
     * FIXME - will be removed before the release
     */
    public Context getContext() {
        return this.context;
    }

    /**
     * Return the environment context object.
     * @return The environment context.
     */
    public org.apache.cocoon.environment.Context getEnvironmentContext() {
        try {
            return (org.apache.cocoon.environment.Context)this.context.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT);
        } catch (ContextException ce) {
            throw new CascadingRuntimeException("Unable to get the environment object from the context.", ce);
        }
    }
    
    public File getWorkDirectory() {
        try {
            return (File)this.context.get(Constants.CONTEXT_WORK_DIR);
        } catch (ContextException ce) {
            throw new CascadingRuntimeException("Unable to get the working directory from the context.", ce);
        }        
    }

    public File getUploadDirectory() {
        try {
            return (File)this.context.get(Constants.CONTEXT_UPLOAD_DIR);
        } catch (ContextException ce) {
            throw new CascadingRuntimeException("Unable to get the upload directory from the context.", ce);
        }        
    }

    public File getCacheDirectory() {
        try {
            return (File)this.context.get(Constants.CONTEXT_CACHE_DIR);
        } catch (ContextException ce) {
            throw new CascadingRuntimeException("Unable to get the cache directory from the context.", ce);
        }        
    }

    /**
     * Return the current settings.
     * Please don't use this method directly, look up the Core component
     * and use {@link #getSettings()} instead.
     * @param context The component context.
     * @return The settings.
     * FIXME - will be removed before the release
     */
    public static final Settings getSettings(Context context) {
        // the settings object is always present
        try {
            return (Settings)context.get(CONTEXT_SETTINGS);
        } catch (ContextException ce) {
            throw new CascadingRuntimeException("Unable to get the settings object from the context.", ce);
        }
    }

    /**
     * Get the settings for Cocoon
     * @param env This provides access to various parts of the used environment.
     */
    public static Settings createSettings(BootstrapEnvironment env) {
        // create an empty settings objects
        final Settings s = new Settings();

        String additionalPropertyFile = System.getProperty(Settings.PROPERTY_USER_SETTINGS);
        
        // read cocoon-settings.properties - if available
        InputStream propsIS = env.getInputStream("cocoon-settings.properties");
        if ( propsIS != null ) {
            env.log("Reading settings from 'cocoon-settings.properties'");
            final Properties p = new Properties();
            try {
                p.load(propsIS);
                propsIS.close();
                s.fill(p);
                additionalPropertyFile = p.getProperty(Settings.PROPERTY_USER_SETTINGS, additionalPropertyFile);
            } catch (IOException ignore) {
                env.log("Unable to read 'cocoon-settings.properties'.", ignore);
                env.log("Continuing initialization.");
            }
        }
        // fill from the environment configuration, like web.xml etc.
        env.configure(s);
        
        // read additional properties file
        if ( additionalPropertyFile != null ) {
            env.log("Reading user settings from '" + additionalPropertyFile + "'");
            final Properties p = new Properties();
            try {
                FileInputStream fis = new FileInputStream(additionalPropertyFile);
                p.load(fis);
                fis.close();
            } catch (IOException ignore) {
                env.log("Unable to read '" + additionalPropertyFile + "'.", ignore);
                env.log("Continuing initialization.");
            }
        }
        // now overwrite with system properties
        s.fill(System.getProperties());

        return s;        
    }

    public static interface BootstrapEnvironment {

        void log(String message);
        void log(String message, Throwable error);
        
        InputStream getInputStream(String path);
        
        void configure(Settings settings);
        
        ClassLoader getInitClassLoader();
    }
    
    /**
     * Bootstrap Cocoon Service Manager
     */
    public static ServiceManager createRootServiceManager(BootstrapEnvironment env) {        
        // create settings
        final Settings s = createSettings(env);
        
        if (s.isInitClassloader()) {
            // Force context classloader so that JAXP can work correctly
            // (see javax.xml.parsers.FactoryFinder.findClassLoader())
            try {
                Thread.currentThread().setContextClassLoader(env.getInitClassLoader());
            } catch (Exception e) {
                // ignore this
            }
        }

        // create new Core
        final Core cocoon = new Core(s);
        
        // create parent service manager
        final ServiceManager parent = getParentServiceManager(s);

        return new RootServiceManager(parent, cocoon);
    }
    
    /**
     * Instatiates the parent service manager, as specified in the
     * parent-service-manager init parameter.
     *
     * If none is specified, the method returns <code>null</code>.
     *
     * @return the parent service manager, or <code>null</code>.
     */
    protected static ServiceManager getParentServiceManager(Settings s) {
        String parentServiceManagerClass = s.getParentServiceManagerClassName();
        String parentServiceManagerInitParam = null;
        if (parentServiceManagerClass != null) {
            int dividerPos = parentServiceManagerClass.indexOf('/');
            if (dividerPos != -1) {
                parentServiceManagerInitParam = parentServiceManagerInitParam.substring(dividerPos + 1);
                parentServiceManagerClass = parentServiceManagerClass.substring(0, dividerPos);
            }
        }

        ServiceManager parentServiceManager = null;
        if (parentServiceManagerClass != null) {
            try {
                Class pcm = ClassUtils.loadClass(parentServiceManagerClass);
                Constructor pcmc = pcm.getConstructor(new Class[]{String.class});
                parentServiceManager = (ServiceManager) pcmc.newInstance(new Object[]{parentServiceManagerInitParam});

                //ContainerUtil.enableLogging(parentServiceManager, getLogger());
                //ContainerUtil.contextualize(parentServiceManager, this.appContext);
                ContainerUtil.initialize(parentServiceManager);
            } catch (Exception e) {
                /*if (getLogger().isErrorEnabled()) {
                    getLogger().error("Could not initialize parent component manager.", e);
                }*/
            }
        }
        return parentServiceManager;
    }

    public static final class RootServiceManager implements ServiceManager {
        
        protected final static String CORE_KEY = Core.class.getName();

        protected final ServiceManager parent;
        protected final Core cocoon;

        public RootServiceManager(ServiceManager p, Core c) {
            this.parent = p;
            this.cocoon = c;
        }

        /* (non-Javadoc)
         * @see org.apache.avalon.framework.service.ServiceManager#hasService(java.lang.String)
         */
        public boolean hasService(String key) {
            if ( CORE_KEY.equals(key) ) {
                return true;
            }
            if ( this.parent != null ) {
                return this.parent.hasService(key);
            }
            return false;
        }

        /* (non-Javadoc)
         * @see org.apache.avalon.framework.service.ServiceManager#lookup(java.lang.String)
         */
        public Object lookup(String key) throws ServiceException {
            if ( CORE_KEY.equals(key) ) {
                return this.cocoon;
            }
            if ( this.parent != null ) {
                return this.parent.lookup(key);
            }
            throw new ServiceException("Cocoon", "Component for key '" + key + "' not found.");
        }

        /* (non-Javadoc)
         * @see org.apache.avalon.framework.service.ServiceManager#release(java.lang.Object)
         */
        public void release(Object component) {
            if ( component != this.cocoon && parent != null ) {
                this.parent.release(component);
            }
        }
    }
}
