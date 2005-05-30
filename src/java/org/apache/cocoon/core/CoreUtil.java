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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.avalon.excalibur.logger.Log4JLoggerManager;
import org.apache.avalon.excalibur.logger.LogKitLoggerManager;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.LogKitLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Cocoon;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.container.ComponentContext;
import org.apache.cocoon.configuration.ConfigurationBuilder;
import org.apache.cocoon.core.source.SimpleSourceResolver;
import org.apache.cocoon.matching.helpers.WildcardHelper;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.StringUtils;
import org.apache.cocoon.util.log.Log4JConfigurator;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.TraversableSource;
import org.apache.log.ErrorHandler;
import org.apache.log.Hierarchy;
import org.apache.log.Priority;
import org.apache.log.util.DefaultErrorHandler;
import org.apache.log4j.LogManager;

/**
 * This is an utility class to create a new Cocoon instance.
 *
 * @version $Id$
 * @since 2.2
 */
public class CoreUtil {

    /** Parameter map for the context protocol */
    protected static final Map CONTEXT_PARAMETERS = Collections.singletonMap("force-traversable", Boolean.TRUE);

    /** The callback to the real environment. */
    protected final BootstrapEnvironment env;

    /** "legacy" support: create an avalon context. */
    protected final DefaultContext appContext = new ComponentContext();

    /** The settings. */
    protected MutableSettings settings;

    /** The parent service manager. */
    protected ServiceManager parentManager;

    /** The root logger. */
    protected Logger log;

    /** The logger manager. */
    protected LoggerManager loggerManager;

    /** The Cocoon instance (the root processor). */
    protected Cocoon cocoon;

    /** The time the cocoon instance was created. */
    protected long creationTime;

    /**
     * Setup a new instance.
     * @param environment The hook back to the environment.
     * @throws Exception
     */
    public CoreUtil(BootstrapEnvironment environment)
    throws Exception {
        this.env = environment;
        this.init();
    }

    protected void init()
    throws Exception {
        // create settings
        this.settings = this.createSettings();

        if (this.settings.isInitClassloader()) {
            // Force context classloader so that JAXP can work correctly
            // (see javax.xml.parsers.FactoryFinder.findClassLoader())
            try {
                Thread.currentThread().setContextClassLoader(this.env.getInitClassLoader());
            } catch (Exception e) {
                // ignore this
            }
        }

        // add root url
        try {
            appContext.put(ContextHelper.CONTEXT_ROOT_URL,
                           new URL(this.env.getContextURL()));
        } catch (MalformedURLException ignore) {
            // we simply ignore this
        }

        // add environment context
        this.appContext.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT,
                            this.env.getEnvironmentContext());

        // now add environment specific information
        this.env.configure(appContext);

