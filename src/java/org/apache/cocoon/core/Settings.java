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

/**
 * This object holds the global configuration of Cocoon.
 *
 * @version SVN $Id$
 */
public class Settings implements BaseSettings, DynamicSettings {

    /** Prefix for properties */
    protected static final String KEYPREFIX = "org.apache.cocoon.";

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

    /**
     * This parameter allows to set system properties
     */
    protected Map forceProperties = new HashMap();

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
     * Log4J has to be configured already.
     */
    protected String loggerManagerClassName;

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

    /**
     * Allow reinstantiating (reloading) of the cocoon instance. If this is
     * set to "yes" or "true", a new cocoon instance can be created using
     * the request parameter "cocoon-reload". It also enables that Cocoon is
     * reloaded when cocoon.xconf changes. Default is no for security reasons.
     */
    protected boolean allowReload = ALLOW_RELOAD;

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
     * This parameter allows you to select the parent service manager.
     * The class will be instantiated via the constructor that takes a single
     * String as a parameter. That String will be equal to the text after the '/'.
     *
     * Cocoon honors the LogEnabled, Initializable and Disposable interfaces for
     * this class, if it implements them.
     */
    protected String parentServiceManagerClassName;

    /**
     * Allow adding processing time to the response
     */
    protected boolean showTime = SHOW_TIME;

    /**
     * If true, processing time will be added as an HTML comment
     */
    protected boolean hideShowTime = HIDE_SHOW_TIME;

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
     * Delay between reload checks for the configuration
     */
    protected long configurationReloadDelay = 1000;

    /**
     * Lazy mode for component loading
     */
    protected boolean lazyMode = false;

    /**
     * Create a new settings object
     */
    public Settings() {
        // nothing to do
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
    public boolean isAllowReload() {
        return this.allowReload;
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
     * @return Returns the initClassloader.
     */
    public boolean isInitClassloader() {
        return this.initClassloader;
    }

    /**
     * @return Returns the loadClasses.
     */
    public Iterator getLoadClasses() {
        return this.loadClasses.iterator();
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
     * @return Returns the parentServiceManagerClassName.
     */
    public String getParentServiceManagerClassName() {
        return this.parentServiceManagerClassName;
    }

    /**
     * @return Returns the showTime.
     */
    public boolean isShowTime() {
        return this.showTime;
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
     * @return Returns the log4jConfiguration.
     */
    public String getLog4jConfiguration() {
        return this.log4jConfiguration;
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
    public long getConfigurationReloadDelay() {
        return configurationReloadDelay;
    }

    /**
     * @return Returns the lazyMode.
     */
    public boolean isLazyMode() {
        return this.lazyMode;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Settings:\n"+
          KEY_CONFIGURATION + " : " + this.configuration + '\n' +
          KEY_CONFIGURATION_RELOAD_DELAY + " : " + this.configurationReloadDelay + '\n' +
          KEY_ALLOW_RELOAD + " : " + this.allowReload + '\n' +
          KEY_INIT_CLASSLOADER + " : " + this.initClassloader + '\n' +
          KEY_EXTRA_CLASSPATHS + " : " + this.toString(this.extraClasspaths) + '\n' +
          KEY_LOAD_CLASSES + " : " + this.toString(this.loadClasses) + '\n' +
          KEY_FORCE_PROPERTIES + " : " + this.toString(this.forceProperties) + '\n' +
          KEY_LOGGING_CONFIGURATION + " : " + this.loggingConfiguration + '\n' +
          KEY_LOGGING_ENVIRONMENT_LOGGER + " : " + this.environmentLogger + '\n' +
          KEY_LOGGING_BOOTSTRAP_LOGLEVEL + " : " + this.bootstrapLogLevel + '\n' +
          KEY_LOGGING_COCOON_LOGGER + " : " + this.cocoonLogger + '\n' +
          KEY_LOGGING_LOG4J_CONFIGURATION + " : " + this.log4jConfiguration + '\n' +
          KEY_LOGGING_MANAGER_CLASS + " : " + this.loggerManagerClassName + '\n' +
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
            } else if ( sKey.equals(KEY_LOGGING_ENVIRONMENT_LOGGER) ) {
                value = this.environmentLogger;
            } else if ( sKey.equals(KEY_LOGGING_COCOON_LOGGER) ) {
                value = this.cocoonLogger;
            } else if ( sKey.equals(KEY_LOGGING_BOOTSTRAP_LOGLEVEL) ) {
                value = this.bootstrapLogLevel;
            } else if ( sKey.equals(KEY_LOGGING_MANAGER_CLASS) ) {
                value = this.loggerManagerClassName;
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
            } else if ( key.equals(KEY_LOAD_CLASSES) ) {
                value = this.toString(this.loadClasses);
            } else if ( key.equals(KEY_EXTRA_CLASSPATHS) ) {
                this.toString(this.extraClasspaths);
            } else if ( key.equals(KEY_FORCE_PROPERTIES) ) {
                this.toString(this.forceProperties);
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
