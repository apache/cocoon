/*
 * Copyright 2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * This object holds the global configuration of Cocoon.
 *
 * @version SVN $Id$
 */
public class Settings {

    /** Prefix for properties */
    protected static final String KEYPREFIX = "org.apache.cocoon.";

    /** Name of the property specifying a user properties file */
    public static final String PROPERTY_USER_SETTINGS = "org.apache.cocoon.settings";

    /**
     * Default value for {@link #isAllowReload()} parameter (false)
     */
    public static final boolean ALLOW_RELOAD = false;

    /**
     * Default value for {@link #isEnableUploads()} parameter (false)
     */
    public static final boolean ENABLE_UPLOADS = false;
    public static final boolean SAVE_UPLOADS_TO_DISK = true;
    public static final int MAX_UPLOAD_SIZE = 10000000; // 10Mb

    /**
     * Default value for {@link #isInitClassloader()} setting (false)
     */
    public static final boolean INIT_CLASSLOADER = false;

    public static final boolean SHOW_TIME = false;
    public static final boolean HIDE_SHOW_TIME = false;
    public static final boolean MANAGE_EXCEPTIONS = true;

    /**
     * The list of properties used to configure Cocoon
     */
    protected List properties = new ArrayList();

    /**
     * This parameter tells Cocoon to set the thread's context classloader to
     * its own classloader. If you experience strange classloader issues,
     * try setting this parameter to "true".
     */
    protected boolean initClassloader = INIT_CLASSLOADER;
    public static final String KEY_INIT_CLASSLOADER = "classloader.init";

    /**
     * This parameter allows to set system properties
     */
    protected Map forceProperties = new HashMap();
    /** FIXME - implement the support for this key: */
    public static final String KEY_FORCE_PROPERTIES = "system.properties";

    /**
     * This parameter points to the main configuration file for Cocoon.
     * Note that the path is specified in absolute notation but it will be
     * resolved relative to the application context path.
     */
    protected String configuration;
    public static final String KEY_CONFIGURATION = "configuration";

    /**
     * This parameter indicates the configuration file of the LogKit management
     */
    protected String loggingConfiguration;
    public static final String KEY_LOGGING_CONFIGURATION = "logging.configuration";

    /**
     * This parameter indicates the category id of the logger from the LogKit
     * configuration used by the environment.
     */
    protected String accessLogger;
    public static final String KEY_LOGGING_ACCESS_LOGGER = "logging.logger.access.category";

    /**
     * This parameter indicates the category id of the logger from the LogKit
     * management configuration for the Cocoon engine.
     * This logger is used for all components described in the cocoon.xconf
     * and sitemap.xmap file not having specified a logger with the
     * logger="..." attribute in the component configuration file.
     */
    protected String cocoonLogger;
    public static final String KEY_LOGGING_COCOON_LOGGER = "logging.logger.cocoon.category";

    /**
     * This parameter indicates the log level to use throughout startup of the
     * system. As soon as the logkit.xconf the setting of the logkit.xconf
     * configuration is used instead! Only for startup and if the logkit.xconf is
     * not readable/available this log level is of importance.
     */
    protected String bootstrapLogLevel;
    public static final String KEY_LOGGING_BOOTSTRAP_LOGLEVEL = "logging.bootstrap.loglevel";

    /**
     * This parameter switches the logging system from LogKit to Log4J for Cocoon.
     * Log4J has to be configured already.
     */
    protected String loggerClassName;
    public static final String KEY_LOGGING_MANAGER_CLASS = "logging.manager.class";

    /**
     * If you want to configure log4j using Cocoon, then you can define
     * an XML configuration file here. You can use the usual log4j property
     * substituation mechanism, e.g. ${context-root} is replaced by the
     * context root of this web application etc.
     * You can configure the log4j configuration even if you use LogKit
     * for Cocoon logging. You can use this to configure third party code
     * for example.
     */
    protected String log4jConfiguration;
    public static final String KEY_LOGGING_LOG4J_CONFIGURATION = "logging.log4j.configuration";

