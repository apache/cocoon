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
package org.apache.cocoon.core;

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
 * @version $Id$
 * @since 2.2
 */
public class MutableSettings implements Settings {

    /** Are we still mutable? */
    protected boolean readOnly = false;

    /** Prefix for properties */
    protected static final String KEYPREFIX = "org.apache.cocoon.";

    /**
     * The list of properties used to configure Cocoon
     */
    protected List properties = new ArrayList();

    /**
     * This parameter allows to set system properties
     */
    protected Map forceProperties = new HashMap();

    /**
     * This parameter indicates what class to use for the root processor.
     */
    protected String processorClassName = DEFAULT_PROCESSOR_CLASS;
    
    /**
     * This parameter points to the main configuration file for Cocoon.
     * Note that the path is specified in absolute notation but it will be
     * resolved relative to the application context path.
     */
    protected String configuration;

    /**
     * This parameter indicates the configuration file of the LogKit management
     */
    protected String loggingConfiguration;

    /**
     * This parameter indicates the category id of the logger from the LogKit
     * configuration used by the environment.
     */
    protected String environmentLogger;

    /**
     * This parameter indicates the category id of the logger from the LogKit
     * management configuration for the Cocoon engine.
     * This logger is used for all components described in the cocoon.xconf
     * and sitemap.xmap file not having specified a logger with the
     * logger="..." attribute in the component configuration file.
     */
    protected String cocoonLogger;

    /**
     * This parameter indicates the log level to use throughout startup of the
     * system. As soon as the logkit.xconf the setting of the logkit.xconf
     * configuration is used instead! Only for startup and if the logkit.xconf is
     * not readable/available this log level is of importance.
     */
    protected String bootstrapLogLevel;

    /**
     * This parameter switches the logging system from LogKit to Log4J for Cocoon.
     */
    protected String loggerManagerClassName;

    /**
     * Allow reinstantiating (reloading) of the cocoon instance. If this is
     * set to "yes" or "true", a new cocoon instance can be created using
     * the request parameter "cocoon-reload". It also enables that Cocoon is
     * reloaded when cocoon.xconf changes. Default is no for security reasons.
     */
    protected boolean reloadingEnabled = RELOADING_ENABLED_DEFAULT;

    /**
     * This parameter is used to list classes that should be loaded at
     * initialization time of the servlet. For example, JDBC Drivers used need to
     * be named here. Additional entries may be inserted here during build
     * depending on your build properties.
     */
    protected List loadClasses = new ArrayList();

    /**
     * Causes all files in multipart requests to be processed.
     * Default is false for security reasons.
     */
    protected boolean enableUploads = ENABLE_UPLOADS;

    /**
     * This parameter allows to specify where Cocoon should put uploaded files.
     * The path specified can be either absolute or relative to the context
     * path of the servlet. On windows platform, absolute directory must start
     * with volume: C:\Path\To\Upload\Directory.
     */
    protected String uploadDirectory;

    /**
     * Causes all files in multipart requests to be saved to upload-dir.
     * Default is true for security reasons.
     */
    protected boolean autosaveUploads = SAVE_UPLOADS_TO_DISK;

    /**
     * Specify handling of name conflicts when saving uploaded files to disk.
     * Acceptable values are deny, allow, rename (default). Files are renamed
     * x_filename where x is an integer value incremented to make the new
     * filename unique.
     */
    protected String overwriteUploads;

    /**
     * Specify maximum allowed size of the upload. Defaults to 10 Mb.
     */
    protected int maxUploadSize = MAX_UPLOAD_SIZE;

    /**
     * This parameter allows to specify where Cocoon should create its page
     * and other objects cache. The path specified can be either absolute or
     * relative to the context path of the servlet. On windows platform,
     * absolute directory must start with volume: C:\Path\To\Cache\Directory.
     */
    protected String cacheDirectory;

    /**
     * This parameter allows to specify where Cocoon should put it's
     * working files. The path specified is either absolute or relative
     * to the context path of the Cocoon servlet. On windows platform,
     * absolute directory must start with volume: C:\Path\To\Work\Directory.
     */
    protected String workDirectory;

