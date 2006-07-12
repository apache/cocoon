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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.source.impl.DelayedRefreshSourceWrapper;
import org.apache.cocoon.configuration.PropertyProvider;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.configuration.impl.MutableSettings;
import org.apache.cocoon.configuration.impl.SettingsHelper;
import org.apache.cocoon.core.container.spring.AvalonEnvironment;
import org.apache.cocoon.core.container.spring.BeanFactoryUtil;
import org.apache.cocoon.core.container.spring.ConfigReader;
import org.apache.cocoon.core.container.spring.ConfigurationInfo;
import org.apache.cocoon.core.container.util.ComponentContext;
import org.apache.cocoon.core.container.util.ConfigurationBuilder;
import org.apache.cocoon.core.container.util.SimpleSourceResolver;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.location.Location;
import org.apache.cocoon.util.location.LocationImpl;
import org.apache.cocoon.util.location.LocationUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.impl.URLSource;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.xml.sax.InputSource;

/**
 * This is an utility class to create a new Cocoon instance.
 * 
 * @version $Id$
 * @since 2.2
 */
public class CoreUtil {

    /** Parameter map for the context protocol */
    protected static final Map CONTEXT_PARAMETERS = Collections.singletonMap("force-traversable", Boolean.TRUE);

    // Register the location finder for Avalon configuration objects and exceptions
    // and keep a strong reference to it.
    private static final LocationUtils.LocationFinder confLocFinder = new LocationUtils.LocationFinder() {
        public Location getLocation(Object obj, String description) {
            if (obj instanceof Configuration) {
                Configuration config = (Configuration)obj;
                String locString = config.getLocation();
                Location result = LocationUtils.parse(locString);
                if (LocationUtils.isKnown(result)) {
                    // Add description
                    StringBuffer desc = new StringBuffer().append('<');
                    // Unfortunately Configuration.getPrefix() is not public
                    try {
                        if (config.getNamespace().startsWith("http://apache.org/cocoon/sitemap/")) {
                            desc.append("map:");
                        }
                    } catch (ConfigurationException e) {
                        // no namespace: ignore
                    }
                    desc.append(config.getName()).append('>');
                    return new LocationImpl(desc.toString(), result);
                } else {
                    return result;
                }
            }
            
            if (obj instanceof Exception) {
                // Many exceptions in Cocoon have a message like "blah blah at file://foo/bar.xml:12:1"
                String msg = ((Exception)obj).getMessage();
                if (msg == null) return null;
                
                int pos = msg.lastIndexOf(" at ");
                if (pos != -1) {
                    return LocationUtils.parse(msg.substring(pos + 4));
                } else {
                    // Will try other finders
                    return null;
                }
            }
            
            // Try next finders.
            return null;
        }
    };
    
    static {
        LocationUtils.addFinder(confLocFinder);
    }
    
    public static ConfigurableBeanFactory createRootContainer(ServletContext context)
    throws Exception {
        return createRootContainer(context, null);
    }

    public static ConfigurableBeanFactory createRootContainer(ServletContext   servletContext,
                                                              PropertyProvider externalPropertyProvider)
    throws Exception {
        // first let's set up the appContext with some values to make
        // the simple source resolver work
        // "legacy" support: create an avalon context.
        final DefaultContext appContext = new ComponentContext();

        // add root url
        String contextUrl = CoreUtil.getContextUrl(servletContext, "/WEB-INF/web.xml");
        CoreUtil.addSourceResolverContext(appContext, servletContext, contextUrl);

        // create settings
        final MutableSettings settings = CoreUtil.createSettings(servletContext, appContext, externalPropertyProvider);

        // Create bootstrap logger
        Logger log = BeanFactoryUtil.createBootstrapLogger(servletContext, settings.getBootstrapLogLevel());

        if (log.isDebugEnabled()) {
            log.debug("Context URL: " + contextUrl);
        }
        // initialize some directories
        CoreUtil.initSettingsFiles(settings, log);

        // update configuration
        final URL u = CoreUtil.getConfigFile(settings.getConfiguration(), servletContext, log);
        settings.setConfiguration(u.toExternalForm());

        // dump system properties
        CoreUtil.dumpSystemProperties(log);

        // settings can't be changed anymore
        settings.makeReadOnly();

        // Init logger
        log = BeanFactoryUtil.createRootLogger(servletContext,
                                               settings);

        // add the Avalon context attributes that are contained in the settings
        CoreUtil.addSettingsContext(appContext, settings);

        // force load classes
        CoreUtil.forceLoad(settings, log);

        // setup of the spring based container
        return CoreUtil.setupSpringContainer(settings, servletContext, appContext, log);
    }

