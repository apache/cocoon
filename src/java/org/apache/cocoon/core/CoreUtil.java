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
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.avalon.excalibur.logger.Log4JConfLoggerManager;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Cocoon;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.container.ComponentContext;
import org.apache.cocoon.configuration.ConfigurationBuilder;
import org.apache.cocoon.core.container.SingleComponentServiceManager;
import org.apache.cocoon.core.logging.CocoonLogKitLoggerManager;
import org.apache.cocoon.core.logging.PerRequestLoggerManager;
import org.apache.cocoon.core.logging.SettingsContext;
import org.apache.cocoon.core.source.SimpleSourceResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.matching.helpers.WildcardHelper;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;

/**
 * This is an utility class to create a new Cocoon instance.
 * 
 * TODO - Remove dependencies to LogKit and Log4J
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

    /** Is this a per request logger manager */
    protected boolean isPerRequestLoggerManager = false;
    
    protected ClassLoader classloader;

    /**
     * Setup a new instance.
     * @param environment The hook back to the environment.
     * @throws Exception
     */
    public CoreUtil(BootstrapEnvironment environment)
    throws Exception {
        this.env = environment;
        this.init();
        this.createClassloader();        
    }

    protected void init()
    throws Exception {
        // first let's set up the appContext with some values to make
        // the simple source resolver work

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

        // create settings
        this.settings = this.createSettings();

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

        // update configuration
        final URL u = this.env.getConfigFile(this.settings.getConfiguration());
        this.settings.setConfiguration(u.toExternalForm());
        this.appContext.put(Constants.CONTEXT_CONFIG_URL, u);

        // set encoding
        this.appContext.put(Constants.CONTEXT_DEFAULT_ENCODING, settings.getFormEncoding());

        // set class loader
        this.appContext.put(Constants.CONTEXT_CLASS_LOADER, this.classloader);

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
        } catch (ServiceException neverIgnore) {
            // this should never happen!
            throw new CoreFatalException("Fatal exception: no Cocoon core available.", neverIgnore);
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
     * Return the settings object.
     */
    public Settings getSettings() {
        return this.settings;
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
        return new SingleComponentServiceManager(parentServiceManager, core, Core.ROLE);
    }

    /**
     * Get the settings for Cocoon.
     * This method reads several property files and merges the result. If there
     * is more than one definition for a property, the last one wins.
     * The property files are read in the following order:
     * 1) context://WEB-INF/cocoon-settings.properties
     *    These are the default values.
     * 2) context://WEB-INF/properties/*.properties
     *    Default values for each block - the order in which the files are read is not guaranteed.
     * 3) context://WEB-INF/properties/[RUNNING_MODE]/*.properties
     *    Default values for the running mode - the order in which the files are read is not guaranteed.
     * 4) Property providers (ToBeDocumented)
     * 5) The environment (CLI, Servlet etc.) adds own properties (e.g. from web.xml)
     * 6) Additional property file specified by the "org.apache.cocoon.settings" system property.
     * 7) System properties
     *
     * @return A new Settings object
     */
    protected MutableSettings createSettings() {
        // get the running mode
        final String mode = System.getProperty(Settings.PROPERTY_RUNNING_MODE, Settings.DEFAULT_RUNNING_MODE);
        this.env.log("Running in mode: " + mode);

        // create an empty settings objects
        final MutableSettings s = new MutableSettings();

        // we need our own resolver
        final SourceResolver resolver = this.createSourceResolver(new LoggerWrapper(this.env));

        // read cocoon-settings.properties - if available
        Source source = null;
        try {
            source = resolver.resolveURI("context://WEB-INF/cocoon-settings.properties");
            if ( source.exists() ) {
                final InputStream propsIS = source.getInputStream();
                this.env.log("Reading settings from '" + source.getURI() + "'");
                final Properties p = new Properties();
                p.load(propsIS);
                propsIS.close();
                s.fill(p);
            }
        } catch (IOException ignore) {
            env.log("Unable to read 'WEB-INF/cocoon-settings.properties'.", ignore);
            env.log("Continuing initialization.");            
        } finally {
            resolver.release(source);
        }

        // now read all properties from the properties directory
        this.readProperties("context://WEB-INF/properties", s, resolver);
        // read all properties from the mode dependent directory
        this.readProperties("context://WEB-INF/properties/" + mode, s, resolver);

        // Next look for custom property providers
        Iterator i = s.getPropertyProviders().iterator();
        while ( i.hasNext() ) {
            final String className = (String)i.next();
            try {
                PropertyProvider provider = (PropertyProvider)ClassUtils.newInstance(className);
                s.fill(provider.getProperties());
            } catch (Exception ignore) {
                env.log("Unable to get property provider for class " + className, ignore);
                env.log("Continuing initialization.");            
            }
        }
        // fill from the environment configuration, like web.xml etc.
        env.configure(s);

        // read additional properties file
        final String additionalPropertyFile = s.getProperty(Settings.PROPERTY_USER_SETTINGS, 
                                                            System.getProperty(Settings.PROPERTY_USER_SETTINGS));
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

    /**
     * Read all property files from the given directory and apply them to the settings.
     */
    protected void readProperties(String directoryName,
                                  MutableSettings s,
                                  SourceResolver resolver) {
        Source directory = null;
        try {
            directory = resolver.resolveURI(directoryName, null, CONTEXT_PARAMETERS);
            if (directory.exists() && directory instanceof TraversableSource) {
                final Iterator c = ((TraversableSource) directory).getChildren().iterator();
                while (c.hasNext()) {
                    final Source src = (Source) c.next();
                    if ( src.getURI().endsWith(".properties") ) {
                        final InputStream propsIS = src.getInputStream();
                        env.log("Reading settings from '" + src.getURI() + "'.");
                        final Properties p = new Properties();
                        p.load(propsIS);
                        propsIS.close();
                        s.fill(p);
                    }
                }
            }
        } catch (IOException ignore) {
            env.log("Unable to read from directory 'WEB-INF/properties'.", ignore);
            env.log("Continuing initialization.");            
        } finally {
            resolver.release(directory);
        }
    }

    /**
     * Initialize the current request.
     * This method can be used to initialize anything required for processing
     * the request. For example, if the logger manager is a {@link PerRequestLoggerManager}
     * than this manager is invoked to initialize the logging context for the request.
     * This method returns a handle that should be used to clean up everything
     * when the request is finished by calling {@link #cleanUpRequest(Object)}.
     */
    public Object initializeRequest(Environment env) {
        if ( this.isPerRequestLoggerManager ) {
            return ((PerRequestLoggerManager)this.loggerManager).initializePerRequestLoggingContext(env);
        }
        return null;   
    }

    /**
     * Cleanup everything initialized during the request processing in
     * {@link #initializeRequest(Environment)}.
     */
    public void cleanUpRequest(Object handle) {
        if ( handle != null && this.isPerRequestLoggerManager) {
            ((PerRequestLoggerManager)this.loggerManager).cleanPerRequestLoggingContext(handle);
        }
    }

    /**
     * Create a simple source resolver.
     */
    protected SourceResolver createSourceResolver(Logger logger) {
        // Create our own resolver
        final SimpleSourceResolver resolver = new SimpleSourceResolver();
        resolver.enableLogging(logger);
        try {
            resolver.contextualize(this.appContext);
        } catch (ContextException ce) {
            throw new CoreInitializationException(
                    "Cannot setup source resolver.", ce);
        }
        return resolver;        
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

        // create bootstrap logger
        final BootstrapEnvironment.LogLevel level = BootstrapEnvironment.LogLevel.getLogLevelForName(logLevel);
        final Logger bootstrapLogger = this.env.getBootstrapLogger(level);

        // Create our own resolver
        final SourceResolver resolver = this.createSourceResolver(bootstrapLogger);

        // create an own service manager for the logger manager
        final ServiceManager loggerManagerServiceManager = new SingleComponentServiceManager(
                 null, resolver, SourceResolver.ROLE);

        // create an own context for the logger manager
        final DefaultContext subcontext = new SettingsContext(this.appContext, this.settings);
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

        // the log4j support requires currently that the log4j system is already
        // configured elsewhere

        final LoggerManager loggerManager = this.newLoggerManager(loggerManagerClass);
        ContainerUtil.enableLogging(loggerManager, bootstrapLogger);

        try {
            ContainerUtil.contextualize(loggerManager, subcontext);
            ContainerUtil.service(loggerManager, loggerManagerServiceManager);

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
                        final Configuration conf = builder.build(source.getInputStream());
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
            ContainerUtil.initialize(loggerManager);
        } catch (Exception e) {
            bootstrapLogger.error(
                    "Could not set up Cocoon Logger, will use screen instead",
                    e);
        }

        this.log = this.loggerManager.getLoggerForCategory(accesslogger);
    }

    /**
     * Create a new logger manager.
     * @param loggerManagerClass The class name or one of the allowed shortcuts.
     * @return A new logger manager.
     */
    private LoggerManager newLoggerManager(String loggerManagerClass) {
        if ("LogKit".equalsIgnoreCase(loggerManagerClass) || loggerManagerClass == null) {
            loggerManagerClass = CocoonLogKitLoggerManager.class.getName();
        } else if ("LOG4J".equalsIgnoreCase(loggerManagerClass)) {
            loggerManagerClass = Log4JConfLoggerManager.class.getName();
        }
        try {
            Class clazz = Class.forName(loggerManagerClass);
            if ( PerRequestLoggerManager.class.isAssignableFrom(clazz) ) {
                this.isPerRequestLoggerManager = true;
            }
            return (LoggerManager) clazz.newInstance();
        } catch (Exception e) {
            this.isPerRequestLoggerManager = true;
            return new CocoonLogKitLoggerManager();
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

    /**
     * Creates the Cocoon object and handles exception handling.
     */
    public synchronized Cocoon createCocoon()
    throws Exception {

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
            this.settings.setCreationTime(System.currentTimeMillis());
            this.cocoon = c;
        } catch (Exception e) {
            this.log.error("Exception reloading Cocoon.", e);
            this.disposeCocoon();
            throw e;
        }
        return this.cocoon;
    }

    /**
     * Create the classloader that inlcudes all the [block]/BLOCK-INF/classes directories. 
     * @throws Exception
     */
    protected void createClassloader() throws Exception {
        // get the wiring
        final SourceResolver resolver = this.createSourceResolver(this.log);    
        Source wiringSource = null;
        final Configuration wiring;
        try {
            wiringSource = resolver.resolveURI(Constants.WIRING);
            DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
            wiring = builder.build( wiringSource.getInputStream() );            
        } catch(org.apache.excalibur.source.SourceNotFoundException snfe) {
            throw new WiringNotFoundException("wiring.xml not found in the root directory of your Cocoon application.");
        } finally {
            resolver.release(wiringSource);
        }
        
        // get all wired blocks and add their classed directory to the classloader
        List urlList = new ArrayList();        
        Configuration[] blocks = wiring.getChildren("block");
        for(int i = 0; i < blocks.length; i++) {
            String location = blocks[i].getAttribute("location");
            if(this.log.isDebugEnabled()) {
                this.log.debug("Found block " + blocks[i].getAttribute("id") + " at " + location);
            }
            Source classesDir = null;
            try {
               classesDir = resolver.resolveURI(location + "/" + Constants.BLOCK_META_DIR + "/classes");
               if(classesDir.exists()) {
                   String classesDirURI = classesDir.getURI();
                   urlList.add(new URL(classesDirURI));
                   if(this.log.isDebugEnabled()) {
                       this.log.debug("added " + classesDir.getURI());
                   }
               }               
            } finally {
                resolver.release(classesDir);
            }
        }

        // setup the classloader using the current classloader as parent
        ClassLoader parentClassloader = Thread.currentThread().getContextClassLoader();
        URL[] urls = (URL[]) urlList.toArray(new URL[urlList.size()]);        
        URLClassLoader classloader = new URLClassLoader(urls, parentClassloader);
        Thread.currentThread().setContextClassLoader(classloader);
        this.classloader = Thread.currentThread().getContextClassLoader();
    }

    /**
     * Gets the current cocoon object.
     * Reload cocoon if configuration changed or we are reloading.
     * Ensure that the correct classloader is set.
     */
    public Cocoon getCocoon(final String pathInfo, final String reloadParam)
    throws Exception {
        
        // set the blocks classloader for this thread
        Thread.currentThread().setContextClassLoader(this.classloader);        
        
        if (this.settings.isReloadingEnabled("config")) {
            boolean reload = false;

            if (this.cocoon != null) {
                if (this.cocoon.modifiedSince(this.settings.getCreationTime())) {
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
        final Iterator i = this.settings.getLoadClasses().iterator();
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
//        // concatenate the class path and the extra class path
//        String classPath = this.env.getClassPath(this.settings);
//        StringBuffer buffer = new StringBuffer();
//        if ( classPath != null && classPath.length() > 0 ) {
//            buffer.append(classPath);
//        }
//        classPath = this.getExtraClassPath();
//        if ( classPath != null && classPath.length() > 0 ) {
//            if ( buffer.length() > 0 ) {
//                buffer.append(File.pathSeparatorChar);
//            }
//            buffer.append(classPath);
//        }
        // FIXME - for now we just set an empty string as this information is looked up
        //         by other components
        this.appContext.put(Constants.CONTEXT_CLASSPATH, "");
    }

    /**
     * Dispose Cocoon when environment is destroyed
     */
    public void destroy() {
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

    protected static final class LoggerWrapper implements Logger {
        private final BootstrapEnvironment env;

        public LoggerWrapper(BootstrapEnvironment env) {
            this.env = env;
        }

        protected void text(String arg0, Throwable arg1) {
            if ( arg1 != null ) {
                this.env.log(arg0, arg1);
            } else {
                this.env.log(arg0);
            }
        }

        public void debug(String arg0, Throwable arg1) {
            // we ignore debug
        }

        public void debug(String arg0) {
            // we ignore debug
        }

        public void error(String arg0, Throwable arg1) {
            this.text(arg0, arg1);
        }

        public void error(String arg0) {
            this.text(arg0, null);
        }

        public void fatalError(String arg0, Throwable arg1) {
            this.text(arg0, arg1);
        }

        public void fatalError(String arg0) {
            this.text(arg0, null);
        }

        public Logger getChildLogger(String arg0) {
            return this;
        }

        public void info(String arg0, Throwable arg1) {
            // we ignore info
        }

        public void info(String arg0) {
            // we ignore info
        }

        public boolean isDebugEnabled() {
            return false;
        }

        public boolean isErrorEnabled() {
            return true;
        }

        public boolean isFatalErrorEnabled() {
            return true;
        }

        public boolean isInfoEnabled() {
            return false;
        }

        public boolean isWarnEnabled() {
            return false;
        }

        public void warn(String arg0, Throwable arg1) {
            // we ignore warn
        }

        public void warn(String arg0) {
            // we ignore warn
        }
    }
}
