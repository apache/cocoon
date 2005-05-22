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

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.math.NumberUtils;

/**
 * This object holds the global configuration of Cocoon.
 *
 * @version SVN $Id$
 */
public class MutableSettings extends Settings {

    /** Are we still mutable? */
    protected boolean readOnly = false;

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

                    if ( key.equals(KEY_INIT_CLASSLOADER) ) {
                        this.initClassloader = BooleanUtils.toBoolean(value);
                    } else if ( key.equals(KEY_CONFIGURATION) ) {
                        this.configuration = value;
                    } else if ( key.equals(KEY_CONFIGURATION_RELOAD_DELAY) ) {
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
     * @param hideShowTime The hideShowTime to set.
     */
    public void setHideShowTime(boolean hideShowTime) {
        this.checkWriteable();
        this.hideShowTime = hideShowTime;
    }

    /**
     * @param allowReload The allowReload to set.
     */
    public void setAllowReload(boolean allowReload) {
        this.checkWriteable();
        this.allowReload = allowReload;
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
     * @param initClassloader The initClassloader to set.
     */
    public void setInitClassloader(boolean initClassloader) {
        this.checkWriteable();
        this.initClassloader = initClassloader;
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
     * @param parentServiceManagerClassName The parentServiceManagerClassName to set.
     */
    public void setParentServiceManagerClassName(
            String parentServiceManagerClassName) {
        this.checkWriteable();
        this.parentServiceManagerClassName = parentServiceManagerClassName;
    }

    /**
     * @param showTime The showTime to set.
     */
    public void setShowTime(boolean showTime) {
        this.checkWriteable();
        this.showTime = showTime;
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
     * @param log4jConfiguration The log4jConfiguration to set.
     */
    public void setLog4jConfiguration(String log4jConfiguration) {
        this.checkWriteable();
        this.log4jConfiguration = log4jConfiguration;
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

}