    /**
     * Init work, upload and cache directory
     * @param settings 
     * @param log 
     */
    public static void initSettingsFiles(MutableSettings settings, Logger log) {
        // first init the work-directory for the logger.
        // this is required if we are running inside a war file!
        final String workDirParam = settings.getWorkDirectory();
        File workDir;
        if (workDirParam != null) {
            // No context path : consider work-directory as absolute
            workDir = new File(workDirParam);
        } else {
            workDir = new File("cocoon-files");
        }
        workDir.mkdirs();
        settings.setWorkDirectory(workDir.getAbsolutePath());

        // Output some debug info
        if (log.isDebugEnabled()) {
            if (workDirParam != null) {
                log.debug("Using work-directory " + workDir);
            } else {
                log.debug("Using default work-directory " + workDir);
            }
        }

        final String uploadDirParam = settings.getUploadDirectory();
        File uploadDir;
        if (uploadDirParam != null) {
            uploadDir = new File(uploadDirParam);
            if (log.isDebugEnabled()) {
                log.debug("Using upload-directory " + uploadDir);
            }
        } else {
            uploadDir = new File(workDir, "upload-dir" + File.separator);
            if (log.isDebugEnabled()) {
                log.debug("Using default upload-directory " + uploadDir);
            }
        }
        uploadDir.mkdirs();
        settings.setUploadDirectory(uploadDir.getAbsolutePath());

        String cacheDirParam = settings.getCacheDirectory();
        File cacheDir;
        if (cacheDirParam != null) {
            cacheDir = new File(cacheDirParam);
            if (log.isDebugEnabled()) {
                log.debug("Using cache-directory " + cacheDir);
            }
        } else {
            cacheDir = new File(workDir, "cache-dir" + File.separator);
            File parent = cacheDir.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            if (log.isDebugEnabled()) {
                log.debug("cache-directory was not set - defaulting to " + cacheDir);
            }
        }
        cacheDir.mkdirs();
        settings.setCacheDirectory(cacheDir.getAbsolutePath());
    }

    public static DefaultContext createContext(Settings       settings,
                                               ServletContext servletContext,
                                               String         contextUrl)
        throws ServletException, MalformedURLException {
        DefaultContext appContext = new ComponentContext();
        CoreUtil.addSourceResolverContext(appContext, servletContext, contextUrl);
        CoreUtil.addSettingsContext(appContext, settings);
        return appContext;
    }

    /**
     * Adding the Avalon context content needed for setting up the <code>SimpleSourceResolver</code>
     * @param appContext the Avalon context
     * @param servletContext the Cocoon context
     * @param contextUrl URL for the context
     */
    private static void addSourceResolverContext(DefaultContext appContext,
                                                 ServletContext servletContext,
                                                 String         contextUrl) {
        try {
            appContext.put(ContextHelper.CONTEXT_ROOT_URL, new URL(contextUrl));
        } catch (MalformedURLException ignore) {
            // we simply ignore this
        }
    
        // add environment context and config
        appContext.put(Constants.CONTEXT_ENVIRONMENT_CONTEXT, servletContext);
    }

    /**
     * Add the Avalon context attributes that are contained in the settings
     * @param appContext 
     * @param settings
     * @param classloader 
     * @throws MalformedURLException
     */
    private static void addSettingsContext(DefaultContext appContext, Settings settings)
    throws MalformedURLException {
        appContext.put(Constants.CONTEXT_WORK_DIR, new File(settings.getWorkDirectory()));
        appContext.put(Constants.CONTEXT_UPLOAD_DIR, new File(settings.getUploadDirectory()));
        appContext.put(Constants.CONTEXT_CACHE_DIR, new File(settings.getCacheDirectory()));
        if(settings.getConfiguration() != null) {
        	appContext.put(Constants.CONTEXT_CONFIG_URL, new URL(settings.getConfiguration()));
        }
        appContext.put(Constants.CONTEXT_DEFAULT_ENCODING, settings.getFormEncoding());
    }

    /**
     * Get the settings for Cocoon.
     * This method reads several property files and merges the result. If there
     * is more than one definition for a property, the last one wins.
     * The property files are read in the following order:
     * 1) context://WEB-INF/properties/*.properties
     *    Default values for the core and each block - the order in which the files are read is not guaranteed.
     * 2) context://WEB-INF/properties/[RUNNING_MODE]/*.properties
     *    Default values for the running mode - the order in which the files are read is not guaranteed.
     * 3) Property providers (ToBeDocumented)
     * 4) The environment (CLI, Servlet etc.) adds own properties (e.g. from web.xml)
     * 5) Additional property file specified by the "org.apache.cocoon.settings" system property or
     *    if the property is not found, the file ".cocoon/settings.properties" is tried to be read from
     *    the user directory.
     * 6) System properties
     *
     * @return A new Settings object
     */
    protected static MutableSettings createSettings(ServletContext   servletContext,
                                                    Context          appContext,
                                                    PropertyProvider externalPropertyProvider) {
        // we need a logger for the settings util which will log info messages
        final Logger logger = new LoggerWrapper(servletContext, true);
        // we need our own resolver (with own logger which just logs errors)
        final SourceResolver resolver = CoreUtil.createSourceResolver(appContext, new LoggerWrapper(servletContext));
        return SettingsHelper.createSettings(servletContext, resolver, logger, externalPropertyProvider);
    }

