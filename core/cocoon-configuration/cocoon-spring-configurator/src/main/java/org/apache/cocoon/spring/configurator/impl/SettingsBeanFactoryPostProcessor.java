/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.spring.configurator.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import org.apache.cocoon.configuration.MutableSettings;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.configuration.SettingsDefaults;
import org.apache.cocoon.spring.configurator.ResourceUtils;

/**
 * This is a bean factory post processor which handles all the settings stuff
 * for Cocoon. It reads in all properties files and replaces references to
 * them in the spring configuration files.
 * In addition this bean acts as a factory bean providing the settings object.
 *
 * The settings object is created by reading several property files and merging of
 * the values. If there is more than one definition for a property, the last one wins.
 * The property files are read in the following order:
 * 1) If {@link #readFromClasspath} is true: classpath*:/META-INF/cocoon/properties/*.properties
 *    Default values for the core and each block - the files are read in alphabetical order.
 *    Actually the files are read in two chunks, the first one containing all property files
 *    from jar files, and the second one containing all property files from WEB-INF/classes.
 * 2) If {@link #readFromClasspath} is true: classpath*:/META-INF/cocoon/properties/[RUNNING_MODE]/*.properties
 *    Default values for the core and each block for a specific running mode - the files are
 *    read in alphabetical order.
 *    Actually the files are read in two chunks, the first one containing all property files
 *    from jar files, and the second one containing all property files from WEB-INF/classes.
 * 3) If {@link #readFromGlobalLocation} is true: /WEB-INF/cocoon/properties/*.properties
 *    Default values for the core and each block - the files are read in alphabetical order.
 *    Actually the files are read in two chunks, the first one containing all property files
 *    from jar files, and the second one containing all property files from WEB-INF/classes.
 * 4) If {@link #readFromGlobalLocation} is true: /WEB-INF/cocoon/properties/[RUNNING_MODE]/*.properties
 *    Default values for the core and each block for a specific running mode - the files are
 *    read in alphabetical order.
 *    Actually the files are read in two chunks, the first one containing all property files
 *    from jar files, and the second one containing all property files from WEB-INF/classes.
 * 5) Working directory from servlet context (if not already set)
 * 6) Optional property file which is stored under ".cocoon/settings.properties" in the user
 *    directory.
 * 7) Additional property file specified by the "org.apache.cocoon.settings" property. If the
 *    property defines a directory, all property files from this directory are read in alphabetical
 *    order and all files from a sub directory with the name of the current running mode
 *    are read in alphabetical order as well.
 * 8) Property provider (if configured in the bean factory)
 * 9) Add properties from configured directories {@link #directories}.
 * 10) Add additional properties configured at {@link #additionalProperties}
 * 11) System properties
 *
 * This means that system properties (provided on startup of the web application) override all
 * others etc.
 *
 * @since 1.0
 * @version $Id$
 */
