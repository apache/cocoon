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

import java.util.List;

/**
 * This object holds the global configuration of Cocoon.
 *
 * @version $Id$
 * @since 2.2
 */
public interface Settings {

    /** The role to lookup this bean. */
    String ROLE = Settings.class.getName();

    /** Name of the property specifying a custom user properties file. */
    String PROPERTY_USER_SETTINGS = "org.apache.cocoon.settings";

    /** Name of the property specifying the running mode. */
    String PROPERTY_RUNNING_MODE = "org.apache.cocoon.mode";

    /**
     * This parameter points to the main configuration file for Cocoon.
     * Note that the path is specified in absolute notation but it will be
     * resolved relative to the application context path.
     */
    String KEY_CONFIGURATION = "org.apache.cocoon.configuration";

    /**
     * This parameter indicates the configuration file of the LogKit management
     */
    String KEY_LOGGING_CONFIGURATION = "org.apache.cocoon.logging.configuration";

    /**
     * This parameter indicates the log level to use throughout startup of the
     * system. As soon as the logging system is setup the setting of the log4j.xconf
     * configuration is used instead! Only for startup this log level is of importance.
     */
    String KEY_LOGGING_BOOTSTRAP_LOGLEVEL = "org.apache.cocoon.logging.bootstrap.loglevel";

    /**
     * This parameter is used to list classes that should be loaded at
     * initialization time of the servlet. For example, JDBC Drivers used need to
     * be named here. Additional entries may be inserted here during build
     * depending on your build properties.
     */
    String KEY_LOAD_CLASSES = "org.apache.cocoon.classloader.load.classes";

    /**
     * This parameter indicates the category id of the logger from the LogKit
     * configuration used by the environment.
     */
    String KEY_LOGGING_ENVIRONMENT_LOGGER = "org.apache.cocoon.logging.category.environment";

    /**
     * This parameter indicates the category id of the logger from the LogKit
     * management configuration for the Cocoon engine.
     * This logger is used for all components described in the cocoon.xconf
     * and sitemap.xmap file not having specified a logger with the
     * logger="..." attribute in the component configuration file.
     */
    String KEY_LOGGING_COCOON_LOGGER = "org.apache.cocoon.logging.category.cocoon";

    /**
     * This parameter allows to specify where Cocoon should put uploaded files.
     * The path specified can be either absolute or relative to the context
     * path of the servlet. On windows platform, absolute directory must start
     * with volume: C:\Path\To\Upload\Directory.
     */
    String KEY_UPLOADS_DIRECTORY = "org.apache.cocoon.uploads.directory";

    /**
     * This parameter allows to specify where Cocoon should create its page
     * and other objects cache. The path specified can be either absolute or
     * relative to the context path of the servlet. On windows platform,
     * absolute directory must start with volume: C:\Path\To\Cache\Directory.
     */
    String KEY_CACHE_DIRECTORY = "org.apache.cocoon.cache.directory";

    /**
     * This parameter allows to specify where Cocoon should put it's
     * working files. The path specified is either absolute or relative
     * to the context path of the Cocoon servlet. On windows platform,
     * absolute directory must start with volume: C:\Path\To\Work\Directory.
     */
    String KEY_WORK_DIRECTORY = "org.apache.cocoon.work.directory";

    /**
     * If true or not set, this class will try to catch and handle all Cocoon exceptions.
     * If false, it will rethrow them to the servlet container.
     */
    String KEY_MANAGE_EXCEPTIONS = "org.apache.cocoon.manageexceptions";

    /**
     * Set form encoding. This will be the character set used to decode request
     * parameters. If not set the ISO-8859-1 encoding will be assumed.
    */
    String KEY_FORM_ENCODING = "org.apache.cocoon.formencoding";

    /**
     * Set encoding used by the container. If not set the ISO-8859-1 encoding
     * will be assumed.
     * Since the servlet specification requires that the ISO-8859-1 encoding
     * is used (by default), you should never change this value unless
     * you have a buggy servlet container.
     */
    String KEY_CONTAINER_ENCODING = "org.apache.cocoon.containerencoding";

    /**
     * If this value is specified, it will be interpreted as a log level and
     * all logging categories will be set to this level regardless of their
     * definition in the logging configuration.
     */
    String KEY_LOGGING_OVERRIDE_LOGLEVEL = "org.apache.cocoon.override.loglevel";

    /**
     * Allow reinstantiating (reloading) of the cocoon instance. If this is
     * set to "yes" or "true", a new cocoon instance can be created using
     * the request parameter "cocoon-reload". It also enables that Cocoon is
     * reloaded when cocoon.xconf changes. Default is no for security reasons.
     */
    String KEY_RELOADING = "org.apache.cocoon.reloading";

    /**
     * Causes all files in multipart requests to be processed.
     * Default is false for security reasons.
     */
    String KEY_UPLOADS_ENABLE = "org.apache.cocoon.uploads.enable";

    /**
     * Causes all files in multipart requests to be saved to upload-dir.
     * Default is true for security reasons.
     */
    String KEY_UPLOADS_AUTOSAVE = "org.apache.cocoon.uploads.autosave";

    /**
     * Specify handling of name conflicts when saving uploaded files to disk.
     * Acceptable values are deny, allow, rename (default). Files are renamed
     * x_filename where x is an integer value incremented to make the new
     * filename unique.
     */
    String KEY_UPLOADS_OVERWRITE = "org.apache.cocoon.uploads.overwrite";