    /**
     * Allow reinstantiating (reloading) of the cocoon instance. If this is
     * set to "yes" or "true", a new cocoon instance can be created using
     * the request parameter "cocoon-reload". It also enables that Cocoon is
     * reloaded when cocoon.xconf changes. Default is no for security reasons.
     */
    protected boolean allowReload = ALLOW_RELOAD;
    public static final String KEY_ALLOW_RELOAD = "allow.reload";

    /**
     * This parameter is used to list classes that should be loaded at
     * initialization time of the servlet. For example, JDBC Drivers used need to
     * be named here. Additional entries may be inserted here during build
     * depending on your build properties.
     */
    protected List loadClasses = new ArrayList();
    /** FIXME: Implement support for this: */
    public static final String KEY_LOAD_CLASSES = "classloader.load.classes";

    /**
     * Causes all files in multipart requests to be processed.
     * Default is false for security reasons.
     */
    protected boolean enableUploads = ENABLE_UPLOADS;
    public static final String KEY_UPLOADS_ENABLE = "uploads.enable";

    /**
     * This parameter allows to specify where Cocoon should put uploaded files.
     * The path specified can be either absolute or relative to the context
     * path of the servlet. On windows platform, absolute directory must start
     * with volume: C:\Path\To\Upload\Directory.
     */
    protected String uploadDirectory;
    public static final String KEY_UPLOADS_DIRECTORY = "uploads.directory";

    /**
     * Causes all files in multipart requests to be saved to upload-dir.
     * Default is true for security reasons.
     */
    protected boolean autosaveUploads = SAVE_UPLOADS_TO_DISK;
    public static final String KEY_UPLOADS_AUTOSAVE = "uploads.autosave";

    /**
     * Specify handling of name conflicts when saving uploaded files to disk.
     * Acceptable values are deny, allow, rename (default). Files are renamed
     * x_filename where x is an integer value incremented to make the new
     * filename unique.
     */
    protected String overwriteUploads;
    public static final String KEY_UPLOADS_OVERWRITE = "uploads.overwrite";

    /**
     * Specify maximum allowed size of the upload. Defaults to 10 Mb.
     */
    protected int maxUploadSize = MAX_UPLOAD_SIZE;
    public static final String KEY_UPLOADS_MAXSIZE = "uploads.maxsize";

    /**
     * This parameter allows to specify where Cocoon should create its page
     * and other objects cache. The path specified can be either absolute or
     * relative to the context path of the servlet. On windows platform,
     * absolute directory must start with volume: C:\Path\To\Cache\Directory.
     */
    protected String cacheDirectory;
    public static final String KEY_CACHE_DIRECTORY = "cache.directory";

    /**
     * This parameter allows to specify where Cocoon should put it's
     * working files. The path specified is either absolute or relative
     * to the context path of the Cocoon servlet. On windows platform,
     * absolute directory must start with volume: C:\Path\To\Work\Directory.
     */
    protected String workDirectory;
    public static final String KEY_WORK_DIRECTORY = "work.directory";

    /**
     * This parameter allows to specify additional directories or jars
     * which Cocoon should put into it's own classpath.
     * Note that absolute pathes are taken as such but relative pathes
     * are rooted at the context root of the Cocoon servlet.
     */
    protected List extraClasspaths = new ArrayList();
    /** FIXME: Implement support for this: */
    public static final String KEY_EXTRA_CLASSPATHS = "extra.classpaths";

    /**
     * This parameter allows you to select the parent service manager.
     * The class will be instantiated via the constructor that takes a single
     * String as a parameter. That String will be equal to the text after the '/'.
     *
     * Cocoon honors the LogEnabled, Initializable and Disposable interfaces for
     * this class, if it implements them.
     */
    protected String parentServiceManagerClassName;
    public static final String KEY_PARENT_SERVICE_MANAGER = "parentservicemanager";

    /**
     * Allow adding processing time to the response
     */
    protected boolean showTime = SHOW_TIME;
    public static final String KEY_SHOWTIME = "showtime";

    /**
     * If true, processing time will be added as an HTML comment
     */
    protected boolean hideShowTime = HIDE_SHOW_TIME;
    public static final String KEY_HIDE_SHOWTIME = "hideshowtime";