    /**
     * This parameter allows to specify additional directories or jars
     * which Cocoon should put into it's own classpath.
     * Note that absolute pathes are taken as such but relative pathes
     * are rooted at the context root of the Cocoon servlet.
     */
    protected List extraClasspaths = new ArrayList();

    /**
     * Allow adding processing time to the response
     */
    protected boolean showTime = SHOW_TIME;

    /**
     * If true, processing time will be added as an HTML comment
     */
    protected boolean hideShowTime = HIDE_SHOW_TIME;

    /**
     * If true, the X-Cocoon-Version response header will be included.
     */
    protected boolean showCocoonVersion = SHOW_COCOON_VERSION;

    /**
     * If true or not set, this class will try to catch and handle all Cocoon exceptions.
     * If false, it will rethrow them to the servlet container.
     */
    protected boolean manageExceptions = MANAGE_EXCEPTIONS;

    /**
     * Set form encoding. This will be the character set used to decode request
     * parameters. If not set the ISO-8859-1 encoding will be assumed.
    */
    protected String formEncoding;

    /**
     * If this value is specified, it will be interpreted as a log level and
     * all logging categories will be set to this level regardless of their
     * definition in the logging configuration.
     */
    protected String overrideLogLevel;

    /**
     * Delay between reload checks for the configuration.
     */
    protected long configurationReloadDelay = 1000;

    /**
     * Lazy mode for component loading
     */
    protected boolean lazyMode = false;

    /** The time the cocoon instance was created. */
    protected long creationTime;

    /** The property providers. */
    protected List propertyProviders = new ArrayList();

    /**
     * Create a new settings object
     */
    public MutableSettings() {
        // nothing to do
    }

