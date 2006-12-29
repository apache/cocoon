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
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.spring.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import org.apache.cocoon.configuration.MutableSettings;
import org.apache.cocoon.configuration.PropertyHelper;
import org.apache.cocoon.configuration.PropertyProvider;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.configuration.SettingsDefaults;
import org.apache.cocoon.spring.ResourceUtils;

/**
 * This is a bean factory post processor which handles all the settings stuff
 * for Cocoon. It reads in all properties files and replaces references to
 * them in the spring configuration files.
 * In addition this bean acts as a factory bean providing the settings object.
 *
 * @since 1.0
 * @version $Id$
 */
public class SettingsBeanFactoryPostProcessor
    extends AbstractSettingsBeanFactoryPostProcessor {

    protected String runningMode = SettingsDefaults.DEFAULT_RUNNING_MODE;

    public void setRunningMode(String runningMode) {
        this.runningMode = runningMode;
    }

    /**
     * Initialize this processor.
     * Setup the settings object.
     * @throws Exception
     */
    public void init()
    throws Exception {
        this.settings = this.createSettings();

        this.doInit();

        this.initSettingsFiles();
        // settings can't be changed anymore
        this.settings.makeReadOnly();

        this.dumpSystemProperties();
        this.dumpSettings();
        this.forceLoad();
    }

    /**
     * Init work, upload and cache directory
     */
    protected void initSettingsFiles() {
        // first init the work-directory for the logger.
        // this is required if we are running inside a war file!
        final String workDirParam = settings.getWorkDirectory();
        File workDir;
        if (workDirParam != null) {
            // No context path : consider work-directory as absolute
            workDir = new File(workDirParam);
        } else {
            workDir = new File("cocoon-files");
        }
        workDir.mkdirs();
        settings.setWorkDirectory(workDir.getAbsolutePath());

        // Output some debug info
        if (this.logger.isDebugEnabled()) {
            if (workDirParam != null) {
                this.logger.debug("Using work-directory " + workDir);
            } else {
                this.logger.debug("Using default work-directory " + workDir);
            }
        }

        String cacheDirParam = settings.getCacheDirectory();
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
        settings.setCacheDirectory(cacheDir.getAbsolutePath());
    }

    /**
     * Get the settings for Cocoon.
     * This method reads several property files and merges the result. If there
     * is more than one definition for a property, the last one wins.
     * The property files are read in the following order:
     * 1) classpath*:/META-INF/cocoon/properties/*.properties
     *    Default values for the core and each block - the files are read in alphabetical order.
     *    Actually the files are read in two chunks, the first one containing all property files
     *    from jar files, and the second one containing all property files from WEB-INF/classes.
     * 2) classpath*:/META-INF/cocoon/properties/[RUNNING_MODE]/*.properties
     *    Default values for the core and each block for a specific running mode - the files are
     *    read in alphabetical order.
     *    Actually the files are read in two chunks, the first one containing all property files
     *    from jar files, and the second one containing all property files from WEB-INF/classes.
     * 3) Working directory from servlet context (if not already set)
     * 4) Optional property file which is stored under ".cocoon/settings.properties" in the user
     *    directory.
     * 5) Additional property file specified by the "org.apache.cocoon.settings" property. If the
     *    property defines a directory, all property files from this directory are read in alphabetical
     *    order and all files from a sub directory with the name of the current running mode
     *    are read in alphabetical order as well.
     * 6) Property provider (if configured in the bean factory)
     * 7) System properties
     *
     * This means that system properties (provided on startup of the web application) override all
     * others etc.
     *
     * @return A new Settings object
     */
    protected MutableSettings createSettings() {
        // get the running mode
        final String mode = RunningModeHelper.determineRunningMode( this.runningMode );

        /*
        if ( !Arrays.asList(SettingsDefaults.RUNNING_MODES).contains(mode) ) {
            final String msg =
                "Invalid running mode: " + mode + " - Use one of: " + Arrays.asList(SettingsDefaults.RUNNING_MODES);
            throw new IllegalArgumentException(msg);
        }
        */
        this.servletContext.log("Apache Cocoon is running in mode: " + mode);

        // create an empty settings objects
        final MutableSettings s = new MutableSettings(mode);
        // create an empty properties object
        final Properties properties = new Properties();

        // now read all properties from the properties directory
        ResourceUtils.readProperties(org.apache.cocoon.spring.impl.Constants.DEFAULT_CLASSPATH_PROPERTIES_LOCATION,
                properties, this.getResourceLoader(), this.logger);
        // read all properties from the mode dependent directory
        ResourceUtils.readProperties(org.apache.cocoon.spring.impl.Constants.DEFAULT_CLASSPATH_PROPERTIES_LOCATION
                + "/" + mode, properties, this.getResourceLoader(), this.logger);

        // fill from the servlet context
        if ( s.getWorkDirectory() == null ) {
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
        // check for property providers
        if (this.beanFactory != null && this.beanFactory.containsBean(PropertyProvider.ROLE) ) {
            try {
                final PropertyProvider provider = (PropertyProvider)this.beanFactory.getBean(PropertyProvider.ROLE);
                final Properties providedProperties = provider.getProperties(s, mode, null);
                if ( providedProperties != null ) {
                    properties.putAll(providedProperties);
                }
            } catch (Exception ignore) {
                this.logger.warn("Unable to get properties from provider.", ignore);
                this.logger.warn("Continuing initialization.");            
            }
        }
        
        if ( this.additionalProperties != null ) {
            PropertyHelper.replaceAll(this.additionalProperties, s);
            properties.putAll(this.additionalProperties);
        }

        // now overwrite with system properties
        try {
            properties.putAll(System.getProperties());
        } catch (SecurityException se) {
            // we ignore this
        }
        PropertyHelper.replaceAll(properties, null);
        s.configure(properties);

        return s;
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