    /**
     * If true or not set, this class will try to catch and handle all Cocoon exceptions.
     * If false, it will rethrow them to the servlet container.
     */
    protected boolean manageExceptions = MANAGE_EXCEPTIONS;
    public static final String KEY_MANAGE_EXCEPTIONS = "manageexceptions";

    /**
     * Set form encoding. This will be the character set used to decode request
     * parameters. If not set the ISO-8859-1 encoding will be assumed.
    */
    protected String formEncoding;
    public static final String KEY_FORM_ENCODING = "formencoding";

    /**
     * If this value is specified, it will be interpreted as a log level and
     * all logging categories will be set to this level regardless of their
     * definition in the logging configuration.
     */
    protected String overrideLogLevel;
    public static final String KEY_LOGGING_OVERRIDE_LOGLEVEL = "override.loglevel";

    /**
     * Delay between reload checks for the configuration
     */
    protected long configurationReloadDelay = 1000;
    public static final String KEY_CONFIGURATION_RELOAD_DELAY = "configuration.reloaddelay";

    /**
     * Lazy mode for component loading
     */
    protected boolean lazyMode = false;
    public static final String KEY_LAZY_MODE = "core.LazyMode";

    /**
     * Create a new settings object
     */
    public Settings() {
        // nothing to do
    }

    /**
     * Fill from a properties object
     */
    public void fill(Properties props) {
        if ( props != null ) {
            final Iterator i = props.entrySet().iterator();
            while ( i.hasNext() ) {
                final Map.Entry current = (Map.Entry)i.next();
                String key = current.getKey().toString();
                if ( key.startsWith(KEYPREFIX) ) {
                    key = key.substring(KEYPREFIX.length());
                    final String value = current.getValue().toString();

                    if ( key.equals(KEY_INIT_CLASSLOADER) ) {
                        this.initClassloader = BooleanUtils.toBoolean(value);
                    } else if ( key.equals(KEY_CONFIGURATION) ) {
                        this.configuration = value;
                    } else if ( key.equals(KEY_CONFIGURATION_RELOAD_DELAY) ) {
                        this.configurationReloadDelay = NumberUtils.toLong(value);
                    } else if ( key.equals(KEY_LOGGING_CONFIGURATION) ) {
                        this.loggingConfiguration = value;
                    } else if ( key.equals(KEY_LOGGING_ACCESS_LOGGER) ) {
                        this.accessLogger = value;
                    } else if ( key.equals(KEY_LOGGING_COCOON_LOGGER) ) {
                        this.cocoonLogger = value;
                    } else if ( key.equals(KEY_LOGGING_BOOTSTRAP_LOGLEVEL) ) {
                        this.bootstrapLogLevel = value;
                    } else if ( key.equals(KEY_LOGGING_MANAGER_CLASS) ) {
                        this.loggerClassName = value;
                    } else if ( key.equals(KEY_LOGGING_LOG4J_CONFIGURATION) ) {
                        this.log4jConfiguration = value;
                    } else if ( key.equals(KEY_ALLOW_RELOAD) ) {
                        this.allowReload = BooleanUtils.toBoolean(value);
                    } else if ( key.equals(KEY_UPLOADS_ENABLE) ) {
                        this.enableUploads = BooleanUtils.toBoolean(value);
                    } else if ( key.equals(KEY_UPLOADS_DIRECTORY) ) {
                        this.uploadDirectory = value;
                    } else if ( key.equals(KEY_UPLOADS_AUTOSAVE) ) {
                        this.autosaveUploads = BooleanUtils.toBoolean(value);
                    } else if ( key.equals(KEY_UPLOADS_OVERWRITE) ) {
                        this.overwriteUploads = value;
                    } else if ( key.equals(KEY_UPLOADS_MAXSIZE) ) {
                        this.maxUploadSize = NumberUtils.toInt(value);
                    } else if ( key.equals(KEY_CACHE_DIRECTORY) ) {
                        this.cacheDirectory = value;
                    } else if ( key.equals(KEY_WORK_DIRECTORY) ) {
                        this.workDirectory = value;
                    } else if ( key.equals(KEY_PARENT_SERVICE_MANAGER) ) {
                        this.parentServiceManagerClassName = value;
                    } else if ( key.equals(KEY_SHOWTIME) ) {
                        this.showTime = BooleanUtils.toBoolean(value);
                    } else if ( key.equals(KEY_HIDE_SHOWTIME) ) {
                        this.hideShowTime = BooleanUtils.toBoolean(value);
                    } else if ( key.equals(KEY_MANAGE_EXCEPTIONS) ) {
                        this.manageExceptions = BooleanUtils.toBoolean(value);
                    } else if ( key.equals(KEY_FORM_ENCODING) ) {
                        this.formEncoding = value;
                    } else if ( key.equals(KEY_LOGGING_OVERRIDE_LOGLEVEL) ) {
                        this.overrideLogLevel = value;
                    } else if ( key.equals(KEY_LAZY_MODE) ) {
                        this.lazyMode = BooleanUtils.toBoolean(value);
                    }
                }
            }
            this.properties.add(props);
        }
    }

