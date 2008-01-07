/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.util;

import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.TraversableSource;
import org.apache.avalon.framework.logger.Logger;


import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Iterator;
import java.util.Map;
import java.util.Collections;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This object holds the property settings for Cocoon. This interface is loosely based on the Settings interface
 * introduced in 2.2 but is note equivalent to it as it is only meant to hold configuration properties.
 *
 * @version $Id$
 */
public class PropertySettings implements Settings {

    /** The list of properties used to configure Cocoon. */
    private Map properties = new java.util.HashMap();

        /** Parameter map for the context protocol */
    protected static final Map CONTEXT_PARAMETERS = Collections.singletonMap("force-traversable", Boolean.TRUE);

     /**
      * Initialize the settings for Cocoon.
      * This method reads several property files and merges the result. If there
      * is more than one definition for a property, the last one wins.
      * The property files are read in the following order:
      * 1) context://WEB-INF/properties/*.properties
      *    Default values for the core and each block - the order in which the files are read is not guaranteed.
      * 2) Property providers (ToBeDocumented)
      * 3) The environment (CLI, Servlet etc.) adds own properties (e.g. from web.xml)
      * 4) Additional property file specified by the "org.apache.cocoon.settings" system property or
      *    if the property is not found, the file ".cocoon/settings.properties" is tried to be read from
      *    the user directory.
      * 5) System properties
      * @param resolver The SourceResolver.
      * @param logger the Logger to use.
      *
      */
     public PropertySettings(SourceResolver resolver, Logger logger) {

         // now read all properties from the properties directory
         readProperties("context://WEB-INF/properties", resolver);

         // read additional properties file

         String additionalPropertyFile = properties.containsKey(Settings.PROPERTY_USER_SETTINGS) ?
                 (String)properties.get(Settings.PROPERTY_USER_SETTINGS) :
                 System.getProperty(Settings.PROPERTY_USER_SETTINGS);

         // if there is no property defining the addition file, we try it in the home directory
         if ( additionalPropertyFile == null ) {
             additionalPropertyFile = System.getProperty("user.home") + File.separator + ".cocoon/settings.properties";
             final File testFile = new File(additionalPropertyFile);
             if ( !testFile.exists() ) {
                 additionalPropertyFile = null;
             }
         }
         if ( additionalPropertyFile != null ) {
             logger.debug("Reading user settings from '" + additionalPropertyFile + "'");
             final Properties p = new Properties();
             try {
                 FileInputStream fis = new FileInputStream(additionalPropertyFile);
                 p.load(fis);
                 fis.close();
             } catch (IOException ignore) {
                 logger.warn("Unable to read '" + additionalPropertyFile + "'.", ignore);
                 logger.warn("Continuing initialization.");
             }
             properties.putAll(p);
         }
         // now overwrite with system properties
         properties.putAll(System.getProperties());

         if (logger.isDebugEnabled()){
             Iterator iter = properties.keySet().iterator();
             logger.debug("Cocoon Properties:");
             while (iter.hasNext()) {
                 String key = (String)iter.next();
                 logger.debug("Key: " + key + " Value: " + properties.get(key));
             }
         }
     }

     /*
      * Read all property files from the given directory and apply them to the settings.
      */
     private void readProperties(String          directoryName,
                                 SourceResolver  resolver) {
         Source directory = null;
         try {
             directory = resolver.resolveURI(directoryName, null, CONTEXT_PARAMETERS);
             if (directory.exists() && directory instanceof TraversableSource) {
                 final Iterator c = ((TraversableSource) directory).getChildren().iterator();
                 while (c.hasNext()) {
                     final Source src = (Source) c.next();
                     if ( src.getURI().endsWith(".properties") ) {
                         Properties props = new Properties();
                         final InputStream propsIS = src.getInputStream();
                         props.load(propsIS);
                         propsIS.close();
                         properties.putAll(props);
                     }
                 }
             }
         } catch (IOException ignore) {

         } finally {
             resolver.release(directory);
         }
     }

    /**
     * @see org.apache.cocoon.util.Settings#getProperty(java.lang.String)
     */
    public String getProperty(String name) {
        return this.getProperty(name, null);
    }
/**
     * @see org.apache.cocoon.util.Settings#getProperty(java.lang.String, java.lang.String)
     */
    public String getProperty(String key, String defaultValue) {
        if ( key == null ) {
            return defaultValue;
        }
        String value = (String)properties.get(key);

        if ( value == null ) {
            value = defaultValue;
        }
        return value;
    }
/**
     * @see org.apache.cocoon.util.Settings#getProperties(java.lang.String)
     */
    public List getProperties(String keyPrefix) {
        final List props = new ArrayList();
        final Iterator kI = this.properties.keySet().iterator();
        while ( kI.hasNext() ) {
            final String name = (String)kI.next();
            if ( name.startsWith(keyPrefix) && !props.contains(name) ) {
                props.add(name);
            }
        }
        return props;
    }

    /**
     * @see org.apache.cocoon.util.Settings#getProperties()
     */
    public List getProperties() {
        final List props = new ArrayList();
        props.addAll(this.properties.keySet());

        return props;
    }

    public int size() {
        return this.properties.size();
    }
}

