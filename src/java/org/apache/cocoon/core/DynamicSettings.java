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


/**
 * This object holds the global configuration of Cocoon. The different settings can
 * be changed during runtime.
 *
 * @version SVN $Id$
 */
public interface DynamicSettings {

    /**
     * Default value for {@link #isAllowReload()} parameter (false)
     */
    boolean ALLOW_RELOAD = false;

    /**
     * Default value for {@link #isEnableUploads()} parameter (false)
     */
    boolean ENABLE_UPLOADS = false;
    boolean SAVE_UPLOADS_TO_DISK = true;
    int MAX_UPLOAD_SIZE = 10000000; // 10Mb

    /**
     * Default value for {@link #isInitClassloader()} setting (false)
     */
    boolean INIT_CLASSLOADER = false;

    boolean SHOW_TIME = false;
    boolean HIDE_SHOW_TIME = false;
    boolean MANAGE_EXCEPTIONS = true;

    /**
     * This parameter tells Cocoon to set the thread's context classloader to
     * its own classloader. If you experience strange classloader issues,
     * try setting this parameter to "true".
     */
    String KEY_INIT_CLASSLOADER = "classloader.init";

    /**
     * This parameter indicates the category id of the logger from the LogKit
     * configuration used by the environment.
     */
    String KEY_LOGGING_ENVIRONMENT_LOGGER = "logging.category.environment";

    /**
     * This parameter indicates the category id of the logger from the LogKit
     * management configuration for the Cocoon engine.
     * This logger is used for all components described in the cocoon.xconf
     * and sitemap.xmap file not having specified a logger with the
     * logger="..." attribute in the component configuration file.
     */
    String KEY_LOGGING_COCOON_LOGGER = "logging.category.cocoon";

    /**
     * Allow reinstantiating (reloading) of the cocoon instance. If this is
     * set to "yes" or "true", a new cocoon instance can be created using
     * the request parameter "cocoon-reload". It also enables that Cocoon is
     * reloaded when cocoon.xconf changes. Default is no for security reasons.
     */
    String KEY_ALLOW_RELOAD = "allow.reload";

    /**
     * Causes all files in multipart requests to be processed.
     * Default is false for security reasons.
     */
    String KEY_UPLOADS_ENABLE = "uploads.enable";

    /**
     * This parameter allows to specify where Cocoon should put uploaded files.
     * The path specified can be either absolute or relative to the context
     * path of the servlet. On windows platform, absolute directory must start
     * with volume: C:\Path\To\Upload\Directory.
     */
    String KEY_UPLOADS_DIRECTORY = "uploads.directory";

    /**
     * Causes all files in multipart requests to be saved to upload-dir.
     * Default is true for security reasons.
     */
    String KEY_UPLOADS_AUTOSAVE = "uploads.autosave";

    /**
     * Specify handling of name conflicts when saving uploaded files to disk.
     * Acceptable values are deny, allow, rename (default). Files are renamed
     * x_filename where x is an integer value incremented to make the new
     * filename unique.
     */
    String KEY_UPLOADS_OVERWRITE = "uploads.overwrite";

    /**
     * Specify maximum allowed size of the upload. Defaults to 10 Mb.
     */
    String KEY_UPLOADS_MAXSIZE = "uploads.maxsize";

    /**
     * This parameter allows to specify where Cocoon should create its page
     * and other objects cache. The path specified can be either absolute or
     * relative to the context path of the servlet. On windows platform,
     * absolute directory must start with volume: C:\Path\To\Cache\Directory.
     */
    String KEY_CACHE_DIRECTORY = "cache.directory";

    /**
     * This parameter allows to specify where Cocoon should put it's
     * working files. The path specified is either absolute or relative
     * to the context path of the Cocoon servlet. On windows platform,
     * absolute directory must start with volume: C:\Path\To\Work\Directory.
     */
    String KEY_WORK_DIRECTORY = "work.directory";

    /**
     * Allow adding processing time to the response
     */
    String KEY_SHOWTIME = "showtime";

    /**
     * If true, processing time will be added as an HTML comment
     */
    String KEY_HIDE_SHOWTIME = "hideshowtime";

    /**
     * If true or not set, this class will try to catch and handle all Cocoon exceptions.
     * If false, it will rethrow them to the servlet container.
     */
    String KEY_MANAGE_EXCEPTIONS = "manageexceptions";

    /**
     * Set form encoding. This will be the character set used to decode request
     * parameters. If not set the ISO-8859-1 encoding will be assumed.
    */
    String KEY_FORM_ENCODING = "formencoding";

    /**
     * If this value is specified, it will be interpreted as a log level and
     * all logging categories will be set to this level regardless of their
     * definition in the logging configuration.
     */
    String KEY_LOGGING_OVERRIDE_LOGLEVEL = "override.loglevel";

    /**
     * Delay between reload checks for the configuration
     */
    String KEY_CONFIGURATION_RELOAD_DELAY = "configuration.reloaddelay";

    /**
     * Lazy mode for component loading
     */
    String KEY_LAZY_MODE = "core.LazyMode";

    /**
     * @return Returns the hideShowTime.
     * @see #KEY_HIDE_SHOWTIME
     */
    boolean isHideShowTime();

    /**
     * @return Returns the allowReload.
     * @see #KEY_ALLOW_RELOAD
     */
    boolean isAllowReload();

    /**
     * @return Returns the autosaveUploads.
     * @see #KEY_UPLOADS_AUTOSAVE
     */
    boolean isAutosaveUploads();

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
     * @return Returns the enableUploads.
     * @see #KEY_UPLOADS_ENABLE
     */
    boolean isEnableUploads();

    /**
     * @return Returns the formEncoding.
     * @see #KEY_FORM_ENCODING
     */
    String getFormEncoding();

    /**
     * @return Returns the initClassloader.
     * @see #KEY_INIT_CLASSLOADER
     */
    boolean isInitClassloader();

    /**
     * @return Returns the manageExceptions.
     * @see #KEY_MANAGE_EXCEPTIONS
     */
    boolean isManageExceptions();

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
     * @return Returns the configurationReloadDelay.
     * @see #KEY_CONFIGURATION_RELOAD_DELAY
     */
    long getConfigurationReloadDelay();

    /**
     * @return Returns the lazyMode.
     * @see #KEY_LAZY_MODE
     */
    boolean isLazyMode();

}