    /**
     * Dump System Properties.
     */
    protected static void dumpSystemProperties(Logger log) {
        if (log.isDebugEnabled()) {
            try {
                Enumeration e = System.getProperties().propertyNames();
                log.debug("===== System Properties Start =====");
                for (; e.hasMoreElements();) {
                    String key = (String) e.nextElement();
                    log.debug(key + "=" + System.getProperty(key));
                }
                log.debug("===== System Properties End =====");
            } catch (SecurityException se) {
                // Ignore Exceptions.
            }
        }
    }

    /**
     * Create a simple source resolver.
     */
    protected static SourceResolver createSourceResolver(Context appContext,
                                                         Logger  logger) {
        // Create our own resolver
        final SimpleSourceResolver resolver = new SimpleSourceResolver();
        resolver.enableLogging(logger);
        try {
            resolver.contextualize(appContext);
        } catch (ContextException ce) {
            throw new CoreInitializationException(
                    "Cannot setup source resolver.", ce);
        }
        return resolver;        
    }

    protected static ConfigurableBeanFactory setupSpringContainer(MutableSettings settings,
                                                                  ServletContext  servletContext,
                                                                  Context         appContext,
                                                                  Logger          log)
    throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Reading root configuration: " + settings.getConfiguration());
        }

        URLSource urlSource = new URLSource();
        urlSource.init(new URL(settings.getConfiguration()), null);
        final Source configurationFile = new DelayedRefreshSourceWrapper(urlSource,
                settings.getReloadDelay("config"));
        final InputSource is = SourceUtil.getInputSource(configurationFile);

        final ConfigurationBuilder builder = new ConfigurationBuilder(settings);
        final Configuration rootConfig = builder.build(is);

        if (!"cocoon".equals(rootConfig.getName())) {
            throw new ConfigurationException("Invalid configuration file\n" + rootConfig.toString());
        }
        if (log.isDebugEnabled()) {
            log.debug("Configuration version: " + rootConfig.getAttribute("version"));
        }
        if (!Constants.CONF_VERSION.equals(rootConfig.getAttribute("version"))) {
            throw new ConfigurationException("Invalid configuration schema version. Must be '" + Constants.CONF_VERSION + "'.");
        }

        if (log.isInfoEnabled()) {
            log.info("Setting up root Spring container.");
        }
        final AvalonEnvironment avalonEnv = new AvalonEnvironment();
        avalonEnv.context = appContext;
        avalonEnv.logger = log;
        avalonEnv.settings = settings;
        ConfigurableBeanFactory rootContext = BeanFactoryUtil.createRootBeanFactory(avalonEnv, servletContext);
        ConfigurationInfo result = ConfigReader.readConfiguration(settings.getConfiguration(), avalonEnv);
        ConfigurableBeanFactory mainContext = BeanFactoryUtil.createBeanFactory(avalonEnv, result, null, rootContext);

        settings.setCreationTime(System.currentTimeMillis());
        return mainContext;
    }

    /**
     * Get the URL of the main Cocoon configuration file.
     */
    protected static URL getConfigFile(final String         configFileName,
                                       final ServletContext servletContext,
                                       final Logger         log)
    throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Using configuration file: " + configFileName);
        }

        URL result;
        try {
            // test if this is a qualified url
            if (configFileName.indexOf(':') == -1) {
                result = servletContext.getResource(configFileName);
            } else {
                result = new URL(configFileName);
            }
        } catch (Exception mue) {
            String msg = "Setting for 'configuration' is invalid : " + configFileName;
            log.error(msg, mue);
            throw new CoreInitializationException(msg, mue);
        }

        if (result == null) {
            File resultFile = new File(configFileName);
            if (resultFile.isFile()) {
                try {
                    result = resultFile.getCanonicalFile().toURL();
                } catch (Exception e) {
                    String msg = "Setting for 'configuration' is invalid : " + configFileName;
                    log.error(msg, e);
                    throw new CoreInitializationException(msg, e);
                }
            }
        }

        if (result == null) {
            String msg = "Setting for 'configuration' doesn't name an existing resource : " + configFileName;
            log.error(msg);
            throw new CoreInitializationException(msg);
        }
        return result;
    }

    /**
     * @param environmentContext
     */
    public static String getWritableContextPath(ServletContext servletContext) {
        return servletContext.getRealPath("/");
    }

    /**
     * @param environmentContext 
     * @param knownFile 
     */
    public static String getContextUrl(ServletContext servletContext, String knownFile) {
        String servletContextURL;
        String servletContextPath = CoreUtil.getWritableContextPath(servletContext);
        String path = servletContextPath;

        if (path == null) {
            // Try to figure out the path of the root from that of a known file in the context
            try {
                path = servletContext.getResource(knownFile).toString();
            } catch (MalformedURLException me) {
                throw new CoreInitializationException("Unable to get resource '" + knownFile + "'.", me);
            }
            path = path.substring(0, path.length() - (knownFile.length() - 1));
        }
        try {
            if (path.indexOf(':') > 1) {
                servletContextURL = path;
            } else {
                servletContextURL = new File(path).toURL().toExternalForm();
            }
        } catch (MalformedURLException me) {
            // VG: Novell has absolute file names starting with the
            // volume name which is easily more then one letter.
            // Examples: sys:/apache/cocoon or sys:\apache\cocoon
            try {
                servletContextURL = new File(path).toURL().toExternalForm();
            } catch (MalformedURLException ignored) {
                throw new CoreInitializationException("Unable to determine servlet context URL.", me);
            }
        }
        return servletContextURL;
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
    protected static void forceLoad(final Settings settings, final Logger log) {
        final Iterator i = settings.getLoadClasses().iterator();
        while (i.hasNext()) {
            final String fqcn = (String)i.next();
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Loading: " + fqcn);
                }
                ClassUtils.loadClass(fqcn).newInstance();
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("Could not load class: " + fqcn, e);
                }
                // Do not throw an exception, because it is not a fatal error.
            }
        }
    }

    protected static final class LoggerWrapper implements Logger {

        private final ServletContext servletContext;

        private final boolean displayInfoAndWarn;

        public LoggerWrapper(ServletContext servletContext) {
            this.servletContext = servletContext;
            this.displayInfoAndWarn = false;
        }

        public LoggerWrapper(ServletContext servletContext, boolean displayInfoAndWarn) {
            this.servletContext = servletContext;
            this.displayInfoAndWarn = displayInfoAndWarn;
        }

        protected void text(String arg0, Throwable arg1) {
            if ( arg1 != null ) {
                this.servletContext.log(arg0, arg1);
            } else {
                this.servletContext.log(arg0);
            }
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#debug(java.lang.String, java.lang.Throwable)
         */
        public void debug(String arg0, Throwable arg1) {
            // we ignore debug
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#debug(java.lang.String)
         */
        public void debug(String arg0) {
            // we ignore debug
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#error(java.lang.String, java.lang.Throwable)
         */
        public void error(String arg0, Throwable arg1) {
            this.text(arg0, arg1);
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#error(java.lang.String)
         */
        public void error(String arg0) {
            this.text(arg0, null);
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#fatalError(java.lang.String, java.lang.Throwable)
         */
        public void fatalError(String arg0, Throwable arg1) {
            this.text(arg0, arg1);
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#fatalError(java.lang.String)
         */
        public void fatalError(String arg0) {
            this.text(arg0, null);
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#getChildLogger(java.lang.String)
         */
        public Logger getChildLogger(String arg0) {
            return this;
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#info(java.lang.String, java.lang.Throwable)
         */
        public void info(String arg0, Throwable arg1) {
            if ( this.displayInfoAndWarn ) {
                this.text(arg0, arg1);
            }
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#info(java.lang.String)
         */
        public void info(String arg0) {
            if ( this.displayInfoAndWarn ) {
                this.text(arg0, null);
            }
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#isDebugEnabled()
         */
        public boolean isDebugEnabled() {
            return false;
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#isErrorEnabled()
         */
        public boolean isErrorEnabled() {
            return true;
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#isFatalErrorEnabled()
         */
        public boolean isFatalErrorEnabled() {
            return true;
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#isInfoEnabled()
         */
        public boolean isInfoEnabled() {
            return this.displayInfoAndWarn;
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#isWarnEnabled()
         */
        public boolean isWarnEnabled() {
            return this.displayInfoAndWarn;
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#warn(java.lang.String, java.lang.Throwable)
         */
        public void warn(String arg0, Throwable arg1) {
            if ( this.displayInfoAndWarn ) {
                this.text(arg0, arg1);
            }
        }

        /**
         * @see org.apache.avalon.framework.logger.Logger#warn(java.lang.String)
         */
        public void warn(String arg0) {
            if ( this.displayInfoAndWarn ) {
                this.text(arg0, null);
            }
        }
    }
}