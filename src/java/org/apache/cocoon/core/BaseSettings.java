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

import java.util.List;
import java.util.Map;

/**
 * The settings (configuration) for the Cocoon core are described through the {@link BaseSettings}
 * interface and the {@link DynamicSettings} interface.
 * Whereas the settings of the {@link BaseSettings} object can't be changed at runtime,
 * the settings of the {@link DynamicSettings} object are mutable. Use the {@link Core} instance
 * to update the settings.
 *
 * @version SVN $Id$
 */
public interface BaseSettings {

    /** Default value for {@link #isManageExceptions()}. */
    boolean MANAGE_EXCEPTIONS = true;

    /**
     * Default value for {@link #isInitClassloader()} setting (false)
     */
    boolean INIT_CLASSLOADER = false;
    
    /** Name of the property specifying a user properties file */
    String PROPERTY_USER_SETTINGS = "org.apache.cocoon.settings";

    /**
     * This parameter allows to set system properties
     */
    String KEY_FORCE_PROPERTIES = "system.properties";

    /**
     * This parameter points to the main configuration file for Cocoon.
     * Note that the path is specified in absolute notation but it will be
     * resolved relative to the application context path.
     */
    String KEY_CONFIGURATION = "configuration";

    /**
     * This parameter indicates the configuration file of the LogKit management
     */
    String KEY_LOGGING_CONFIGURATION = "logging.configuration";

    /**
     * This parameter indicates the log level to use throughout startup of the
     * system. As soon as the logkit.xconf the setting of the logkit.xconf
     * configuration is used instead! Only for startup and if the logkit.xconf is
     * not readable/available this log level is of importance.
     */
    String KEY_LOGGING_BOOTSTRAP_LOGLEVEL = "logging.bootstrap.loglevel";

    /**
     * This parameter switches the logging system from LogKit to Log4J for Cocoon.
     * Log4J has to be configured already.
     */
    String KEY_LOGGING_MANAGER_CLASS = "logging.manager.class";

    /**
     * This parameter is used to list classes that should be loaded at
     * initialization time of the servlet. For example, JDBC Drivers used need to
     * be named here. Additional entries may be inserted here during build
     * depending on your build properties.
     */
    String KEY_LOAD_CLASSES = "classloader.load.classes";

    /**
     * This parameter allows to specify additional directories or jars
     * which Cocoon should put into it's own classpath.
     * Note that absolute pathes are taken as such but relative pathes
     * are rooted at the context root of the Cocoon servlet.
     */
    String KEY_EXTRA_CLASSPATHS = "extra.classpaths";

    /**
     * This parameter allows you to select the parent service manager.
     * The class will be instantiated via the constructor that takes a single
     * String as a parameter. That String will be equal to the text after the '/'.
     *
     * Cocoon honors the LogEnabled, Initializable and Disposable interfaces for
     * this class, if it implements them.
     */
    String KEY_PARENT_SERVICE_MANAGER = "parentservicemanager";

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
     * This parameter allows to specify where Cocoon should put uploaded files.
     * The path specified can be either absolute or relative to the context
     * path of the servlet. On windows platform, absolute directory must start
     * with volume: C:\Path\To\Upload\Directory.
     */
    String KEY_UPLOADS_DIRECTORY = "uploads.directory";

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
     * This key allows to add own {@link PropertyProvider}s.
     */
    String KEY_PROPERTY_PROVIDER = "property.provider";

    /**
     * @return Returns the configuration.
     * @see #KEY_CONFIGURATION
     */
    String getConfiguration();

    /**
     * @return Returns the extraClasspaths.
     * @see #KEY_EXTRA_CLASSPATHS
     */
    List getExtraClasspaths();

    /**
     * @return Returns the forceProperties.
     * @see #KEY_FORCE_PROPERTIES
     */
    Map getForceProperties();

    /**
     * @return Returns the loadClasses.
     * @see #KEY_LOAD_CLASSES
     */
    List getLoadClasses();

    /**
     * @return Returns the loggerManagerClassName.
     * @see #KEY_LOGGING_MANAGER_CLASS
     */
    String getLoggerManagerClassName();

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
     * @return Returns the parentServiceManagerClassName.
     * @see #KEY_PARENT_SERVICE_MANAGER
     */
    String getParentServiceManagerClassName();

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
     * @return All property providers.
     * @see #KEY_PROPERTY_PROVIDER
     */
    List getPropertyProviders();
}