    /**
     * Specify maximum allowed size of the upload. Defaults to 10 Mb.
     */
    String KEY_UPLOADS_MAXSIZE = "org.apache.cocoon.uploads.maxsize";

    /**
     * Allow adding processing time to the response
     */
    String KEY_SHOWTIME = "org.apache.cocoon.showtime";

    /**
     * If true, processing time will be added as an HTML comment
     */
    String KEY_HIDE_SHOWTIME = "org.apache.cocoon.hideshowtime";

    /**
     * If true, the X-Cocoon-Version response header will be included.
     */
    String KEY_SHOW_VERSION = "org.apache.cocoon.show-version";

    /**
     * Delay between reload checks for the configuration
     */
    String KEY_RELOAD_DELAY = "org.apache.cocoon.reload-delay";

    /**
     * Get the value of a property.
     * @param key The name of the property.
     * @return The value of the property or null.
     */
    String getProperty(String key);

    /**
     * Get the value of a property.
     * @param key The name of the property.
     * @param defaultValue The value returned if the property is not available.
     * @return The value of the property or if the property cannot
     *         be found the default value.
     */
    String getProperty(String key, String defaultValue);

    /**
     * Return all available property names starting with the prefix.
     * @param keyPrefix The prefix each property name must have.
     * @return A list of property names (including the prefix) or
     *         an empty list.
     */
    List getPropertyNames(String keyPrefix);
    
    /**
     * Return all available property names.
     * @return A list of all property names or an empty list.
     */
    List getPropertyNames();

    /**
     * @return Returns the configuration.
     * @see #KEY_CONFIGURATION
     */
    String getConfiguration();

    /**
     * @return Returns the loadClasses.
     * @see #KEY_LOAD_CLASSES
     */
    List getLoadClasses();

    /**
     * @return Returns the loggingConfiguration.
     * @see #KEY_LOGGING_CONFIGURATION
     */
    String getLoggingConfiguration();

    /**
     * @return Returns the logLevel.
     * @see #KEY_LOGGING_BOOTSTRAP_LOGLEVEL
     */
    String getBootstrapLogLevel();

    /**
     * @return Returns the uploadDirectory.
     * @see #KEY_UPLOADS_DIRECTORY
     */
    String getUploadDirectory();

    /**
     * @return Returns the workDirectory.
     * @see #KEY_WORK_DIRECTORY
     */
    String getWorkDirectory();

    /**
     * @return Returns the logger for the environment.
     * @see #KEY_LOGGING_ENVIRONMENT_LOGGER
     */
    String getEnvironmentLogger();

    /**
     * @return Returns the overrideLogLevel.
     * @see #KEY_LOGGING_OVERRIDE_LOGLEVEL
     */
    String getOverrideLogLevel();

    /**
     * @return Returns the formEncoding.
     * @see #KEY_FORM_ENCODING
     */
    String getFormEncoding();

    /**
     * @return Returns the container encoding
     * @see #KEY_CONTAINER_ENCODING
     */
    String getContainerEncoding();

    /**
     * @return Returns the manageExceptions.
     * @see #KEY_MANAGE_EXCEPTIONS
     */
    boolean isManageExceptions();

    /**
     * @return Returns the cacheDirectory.
     * @see #KEY_CACHE_DIRECTORY
     */
    String getCacheDirectory();

    /**
     * @return Returns the cocoonLogger.
     * @see #KEY_LOGGING_COCOON_LOGGER
     */
    String getCocoonLogger();

    /**
     * The creation time of the current Cocoon instance.
     */
    long getCreationTime();

    /**
     * @return Returns the hideShowTime.
     * @see #KEY_HIDE_SHOWTIME
     */
    boolean isHideShowTime();

    /**
     * @return Returns the showCocoonVersion.
     * @see #KEY_SHOW_VERSION
     */
    boolean isShowVersion();

    /**
     * This method can be used by components to query if they are
     * configured to check for reloading.
     * @param type The type of the component that wants to check for reload.
     * @return Returns if reloading is enabled for this component.
     * @see #KEY_RELOADING
     */
    boolean isReloadingEnabled(String type);

    /**
     * This method can be used by components to get the configured
     * delay period inbetween checks.
     * @param type The type of the component that wants to check for reload.
     * @return Returns the delay inbetween checks in milliseconds.
     * @see #KEY_RELOAD_DELAY
     */
    long getReloadDelay(String type);

    /**
     * @return Returns the autosaveUploads.
     * @see #KEY_UPLOADS_AUTOSAVE
     */
    boolean isAutosaveUploads();

    /**
     * @return Returns the enableUploads.
     * @see #KEY_UPLOADS_ENABLE
     */
    boolean isEnableUploads();

    /**
     * @return Returns the maxUploadSize.
     * @see #KEY_UPLOADS_MAXSIZE
     */
    int getMaxUploadSize();

    /**
     * @return Returns the overwriteUploads.
     * @see #KEY_UPLOADS_OVERWRITE
     */
    String getOverwriteUploads();

    /**
     * @return Returns the showTime.
     * @see #KEY_SHOWTIME
     */
    boolean isShowTime();

    boolean isAllowOverwrite();

    boolean isSilentlyRename();

    /**
     * Return the current running mode.
     */
    String getRunningMode();
}
