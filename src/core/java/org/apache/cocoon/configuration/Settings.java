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
    protected String accessLogger;

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
    protected String loggerClassName;

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
                if ( key.startsWith("org.apache.cocoon.") ) {
                    key = key.substring("org.apache.cocoon.".length());
                    final String value = current.getValue().toString();

                    if ( key.equals("init.classloader") ) {
                        this.initClassloader = BooleanUtils.toBoolean(value);
                    } else if ( key.equals("configuration") ) {
                        this.configuration = value;
                    } else if ( key.equals("configuration.reloaddelay") ) {
                        this.configurationReloadDelay = NumberUtils.toLong(value);
                    } else if ( key.equals("logging.configuration") ) {
                        this.loggingConfiguration = value;
                    } else if ( key.equals("logging.logger.access") ) {
                        this.accessLogger = value;
                    } else if ( key.equals("logging.logger.cocoon") ) {
                        this.cocoonLogger = value;
                    } else if ( key.equals("logging.bootstrap.level") ) {
                        this.bootstrapLogLevel = value;
                    } else if ( key.equals("logging.manager.class") ) {
                        this.loggerClassName = value;
                    } else if ( key.equals("logging.log4j.configuration") ) {
                        this.log4jConfiguration = value;
                    } else if ( key.equals("allow.reload") ) {
                        this.allowReload = BooleanUtils.toBoolean(value);
                    } else if ( key.equals("uploads.enable") ) {
                        this.enableUploads = BooleanUtils.toBoolean(value);
                    } else if ( key.equals("uploads.directory") ) {
                        this.uploadDirectory = value;
                    } else if ( key.equals("uploads.autosave") ) {
                        this.autosaveUploads = BooleanUtils.toBoolean(value);
                    } else if ( key.equals("uploads.overwrite") ) {
                        this.overwriteUploads = value;
                    } else if ( key.equals("uploads.maxsize") ) {
                        this.maxUploadSize = NumberUtils.toInt(value);
                    } else if ( key.equals("cache.directory") ) {
                        this.cacheDirectory = value;
                    } else if ( key.equals("work.directory") ) {
                        this.workDirectory = value;
                    } else if ( key.equals("parentservicemanager") ) {
                        this.parentServiceManagerClassName = value;
                    } else if ( key.equals("showtime") ) {
                        this.showTime = BooleanUtils.toBoolean(value);
                    } else if ( key.equals("hideshowtime") ) {
                        this.hideShowTime = BooleanUtils.toBoolean(value);
                    } else if ( key.equals("manageexceptions") ) {
                        this.manageExceptions = BooleanUtils.toBoolean(value);
                    } else if ( key.equals("formencoding") ) {
                        this.formEncoding = value;
                    } else if ( key.equals("override.loglevel") ) {
                        this.overrideLogLevel = value;
                    }
                    // TODO - force property, load classes, extra class path
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
     * @param extraClasspaths The extraClasspaths to set.
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
     * @param forceProperties The forceProperties to set.
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
     * @param loadClasses The loadClasses to set.
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
    public String get(String property, String defaultValue) {
        if ( property == null ) {
            return defaultValue;
        }
        return property;
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        // FIXME - add all
        return "Settings:\n"+
               "- Configuration: " + this.configuration + "\n" +
               "- Configuration-reload-delay: " + this.configurationReloadDelay + "\n" +
               "- InitClassloader: " + this.initClassloader + "\n" + 
               "- ForceProperties: " + ( this.forceProperties == null ? "-" : this.forceProperties.toString() ) + "\n" +
               "- Logging-Configuration: " + this.loggingConfiguration + "\n" +
               "- Cocoon-Logger: " + this.cocoonLogger + "\n" + 
               "- Access-Logger: " + this.accessLogger + "\n" +
               "- Logger-Classname: " + this.loggerClassName + "\n" + 
               "- Bootstrap-Loglevel: " + this.bootstrapLogLevel + "\n" +
               "- Allow-Reload: " + this.allowReload + "\n" +
               "- Enable-Uploads: " + this.enableUploads + "\n" +
               "- Upload-Directory: " + this.uploadDirectory + "\n" +
               "- Autosave-Uploads: " + this.autosaveUploads + "\n" +
               "- Overwrite-Uploads: " + this.overwriteUploads + "\n" +
               "- Max.Uploadsize: " + this.maxUploadSize + "\n" +
               "- Cache-Directory: " + this.cacheDirectory + "\n" + 
               "- Work-Directory: " + this.workDirectory + "\n" +
               "- Load Classes: " + this.loadClasses + "\n" + 
               "- Extra Classpath: " + this.extraClasspaths + "\n" +
               "- Parent ServiceManager: " + this.parentServiceManagerClassName + "\n" +
               "- Show Time: " + this.showTime + "\n" + 
               "- Manage Exceptions: " + this.manageExceptions + "\n" +
               "- Form-Encoding: " + this.formEncoding + "\n" + 
               "- Log4J Configuration: " + this.log4jConfiguration + "\n" +
               "- Override Loglevel: " + this.overrideLogLevel + "\n";

    }

    public String getProperty(String name) {
        return this.getProperty(name, null);
    }

    public String getProperty(String key, String defaultValue) {
        if ( key == null ) {
            return defaultValue;
        }
        String value = null;
        if ( key.startsWith("org.apache.cocoon.") ) {
            final String sKey = key.substring("org.apache.cocoon.".length());
            if ( sKey.equals("init.classloader") ) {
                value = String.valueOf(this.initClassloader);
            } else if ( sKey.equals("configuration") ) {
                value = this.configuration;
            } else if ( sKey.equals("configuration.reloaddelay") ) {
                value = String.valueOf(this.configurationReloadDelay);
            } else if ( sKey.equals("logging.configuration") ) {
                value = this.loggingConfiguration;
            } else if ( sKey.equals("logging.logger.access") ) {
                value = this.accessLogger;
            } else if ( sKey.equals("logging.logger.cocoon") ) {
                value = this.cocoonLogger;
            } else if ( sKey.equals("logging.bootstrap.level") ) {
                value = this.bootstrapLogLevel;
            } else if ( sKey.equals("logging.manager.class") ) {
                value = this.loggerClassName;
            } else if ( sKey.equals("logging.log4j.configuration") ) {
                value = this.log4jConfiguration;
            } else if ( sKey.equals("allow.reload") ) {
                value = String.valueOf(this.allowReload);
            } else if ( sKey.equals("uploads.enable") ) {
                value = String.valueOf(this.enableUploads);
            } else if ( sKey.equals("uploads.directory") ) {
                value = this.uploadDirectory = value;
            } else if ( sKey.equals("uploads.autosave") ) {
                value = String.valueOf(this.autosaveUploads);
            } else if ( sKey.equals("uploads.overwrite") ) {
                value = this.overwriteUploads;
            } else if ( sKey.equals("uploads.maxsize") ) {
                value = String.valueOf(this.maxUploadSize);
            } else if ( sKey.equals("cache.directory") ) {
                value = this.cacheDirectory;
            } else if ( sKey.equals("work.directory") ) {
                value = this.workDirectory;
            } else if ( sKey.equals("parentservicemanager") ) {
                value = this.parentServiceManagerClassName;
            } else if ( sKey.equals("showtime") ) {
                value = String.valueOf(this.showTime);
            } else if ( sKey.equals("hideshowtime") ) {
                value = String.valueOf(this.hideShowTime);
            } else if ( sKey.equals("manageexceptions") ) {
                value = String.valueOf(this.manageExceptions);
            } else if ( sKey.equals("formencoding") ) {
                value = this.formEncoding;
            } else if ( sKey.equals("override.loglevel") ) {
                value = this.overrideLogLevel;
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
