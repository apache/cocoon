/* 
 * Copyright 2006 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at 
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
package org.apache.cocoon.configuration.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletContext;

import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.configuration.PropertyProvider;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.configuration.SettingsDefaults;
import org.apache.cocoon.core.container.spring.BeanFactoryUtil;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;
import org.springframework.beans.factory.BeanFactory;

/**
 * Helper class creating a settings object
 *
 * @version $Id$
 * @since 2.2
 */
public class SettingsHelper {

    /** Parameter map for the context protocol. */
    protected static final Map CONTEXT_PARAMETERS = Collections.singletonMap("force-traversable", Boolean.TRUE);

    /**
     * Get the settings for Cocoon.
     * This method reads several property files and merges the result. If there
     * is more than one definition for a property, the last one wins.
     * The property files are read in the following order:
     * 1) context://WEB-INF/cocoon/properties/*.properties
     *    Default values for the core and each block - the order in which the files are read is not guaranteed.
     * 2) context://WEB-INF/cocoon/properties/[RUNNING_MODE]/*.properties
     *    Default values for the running mode - the order in which the files are read is not guaranteed.
     * 3) Property providers (ToBeDocumented)
     * 4) The environment (CLI, Servlet etc.) adds own properties (e.g. from web.xml)
     * 5) Optional property file which is stored under ".cocoon/settings.properties" in the user
     *    directory.
     * 6) Additional property file specified by the "org.apache.cocoon.settings" property.
     * 7) System properties
     *
     * This means that system properties (provided on startup of the web application) override all
     * others etc.
     *
     * @return A new Settings object
     */
    public static MutableSettings createSettings(final ServletContext   servletContext,
                                                 final SourceResolver   resolver,
                                                 final Logger           logger,
                                                 final PropertyProvider externalPropertyProvider) {
        // get the running mode
        final String mode = SettingsHelper.getSystemProperty(Settings.PROPERTY_RUNNING_MODE, SettingsDefaults.DEFAULT_RUNNING_MODE);
        logger.info("Running in mode: " + mode);

        // create an empty settings objects
        final MutableSettings s = new MutableSettings();

        // now read all properties from the properties directory
        readProperties("context://WEB-INF/cocoon/properties", s, resolver, logger);
        // read all properties from the mode dependent directory
        readProperties("context://WEB-INF/cocoon/properties/" + mode, s, resolver, logger);

        // Next look for a custom property provider in the spring root context
        BeanFactory rootContext = BeanFactoryUtil.getWebApplicationContext(servletContext);
        if (rootContext != null && rootContext.containsBean(PropertyProvider.ROLE) ) {
            try {
                PropertyProvider provider = (PropertyProvider)rootContext.getBean(PropertyProvider.ROLE);
                s.fill(provider.getProperties(s, mode, null));
            } catch (Exception ignore) {
                logger.info("Unable to get properties from configured property provider - continuing with initialization.");
                logger.debug("Unable to get properties from provider.", ignore);
            }
        }
        // fill from the environment configuration, like web.xml etc.
        if ( externalPropertyProvider != null ) {
            s.fill(externalPropertyProvider.getProperties(s, mode, null));
        }

        // read additional properties file
        // first try in home directory
        final String homeDir = SettingsHelper.getSystemProperty("user.home");
        if ( homeDir != null ) {
            final String fileName = homeDir + File.separator + ".cocoon" + File.separator + "settings.properties";
            final File testFile = new File(fileName);
            if ( testFile.exists() ) {
                logger.info("Reading user settings from '" + fileName + "'");
                try {
                    final Properties p = new Properties();
                    FileInputStream fis = new FileInputStream(fileName);
                    p.load(fis);
                    fis.close();
                    s.fill(p);
                } catch (IOException ignore) {
                    logger.info("Unable to read '" + fileName + "' - continuing with initialization.");
                    logger.debug("Unable to read '" + fileName + "'.", ignore);
                }
            }
        }
        // check for additionally specified custom file        
        String additionalPropertyFile = s.getProperty(Settings.PROPERTY_USER_SETTINGS, 
                                                      SettingsHelper.getSystemProperty(Settings.PROPERTY_USER_SETTINGS));
        if ( additionalPropertyFile != null ) {
            logger.info("Reading user settings from '" + additionalPropertyFile + "'");
            try {
                final Properties p = new Properties();
                FileInputStream fis = new FileInputStream(additionalPropertyFile);
                p.load(fis);
                fis.close();
                s.fill(p);
            } catch (IOException ignore) {
                logger.info("Unable to read '" + additionalPropertyFile + "' - continuing with initialization.");
                logger.debug("Unable to read '" + additionalPropertyFile + "'.", ignore);
            }
        }
        // now overwrite with system properties
        try {
            s.fill(System.getProperties());
        } catch (SecurityException se) {
            // we ignore this
        }

        return s;
    }

    /**
     * Read all property files from the given directory and apply them to the settings.
     */
    protected static void readProperties(String          directoryName,
                                         MutableSettings s,
                                         SourceResolver  resolver,
                                         Logger  logger) {
        Source directory = null;
        try {
            directory = resolver.resolveURI(directoryName, null, CONTEXT_PARAMETERS);
            if (directory.exists() && directory instanceof TraversableSource) {
                final List propertyUris = new ArrayList();
                final Iterator c = ((TraversableSource) directory).getChildren().iterator();
                while (c.hasNext()) {
                    final Source src = (Source) c.next();
                    if ( src.getURI().endsWith(".properties") ) {
                        propertyUris.add(src);
                    }
                }
                // sort
                Collections.sort(propertyUris, getSourceComparator());
                // now process
                final Iterator i = propertyUris.iterator();
                while ( i.hasNext() ) {
                    final Source src = (Source)i.next();
                    final InputStream propsIS = src.getInputStream();
                    logger.info("Reading settings from '" + src.getURI() + "'.");
                    final Properties p = new Properties();
                    p.load(propsIS);
                    propsIS.close();
                    s.fill(p);
                }
            }
        } catch (IOException ignore) {
            logger.info("Unable to read properties from directory '" + directoryName + "' - Continuing initialization.");
            logger.debug("Unable to read properties from directory '" + directoryName + "'.", ignore);
        } finally {
            resolver.release(directory);
        }
    }

    /**
     * Return a source comparator
     */
    public static Comparator getSourceComparator() {
        return new SourceComparator();
    }

    protected static String getSystemProperty(String key) {
        return SettingsHelper.getSystemProperty(key, null);
    }

    protected static String getSystemProperty(String key, String defaultValue) {
        try {
            return System.getProperty(key, defaultValue);
        } catch (SecurityException se) {
            // we ignore this
            return defaultValue;
        }
    }

    protected final static class SourceComparator implements Comparator {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2) {
            if ( !(o1 instanceof Source) || !(o2 instanceof Source)) {
                return 0;
            }
            return ((Source)o1).getURI().compareTo(((Source)o2).getURI());
        }
    }
}