    /**
     * @return Returns the hideShowTime.
     */
    public boolean isHideShowTime() {
        return this.hideShowTime;
    }
    /**
     * @param hideShowTime The hideShowTime to set.
     */
    public void setHideShowTime(boolean hideShowTime) {
        this.hideShowTime = hideShowTime;
    }
    /**
     * @return Returns the allowReload.
     */
    public boolean isAllowReload() {
        return this.allowReload;
    }
    /**
     * @param allowReload The allowReload to set.
     */
    public void setAllowReload(boolean allowReload) {
        this.allowReload = allowReload;
    }
    /**
     * @return Returns the autosaveUploads.
     */
    public boolean isAutosaveUploads() {
        return this.autosaveUploads;
    }
    /**
     * @param autosaveUploads The autosaveUploads to set.
     */
    public void setAutosaveUploads(boolean autosaveUploads) {
        this.autosaveUploads = autosaveUploads;
    }
    /**
     * @return Returns the cacheDirectory.
     */
    public String getCacheDirectory() {
        return this.cacheDirectory;
    }
    /**
     * @param cacheDirectory The cacheDirectory to set.
     */
    public void setCacheDirectory(String cacheDirectory) {
        this.cacheDirectory = cacheDirectory;
    }
    /**
     * @return Returns the cocoonLogger.
     */
    public String getCocoonLogger() {
        return this.cocoonLogger;
    }
    /**
     * @param cocoonLogger The cocoonLogger to set.
     */
    public void setCocoonLogger(String cocoonLogger) {
        this.cocoonLogger = cocoonLogger;
    }
    /**
     * @return Returns the configuration.
     */
    public String getConfiguration() {
        return this.configuration;
    }
    /**
     * @param configuration The configuration to set.
     */
    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }
    /**
     * @return Returns the enableUploads.
     */
    public boolean isEnableUploads() {
        return this.enableUploads;
    }
    /**
     * @param enableUploads The enableUploads to set.
     */
    public void setEnableUploads(boolean enableUploads) {
        this.enableUploads = enableUploads;
    }
    /**
     * @return Returns the extraClasspaths.
     */
    public List getExtraClasspaths() {
        return this.extraClasspaths;
    }
    /**
     * @param extraClasspath The extraClasspaths to set.
     */
    public void addToExtraClasspaths(String extraClasspath) {
        this.extraClasspaths.add(extraClasspath);
    }

    /**
     * @return Returns the forceProperties.
     */
    public Map getForceProperties() {
        return this.forceProperties;
    }

    /**
     * @param key The forceProperties to set.
     * @param value The forceProperties value to set.
     */
    public void addToForceProperties(String key, String value) {
        this.forceProperties.put(key, value);
    }

    /**
     * @return Returns the formEncoding.
     */
    public String getFormEncoding() {
        return this.formEncoding;
    }
    /**
     * @param formEncoding The formEncoding to set.
     */
    public void setFormEncoding(String formEncoding) {
        this.formEncoding = formEncoding;
    }
    /**
     * @return Returns the initClassloader.
     */
    public boolean isInitClassloader() {
        return this.initClassloader;
    }
    /**
     * @param initClassloader The initClassloader to set.
     */
    public void setInitClassloader(boolean initClassloader) {
        this.initClassloader = initClassloader;
    }

    /**
     * @return Returns the loadClasses.
     */
    public Iterator getLoadClasses() {
        return this.loadClasses.iterator();
    }

    /**
     * @param className The loadClasses to set.
     */
    public void addToLoadClasses(String className) {
        this.loadClasses.add(className);
    }

    /**
     * @return Returns the loggerClassName.
     */
    public String getLoggerClassName() {
        return this.loggerClassName;
    }
    /**
     * @param loggerClassName The loggerClassName to set.
     */
    public void setLoggerClassName(String loggerClassName) {
        this.loggerClassName = loggerClassName;
    }
    /**
     * @return Returns the loggingConfiguration.
     */
    public String getLoggingConfiguration() {
        return this.loggingConfiguration;
    }
    /**
     * @param loggingConfiguration The loggingConfiguration to set.
     */
    public void setLoggingConfiguration(String loggingConfiguration) {
        this.loggingConfiguration = loggingConfiguration;
    }
    /**
     * @return Returns the logLevel.
     */
    public String getBootstrapLogLevel() {
        return this.bootstrapLogLevel;
    }
    /**
     * @param logLevel The logLevel to set.
     */
    public void setBootstrapLogLevel(String logLevel) {
        this.bootstrapLogLevel = logLevel;
    }
    /**
     * @return Returns the manageExceptions.
     */
    public boolean isManageExceptions() {
        return this.manageExceptions;
    }
    /**
     * @param manageExceptions The manageExceptions to set.
     */
    public void setManageExceptions(boolean manageExceptions) {
        this.manageExceptions = manageExceptions;
    }
    /**
     * @return Returns the maxUploadSize.
     */
    public int getMaxUploadSize() {
        return this.maxUploadSize;
    }
    /**
     * @param maxUploadSize The maxUploadSize to set.
     */
    public void setMaxUploadSize(int maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }
    /**
     * @return Returns the overwriteUploads.
     */
    public String getOverwriteUploads() {
        return this.overwriteUploads;
    }
    /**
     * @param overwriteUploads The overwriteUploads to set.
     */
    public void setOverwriteUploads(String overwriteUploads) {
        this.overwriteUploads = overwriteUploads;
    }
    /**
     * @return Returns the parentServiceManagerClassName.
     */
    public String getParentServiceManagerClassName() {
        return this.parentServiceManagerClassName;
    }
    /**
     * @param parentServiceManagerClassName The parentServiceManagerClassName to set.
     */
    public void setParentServiceManagerClassName(
            String parentServiceManagerClassName) {
        this.parentServiceManagerClassName = parentServiceManagerClassName;
    }
    /**
     * @return Returns the showTime.
     */
    public boolean isShowTime() {
        return this.showTime;
    }
    /**
     * @param showTime The showTime to set.
     */
    public void setShowTime(boolean showTime) {
        this.showTime = showTime;
    }
    /**
     * @return Returns the uploadDirectory.
     */
    public String getUploadDirectory() {
        return this.uploadDirectory;
    }
    /**
     * @param uploadDirectory The uploadDirectory to set.
     */
    public void setUploadDirectory(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }
    /**
     * @return Returns the workDirectory.
     */
    public String getWorkDirectory() {
        return this.workDirectory;
    }
    /**
     * @param workDirectory The workDirectory to set.
     */
    public void setWorkDirectory(String workDirectory) {
        this.workDirectory = workDirectory;
    }

    /**
     * @return Returns the log4jConfiguration.
     */
    public String getLog4jConfiguration() {
        return this.log4jConfiguration;
    }
    /**
     * @param log4jConfiguration The log4jConfiguration to set.
     */
    public void setLog4jConfiguration(String log4jConfiguration) {
        this.log4jConfiguration = log4jConfiguration;
    }

    /**
     * @return Returns the accessLogger.
     */
    public String getAccessLogger() {
        return this.accessLogger;
    }
    /**
     * @param servletLogger The servletLogger to set.
     */
    public void setAccessLogger(String servletLogger) {
        this.accessLogger = servletLogger;
    }

    /**
     * @return Returns the overrideLogLevel.
     */
    public String getOverrideLogLevel() {
        return this.overrideLogLevel;
    }

    /**
     * @param overrideLogLevel The overrideLogLevel to set.
     */
    public void setOverrideLogLevel(String overrideLogLevel) {
        this.overrideLogLevel = overrideLogLevel;
    }

    public boolean isAllowOverwrite() {
        if ("deny".equalsIgnoreCase(this.overwriteUploads)) {
            return false;
        } else if ("allow".equalsIgnoreCase(this.overwriteUploads)) {
            return true;
        } else {
            // either rename is specified or unsupported value - default to rename.
            return false;
        }
    }

    public boolean isSilentlyRename() {
        if ("deny".equalsIgnoreCase(this.overwriteUploads)) {
            return false;
        } else if ("allow".equalsIgnoreCase(this.overwriteUploads)) {
            return false; // ignored in this case
        } else {
            // either rename is specified or unsupported value - default to rename.
            return true;
        }
    }

    /**
     * @return Returns the configurationReloadDelay.
     */
    public long getConfigurationReloadDelay() {
        return configurationReloadDelay;
    }

    /**
     * @param configurationReloadDelay The configurationReloadDelay to set.
     */
    public void setConfigurationReloadDelay(long configurationReloadDelay) {
        this.configurationReloadDelay = configurationReloadDelay;
    }

    /**
     * @return Returns the lazyMode.
     */
    public boolean isLazyMode() {
        return this.lazyMode;
    }

    /**
     * @param lazyMode The lazyMode to set.
     */
    public void setLazyMode(boolean lazyMode) {
        this.lazyMode = lazyMode;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Settings:\n"+
          KEY_CONFIGURATION + " : " + this.configuration + '\n' +
          KEY_CONFIGURATION_RELOAD_DELAY + " : " + this.configurationReloadDelay + '\n' +
          KEY_ALLOW_RELOAD + " : " + this.allowReload + '\n' +
          KEY_INIT_CLASSLOADER + " : " + this.initClassloader + '\n' +
          KEY_EXTRA_CLASSPATHS + " : " + this.extraClasspaths + '\n' +
          KEY_LOAD_CLASSES + " : " + this.loadClasses + '\n' +
          KEY_FORCE_PROPERTIES + " : " + this.forceProperties + '\n' +
          KEY_LOGGING_CONFIGURATION + " : " + this.loggingConfiguration + '\n' +
          KEY_LOGGING_ACCESS_LOGGER + " : " + this.accessLogger + '\n' +
          KEY_LOGGING_BOOTSTRAP_LOGLEVEL + " : " + this.bootstrapLogLevel + '\n' +
          KEY_LOGGING_COCOON_LOGGER + " : " + this.cocoonLogger + '\n' +
          KEY_LOGGING_LOG4J_CONFIGURATION + " : " + this.log4jConfiguration + '\n' +
          KEY_LOGGING_MANAGER_CLASS + " : " + this.loggerClassName + '\n' +
          KEY_LOGGING_OVERRIDE_LOGLEVEL + " : " + this.overrideLogLevel + '\n' +
          KEY_MANAGE_EXCEPTIONS + " : " + this.manageExceptions + '\n' +
          KEY_PARENT_SERVICE_MANAGER + " : " + this.parentServiceManagerClassName + '\n' +
          KEY_UPLOADS_DIRECTORY + " : " + this.uploadDirectory + '\n' +
          KEY_UPLOADS_AUTOSAVE + " : " + this.autosaveUploads + '\n' +
          KEY_UPLOADS_ENABLE + " : " + this.enableUploads + '\n' +
          KEY_UPLOADS_MAXSIZE + " : " + this.maxUploadSize + '\n' +
          KEY_UPLOADS_OVERWRITE + " : " + this.overwriteUploads + '\n' +
          KEY_CACHE_DIRECTORY + " : " + this.cacheDirectory + '\n' +
          KEY_WORK_DIRECTORY + " : " + this.workDirectory + '\n' +
          KEY_FORM_ENCODING + " : " + this.formEncoding + '\n' +
          KEY_SHOWTIME + " : " + this.showTime + '\n' +
          KEY_HIDE_SHOWTIME + " : " + this.hideShowTime + '\n' +
          KEY_LAZY_MODE + " : " + this.lazyMode + '\n';
    }

    public String getProperty(String name) {
        return this.getProperty(name, null);
    }

    public String getProperty(String key, String defaultValue) {
        if ( key == null ) {
            return defaultValue;
        }
        String value = null;
        if ( key.startsWith(KEYPREFIX) ) {
            final String sKey = key.substring(KEYPREFIX.length());
            if ( sKey.equals(KEY_INIT_CLASSLOADER) ) {
                value = String.valueOf(this.initClassloader);
            } else if ( sKey.equals(KEY_CONFIGURATION) ) {
                value = this.configuration;
            } else if ( sKey.equals(KEY_CONFIGURATION_RELOAD_DELAY) ) {
                value = String.valueOf(this.configurationReloadDelay);
            } else if ( sKey.equals(KEY_LOGGING_CONFIGURATION) ) {
                value = this.loggingConfiguration;
            } else if ( sKey.equals(KEY_LOGGING_ACCESS_LOGGER) ) {
                value = this.accessLogger;
            } else if ( sKey.equals(KEY_LOGGING_COCOON_LOGGER) ) {
                value = this.cocoonLogger;
            } else if ( sKey.equals(KEY_LOGGING_BOOTSTRAP_LOGLEVEL) ) {
                value = this.bootstrapLogLevel;
            } else if ( sKey.equals(KEY_LOGGING_MANAGER_CLASS) ) {
                value = this.loggerClassName;
            } else if ( sKey.equals(KEY_LOGGING_LOG4J_CONFIGURATION) ) {
                value = this.log4jConfiguration;
            } else if ( sKey.equals(KEY_ALLOW_RELOAD) ) {
                value = String.valueOf(this.allowReload);
            } else if ( sKey.equals(KEY_UPLOADS_ENABLE) ) {
                value = String.valueOf(this.enableUploads);
            } else if ( sKey.equals(KEY_UPLOADS_DIRECTORY) ) {
                value = this.uploadDirectory = value;
            } else if ( sKey.equals(KEY_UPLOADS_AUTOSAVE) ) {
                value = String.valueOf(this.autosaveUploads);
            } else if ( sKey.equals(KEY_UPLOADS_OVERWRITE) ) {
                value = this.overwriteUploads;
            } else if ( sKey.equals(KEY_UPLOADS_MAXSIZE) ) {
                value = String.valueOf(this.maxUploadSize);
            } else if ( sKey.equals(KEY_CACHE_DIRECTORY) ) {
                value = this.cacheDirectory;
            } else if ( sKey.equals(KEY_WORK_DIRECTORY) ) {
                value = this.workDirectory;
            } else if ( sKey.equals(KEY_PARENT_SERVICE_MANAGER) ) {
                value = this.parentServiceManagerClassName;
            } else if ( sKey.equals(KEY_SHOWTIME) ) {
                value = String.valueOf(this.showTime);
            } else if ( sKey.equals(KEY_HIDE_SHOWTIME) ) {
                value = String.valueOf(this.hideShowTime);
            } else if ( sKey.equals(KEY_MANAGE_EXCEPTIONS) ) {
                value = String.valueOf(this.manageExceptions);
            } else if ( sKey.equals(KEY_FORM_ENCODING) ) {
                value = this.formEncoding;
            } else if ( sKey.equals(KEY_LOGGING_OVERRIDE_LOGLEVEL) ) {
                value = this.overrideLogLevel;
            } else if ( sKey.equals(KEY_LAZY_MODE) ) {
                value = String.valueOf(this.lazyMode);
            }
       }

        int i = 0;
        while ( i < this.properties.size() && value == null ) {
            final Properties p = (Properties)this.properties.get(i);
            value = p.getProperty(key);
            i++;
        }
        if ( value == null ) {
            value = defaultValue;
        }
        return value;
    }
}
