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

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

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

    protected boolean initClassloader = false;
    protected String[] forceProperties;
    protected String configuration;    
    protected String loggingConfiguration;
    protected String cocoonLogger;    
    protected String accessLogger;
    protected String bootstrapLogLevel;
    protected String loggerClassName;

    /**
     * Allow reloading of cocoon by specifying the <code>cocoon-reload=true</code> parameter with a request
     */
    protected boolean allowReload = ALLOW_RELOAD;

    protected String[] loadClasses;

    /**
     * Allow processing of upload requests (mime/multipart)
     */
    protected boolean enableUploads = ENABLE_UPLOADS;
    protected String uploadDirectory;    
    protected boolean autosaveUploads = SAVE_UPLOADS_TO_DISK;
    // accepted values are deny|allow|rename - rename is default.
    protected String overwriteUploads;
    protected int maxUploadSize = MAX_UPLOAD_SIZE;
    protected String cacheDirectory;
    protected String workDirectory;
    protected String[] extraClasspaths;
    protected String parentServiceManagerClassName;

    /**
     * Allow adding processing time to the response
     */
    protected boolean showTime = false;
    /**
     * If true, processing time will be added as an HTML comment
     */
    protected boolean hideShowTime = false;
    /**
     * If true or not set, this class will try to catch and handle all Cocoon exceptions.
     * If false, it will rethrow them to the servlet container.
     */
    protected boolean manageExceptions = true;
    protected String formEncoding;
    protected String log4jConfiguration;
    protected String overrideLogLevel;
    
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
                    if ( key.equals("override.loglevel") ) {
                        this.overrideLogLevel = current.getValue().toString();
                    }
                }
            }
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
    public String[] getExtraClasspaths() {
        return this.extraClasspaths;
    }
    /**
     * @param extraClasspaths The extraClasspaths to set.
     */
    public void setExtraClasspaths(String[] extraClasspaths) {
        this.extraClasspaths = extraClasspaths;
    }
    /**
     * @return Returns the forceProperties.
     */
    public String[] getForceProperties() {
        return this.forceProperties;
    }
    /**
     * @param forceProperties The forceProperties to set.
     */
    public void setForceProperties(String[] forceProperties) {
        this.forceProperties = forceProperties;
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
    public String[] getLoadClasses() {
        return this.loadClasses;
    }
    /**
     * @param loadClasses The loadClasses to set.
     */
    public void setLoadClasses(String[] loadClasses) {
        this.loadClasses = loadClasses;
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "Settings:\n"+
               "- Configuration: " + this.configuration + "\n" + 
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
}
