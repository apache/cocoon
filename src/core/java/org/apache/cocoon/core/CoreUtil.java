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
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.log.LoggingHelper;

/**
 *
 * @version SVN $Id$
 * @since 2.2
 */
public class CoreUtil {

    /** The callback to the real environment */
    protected final Core.BootstrapEnvironment env;

    /** "legacy" support: create an avalon context */
    protected final DefaultContext appContext = new DefaultContext();
    
    /** The settings */
    protected final Settings settings;

    private Logger log;
    private LoggerManager loggerManager;

    public CoreUtil(Core.BootstrapEnvironment e) 
    throws Exception {
        this.env = e;

        // create settings
        this.settings = Core.createSettings(this.env);

        this.createRootServiceManager();
    }

    /**
     * Bootstrap Cocoon Service Manager
     */
    public ServiceManager createRootServiceManager() 
    throws Exception {

        
        if (this.settings.isInitClassloader()) {
            // Force context classloader so that JAXP can work correctly
            // (see javax.xml.parsers.FactoryFinder.findClassLoader())
            try {
                Thread.currentThread().setContextClassLoader(this.env.getInitClassLoader());
            } catch (Exception e) {
                // ignore this
            }
        }

        this.appContext.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT, this.env.getEnvironmentContext());

        // first init the work-directory for the logger.
        // this is required if we are running inside a war file!
        final String workDirParam = this.settings.getWorkDirectory();
        File workDir;
        if (workDirParam != null) {
            if (this.env.getContextPath() == null) {
                // No context path : consider work-directory as absolute
                workDir = new File(workDirParam);
            } else {
                // Context path exists : is work-directory absolute ?
                File workDirParamFile = new File(workDirParam);
                if (workDirParamFile.isAbsolute()) {
                    // Yes : keep it as is
                    workDir = workDirParamFile;
                } else {
                    // No : consider it relative to context path
                    workDir = new File(this.env.getContextPath(), workDirParam);
                }
            }
        } else {
            workDir = new File("cocoon-files");
        }
        workDir.mkdirs();
        this.appContext.put(Constants.CONTEXT_WORK_DIR, workDir);
        this.settings.setWorkDirectory(workDir.getAbsolutePath());

        // TODO we should move the following into the bootstrap env
        String contextURL;
        String path = env.getContextPath();
        // these two variables are just for debugging. We can't log at this point
        // as the logger isn't initialized yet.
        String debugPathOne = null, debugPathTwo = null;
        if (path == null) {
            // Try to figure out the path of the root from that of WEB-INF
            //try {
                // TODO:
                //path = this.servletContext.getResource("/WEB-INF").toString();
            //} catch (MalformedURLException me) {
            //    throw new ServletException("Unable to get resource 'WEB-INF'.", me);
            //}
            debugPathOne = path;
            path = path.substring(0, path.length() - "WEB-INF".length());
            debugPathTwo = path;
        }
        try {
            if (path.indexOf(':') > 1) {
                contextURL = path;
            } else {
                contextURL = new File(path).toURL().toExternalForm();
            }
        } catch (MalformedURLException me) {
            // VG: Novell has absolute file names starting with the
            // volume name which is easily more then one letter.
            // Examples: sys:/apache/cocoon or sys:\apache\cocoon
            try {
                contextURL = new File(path).toURL().toExternalForm();
            } catch (MalformedURLException ignored) {
                throw new Exception("Unable to determine context URL.", me);
            }
        }

        try {
            appContext.put(ContextHelper.CONTEXT_ROOT_URL, new URL(contextURL));            
        } catch (MalformedURLException ignore) {
            // we simply ignore this
        }

        // Init logger
        initLogger();
        if (this.log.isDebugEnabled()) {
            this.log.debug(this.settings.toString());
            this.log.debug("getRealPath for /: " + this.env.getContextPath());
            if ( this.env.getContextPath() == null ) {                
                this.log.debug("getResource for /WEB-INF: " + debugPathOne);
                this.log.debug("Path for Root: " + debugPathTwo);
            }
        }


