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

    protected String classloaderClassName;
    protected boolean initClassloader;
    protected String[] forceProperties;
    protected String configuration;    
    protected String loggingConfiguration;
    protected String cocoonLogger;    
    protected String accessLogger;
    protected String bootstrapLogLevel;
    protected String loggerClassName;
    protected boolean allowReload;
    protected String[] loadClasses;    
    protected boolean enableUploads;
    protected String uploadDirectory;    
    protected boolean autosaveUploads;
    protected boolean overwriteUploads;
    protected long maxUploadSize;
    protected String cacheDirectory;
    protected String workDirectory;
    protected String[] extraClasspaths;
    protected String parentServiceManagerClassName;
    protected boolean showTime;
    protected boolean manageExceptions;
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
     * @return Returns the classloaderClassName.
     */
    public String getClassloaderClassName() {
        return this.classloaderClassName;
    }
    /**
     * @param classloaderClassName The classloaderClassName to set.
     */
    public void setClassloaderClassName(String classloaderClassName) {
        this.classloaderClassName = classloaderClassName;
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
    public long getMaxUploadSize() {
        return this.maxUploadSize;
    }
    /**
     * @param maxUploadSize The maxUploadSize to set.
     */
    public void setMaxUploadSize(long maxUploadSize) {
        this.maxUploadSize = maxUploadSize;
    }
    /**
     * @return Returns the overwriteUploads.
     */
    public boolean isOverwriteUploads() {
        return this.overwriteUploads;
    }
    /**
     * @param overwriteUploads The overwriteUploads to set.
     */
    public void setOverwriteUploads(boolean overwriteUploads) {
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
}