    /**
     * Fill from a properties object
     */
    public void fill(Properties props) {
        this.checkWriteable();
        if ( props != null ) {
            final Iterator i = props.entrySet().iterator();
            while ( i.hasNext() ) {
                final Map.Entry current = (Map.Entry)i.next();
                String key = current.getKey().toString();
                if ( key.startsWith(KEYPREFIX) ) {
                    key = key.substring(KEYPREFIX.length());
                    final String value = current.getValue().toString();

                    if ( key.equals(KEY_PROCESSOR_CLASS) ) {
                        this.processorClassName = value;
                    } else if ( key.equals(KEY_CONFIGURATION) ) {
                        this.configuration = value;
                    } else if ( key.equals(KEY_RELOAD_DELAY) ) {
                        this.configurationReloadDelay = NumberUtils.toLong(value);
                    } else if ( key.equals(KEY_LOGGING_CONFIGURATION) ) {
                        this.loggingConfiguration = value;
                    } else if ( key.equals(KEY_LOGGING_ENVIRONMENT_LOGGER) ) {
                        this.environmentLogger = value;
                    } else if ( key.equals(KEY_LOGGING_COCOON_LOGGER) ) {
                        this.cocoonLogger = value;
                    } else if ( key.equals(KEY_LOGGING_BOOTSTRAP_LOGLEVEL) ) {
                        this.bootstrapLogLevel = value;
                    } else if ( key.equals(KEY_LOGGING_MANAGER_CLASS) ) {
                        this.loggerManagerClassName = value;
                    } else if ( key.equals(KEY_RELOADING) ) {
                        this.reloadingEnabled = BooleanUtils.toBoolean(value);
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
                    } else if ( key.equals(KEY_SHOWTIME) ) {
                        this.showTime = BooleanUtils.toBoolean(value);
                    } else if ( key.equals(KEY_HIDE_SHOWTIME) ) {
                        this.hideShowTime = BooleanUtils.toBoolean(value);
                    } else if ( key.equals(KEY_SHOW_VERSION) ) {
                        this.showCocoonVersion = BooleanUtils.toBoolean(value);
                    } else if ( key.equals(KEY_MANAGE_EXCEPTIONS) ) {
                        this.manageExceptions = BooleanUtils.toBoolean(value);
                    } else if ( key.equals(KEY_FORM_ENCODING) ) {
                        this.formEncoding = value;
                    } else if ( key.equals(KEY_LOGGING_OVERRIDE_LOGLEVEL) ) {
                        this.overrideLogLevel = value;
                    } else if ( key.equals(KEY_LAZY_MODE) ) {
                        this.lazyMode = BooleanUtils.toBoolean(value);
                    } else if ( key.startsWith(KEY_LOAD_CLASSES) ) {
                        this.addToLoadClasses(value);
                    } else if ( key.startsWith(KEY_EXTRA_CLASSPATHS) ) {
                        this.addToExtraClasspaths(value);
                    } else if ( key.startsWith(KEY_PROPERTY_PROVIDER) ) {
                        this.addToPropertyProviders(value);
                    } else if ( key.startsWith(KEY_FORCE_PROPERTIES) ) {
                        key = key.substring(KEY_FORCE_PROPERTIES.length() + 1);
                        this.addToForceProperties(key, value);
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
     * @return Returns the allowReload.
     */
    public boolean isReloadingEnabled(String type) {
        boolean result = this.reloadingEnabled;
        if ( type != null ) {
            String o = this.getProperty(KEYPREFIX + KEY_RELOADING + '.' + type);
            if ( o != null ) {
                result = BooleanUtils.toBoolean(o);
            }
        }
        return result;
    }

    /**
     * @return Returns the autosaveUploads.
     */
    public boolean isAutosaveUploads() {
        return this.autosaveUploads;
    }

    /**
     * @return Returns the cacheDirectory.
     */
    public String getCacheDirectory() {
        return this.cacheDirectory;
    }

    /**
     * @return Returns the cocoonLogger.
     */
    public String getCocoonLogger() {
        return this.cocoonLogger;
    }

    /**
     * @return Returns the processorClassName.
     */
    public String getProcessorClassName() {
        return this.processorClassName;
    }

    /**
     * @return Returns the configuration.
     */
    public String getConfiguration() {
        return this.configuration;
    }

    /**
     * @return Returns the enableUploads.
     */
    public boolean isEnableUploads() {
        return this.enableUploads;
    }

    /**
     * @return Returns the extraClasspaths.
     */
    public List getExtraClasspaths() {
        return this.extraClasspaths;
    }

    /**
     * @return Returns the forceProperties.
     */
    public Map getForceProperties() {
        return this.forceProperties;
    }

    /**
     * @return Returns the formEncoding.
     */
    public String getFormEncoding() {
        return this.formEncoding;
    }

    /**
     * @return Returns the loadClasses.
     */
    public List getLoadClasses() {
        return this.loadClasses;
    }

    /**
     * @return Returns the loggerClassName.
     */
    public String getLoggerManagerClassName() {
        return this.loggerManagerClassName;
    }

    /**
     * @return Returns the loggingConfiguration.
     */
    public String getLoggingConfiguration() {
        return this.loggingConfiguration;
    }

    /**
     * @return Returns the logLevel.
     */
    public String getBootstrapLogLevel() {
        return this.bootstrapLogLevel;
    }

    /**
     * @return Returns the manageExceptions.
     */
    public boolean isManageExceptions() {
        return this.manageExceptions;
    }

    /**
     * @return Returns the maxUploadSize.
     */
    public int getMaxUploadSize() {
        return this.maxUploadSize;
    }

    /**
     * @return Returns the overwriteUploads.
     */
    public String getOverwriteUploads() {
        return this.overwriteUploads;
    }

    /**
     * @return Returns the showTime.
     */
    public boolean isShowTime() {
        return this.showTime;
    }

    /**
     * @return Returns the showCocoonVersion flag.
     */
    public boolean isShowVersion() {
        return this.showCocoonVersion;
    }

    /**
     * @return Returns the uploadDirectory.
     */
    public String getUploadDirectory() {
        return this.uploadDirectory;
    }

    /**
     * @return Returns the workDirectory.
     */
    public String getWorkDirectory() {
        return this.workDirectory;
    }

    /**
     * @return Returns the accessLogger.
     */
    public String getEnvironmentLogger() {
        return this.environmentLogger;
    }

    /**
     * @return Returns the overrideLogLevel.
     */
    public String getOverrideLogLevel() {
        return this.overrideLogLevel;
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
    public long getReloadDelay(String type) {
        long value = this.configurationReloadDelay;
        if ( type != null ) {
            String o = this.getProperty(KEYPREFIX + KEY_RELOAD_DELAY + '.' + type);
            if ( o != null ) {
                value = NumberUtils.toLong(o);
            }
        }
        return value;
    }

    /**
     * @return Returns the lazyMode.
     */
    public boolean isLazyMode() {
        return this.lazyMode;
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
            if ( sKey.equals(KEY_PROCESSOR_CLASS) ) {
                value = this.processorClassName;
            } else if ( sKey.equals(KEY_CONFIGURATION) ) {
                value = this.configuration;
            } else if ( sKey.equals(KEY_RELOAD_DELAY) ) {
                value = String.valueOf(this.configurationReloadDelay);
            } else if ( sKey.equals(KEY_LOGGING_CONFIGURATION) ) {
                value = this.loggingConfiguration;
            } else if ( sKey.equals(KEY_LOGGING_ENVIRONMENT_LOGGER) ) {
                value = this.environmentLogger;
            } else if ( sKey.equals(KEY_LOGGING_COCOON_LOGGER) ) {
                value = this.cocoonLogger;
            } else if ( sKey.equals(KEY_LOGGING_BOOTSTRAP_LOGLEVEL) ) {
                value = this.bootstrapLogLevel;
            } else if ( sKey.equals(KEY_LOGGING_MANAGER_CLASS) ) {
                value = this.loggerManagerClassName;
            } else if ( sKey.equals(KEY_RELOADING) ) {
                value = String.valueOf(this.reloadingEnabled);
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
            } else if ( key.equals(KEY_LOAD_CLASSES) ) {
                value = this.toString(this.loadClasses);
            } else if ( key.equals(KEY_EXTRA_CLASSPATHS) ) {
                this.toString(this.extraClasspaths);
            } else if ( key.equals(KEY_FORCE_PROPERTIES) ) {
                this.toString(this.forceProperties);
            } else if ( key.equals(KEY_PROPERTY_PROVIDER) ) {
                this.toString(this.propertyProviders);
            }
        }

        // Iterate in reverse order, as most specific property sources are added last
        for (int i = this.properties.size() - 1; i >= 0 && value == null; i--) {
            final Properties p = (Properties)this.properties.get(i);
            value = p.getProperty(key);
        }

        if ( value == null ) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Settings:\n" +
          "Running mode : " + this.getProperty(PROPERTY_RUNNING_MODE, DEFAULT_RUNNING_MODE) + '\n' +
          KEY_PROCESSOR_CLASS + " : " + this.processorClassName + '\n' +
          KEY_CONFIGURATION + " : " + this.configuration + '\n' +
          KEY_RELOAD_DELAY + " : " + this.configurationReloadDelay + '\n' +
          KEY_RELOADING + " : " + this.reloadingEnabled + '\n' +
          KEY_EXTRA_CLASSPATHS + " : " + this.toString(this.extraClasspaths) + '\n' +
          KEY_LOAD_CLASSES + " : " + this.toString(this.loadClasses) + '\n' +
          KEY_FORCE_PROPERTIES + " : " + this.toString(this.forceProperties) + '\n' +
          KEY_LOGGING_CONFIGURATION + " : " + this.loggingConfiguration + '\n' +
          KEY_LOGGING_ENVIRONMENT_LOGGER + " : " + this.environmentLogger + '\n' +
          KEY_LOGGING_BOOTSTRAP_LOGLEVEL + " : " + this.bootstrapLogLevel + '\n' +
          KEY_LOGGING_COCOON_LOGGER + " : " + this.cocoonLogger + '\n' +
          KEY_LOGGING_MANAGER_CLASS + " : " + this.loggerManagerClassName + '\n' +
          KEY_LOGGING_OVERRIDE_LOGLEVEL + " : " + this.overrideLogLevel + '\n' +
          KEY_MANAGE_EXCEPTIONS + " : " + this.manageExceptions + '\n' +
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
          KEY_SHOW_VERSION + " : " + this.showCocoonVersion + '\n' +
          KEY_LAZY_MODE + " : " + this.lazyMode + '\n';
    }

    /**
     * Helper method to make a string out of a list of objects.
     */
    protected String toString(List a) {
        final StringBuffer buffer = new StringBuffer();
        final Iterator i = a.iterator();
        boolean first = true;
        while ( i.hasNext() ) {
            if ( first ) {
                first = false;
            } else {
                buffer.append(", ");
            }
            buffer.append(i.next());
        }
        return buffer.toString();        
    }

    /**
     * Helper method to make a string out of a map of objects.
     */
    protected String toString(Map a) {
        final StringBuffer buffer = new StringBuffer("{");
        final Iterator i = a.entrySet().iterator();
        boolean first = true;
        while ( i.hasNext() ) {
            if ( first ) {
                first = false;
            } else {
                buffer.append(", ");
            }
            final Map.Entry current = (Map.Entry)i.next();
            buffer.append(current.getKey());
            buffer.append("=");
            buffer.append(current.getValue());
        }
        buffer.append("}");
        return buffer.toString();        
    }

    /**
     * @param hideShowTime The hideShowTime to set.
     */
    public void setHideShowTime(boolean hideShowTime) {
        this.checkWriteable();
        this.hideShowTime = hideShowTime;
    }

    /**
     * @param allowReload The allowReload to set.
     */
    public void setReloadingEnabled(boolean allowReload) {
        this.checkWriteable();
        this.reloadingEnabled = allowReload;
    }

    /**
     * @param autosaveUploads The autosaveUploads to set.
     */
    public void setAutosaveUploads(boolean autosaveUploads) {
        this.checkWriteable();
        this.autosaveUploads = autosaveUploads;
    }

    /**
     * @param cacheDirectory The cacheDirectory to set.
     */
    public void setCacheDirectory(String cacheDirectory) {
        this.checkWriteable();
        this.cacheDirectory = cacheDirectory;
    }

    /**
     * @param processorClassName The processorClassName to set.
     */
    public void setProcessorClassName(String processorClassName) {
        this.checkWriteable();
        this.processorClassName = processorClassName;
    }

    /**
     * @param cocoonLogger The cocoonLogger to set.
     */
    public void setCocoonLogger(String cocoonLogger) {
        this.checkWriteable();
        this.cocoonLogger = cocoonLogger;
    }

    /**
     * @param configuration The configuration to set.
     */
    public void setConfiguration(String configuration) {
        this.checkWriteable();
        this.configuration = configuration;
    }

    /**
     * @param enableUploads The enableUploads to set.
     */
    public void setEnableUploads(boolean enableUploads) {
        this.checkWriteable();
        this.enableUploads = enableUploads;
    }

    /**
     * @param extraClasspath The extraClasspaths to set.
     */
    public void addToExtraClasspaths(String extraClasspath) {
        this.checkWriteable();
        this.extraClasspaths.add(extraClasspath);
    }

    /**
     * @param key The forceProperties to set.
     * @param value The forceProperties value to set.
     */
    public void addToForceProperties(String key, String value) {
        this.checkWriteable();
        this.forceProperties.put(key, value);
    }

    /**
     * @param formEncoding The formEncoding to set.
     */
    public void setFormEncoding(String formEncoding) {
        this.checkWriteable();
        this.formEncoding = formEncoding;
    }

    /**
     * @param className The loadClasses to set.
     */
    public void addToLoadClasses(String className) {
        this.checkWriteable();
        this.loadClasses.add(className);
    }

    /**
     * @param loggerClassName The loggerClassName to set.
     */
    public void setLoggerManagerClassName(String loggerClassName) {
        this.checkWriteable();
        this.loggerManagerClassName = loggerClassName;
    }

    /**
     * @param loggingConfiguration The loggingConfiguration to set.
     */
    public void setLoggingConfiguration(String loggingConfiguration) {
        this.checkWriteable();
        this.loggingConfiguration = loggingConfiguration;
    }

    /**
     * @param logLevel The logLevel to set.
     */
    public void setBootstrapLogLevel(String logLevel) {
        this.checkWriteable();
        this.bootstrapLogLevel = logLevel;
    }

    /**
     * @param manageExceptions The manageExceptions to set.
     */
    public void setManageExceptions(boolean manageExceptions) {
        this.checkWriteable();
        this.manageExceptions = manageExceptions;
    }

    /**
     * @param maxUploadSize The maxUploadSize to set.
     */
    public void setMaxUploadSize(int maxUploadSize) {
        this.checkWriteable();
        this.maxUploadSize = maxUploadSize;
    }

    /**
     * @param overwriteUploads The overwriteUploads to set.
     */
    public void setOverwriteUploads(String overwriteUploads) {
        this.checkWriteable();
        this.overwriteUploads = overwriteUploads;
    }
    
    /**
     * @param showTime The showTime to set.
     */
    public void setShowTime(boolean showTime) {
        this.checkWriteable();
        this.showTime = showTime;
    }

    /**
     * @param showCocoonVersion The showCocoonVersion flag to set.
     */
    public void setShowCocoonVersion(boolean showCocoonVersion) {
        this.checkWriteable();
        this.showCocoonVersion = showCocoonVersion;
    }

    /**
     * @param uploadDirectory The uploadDirectory to set.
     */
    public void setUploadDirectory(String uploadDirectory) {
        this.checkWriteable();
        this.uploadDirectory = uploadDirectory;
    }

    /**
     * @param workDirectory The workDirectory to set.
     */
    public void setWorkDirectory(String workDirectory) {
        this.checkWriteable();
        this.workDirectory = workDirectory;
    }

    /**
     * @param logger The logger for the environment.
     */
    public void setEnvironmentLogger(String logger) {
        this.checkWriteable();
        this.environmentLogger = logger;
    }

    /**
     * @param overrideLogLevel The overrideLogLevel to set.
     */
    public void setOverrideLogLevel(String overrideLogLevel) {
        this.checkWriteable();
        this.overrideLogLevel = overrideLogLevel;
    }

    /**
     * @param configurationReloadDelay The configurationReloadDelay to set.
     */
    public void setConfigurationReloadDelay(long configurationReloadDelay) {
        this.checkWriteable();
        this.configurationReloadDelay = configurationReloadDelay;
    }

    /**
     * @param lazyMode The lazyMode to set.
     */
    public void setLazyMode(boolean lazyMode) {
        this.checkWriteable();
        this.lazyMode = lazyMode;
    }

    /**
     * Mark this object as read-only.
     */
    public void makeReadOnly() {
        this.readOnly = false;
    }

    /**
     * check if this configuration is writeable.
     *
     * @throws IllegalStateException if this setting is read-only
     */
    protected final void checkWriteable()
    throws IllegalStateException {
        if( this.readOnly ) {
            throw new IllegalStateException
                ( "Settings is read only and can not be modified anymore." );
        }
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getCreationTime()
     */
    public long getCreationTime() {
        return this.creationTime;
    }

    /**
     * Set the creation time of the current cocoon instance.
     */
    public void setCreationTime(long value) {
        // Don't check read only here as this will change if Cocoon
        // is reloaded while the settings remain the same.
        this.creationTime = value;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getPropertyProviders()
     */
    public List getPropertyProviders() {
        return this.propertyProviders;
    }

    /**
     * Add a property provider.
     */
    public void addToPropertyProviders(String className) {
        this.checkWriteable();
        this.propertyProviders.add(className);
    }

    /**
     * @see org.apache.cocoon.core.Settings#getProperties(java.lang.String)
     */
    public List getProperties(String keyPrefix) {
        final List props = new ArrayList();
        for(int i=0; i < this.properties.size(); i++) {
            final Properties p = (Properties)this.properties.get(i);
            final Iterator kI = p.keySet().iterator();
            while ( kI.hasNext() ) {
                final String name = (String)kI.next();
                if ( name.startsWith(keyPrefix) && !props.contains(name) ) {
                    props.add(name);
                }
            }
        }
        return props;
    }
    
    /**
     * @see org.apache.cocoon.core.Settings#getProperties()
     */
    public List getProperties() {
        final List props = new ArrayList();
        for(int i=0; i < this.properties.size(); i++) {
            final Properties p = (Properties)this.properties.get(i);
            final Iterator kI = p.keySet().iterator();
            while ( kI.hasNext() ) {
                final String name = (String)kI.next();
                if (!props.contains(name) ) {
                    props.add(name);
                }
            }
        }
        return props;
    }
}