        // Output some debug info
        if (this.log.isDebugEnabled()) {
            this.log.debug("Context URL: " + contextURL);
            if (workDirParam != null) {
                this.log.debug("Using work-directory " + workDir);
            } else {
                this.log.debug("Using default work-directory " + workDir);
            }
        }

        final String uploadDirParam = this.settings.getUploadDirectory();
        File uploadDir;
        if (uploadDirParam != null) {
            if (env.getContextPath() == null) {
                uploadDir = new File(uploadDirParam);
            } else {
                // Context path exists : is upload-directory absolute ?
                File uploadDirParamFile = new File(uploadDirParam);
                if (uploadDirParamFile.isAbsolute()) {
                    // Yes : keep it as is
                    uploadDir = uploadDirParamFile;
                } else {
                    // No : consider it relative to context path
                    uploadDir = new File(env.getContextPath(), uploadDirParam);
                }
            }
            if (this.log.isDebugEnabled()) {
                this.log.debug("Using upload-directory " + uploadDir);
            }
        } else {
            uploadDir = new File(workDir, "upload-dir" + File.separator);
            if (this.log.isDebugEnabled()) {
                this.log.debug("Using default upload-directory " + uploadDir);
            }
        }
        uploadDir.mkdirs();
        appContext.put(Constants.CONTEXT_UPLOAD_DIR, uploadDir);
        this.settings.setUploadDirectory(uploadDir.getAbsolutePath());

        String cacheDirParam = this.settings.getCacheDirectory();
        File cacheDir;
        if (cacheDirParam != null) {
            if (env.getContextPath() == null) {
                cacheDir = new File(cacheDirParam);
            } else {
                // Context path exists : is cache-directory absolute ?
                File cacheDirParamFile = new File(cacheDirParam);
                if (cacheDirParamFile.isAbsolute()) {
                    // Yes : keep it as is
                    cacheDir = cacheDirParamFile;
                } else {
                    // No : consider it relative to context path
                    cacheDir = new File(env.getContextPath(), cacheDirParam);
                }
            }
            if (this.log.isDebugEnabled()) {
                this.log.debug("Using cache-directory " + cacheDir);
            }
        } else {
            cacheDir = new File(workDir, "cache-dir" + File.separator);
            File parent = cacheDir.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            if (this.log.isDebugEnabled()) {
                this.log.debug("cache-directory was not set - defaulting to " + cacheDir);
            }
        }
        cacheDir.mkdirs();
        appContext.put(Constants.CONTEXT_CACHE_DIR, cacheDir);
        this.settings.setCacheDirectory(cacheDir.getAbsolutePath());

        // create new Core
        final Core cocoon = new Core(this.settings);
        
        // create parent service manager
        final ServiceManager parent = this.getParentServiceManager();

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
    protected ServiceManager getParentServiceManager() {
        String parentServiceManagerClass = this.settings.getParentServiceManagerClassName();
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

    protected void initLogger() {
        final DefaultContext subcontext = new DefaultContext(this.appContext);
        subcontext.put("context-work", new File(this.settings.getWorkDirectory()));
        if (this.env.getContextPath() == null) {
            File logSCDir = new File(this.settings.getWorkDirectory(), "log");
            logSCDir.mkdirs();
            subcontext.put("context-root", logSCDir.toString());
        } else {
            subcontext.put("context-root", this.env.getContextPath());
        }
        this.env.configureLoggingContext(subcontext);

        // FIXME - we can move the logging helper into this class
        LoggingHelper loggingHelper = new LoggingHelper(this.settings, this.env.getDefaultLogTarget(), subcontext);
        this.loggerManager = loggingHelper.getLoggerManager();
        this.log = loggingHelper.getLogger();
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