public class SettingsBeanFactoryPostProcessor
    extends AbstractSettingsBeanFactoryPostProcessor {

    /**
     * The running mode for the web application.
     */
    protected String runningMode = SettingsDefaults.DEFAULT_RUNNING_MODE;

    /**
     * Should we read the properties from the classpath?
     * @see Constants#CLASSPATH_PROPERTIES_LOCATION
     */
    protected boolean readFromClasspath = true;

    /**
     * Should we read the properties from the global location?
     * @see Constants#GLOBAL_PROPERTIES_LOCATION
     */
    protected boolean readFromGlobalLocation = true;

    /**
     * Set the running mode.
     */
    public void setRunningMode(String runningMode) {
        this.runningMode = runningMode;
    }

    /**
     * Set if we read property configurations from the classpath.
     * @param readFromClasspath
     */
    public void setReadFromClasspath(boolean readFromClasspath) {
        this.readFromClasspath = readFromClasspath;
    }

    /**
     * Set if we read property configurations from the global location.
     */
    public void setReadFromGlobalLocation(boolean readFromGlobalLocation) {
        this.readFromGlobalLocation = readFromGlobalLocation;
    }

    /**
     * Initialize this processor.
     * Setup the settings object.
     * @throws Exception
     */
    public void init()
    throws Exception {
        // get the running mode
        final String mode = this.getRunningMode();
        RunningModeHelper.checkRunningMode(mode);

        // print out version information
        final Properties pomProps = ResourceUtils.getPOMProperties("org.apache.cocoon", "cocoon-spring-configurator");
        final String version;
        if ( pomProps != null ) {
            version = pomProps.getProperty("version");
        } else {
            version = null;
        }
        // give a startup message
        final String msg = "Apache Cocoon Spring Configurator " +
                           (version != null ? "v" + version + " " : "") +
                           "is running in mode '" + mode + "'.";
        if ( this.servletContext != null ) {
            this.servletContext.log(msg);
        } else {
            this.logger.info(msg);
        }

        // first we dump the system properties
        this.dumpSystemProperties();
        // now create the settings object
        super.init();

        // finally pre load classes
        this.forceLoad();
    }

    /**
     * @see org.apache.cocoon.spring.configurator.impl.AbstractSettingsBeanFactoryPostProcessor#getRunningMode()
     */
    protected String getRunningMode() {
        return RunningModeHelper.determineRunningMode( this.runningMode );
    }

    protected void preInit(final MutableSettings s, final Properties properties) {
        final String mode = this.getRunningMode();
        if ( this.readFromClasspath ) {
            // now read all properties from classpath directory
            ResourceUtils.readProperties(Constants.CLASSPATH_PROPERTIES_LOCATION,
                    properties, this.getResourceLoader(), this.logger);
            // read all properties from the mode dependent directory
            ResourceUtils.readProperties(Constants.CLASSPATH_PROPERTIES_LOCATION
                    + "/" + mode, properties, this.getResourceLoader(), this.logger);
        }

        if ( this.readFromGlobalLocation ) {
            // now read all properties from the properties directory
            ResourceUtils.readProperties(Constants.GLOBAL_PROPERTIES_LOCATION,
                    properties, this.getResourceLoader(), this.logger);
            // read all properties from the mode dependent directory
            ResourceUtils.readProperties(Constants.GLOBAL_PROPERTIES_LOCATION
                    + "/" + mode, properties, this.getResourceLoader(), this.logger);
        }

        // fill from the servlet context
        if ( this.servletContext != null && s.getWorkDirectory() == null ) {
            final File workDir = (File)this.servletContext.getAttribute("javax.servlet.context.tempdir");
            s.setWorkDirectory(workDir.getAbsolutePath());
        }

        // read additional properties file
        // first try in home directory
        final String homeDir = getSystemProperty("user.home");
        if ( homeDir != null ) {
            final String fileName = homeDir + File.separator + ".cocoon" + File.separator + "settings.properties";
            final File testFile = new File(fileName);
            if ( testFile.exists() ) {
                if ( this.logger.isDebugEnabled() ) {
                    this.logger.debug("Reading user settings from '" + fileName + "'");
                }
                try {
                    final FileInputStream fis = new FileInputStream(fileName);
                    properties.load(fis);
                } catch (IOException ignore) {
                    this.logger.info("Unable to read '" + fileName + "' - continuing with initialization.", ignore);
                }
            }
        }
        // check for additionally specified custom file
        String additionalPropertyFile = s.getProperty(Settings.PROPERTY_USER_SETTINGS,
                                                      getSystemProperty(Settings.PROPERTY_USER_SETTINGS));
        if ( additionalPropertyFile != null ) {
            if ( this.logger.isDebugEnabled() ) {
                this.logger.debug("Reading user settings from '" + additionalPropertyFile + "'");
            }
            final File additionalFile = new File(additionalPropertyFile);
            if ( additionalFile.exists() ) {
                if ( additionalFile.isDirectory() ) {
                    // read from directory
                    ResourceUtils.readProperties(additionalFile.getAbsolutePath(),
                            properties, this.getResourceLoader(), this.logger);
                    // read all properties from the mode dependent directory
                    ResourceUtils.readProperties(additionalFile.getAbsolutePath() + File.separatorChar + mode,
                                                 properties, this.getResourceLoader(), this.logger);
                } else {
                    // read the file
                    try {
                        final FileInputStream fis = new FileInputStream(additionalFile);
                        properties.load(fis);
                        fis.close();
                    } catch (IOException ignore) {
                        this.logger.info("Unable to read '" + additionalPropertyFile + "' - continuing with initialization.", ignore);
                    }
                }
            } else {
                this.logger.info("Additional settings file '" + additionalPropertyFile + "' does not exist - continuing with initialization.");
            }
        }
    }

    /**
     * Init work, upload and cache directory
     */
    protected void postInit(final MutableSettings s, final Properties properties) {
        // first init the work-directory for the logger.
        // this is required if we are running inside a war file!
        final String workDirParam = s.getWorkDirectory();
        File workDir;
        if (workDirParam != null) {
            // No context path : consider work-directory as absolute
            workDir = new File(workDirParam);
        } else {
            workDir = new File("cocoon-files");
        }
        workDir.mkdirs();
        s.setWorkDirectory(workDir.getAbsolutePath());

        // Output some debug info
        if (this.logger.isDebugEnabled()) {
            if (workDirParam != null) {
                this.logger.debug("Using work-directory " + workDir);
            } else {
                this.logger.debug("Using default work-directory " + workDir);
            }
        }

        String cacheDirParam = s.getCacheDirectory();
        File cacheDir;
        if (cacheDirParam != null) {
            cacheDir = new File(cacheDirParam);
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Using cache-directory " + cacheDir);
            }
        } else {
            cacheDir = new File(workDir, "cache-dir" + File.separator);
            File parent = cacheDir.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("cache-directory was not set - defaulting to " + cacheDir);
            }
        }
        cacheDir.mkdirs();
        s.setCacheDirectory(cacheDir.getAbsolutePath());
    }

    /**
     * Dump System Properties.
     */
    protected void dumpSystemProperties() {
        if (this.logger.isDebugEnabled()) {
            try {
                Enumeration e = System.getProperties().propertyNames();
                this.logger.debug("===== System Properties Start =====");
                while (e.hasMoreElements()) {
                    String key = (String) e.nextElement();
                    this.logger.debug(key + "=" + System.getProperty(key));
                }
                this.logger.debug("===== System Properties End =====");
            } catch (SecurityException se) {
                // Ignore Exceptions.
            }
        }
    }

    /**
     * Handle the <code>load-class</code> settings. This overcomes
     * limits in many classpath issues. One of the more notorious
     * ones is a bug in WebSphere that does not load the URL handler
     * for the <code>classloader://</code> protocol. In order to
     * overcome that bug, set <code>org.apache.cocoon.classloader.load.classes.XY</code> property to
     * the <code>com.ibm.servlet.classloader.Handler</code> value.
     *
     * <p>If you need to load more than one class, then add several
     * properties, all starting with <cod>org.apache.cocoon.classloader.load.classes.</code>
     * followed by a self defined identifier.</p>
     */
    protected void forceLoad() {
        final Iterator i = this.settings.getLoadClasses().iterator();
        while (i.hasNext()) {
            final String fqcn = (String)i.next();
            try {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("Loading class: " + fqcn);
                }
                Thread.currentThread().getContextClassLoader().loadClass(fqcn).newInstance();
            } catch (Exception e) {
                if (this.logger.isWarnEnabled()) {
                    this.logger.warn("Could not load class: " + fqcn + ". Continuing initialization.", e);
                }
                // Do not throw an exception, because it is not a fatal error.
            }
        }
    }
}
