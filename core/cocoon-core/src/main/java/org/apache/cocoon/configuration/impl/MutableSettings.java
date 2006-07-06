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
package org.apache.cocoon.configuration.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.cocoon.configuration.Settings;
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

    /** Prefix for properties. */
    protected static final String KEYPREFIX = "org.apache.cocoon.";

    /** The list of properties used to configure Cocoon. */
    protected final List properties = new ArrayList();

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
     * @see #setBootstrapLogLevel(String)
     */
    protected String bootstrapLogLevel;

    /**
     * Allow reinstantiating (reloading) of the cocoon instance. If this is
     * set to "yes" or "true", a new cocoon instance can be created using
     * the request parameter "cocoon-reload". It also enables that Cocoon is
     * reloaded when cocoon.xconf changes. Default is no for security reasons.
     */
    protected boolean reloadingEnabled;

    /**
     * This parameter is used to list classes that should be loaded at
     * initialization time of the servlet. For example, JDBC Drivers used need to
     * be named here. Additional entries may be inserted here during build
     * depending on your build properties.
     */
    protected final List loadClasses = new ArrayList();

    /**
     * Causes all files in multipart requests to be processed.
     * Default is false for security reasons.
     */
    protected boolean enableUploads;

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
    protected boolean autosaveUploads;

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
    protected int maxUploadSize;

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
     * Allow adding processing time to the response
     */
    protected boolean showTime;

    /**
     * If true, processing time will be added as an HTML comment
     */
    protected boolean hideShowTime;

    /**
     * If true, the X-Cocoon-Version response header will be included.
     */
    protected boolean showCocoonVersion;

    /**
     * If true or not set, this class will try to catch and handle all Cocoon exceptions.
     * If false, it will rethrow them to the servlet container.
     */
    protected boolean manageExceptions;

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
    protected long configurationReloadDelay;

    /** The time the cocoon instance was created. */
    protected long creationTime;

    /** The container encoding.
     * @see BaseSettings#KEY_CONTAINER_ENCODING 
     */
    protected String containerEncoding;

    /** The optional parent settings object. */
    protected Settings parent;

    /**
     * Create a new settings object.
     */
    public MutableSettings() {
        // set default values
        this.reloadingEnabled = RELOADING_ENABLED_DEFAULT;
        this.enableUploads = ENABLE_UPLOADS;
        this.autosaveUploads = SAVE_UPLOADS_TO_DISK;
        this.maxUploadSize = MAX_UPLOAD_SIZE;
        this.showTime = SHOW_TIME;
        this.hideShowTime = HIDE_SHOW_TIME;
        this.showCocoonVersion = SHOW_COCOON_VERSION;
        this.manageExceptions = MANAGE_EXCEPTIONS;
        this.configurationReloadDelay = 1000;
        this.containerEncoding = "ISO-8859-1";
        this.loggingConfiguration = DEFAULT_LOGGING_CONFIGURATION;
        this.configuration = DEFAULT_CONFIGURATION;
    }

    public MutableSettings(Settings parent) {
        this.parent = parent;
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
                    final String value = current.getValue().toString();

                    if ( key.equals(KEY_CONFIGURATION) ) {
                        this.setConfiguration(value);
                    } else if ( key.equals(KEY_RELOAD_DELAY) ) {
                        this.setConfigurationReloadDelay(Long.valueOf(value).longValue());
                    } else if ( key.equals(KEY_LOGGING_CONFIGURATION) ) {
                        this.setLoggingConfiguration(value);
                    } else if ( key.equals(KEY_LOGGING_ENVIRONMENT_LOGGER) ) {
                        this.setEnvironmentLogger(value);
                    } else if ( key.equals(KEY_LOGGING_COCOON_LOGGER) ) {
                        this.setCocoonLogger(value);
                    } else if ( key.equals(KEY_LOGGING_BOOTSTRAP_LOGLEVEL) ) {
                        this.setBootstrapLogLevel(value);
                    } else if ( key.equals(KEY_RELOADING) ) {
                        this.setReloadingEnabled(BooleanUtils.toBoolean(value));
                    } else if ( key.equals(KEY_UPLOADS_ENABLE) ) {
                        this.setEnableUploads(BooleanUtils.toBoolean(value));
                    } else if ( key.equals(KEY_UPLOADS_DIRECTORY) ) {
                        this.setUploadDirectory(value);
                    } else if ( key.equals(KEY_UPLOADS_AUTOSAVE) ) {
                        this.setAutosaveUploads(BooleanUtils.toBoolean(value));
                    } else if ( key.equals(KEY_UPLOADS_OVERWRITE) ) {
                        this.setOverwriteUploads(value);
                    } else if ( key.equals(KEY_UPLOADS_MAXSIZE) ) {
                        this.setMaxUploadSize(Integer.valueOf(value).intValue());
                    } else if ( key.equals(KEY_CACHE_DIRECTORY) ) {
                        this.setCacheDirectory(value);
                    } else if ( key.equals(KEY_WORK_DIRECTORY) ) {
                        this.setWorkDirectory(value);
                    } else if ( key.equals(KEY_SHOWTIME) ) {
                        this.setShowTime(BooleanUtils.toBoolean(value));
                    } else if ( key.equals(KEY_HIDE_SHOWTIME) ) {
                        this.setHideShowTime(BooleanUtils.toBoolean(value));
                    } else if ( key.equals(KEY_SHOW_VERSION) ) {
                        this.setShowCocoonVersion(BooleanUtils.toBoolean(value));
                    } else if ( key.equals(KEY_MANAGE_EXCEPTIONS) ) {
                        this.setManageExceptions(BooleanUtils.toBoolean(value));
                    } else if ( key.equals(KEY_FORM_ENCODING) ) {
                        this.setFormEncoding(value);
                    } else if ( key.equals(KEY_LOGGING_OVERRIDE_LOGLEVEL) ) {
                        this.setOverrideLogLevel(value);
                    } else if ( key.startsWith(KEY_LOAD_CLASSES) ) {
                        this.addToLoadClasses(value);
                    } else if ( key.startsWith(KEY_CONTAINER_ENCODING ) ) {
                        this.setContainerEncoding(value);
                    }
                }
            }
            this.properties.add(props);
        }
    }

    /**
     * @see org.apache.cocoon.core.DynamicSettings#isHideShowTime()
     */
    public boolean isHideShowTime() {
        if ( this.parent != null ) {
            return this.parent.isHideShowTime();
        }
        return this.hideShowTime;
    }

    /**
     * @see org.apache.cocoon.core.DynamicSettings#isReloadingEnabled(java.lang.String)
     */
    public boolean isReloadingEnabled(String type) {
        if ( type == null ) {
            if ( parent != null ) {
                return parent.isReloadingEnabled(type);
            }
            return this.reloadingEnabled;
        }
        String o = this.getProperty(KEY_RELOADING + '.' + type);
        if ( o != null ) {
            return BooleanUtils.toBoolean(o);
        }
        if ( this.parent != null ) {
            return this.parent.isReloadingEnabled(type);
        }
        return this.reloadingEnabled;
    }

    /**
     * @see org.apache.cocoon.core.DynamicSettings#isAutosaveUploads()
     */
    public boolean isAutosaveUploads() {
        if ( parent != null ) {
            return parent.isAutosaveUploads();
        }
        return this.autosaveUploads;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getCacheDirectory()
     */
    public String getCacheDirectory() {
        if ( this.parent != null ) {
            return this.parent.getCacheDirectory();
        }
        return this.cacheDirectory;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getCocoonLogger()
     */
    public String getCocoonLogger() {
        if ( this.parent != null ) {
            return this.parent.getCocoonLogger();
        }
        return this.cocoonLogger;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getConfiguration()
     */
    public String getConfiguration() {
        if ( this.parent != null ) {
            return this.parent.getConfiguration();
        }
        return this.configuration;
    }

    /**
     * @see org.apache.cocoon.core.DynamicSettings#isEnableUploads()
     */
    public boolean isEnableUploads() {
        if ( parent != null ) {
            return parent.isEnableUploads();
        }
        return this.enableUploads;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getFormEncoding()
     */
    public String getFormEncoding() {
        if ( this.parent != null ) {
            return this.parent.getFormEncoding();
        }
        return this.formEncoding;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getContainerEncoding()
     */
    public String getContainerEncoding() {
        if ( this.parent != null ) {
            return this.parent.getContainerEncoding();
        }
        return this.containerEncoding;
    }

    /**
     * Set the container encoding.
     * @param value The new encoding value.
     */
    public void setContainerEncoding(String value) {
        this.checkSubSetting();
        this.containerEncoding = value;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getLoadClasses()
     */
    public List getLoadClasses() {
        // we don't ask the parent here as the classes of the parent
        // have already been loaded
        return this.loadClasses;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getLoggingConfiguration()
     */
    public String getLoggingConfiguration() {
        if ( this.parent != null ) {
            return this.parent.getLoggingConfiguration();
        }
        return this.loggingConfiguration;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getBootstrapLogLevel()
     */
    public String getBootstrapLogLevel() {
        if ( this.parent != null ) {
            return this.parent.getBootstrapLogLevel();
        }
        return this.bootstrapLogLevel;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#isManageExceptions()
     */
    public boolean isManageExceptions() {
        if ( parent != null ) {
            return parent.isManageExceptions();
        }
        return this.manageExceptions;
    }

    /**
     * @see org.apache.cocoon.core.DynamicSettings#getMaxUploadSize()
     */
    public int getMaxUploadSize() {
        if ( parent != null ) {
            return parent.getMaxUploadSize();
        }
        return this.maxUploadSize;
    }

    /**
     * @see org.apache.cocoon.core.DynamicSettings#getOverwriteUploads()
     */
    public String getOverwriteUploads() {
        if ( this.parent != null ) {
            return this.parent.getOverwriteUploads();
        }
        return this.overwriteUploads;
    }

    /**
     * @see org.apache.cocoon.core.DynamicSettings#isShowTime()
     */
    public boolean isShowTime() {
        if ( parent != null ) {
            return parent.isShowTime();
        }
        return this.showTime;
    }

    /**
     * @return Returns the showCocoonVersion flag.
     */
    public boolean isShowVersion() {
        if ( parent != null ) {
            return parent.isShowVersion();
        }
        return this.showCocoonVersion;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getUploadDirectory()
     */
    public String getUploadDirectory() {
        if ( this.parent != null ) {
            return this.parent.getUploadDirectory();
        }
        return this.uploadDirectory;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getWorkDirectory()
     */
    public String getWorkDirectory() {
        if ( this.parent != null ) {
            return this.parent.getWorkDirectory();
        }
        return this.workDirectory;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getEnvironmentLogger()
     */
    public String getEnvironmentLogger() {
        if ( this.parent != null ) {
            return this.parent.getEnvironmentLogger();
        }
        return this.environmentLogger;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getOverrideLogLevel()
     */
    public String getOverrideLogLevel() {
        if ( this.parent != null ) {
            return this.parent.getOverrideLogLevel();
        }
        return this.overrideLogLevel;
    }

    /**
     * @see org.apache.cocoon.core.DynamicSettings#isAllowOverwrite()
     */
    public boolean isAllowOverwrite() {
        final String value = this.getOverwriteUploads();
        if ("deny".equalsIgnoreCase(value)) {
            return false;
        } else if ("allow".equalsIgnoreCase(value)) {
            return true;
        } else {
            // either rename is specified or unsupported value - default to rename.
            return false;
        }
    }

    /**
     * @see org.apache.cocoon.core.DynamicSettings#isSilentlyRename()
     */
    public boolean isSilentlyRename() {
        final String value = this.getOverwriteUploads();
        if ("deny".equalsIgnoreCase(value)) {
            return false;
        } else if ("allow".equalsIgnoreCase(value)) {
            return false; // ignored in this case
        } else {
            // either rename is specified or unsupported value - default to rename.
            return true;
        }
    }

    /**
     * @see org.apache.cocoon.core.DynamicSettings#getReloadDelay(java.lang.String)
     */
    public long getReloadDelay(String type) {
        if ( type == null ) {
            if ( parent != null ) {
                return parent.getReloadDelay(type);
            }
            return this.configurationReloadDelay;
        }
        String o = this.getProperty(KEY_RELOAD_DELAY + '.' + type);
        if ( o != null ) {
            return NumberUtils.toLong(o);
        }
        if ( this.parent != null ) {
            return this.parent.getReloadDelay(type);
        }
        return this.configurationReloadDelay;
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getProperty(java.lang.String)
     */
    public String getProperty(String name) {
        return this.getProperty(name, null);
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getProperty(java.lang.String, java.lang.String)
     */
    public String getProperty(String key, String defaultValue) {
        if ( key == null ) {
            return defaultValue;
        }
        String value = null;
        if ( key.startsWith(KEYPREFIX) ) {
            if ( key.equals(KEY_CONFIGURATION) ) {
                value = this.getConfiguration();
            } else if ( key.equals(KEY_RELOAD_DELAY) ) {
                value = String.valueOf(this.getReloadDelay(null));
            } else if ( key.equals(KEY_LOGGING_CONFIGURATION) ) {
                value = this.getLoggingConfiguration();
            } else if ( key.equals(KEY_LOGGING_ENVIRONMENT_LOGGER) ) {
                value = this.getEnvironmentLogger();
            } else if ( key.equals(KEY_LOGGING_COCOON_LOGGER) ) {
                value = this.getCocoonLogger();
            } else if ( key.equals(KEY_LOGGING_BOOTSTRAP_LOGLEVEL) ) {
                value = this.getBootstrapLogLevel();
            } else if ( key.equals(KEY_RELOADING) ) {
                value = String.valueOf(this.isReloadingEnabled(null));
            } else if ( key.equals(KEY_UPLOADS_ENABLE) ) {
                value = String.valueOf(this.isEnableUploads());
            } else if ( key.equals(KEY_UPLOADS_DIRECTORY) ) {
                value = this.getUploadDirectory();
            } else if ( key.equals(KEY_UPLOADS_AUTOSAVE) ) {
                value = String.valueOf(this.isAutosaveUploads());
            } else if ( key.equals(KEY_UPLOADS_OVERWRITE) ) {
                value = this.getOverwriteUploads();
            } else if ( key.equals(KEY_UPLOADS_MAXSIZE) ) {
                value = String.valueOf(this.getMaxUploadSize());
            } else if ( key.equals(KEY_CACHE_DIRECTORY) ) {
                value = this.getCacheDirectory();
            } else if ( key.equals(KEY_WORK_DIRECTORY) ) {
                value = this.getWorkDirectory();
            } else if ( key.equals(KEY_SHOWTIME) ) {
                value = String.valueOf(this.isShowTime());
            } else if ( key.equals(KEY_HIDE_SHOWTIME) ) {
                value = String.valueOf(this.isHideShowTime());
            } else if ( key.equals(KEY_MANAGE_EXCEPTIONS) ) {
                value = String.valueOf(this.isManageExceptions());
            } else if ( key.equals(KEY_FORM_ENCODING) ) {
                value = this.getFormEncoding();
            } else if ( key.equals(KEY_LOGGING_OVERRIDE_LOGLEVEL) ) {
                value = this.getOverrideLogLevel();
            } else if ( key.equals(KEY_LOAD_CLASSES) ) {
                value = this.toString(this.getLoadClasses());
            } else if ( key.equals(KEY_CONTAINER_ENCODING) ) {
                value = this.containerEncoding;
            }
        }

        // Iterate in reverse order, as most specific property sources are added last
        for (int i = this.properties.size() - 1; i >= 0 && value == null; i--) {
            final Properties p = (Properties)this.properties.get(i);
            value = p.getProperty(key);
        }

        if ( value == null ) {
            if ( this.parent != null ) {
                value = this.parent.getProperty(key, defaultValue);
            } else {
                value = defaultValue;
            }
        }
        return value;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Settings:\n" +
          "Running mode : " + this.getRunningMode()+ '\n' +
          KEY_CONFIGURATION + " : " + this.getConfiguration() + '\n' +
          KEY_RELOAD_DELAY + " : " + this.getReloadDelay(null) + '\n' +
          KEY_RELOADING + " : " + this.isReloadingEnabled(null) + '\n' +
          KEY_LOAD_CLASSES + " : " + this.toString(this.getLoadClasses()) + '\n' +
          KEY_LOGGING_CONFIGURATION + " : " + this.getLoggingConfiguration() + '\n' +
          KEY_LOGGING_ENVIRONMENT_LOGGER + " : " + this.getEnvironmentLogger() + '\n' +
          KEY_LOGGING_BOOTSTRAP_LOGLEVEL + " : " + this.getBootstrapLogLevel() + '\n' +
          KEY_LOGGING_COCOON_LOGGER + " : " + this.getCocoonLogger() + '\n' +
          KEY_LOGGING_OVERRIDE_LOGLEVEL + " : " + this.getOverrideLogLevel() + '\n' +
          KEY_MANAGE_EXCEPTIONS + " : " + this.isManageExceptions() + '\n' +
          KEY_UPLOADS_DIRECTORY + " : " + this.getUploadDirectory() + '\n' +
          KEY_UPLOADS_AUTOSAVE + " : " + this.isAutosaveUploads() + '\n' +
          KEY_UPLOADS_ENABLE + " : " + this.isEnableUploads() + '\n' +
          KEY_UPLOADS_MAXSIZE + " : " + this.getMaxUploadSize() + '\n' +
          KEY_UPLOADS_OVERWRITE + " : " + this.isAllowOverwrite() + '\n' +
          KEY_CACHE_DIRECTORY + " : " + this.getCacheDirectory() + '\n' +
          KEY_WORK_DIRECTORY + " : " + this.getWorkDirectory() + '\n' +
          KEY_FORM_ENCODING + " : " + this.getFormEncoding() + '\n' +
          KEY_CONTAINER_ENCODING + " : " + this.getContainerEncoding() + '\n' +
          KEY_SHOWTIME + " : " + this.isShowTime() + '\n' +
          KEY_HIDE_SHOWTIME + " : " + this.isHideShowTime() + '\n' +
          KEY_SHOW_VERSION + " : " + this.isShowVersion() + '\n';
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
     * @param hideShowTime The hideShowTime to set.
     */
    public void setHideShowTime(boolean hideShowTime) {
        this.checkWriteable();
        this.checkSubSetting();
        this.hideShowTime = hideShowTime;
    }

    /**
     * @param allowReload The allowReload to set.
     */
    public void setReloadingEnabled(boolean allowReload) {
        this.checkWriteable();
        this.checkSubSetting();
        this.reloadingEnabled = allowReload;
    }

    /**
     * @param autosaveUploads The autosaveUploads to set.
     */
    public void setAutosaveUploads(boolean autosaveUploadsValue) {
        this.checkWriteable();
        this.checkSubSetting();
        this.autosaveUploads = autosaveUploadsValue;
    }

    /**
     * @param cacheDirectory The cacheDirectory to set.
     */
    public void setCacheDirectory(String cacheDirectory) {
        this.checkWriteable();
        this.checkSubSetting();
        this.cacheDirectory = cacheDirectory;
    }

    /**
     * @param cocoonLogger The cocoonLogger to set.
     */
    public void setCocoonLogger(String cocoonLogger) {
        this.checkWriteable();
        this.checkSubSetting();
        this.cocoonLogger = cocoonLogger;
    }

    /**
     * @param configuration The configuration to set.
     */
    public void setConfiguration(String configuration) {
        this.checkWriteable();
        this.checkSubSetting();
        this.configuration = configuration;
    }

    /**
     * @param enableUploads The enableUploads to set.
     */
    public void setEnableUploads(boolean enableUploads) {
        this.checkWriteable();
        this.checkSubSetting();
        this.enableUploads = enableUploads;
    }

    /**
     * @param formEncoding The formEncoding to set.
     */
    public void setFormEncoding(String formEncoding) {
        this.checkWriteable();
        this.checkSubSetting();
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
     * @param loggingConfiguration The loggingConfiguration to set.
     */
    public void setLoggingConfiguration(String loggingConfiguration) {
        this.checkWriteable();
        this.checkSubSetting();
        this.loggingConfiguration = loggingConfiguration;
    }

    /**
     * @param logLevel The logLevel to set.
     */
    public void setBootstrapLogLevel(String logLevel) {
        this.checkWriteable();
        this.checkSubSetting();
        this.bootstrapLogLevel = logLevel;
    }

    /**
     * @param manageExceptions The manageExceptions to set.
     */
    public void setManageExceptions(boolean manageExceptions) {
        this.checkWriteable();
        this.checkSubSetting();
        this.manageExceptions = manageExceptions;
    }

    /**
     * @param maxUploadSize The maxUploadSize to set.
     */
    public void setMaxUploadSize(int maxUploadSize) {
        this.checkWriteable();
        this.checkSubSetting();
        this.maxUploadSize = maxUploadSize;
    }

    /**
     * @param overwriteUploads The overwriteUploads to set.
     */
    public void setOverwriteUploads(String overwriteUploads) {
        this.checkWriteable();
        this.checkSubSetting();
        this.overwriteUploads = overwriteUploads;
    }
    
    /**
     * @param showTime The showTime to set.
     */
    public void setShowTime(boolean showTime) {
        this.checkWriteable();
        this.checkSubSetting();
        this.showTime = showTime;
    }

    /**
     * @param showCocoonVersion The showCocoonVersion flag to set.
     */
    public void setShowCocoonVersion(boolean showCocoonVersion) {
        this.checkWriteable();
        this.checkSubSetting();
        this.showCocoonVersion = showCocoonVersion;
    }

    /**
     * @param uploadDirectory The uploadDirectory to set.
     */
    public void setUploadDirectory(String uploadDirectory) {
        this.checkWriteable();
        this.checkSubSetting();
        this.uploadDirectory = uploadDirectory;
    }

    /**
     * @param workDirectory The workDirectory to set.
     */
    public void setWorkDirectory(String workDirectory) {
        this.checkWriteable();
        this.checkSubSetting();
        this.workDirectory = workDirectory;
    }

    /**
     * @param logger The logger for the environment.
     */
    public void setEnvironmentLogger(String logger) {
        this.checkWriteable();
        this.checkSubSetting();
        this.environmentLogger = logger;
    }

    /**
     * @param overrideLogLevel The overrideLogLevel to set.
     */
    public void setOverrideLogLevel(String overrideLogLevel) {
        this.checkWriteable();
        this.checkSubSetting();
        this.overrideLogLevel = overrideLogLevel;
    }

    /**
     * @param configurationReloadDelay The configurationReloadDelay to set.
     */
    public void setConfigurationReloadDelay(long configurationReloadDelay) {
        this.checkWriteable();
        this.checkSubSetting();
        this.configurationReloadDelay = configurationReloadDelay;
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
     * check if this configuration is tried to be set for a sub settings
     * object.
     *
     * @throws IllegalStateException if this setting is a sub setting
     */
    protected final void checkSubSetting()
    throws IllegalStateException {
        if( this.parent != null ) {
            throw new IllegalStateException
                ( "This value can only be changed for the root settings object." );
        }
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getCreationTime()
     */
    public long getCreationTime() {
        if ( this.parent != null ) {
            return this.parent.getCreationTime();
        }
        return this.creationTime;
    }

    /**
     * Set the creation time of the current cocoon instance.
     */
    public void setCreationTime(long value) {
        // we don't check for writable here as this value is set after the whole
        // container is setup
        this.checkSubSetting();
        this.creationTime = value;
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getPropertyNames(java.lang.String)
     */
    public List getPropertyNames(String keyPrefix) {
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
        if ( this.parent != null ) {
            final List parentList = this.parent.getPropertyNames(keyPrefix);
            final Iterator i = parentList.iterator();
            while ( i.hasNext() ) {
                final String name = (String)i.next();
                if ( !props.contains(name) ) {
                    props.add(name);
                }
            }
        }
        return props;
    }
    
    /**
     * @see org.apache.cocoon.configuration.Settings#getPropertyNames()
     */
    public List getPropertyNames() {
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
        if ( this.parent != null ) {
            final List parentList = this.parent.getPropertyNames();
            final Iterator i = parentList.iterator();
            while ( i.hasNext() ) {
                final String name = (String)i.next();
                if ( !props.contains(name) ) {
                    props.add(name);
                }
            }
        }
        return props;
    }

    /**
     * @see org.apache.cocoon.configuration.Settings#getRunningMode()
     */
    public String getRunningMode() {
        return null;
    }
}
