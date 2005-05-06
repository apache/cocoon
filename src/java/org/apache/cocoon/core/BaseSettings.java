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
import java.util.List;
import java.util.Map;

/**
 * This object holds the global configuration of Cocoon that can't be changed
 * during runtime.
 *
 * @version SVN $Id$
 */
public interface BaseSettings {

    /** Name of the property specifying a user properties file */
    String PROPERTY_USER_SETTINGS = "org.apache.cocoon.settings";

    /**
     * This parameter allows to set system properties
     */
    /** FIXME - implement the support for this key: */
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
     * If you want to configure log4j using Cocoon, then you can define
     * an XML configuration file here. You can use the usual log4j property
     * substituation mechanism, e.g. ${context-root} is replaced by the
     * context root of this web application etc.
     * You can configure the log4j configuration even if you use LogKit
     * for Cocoon logging. You can use this to configure third party code
     * for example.
     */
    String KEY_LOGGING_LOG4J_CONFIGURATION = "logging.log4j.configuration";

    /**
     * This parameter is used to list classes that should be loaded at
     * initialization time of the servlet. For example, JDBC Drivers used need to
     * be named here. Additional entries may be inserted here during build
     * depending on your build properties.
     */
    /** FIXME: Implement support for this: */
    String KEY_LOAD_CLASSES = "classloader.load.classes";

    /**
     * This parameter allows to specify additional directories or jars
     * which Cocoon should put into it's own classpath.
     * Note that absolute pathes are taken as such but relative pathes
     * are rooted at the context root of the Cocoon servlet.
     */
    /** FIXME: Implement support for this: */
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
     * @return Returns the configuration.
     */
    String getConfiguration();

    /**
     * @return Returns the extraClasspaths.
     */
    List getExtraClasspaths();

    /**
     * @return Returns the forceProperties.
     */
    Map getForceProperties();

    /**
     * @return Returns the loadClasses.
     */
    Iterator getLoadClasses();

    /**
     * @return Returns the loggerClassName.
     */
    String getLoggerClassName();

    /**
     * @return Returns the loggingConfiguration.
     */
    String getLoggingConfiguration();

    /**
     * @return Returns the logLevel.
     */
    String getBootstrapLogLevel();

    /**
     * @return Returns the parentServiceManagerClassName.
     */
    String getParentServiceManagerClassName();

    /**
     * @return Returns the log4jConfiguration.
     */
    String getLog4jConfiguration();

}