        // first init the work-directory for the logger.
        // this is required if we are running inside a war file!
        final String workDirParam = this.settings.getWorkDirectory();
        File workDir;
        if (workDirParam != null) {
            if (this.env.getContextForWriting() == null) {
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
                    workDir = new File(this.env.getContextForWriting(), workDirParam);
                }
            }
        } else {
            workDir = new File("cocoon-files");
        }
        workDir.mkdirs();
        this.appContext.put(Constants.CONTEXT_WORK_DIR, workDir);
        this.settings.setWorkDirectory(workDir.getAbsolutePath());

        // Init logger
        this.initLogger();
        this.env.setLogger(this.log);

        // Output some debug info
        if (this.log.isDebugEnabled()) {
            this.log.debug("Context URL: " + this.env.getContextURL());
            this.log.debug("Writeable Context: " + this.env.getContextForWriting());
            if (workDirParam != null) {
                this.log.debug("Using work-directory " + workDir);
            } else {
                this.log.debug("Using default work-directory " + workDir);
            }
        }

        final String uploadDirParam = this.settings.getUploadDirectory();
        File uploadDir;
        if (uploadDirParam != null) {
            if (this.env.getContextForWriting() == null) {
                uploadDir = new File(uploadDirParam);
            } else {
                // Context path exists : is upload-directory absolute ?
                File uploadDirParamFile = new File(uploadDirParam);
                if (uploadDirParamFile.isAbsolute()) {
                    // Yes : keep it as is
                    uploadDir = uploadDirParamFile;
                } else {
                    // No : consider it relative to context path
                    uploadDir = new File(this.env.getContextForWriting(), uploadDirParam);
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
            if (this.env.getContextForWriting() == null) {
                cacheDir = new File(cacheDirParam);
            } else {
                // Context path exists : is cache-directory absolute ?
                File cacheDirParamFile = new File(cacheDirParam);
                if (cacheDirParamFile.isAbsolute()) {
                    // Yes : keep it as is
                    cacheDir = cacheDirParamFile;
                } else {
                    // No : consider it relative to context path
                    cacheDir = new File(this.env.getContextForWriting(), cacheDirParam);
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

        // update settings
        final URL u = this.env.getConfigFile(this.settings.getConfiguration());
        this.settings.setConfiguration(u.toExternalForm());
        this.appContext.put(Constants.CONTEXT_CONFIG_URL, u);

        // set encoding
        this.appContext.put(Constants.CONTEXT_DEFAULT_ENCODING, settings.getFormEncoding());

        // set class loader
        this.appContext.put(Constants.CONTEXT_CLASS_LOADER, this.env.getInitClassLoader());

        // create the Core object
        final Core core = this.createCore();

        // create parent service manager
        this.parentManager = this.getParentServiceManager(core);

        // settings can't be changed anymore
        settings.makeReadOnly();

        // put the core into the context - this is for internal use only
        // The Cocoon container fetches the Core object using the context.
        this.appContext.put(Core.ROLE, core);
    }

    public Core getCore() {
        try {
            return (Core)this.parentManager.lookup(Core.ROLE);
        } catch (ServiceException ignore) {
            // this can never happen!
            throw new RuntimeException("Fatal error: no Cocoon core available.");
        }
    }

    /**
     * Create a new core instance.
     * This method can be overwritten in sub classes.
     * @return A new core object.
     */
    protected Core createCore() {
        final Core c = new Core(this.settings, this.appContext);
        return c;
    }

    /**
     * Instatiates the parent service manager, as specified in the
     * parent-service-manager init parameter.
     *
     * If none is specified, the method returns <code>null</code>.
     *
     * @return the parent service manager, or <code>null</code>.
     */
    protected ServiceManager getParentServiceManager(Core core) {
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

                ContainerUtil.enableLogging(parentServiceManager, this.log);
                ContainerUtil.contextualize(parentServiceManager, this.appContext);
                ContainerUtil.initialize(parentServiceManager);
            } catch (Exception e) {
                if (this.log.isErrorEnabled()) {
                    this.log.error("Could not initialize parent component manager.", e);
                }
            }
        }
        return new RootServiceManager(parentServiceManager, core);
    }

    /**
     * Get the settings for Cocoon.
     * @return A new Settings object
     */
    protected MutableSettings createSettings() {
        // create an empty settings objects
        final MutableSettings s = new MutableSettings();

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

    protected void initLogger() {
        String logLevel = settings.getBootstrapLogLevel();
        if (logLevel == null) {
            logLevel = "INFO";
        }

        String accesslogger = settings.getEnvironmentLogger();
        if (accesslogger == null) {
            accesslogger = "cocoon";
        }

        final Priority logPriority = Priority.getPriorityForName(logLevel);

        final Hierarchy defaultHierarchy;
        if ( settings.isCreateLogKitHierarchy() ) {
            defaultHierarchy = new Hierarchy();            
        } else {
            defaultHierarchy = Hierarchy.getDefaultHierarchy();
        }
        final ErrorHandler errorHandler = new DefaultErrorHandler();
        defaultHierarchy.setErrorHandler(errorHandler);
        if ( this.env.getDefaultLogTarget() != null ) {
            defaultHierarchy.setDefaultLogTarget(this.env.getDefaultLogTarget());
        }
        defaultHierarchy.setDefaultPriority(logPriority);
        final Logger logger = new LogKitLogger(Hierarchy.getDefaultHierarchy().getLoggerFor(""));

        // Create our own resolver
        SimpleSourceResolver resolver = new SimpleSourceResolver();
        resolver.enableLogging(logger);
        try {
            resolver.contextualize(this.appContext);
        } catch (ContextException ce) {
            throw new CascadingRuntimeException(
                    "Cannot setup source resolver.", ce);
        }

        // create an own context for the logger manager
        final DefaultContext subcontext = new DefaultContext(this.appContext);
        subcontext.put("context-work", new File(this.settings.getWorkDirectory()));
        if (this.env.getContextForWriting() == null) {
            File logSCDir = new File(this.settings.getWorkDirectory(), "log");
            logSCDir.mkdirs();
            subcontext.put("context-root", logSCDir.toString());
        } else {
            subcontext.put("context-root", this.env.getContextForWriting().toString());
        }
        this.env.configureLoggingContext(subcontext);

        String loggerManagerClass = settings.getLoggerManagerClassName();
        if (loggerManagerClass == null) {
            loggerManagerClass = LogKitLoggerManager.class.getName();
        }

        // the log4j support requires currently that the log4j system is already
        // configured elsewhere

        final LoggerManager loggerManager = newLoggerManager(
                loggerManagerClass, defaultHierarchy);
        ContainerUtil.enableLogging(loggerManager, logger);

        try {
            ContainerUtil.contextualize(loggerManager, subcontext);
            this.loggerManager = loggerManager;

            if (loggerManager instanceof Configurable) {
                //Configure the logkit management
                String logkitConfig = settings.getLoggingConfiguration();

                if ( logkitConfig != null ) {
                    Source source = null;
                    try {
                        source = resolver.resolveURI(logkitConfig);
                        final ConfigurationBuilder builder = new ConfigurationBuilder(
                                settings);
                        final Configuration conf = builder.build(source
                                .getInputStream());
                        final DefaultConfiguration categories = (DefaultConfiguration) conf
                                .getChild("categories");
                        final DefaultConfiguration targets = (DefaultConfiguration) conf
                                .getChild("targets");
                        final DefaultConfiguration factories = (DefaultConfiguration) conf
                                .getChild("factories");
    
                        // now process includes
                        final Configuration[] children = conf
                                .getChildren("include");
                        for (int i = 0; i < children.length; i++) {
                            String directoryURI = children[i].getAttribute("dir");
                            final String pattern = children[i].getAttribute(
                                    "pattern", null);
                            int[] parsedPattern = null;
                            if (pattern != null) {
                                parsedPattern = WildcardHelper
                                        .compilePattern(pattern);
                            }
                            Source directory = null;
                            try {
                                directory = resolver.resolveURI(directoryURI,
                                        source.getURI(), CONTEXT_PARAMETERS);
                                if (directory instanceof TraversableSource) {
                                    final Iterator c = ((TraversableSource) directory)
                                            .getChildren().iterator();
                                    while (c.hasNext()) {
                                        final Source s = (Source) c.next();
                                        if (parsedPattern == null
                                                || this.match(s.getURI(),
                                                        parsedPattern)) {
                                            final Configuration includeConf = builder
                                                    .build(s.getInputStream());
                                            // add targets and categories
                                            categories.addAllChildren(includeConf
                                                    .getChild("categories"));
                                            targets.addAllChildren(includeConf
                                                    .getChild("targets"));
                                            factories.addAllChildren(includeConf
                                                    .getChild("factories"));
                                        }
                                    }
                                } else {
                                    throw new ConfigurationException(
                                            "Include.dir must point to a directory, '"
                                                    + directory.getURI()
                                                    + "' is not a directory.'");
                                }
                            } catch (IOException ioe) {
                                throw new ConfigurationException(
                                        "Unable to read configurations from "
                                                + directoryURI);
                            } finally {
                                resolver.release(directory);
                            }
    
                            // finally remove include
                            ((DefaultConfiguration) conf).removeChild(children[i]);
                        }
                        // override log level?
                        if (settings.getOverrideLogLevel() != null) {
                            this.overrideLogLevel(conf.getChild("categories"),
                                    settings.getOverrideLogLevel());
                        }
                        ContainerUtil.configure(loggerManager, conf);
                    } finally {
                        resolver.release(source);
                    }
                }
            }

            // let's configure log4j
            final String log4jConfig = settings.getLog4jConfiguration();
            if (log4jConfig != null) {
                final Log4JConfigurator configurator = new Log4JConfigurator(subcontext);

                Source source = null;
                try {
                    source = resolver.resolveURI(log4jConfig);
                    configurator.doConfigure(source.getInputStream(),
                            LogManager.getLoggerRepository());
                } finally {
                    resolver.release(source);
                }
            }

            ContainerUtil.initialize(loggerManager);
        } catch (Exception e) {
            errorHandler.error(
                    "Could not set up Cocoon Logger, will use screen instead",
                    e, null);
        }

        this.log = this.loggerManager.getLoggerForCategory(accesslogger);
    }

    private LoggerManager newLoggerManager(String loggerManagerClass,
            Hierarchy hierarchy) {
        if (loggerManagerClass.equals(LogKitLoggerManager.class.getName())) {
            return new LogKitLoggerManager(hierarchy);
        } else if (loggerManagerClass
                .equals(Log4JLoggerManager.class.getName())
                || loggerManagerClass.equalsIgnoreCase("LOG4J")) {
            return new Log4JLoggerManager();
        } else {
            try {
                Class clazz = Class.forName(loggerManagerClass);
                return (LoggerManager) clazz.newInstance();
            } catch (Exception e) {
                return new LogKitLoggerManager(hierarchy);
            }
        }
    }

    protected void overrideLogLevel(Configuration root, String value) {
        Configuration[] c = root.getChildren("category");
        for(int i=0;i<c.length;i++) {
            ((DefaultConfiguration)c[i]).setAttribute("log-level", value);
            this.overrideLogLevel(c[i], value);
        }
    }

    private boolean match(String uri, int[] parsedPattern ) {
        int pos = uri.lastIndexOf('/');
        if ( pos != -1 ) {
            uri = uri.substring(pos+1);
        }
        return WildcardHelper.match(null, uri, parsedPattern);
    }

    public static final class RootServiceManager
    implements ServiceManager, Disposable {

        protected final ServiceManager parent;
        protected final Core cocoon;

        public RootServiceManager(ServiceManager p, Core c) {
            this.parent = p;
            this.cocoon = c;
        }

        /**
         * @see org.apache.avalon.framework.service.ServiceManager#hasService(java.lang.String)
         */
        public boolean hasService(String key) {
            if ( Core.ROLE.equals(key) ) {
                return true;
            }
            if ( this.parent != null ) {
                return this.parent.hasService(key);
            }
            return false;
        }

        /**
         * @see org.apache.avalon.framework.service.ServiceManager#lookup(java.lang.String)
         */
        public Object lookup(String key) throws ServiceException {
            if ( Core.ROLE.equals(key) ) {
                return this.cocoon;
            }
            if ( this.parent != null ) {
                return this.parent.lookup(key);
            }
            throw new ServiceException("Cocoon", "Component for key '" + key + "' not found.");
        }

        /**
         * @see org.apache.avalon.framework.service.ServiceManager#release(java.lang.Object)
         */
        public void release(Object component) {
            if ( component != this.cocoon && parent != null ) {
                this.parent.release(component);
            }
        }

        /**
         * @see org.apache.avalon.framework.activity.Disposable#dispose()
         */
        public void dispose() {
            ContainerUtil.dispose(this.parent);
        }
    }

    /**
     * Creates the Cocoon object and handles exception handling.
     */
    public synchronized Cocoon createCocoon()
    throws Exception {

        /* HACK for reducing class loader problems.                                     */
        /* example: xalan extensions fail if someone adds xalan jars in tomcat3.2.1/lib */
        if (this.settings.isInitClassloader()) {
            try {
                Thread.currentThread().setContextClassLoader(this.env.getInitClassLoader());
            } catch (Exception e) {
                // ignore
            }
        }

        this.updateEnvironment();
        this.forceLoad();
        this.forceProperty();

        try {
            if (this.log.isInfoEnabled()) {
                this.log.info("Reloading from: " + this.settings.getConfiguration());
            }
            Cocoon c = (Cocoon)ClassUtils.newInstance("org.apache.cocoon.Cocoon");
            ContainerUtil.enableLogging(c, getCocoonLogger());
            c.setLoggerManager(this.loggerManager);
            ContainerUtil.contextualize(c, this.appContext);

            // create the Core object
            final Core core = this.createCore();
            this.parentManager = this.getParentServiceManager(core);
            ContainerUtil.service(c, this.parentManager);

            ContainerUtil.initialize(c);
            this.creationTime = System.currentTimeMillis();

            this.cocoon = c;
        } catch (Exception e) {
            this.log.error("Exception reloading Cocoon.", e);
            this.disposeCocoon();
            throw e;
        }
        return this.cocoon;
    }

    /**
     * Gets the current cocoon object.
     * Reload cocoon if configuration changed or we are reloading.
     */
    public Cocoon getCocoon(final String pathInfo, final String reloadParam)
    throws Exception {
        if (this.settings.isAllowReload()) {
            boolean reload = false;

            if (this.cocoon != null) {
                if (this.cocoon.modifiedSince(this.creationTime)) {
                    if (this.log.isInfoEnabled()) {
                        this.log.info("Configuration changed reload attempt");
                    }
                    reload = true;
                } else if (pathInfo == null && reloadParam != null) {
                    if (this.log.isInfoEnabled()) {
                        this.log.info("Forced reload attempt");
                    }
                    reload = true;
                }
            } else if (pathInfo == null && reloadParam != null) {
                if (this.log.isInfoEnabled()) {
                    this.log.info("Invalid configurations reload");
                }
                reload = true;
            }

            if (reload) {
                this.init();
                this.createCocoon();
            }
        }
        return this.cocoon;
    }

    /**
     * Destroy Cocoon
     */
    protected final void disposeCocoon() {
        if (this.cocoon != null) {
            if (this.log.isDebugEnabled()) {
                this.log.debug("Disposing Cocoon");
            }
            ContainerUtil.dispose(this.cocoon);
            this.cocoon = null;
        }
        ContainerUtil.dispose(this.parentManager);
        this.parentManager = null;
    }

    protected Logger getCocoonLogger() {
        final String rootlogger = this.settings.getCocoonLogger();
        if (rootlogger != null) {
            return this.loggerManager.getLoggerForCategory(rootlogger);
        }
        return this.log;
    }

    /**
     * Handle the <code>load-class</code> parameter. This overcomes
     * limits in many classpath issues. One of the more notorious
     * ones is a bug in WebSphere that does not load the URL handler
     * for the <code>classloader://</code> protocol. In order to
     * overcome that bug, set <code>load-class</code> parameter to
     * the <code>com.ibm.servlet.classloader.Handler</code> value.
     *
     * <p>If you need to load more than one class, then separate each
     * entry with whitespace, a comma, or a semi-colon. Cocoon will
     * strip any whitespace from the entry.</p>
     */
    protected void forceLoad() {
        final Iterator i = this.settings.getLoadClasses();
        while (i.hasNext()) {
            final String fqcn = (String)i.next();
            try {
                if (this.log.isDebugEnabled()) {
                    this.log.debug("Loading: " + fqcn);
                }
                ClassUtils.loadClass(fqcn).newInstance();
            } catch (Exception e) {
                if (this.log.isWarnEnabled()) {
                    this.log.warn("Could not load class: " + fqcn, e);
                }
                // Do not throw an exception, because it is not a fatal error.
            }
        }
    }

    /**
     * Handle the "force-property" parameter.
     *
     * If you need to force more than one property to load, then
     * separate each entry with whitespace, a comma, or a semi-colon.
     * Cocoon will strip any whitespace from the entry.
     */
    protected void forceProperty() {
        if (this.settings.getForceProperties().size() > 0) {
            final Iterator i = this.settings.getForceProperties().entrySet().iterator();
            while (i.hasNext()) {
                final Map.Entry current = (Map.Entry)i.next();
                try {
                    if (this.log.isDebugEnabled()) {
                        this.log.debug("Setting: " + current.getKey() + "=" + current.getValue());
                    }
                    System.setProperty(current.getKey().toString(), current.getValue().toString());
                } catch (Exception e) {
                    if (this.log.isWarnEnabled()) {
                        this.log.warn("Could not set property: " + current.getKey(), e);
                    }
                    // Do not throw an exception, because it is not a fatal error.
                }
            }
        }
    }

    /**
     * Method to update the environment before Cocoon instances are created.
     *
     * This is also useful if you wish to customize any of the 'protected'
     * variables from this class before a Cocoon instance is built in a derivative
     * of this class (eg. Cocoon Context).
     */
    protected void updateEnvironment() throws Exception {
        StringBuffer buffer = new StringBuffer(this.env.getClassPath(this.settings));
        buffer.append(File.pathSeparatorChar).append(this.getExtraClassPath());

        this.appContext.put(Constants.CONTEXT_CLASSPATH, buffer.toString());
    }

    /**
     * Dispose Cocoon when environment is destroyed
     */
    public void destroy() {
        if (this.settings.isInitClassloader()) {
            try {
                Thread.currentThread().setContextClassLoader(this.env.getInitClassLoader());
            } catch (Exception e) {
                // ignore this
            }
        }
        this.disposeCocoon();
    }

    /**
     * Retreives the "extra-classpath" attribute, that needs to be
     * added to the class path.
     */
    protected String getExtraClassPath() {
        if (this.settings.getExtraClasspaths().size() > 0) {
            StringBuffer sb = new StringBuffer();
            final Iterator iter = this.settings.getExtraClasspaths().iterator();
            int i = 0;
            while (iter.hasNext()) {
                String s = (String)iter.next();
                if (i++ > 0) {
                    sb.append(File.pathSeparatorChar);
                }
                if ((s.charAt(0) == File.separatorChar) ||
                        (s.charAt(1) == ':')) {
                    if (this.log.isDebugEnabled()) {
                        this.log.debug("extraClassPath is absolute: " + s);
                    }
                    sb.append(s);

                } else {
                    if (s.indexOf("${") != -1) {
                        String path = StringUtils.replaceToken(s);
                        sb.append(path);
                        if (this.log.isDebugEnabled()) {
                            this.log.debug("extraClassPath is not absolute replacing using token: [" + s + "] : " + path);
                        }
                    } else {
                        String path = null;
                        if (this.env.getContextForWriting() != null) {
                            path = this.env.getContextForWriting() + s;
                            if (this.log.isDebugEnabled()) {
                                this.log.debug("extraClassPath is not absolute pre-pending context path: " + path);
                            }
                        } else {
                            path = this.settings.getWorkDirectory() + s;
                            if (this.log.isDebugEnabled()) {
                                this.log.debug("extraClassPath is not absolute pre-pending work-directory: " + path);
                            }
                        }
                        sb.append(path);
                    }
                }
            }
            return sb.toString();
        }
        return "";
    }

}
