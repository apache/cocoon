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
    protected Boolean reloadingEnabled;

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
    protected Boolean enableUploads;

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
    protected Boolean autosaveUploads;

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
    protected Integer maxUploadSize;

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
    protected Boolean showTime;

    /**
     * If true, processing time will be added as an HTML comment
     */
    protected Boolean hideShowTime;

    /**
     * If true, the X-Cocoon-Version response header will be included.
     */
    protected Boolean showCocoonVersion;

    /**
     * If true or not set, this class will try to catch and handle all Cocoon exceptions.
     * If false, it will rethrow them to the servlet container.
     */
    protected Boolean manageExceptions;

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
    protected Long configurationReloadDelay;

    /** The time the cocoon instance was created. */
    protected Long creationTime;

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
        this.reloadingEnabled = BooleanUtils.toBooleanObject(RELOADING_ENABLED_DEFAULT);
        this.enableUploads = BooleanUtils.toBooleanObject(ENABLE_UPLOADS);
        this.autosaveUploads = BooleanUtils.toBooleanObject(SAVE_UPLOADS_TO_DISK);
        this.maxUploadSize = new Integer(MAX_UPLOAD_SIZE);
        this.showTime = BooleanUtils.toBooleanObject(SHOW_TIME);
        this.hideShowTime = BooleanUtils.toBooleanObject(HIDE_SHOW_TIME);
        this.showCocoonVersion = BooleanUtils.toBooleanObject(SHOW_COCOON_VERSION);
        this.manageExceptions = BooleanUtils.toBooleanObject(MANAGE_EXCEPTIONS);
        this.configurationReloadDelay = new Long(1000);
        this.containerEncoding = "ISO-8859-1";
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
                    key = key.substring(KEYPREFIX.length());
                    final String value = current.getValue().toString();

                    if ( key.equals(KEY_CONFIGURATION) ) {
                        this.configuration = value;
                    } else if ( key.equals(KEY_RELOAD_DELAY) ) {
                        this.configurationReloadDelay = Long.valueOf(value);
                    } else if ( key.equals(KEY_LOGGING_CONFIGURATION) ) {
                        this.loggingConfiguration = value;
                    } else if ( key.equals(KEY_LOGGING_ENVIRONMENT_LOGGER) ) {
                        this.environmentLogger = value;
                    } else if ( key.equals(KEY_LOGGING_COCOON_LOGGER) ) {
                        this.cocoonLogger = value;
                    } else if ( key.equals(KEY_LOGGING_BOOTSTRAP_LOGLEVEL) ) {
                        this.bootstrapLogLevel = value;
                    } else if ( key.equals(KEY_RELOADING) ) {
                        this.reloadingEnabled = BooleanUtils.toBooleanObject(value);
                    } else if ( key.equals(KEY_UPLOADS_ENABLE) ) {
                        this.enableUploads = BooleanUtils.toBooleanObject(value);
                    } else if ( key.equals(KEY_UPLOADS_DIRECTORY) ) {
                        this.uploadDirectory = value;
                    } else if ( key.equals(KEY_UPLOADS_AUTOSAVE) ) {
                        this.autosaveUploads = BooleanUtils.toBooleanObject(value);
                    } else if ( key.equals(KEY_UPLOADS_OVERWRITE) ) {
                        this.overwriteUploads = value;
                    } else if ( key.equals(KEY_UPLOADS_MAXSIZE) ) {
                        this.maxUploadSize = Integer.valueOf(value);
                    } else if ( key.equals(KEY_CACHE_DIRECTORY) ) {
                        this.cacheDirectory = value;
                    } else if ( key.equals(KEY_WORK_DIRECTORY) ) {
                        this.workDirectory = value;
                    } else if ( key.equals(KEY_SHOWTIME) ) {
                        this.showTime = BooleanUtils.toBooleanObject(value);
                    } else if ( key.equals(KEY_HIDE_SHOWTIME) ) {
                        this.hideShowTime = BooleanUtils.toBooleanObject(value);
                    } else if ( key.equals(KEY_SHOW_VERSION) ) {
                        this.showCocoonVersion = BooleanUtils.toBooleanObject(value);
                    } else if ( key.equals(KEY_MANAGE_EXCEPTIONS) ) {
                        this.manageExceptions = BooleanUtils.toBooleanObject(value);
                    } else if ( key.equals(KEY_FORM_ENCODING) ) {
                        this.formEncoding = value;
                    } else if ( key.equals(KEY_LOGGING_OVERRIDE_LOGLEVEL) ) {
                        this.overrideLogLevel = value;
                    } else if ( key.startsWith(KEY_LOAD_CLASSES) ) {
                        this.addToLoadClasses(value);
                    } else if ( key.startsWith(KEY_CONTAINER_ENCODING ) ) {
                        this.containerEncoding = value;
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
        if ( this.hideShowTime == null ) {
            if ( parent != null ) {
                return parent.isShowTime();
            }
            return HIDE_SHOW_TIME;
        }
        return this.hideShowTime.booleanValue();
    }

    /**
     * @see org.apache.cocoon.core.DynamicSettings#isReloadingEnabled(java.lang.String)
     */
    public boolean isReloadingEnabled(String type) {
        if ( type == null ) {
            if ( this.reloadingEnabled == null ) {
                if ( parent != null ) {
                    return parent.isReloadingEnabled(type);
                }
                return RELOADING_ENABLED_DEFAULT;
            }
            return this.reloadingEnabled.booleanValue();
        }
        String o = this.getProperty(KEYPREFIX + KEY_RELOADING + '.' + type);
        if ( o != null ) {
            return BooleanUtils.toBoolean(o);
        }
        if ( this.parent != null ) {
            o = this.parent.getProperty(KEYPREFIX + KEY_RELOADING + '.' + type);
            if ( o != null ) {
                return BooleanUtils.toBoolean(o);
            }
        }
        if ( this.reloadingEnabled == null ) {
            if ( this.parent != null ) {
                return this.parent.isReloadingEnabled(type);
            }
            return RELOADING_ENABLED_DEFAULT;
        }
        return this.reloadingEnabled.booleanValue();
    }

    /**
     * @see org.apache.cocoon.core.DynamicSettings#isAutosaveUploads()
     */
    public boolean isAutosaveUploads() {
        if ( this.autosaveUploads == null ) {
            if ( parent != null ) {
                return parent.isAutosaveUploads();
            }
            return SAVE_UPLOADS_TO_DISK;
        }
        return this.autosaveUploads.booleanValue();
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getCacheDirectory()
     */
    public String getCacheDirectory() {
        if ( this.cacheDirectory == null && this.parent != null ) {
            return this.parent.getCacheDirectory();
        }
        return this.cacheDirectory;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getCocoonLogger()
     */
    public String getCocoonLogger() {
        if ( this.cocoonLogger == null && this.parent != null ) {
            return this.parent.getCocoonLogger();
        }
        return this.cocoonLogger;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getConfiguration()
     */
    public String getConfiguration() {
        if ( this.configuration == null && this.parent != null ) {
            return this.parent.getConfiguration();
        }
        return this.configuration;
    }

    /**
     * @see org.apache.cocoon.core.DynamicSettings#isEnableUploads()
     */
    public boolean isEnableUploads() {
        if ( this.enableUploads == null ) {
            if ( parent != null ) {
                return parent.isEnableUploads();
            }
            return ENABLE_UPLOADS;
        }
        return this.enableUploads.booleanValue();
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getFormEncoding()
     */
    public String getFormEncoding() {
        if ( this.formEncoding == null && this.parent != null ) {
            return this.parent.getFormEncoding();
        }
        return this.formEncoding;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getContainerEncoding()
     */
    public String getContainerEncoding() {
        if ( this.containerEncoding == null && this.parent != null ) {
            return this.parent.getContainerEncoding();
        }
        return this.containerEncoding;
    }

    /**
     * Set the container encoding.
     * @param value The new encoding value.
     */
    public void setContainerEncoding(String value) {
        this.containerEncoding = value;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getLoadClasses()
     */
    public List getLoadClasses() {
        // we don't ask the parent here as that one already loaded the classe
        return this.loadClasses;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getLoggingConfiguration()
     */
    public String getLoggingConfiguration() {
        if ( this.loggingConfiguration == null && this.parent != null ) {
            return this.parent.getLoggingConfiguration();
        }
        return this.loggingConfiguration;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getBootstrapLogLevel()
     */
    public String getBootstrapLogLevel() {
        if ( this.bootstrapLogLevel == null && this.parent != null ) {
            return this.parent.getBootstrapLogLevel();
        }
        return this.bootstrapLogLevel;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#isManageExceptions()
     */
    public boolean isManageExceptions() {
        if ( this.manageExceptions == null ) {
            if ( parent != null ) {
                return parent.isManageExceptions();
            }
            return MANAGE_EXCEPTIONS;
        }
        return this.manageExceptions.booleanValue();
    }

    /**
     * @see org.apache.cocoon.core.DynamicSettings#getMaxUploadSize()
     */
    public int getMaxUploadSize() {
        if ( this.maxUploadSize == null ) {
            if ( parent != null ) {
                return parent.getMaxUploadSize();
            }
            return MAX_UPLOAD_SIZE;
        }
        return this.maxUploadSize.intValue();
    }

    /**
     * @see org.apache.cocoon.core.DynamicSettings#getOverwriteUploads()
     */
    public String getOverwriteUploads() {
        if ( this.overwriteUploads == null && this.parent != null ) {
            return this.parent.getOverwriteUploads();
        }
        return this.overwriteUploads;
    }

    /**
     * @see org.apache.cocoon.core.DynamicSettings#isShowTime()
     */
    public boolean isShowTime() {
        if ( this.showTime == null ) {
            if ( parent != null ) {
                return parent.isShowTime();
            }
            return SHOW_TIME;
        }
        return this.showTime.booleanValue();
    }

    /**
     * @return Returns the showCocoonVersion flag.
     */
    public boolean isShowVersion() {
        if ( this.showCocoonVersion == null ) {
            if ( parent != null ) {
                return parent.isShowVersion();
            }
            return SHOW_COCOON_VERSION;
        }
        return this.showCocoonVersion.booleanValue();
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getUploadDirectory()
     */
    public String getUploadDirectory() {
        if ( this.uploadDirectory == null && this.parent != null ) {
            return this.parent.getUploadDirectory();
        }
        return this.uploadDirectory;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getWorkDirectory()
     */
    public String getWorkDirectory() {
        if ( this.workDirectory == null && this.parent != null ) {
            return this.parent.getWorkDirectory();
        }
        return this.workDirectory;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getEnvironmentLogger()
     */
    public String getEnvironmentLogger() {
        if ( this.environmentLogger == null && this.parent != null ) {
            return this.parent.getEnvironmentLogger();
        }
        return this.environmentLogger;
    }

    /**
     * @see org.apache.cocoon.core.BaseSettings#getOverrideLogLevel()
     */
    public String getOverrideLogLevel() {
        if ( this.overrideLogLevel == null && this.parent != null ) {
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
            if ( this.configurationReloadDelay == null ) {
                if ( parent != null ) {
                    return parent.getReloadDelay(type);
                }
                return 1000;
            }
            return this.configurationReloadDelay.longValue();
        }
        String o = this.getProperty(KEYPREFIX + KEY_RELOAD_DELAY + '.' + type);
        if ( o != null ) {
            return NumberUtils.toLong(o);
        }
        if ( this.parent != null ) {
            o = this.parent.getProperty(KEYPREFIX + KEY_RELOAD_DELAY + '.' + type);
            if ( o != null ) {
                return NumberUtils.toLong(o);
            }
        }
        if ( this.configurationReloadDelay == null ) {
            if ( this.parent != null ) {
                return this.parent.getReloadDelay(type);
            }
            return 1000;
        }
        return this.configurationReloadDelay.longValue();
    }

    /**
     * @see org.apache.cocoon.core.Settings#getProperty(java.lang.String)
     */
    public String getProperty(String name) {
        return this.getProperty(name, null);
    }

    /**
     * @see org.apache.cocoon.core.Settings#getProperty(java.lang.String, java.lang.String)
     */
    public String getProperty(String key, String defaultValue) {
        if ( key == null ) {
            return defaultValue;
        }
        String value = null;
        if ( key.startsWith(KEYPREFIX) ) {
            final String sKey = key.substring(KEYPREFIX.length());
            if ( sKey.equals(KEY_CONFIGURATION) ) {
                value = this.getConfiguration();
            } else if ( sKey.equals(KEY_RELOAD_DELAY) ) {
                value = String.valueOf(this.getReloadDelay(null));
            } else if ( sKey.equals(KEY_LOGGING_CONFIGURATION) ) {
                value = this.getLoggingConfiguration();
            } else if ( sKey.equals(KEY_LOGGING_ENVIRONMENT_LOGGER) ) {
                value = this.getEnvironmentLogger();
            } else if ( sKey.equals(KEY_LOGGING_COCOON_LOGGER) ) {
                value = this.getCocoonLogger();
            } else if ( sKey.equals(KEY_LOGGING_BOOTSTRAP_LOGLEVEL) ) {
                value = this.getBootstrapLogLevel();
            } else if ( sKey.equals(KEY_RELOADING) ) {
                value = String.valueOf(this.isReloadingEnabled(null));
            } else if ( sKey.equals(KEY_UPLOADS_ENABLE) ) {
                value = String.valueOf(this.isEnableUploads());
            } else if ( sKey.equals(KEY_UPLOADS_DIRECTORY) ) {
                value = this.getUploadDirectory();
            } else if ( sKey.equals(KEY_UPLOADS_AUTOSAVE) ) {
                value = String.valueOf(this.isAutosaveUploads());
            } else if ( sKey.equals(KEY_UPLOADS_OVERWRITE) ) {
                value = this.getOverwriteUploads();
            } else if ( sKey.equals(KEY_UPLOADS_MAXSIZE) ) {
                value = String.valueOf(this.getMaxUploadSize());
            } else if ( sKey.equals(KEY_CACHE_DIRECTORY) ) {
                value = this.getCacheDirectory();
            } else if ( sKey.equals(KEY_WORK_DIRECTORY) ) {
                value = this.getWorkDirectory();
            } else if ( sKey.equals(KEY_SHOWTIME) ) {
                value = String.valueOf(this.isShowTime());
            } else if ( sKey.equals(KEY_HIDE_SHOWTIME) ) {
                value = String.valueOf(this.isHideShowTime());
            } else if ( sKey.equals(KEY_MANAGE_EXCEPTIONS) ) {
                value = String.valueOf(this.isManageExceptions());
            } else if ( sKey.equals(KEY_FORM_ENCODING) ) {
                value = this.getFormEncoding();
            } else if ( sKey.equals(KEY_LOGGING_OVERRIDE_LOGLEVEL) ) {
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
          "Running mode : " + this.getProperty(PROPERTY_RUNNING_MODE, DEFAULT_RUNNING_MODE) + '\n' +
          KEY_CONFIGURATION + " : " + this.configuration + '\n' +
          KEY_RELOAD_DELAY + " : " + this.configurationReloadDelay + '\n' +
          KEY_RELOADING + " : " + this.reloadingEnabled + '\n' +
          KEY_LOAD_CLASSES + " : " + this.toString(this.loadClasses) + '\n' +
          KEY_LOGGING_CONFIGURATION + " : " + this.loggingConfiguration + '\n' +
          KEY_LOGGING_ENVIRONMENT_LOGGER + " : " + this.environmentLogger + '\n' +
          KEY_LOGGING_BOOTSTRAP_LOGLEVEL + " : " + this.bootstrapLogLevel + '\n' +
          KEY_LOGGING_COCOON_LOGGER + " : " + this.cocoonLogger + '\n' +
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
          KEY_CONTAINER_ENCODING + " : " + this.containerEncoding + '\n' +
          KEY_SHOWTIME + " : " + this.showTime + '\n' +
          KEY_HIDE_SHOWTIME + " : " + this.hideShowTime + '\n' +
          KEY_SHOW_VERSION + " : " + this.showCocoonVersion + '\n';
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
        this.hideShowTime = BooleanUtils.toBooleanObject(hideShowTime);
    }

    /**
     * @param allowReload The allowReload to set.
     */
    public void setReloadingEnabled(boolean allowReload) {
        this.checkWriteable();
        this.reloadingEnabled = BooleanUtils.toBooleanObject(allowReload);
    }

    /**
     * @param autosaveUploads The autosaveUploads to set.
     */
    public void setAutosaveUploads(boolean autosaveUploadsValue) {
        this.checkWriteable();
        this.autosaveUploads = BooleanUtils.toBooleanObject(autosaveUploadsValue);
    }

    /**
     * @param cacheDirectory The cacheDirectory to set.
     */
    public void setCacheDirectory(String cacheDirectory) {
        this.checkWriteable();
        this.cacheDirectory = cacheDirectory;
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
        this.enableUploads = BooleanUtils.toBooleanObject(enableUploads);
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
        this.manageExceptions = BooleanUtils.toBooleanObject(manageExceptions);
    }

    /**
     * @param maxUploadSize The maxUploadSize to set.
     */
    public void setMaxUploadSize(int maxUploadSize) {
        this.checkWriteable();
        this.maxUploadSize = new Integer(maxUploadSize);
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
        this.showTime = BooleanUtils.toBooleanObject(showTime);
    }

    /**
     * @param showCocoonVersion The showCocoonVersion flag to set.
     */
    public void setShowCocoonVersion(boolean showCocoonVersion) {
        this.checkWriteable();
        this.showCocoonVersion = BooleanUtils.toBooleanObject(showCocoonVersion);
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
        this.configurationReloadDelay = new Long(configurationReloadDelay);
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
        if ( this.creationTime == null ) {
            if ( this.parent != null ) {
                return this.parent.getCreationTime();
            }
            return 0;
        }
        return this.creationTime.longValue();
    }

    /**
     * Set the creation time of the current cocoon instance.
     */
    public void setCreationTime(long value) {
        // Don't check read only here as this will change if Cocoon
        // is reloaded while the settings remain the same.
        this.creationTime = new Long(value);
    }

    /**
     * @see org.apache.cocoon.core.Settings#getPropertyNames(java.lang.String)
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
     * @see org.apache.cocoon.core.Settings#getPropertyNames()
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
}
