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
package org.apache.cocoon.core.container.spring;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.web.context.support.ServletContextResourcePatternResolver;

/**
 * Utility class for Spring resource handling
 * @version $Id$
 */
public class ResourceUtils {

    /**
     * Get the uri of a resource.
     * This method corrects the uri in the case of the file protocol
     * on windows.
     * @param resource The resource.
     * @return The uri.
     * @throws IOException
     */
    public static String getUri(Resource resource)
    throws IOException {
        if ( resource == null ) {
            return null;
        }
        return correctUri(resource.getURL().toExternalForm());
    }

    protected static String correctUri(String uri) {
        // if it is a file we have to recreate the url,
        // otherwise we get problems under windows with some file
        // references starting with "/DRIVELETTER" and some
        // just with "DRIVELETTER"
        if ( uri.startsWith("file:") ) {
            final File f = new File(uri.substring(5));
            return "file://" + f.getAbsolutePath();
        }
        return uri;
    }

    /**
     * Read all property files from the given directory and apply them to the supplied properties.
     */
    public static void readProperties(String          directoryName,
                                      Properties      properties,
                                      ResourceLoader  resourceLoader,
                                      Log             logger) {
        if ( logger != null && logger.isDebugEnabled() ) {
            logger.debug("Reading properties from directory: " + directoryName);
        }
        // check if directory exists
        Resource directoryResource = resourceLoader.getResource(directoryName);
        if ( directoryResource.exists() ) {
            final String pattern = directoryName + "/*.properties";

            final ResourcePatternResolver resolver = new ServletContextResourcePatternResolver(resourceLoader);
            Resource[] resources = null;
            try {
                resources = resolver.getResources(pattern);
            } catch (IOException ignore) {
                if ( logger != null && logger.isDebugEnabled() ) {
                    logger.debug("Unable to read properties from directory '" + directoryName + "' - Continuing initialization.", ignore);
                }
            }
            if ( resources != null ) {
                // we process the resources in alphabetical order, so we put
                // them first into a list, sort them and then read the properties.
                final List propertyUris = new ArrayList();
                for(int i=0; i<resources.length; i++ ) {
                    propertyUris.add(resources[i]);
                }
                // sort
                Collections.sort(propertyUris, getResourceComparator());
                // now process
                final Iterator i = propertyUris.iterator();
                while ( i.hasNext() ) {
                    final Resource src = (Resource)i.next();
                    try {
                        if ( logger != null && logger.isDebugEnabled() ) {
                            logger.debug("Reading settings from '" + src.getURL() + "'.");
                        }
                        final InputStream propsIS = src.getInputStream();
                        properties.load(propsIS);
                        propsIS.close();
                    } catch (IOException ignore) {
                        if ( logger != null && logger.isDebugEnabled() ) {
                            logger.info("Unable to read properties from file '" + src.getDescription() + "' - Continuing initialization.", ignore);
                        }
                    }
                }
            }
        } else {
            if ( logger != null && logger.isDebugEnabled() ) {
                logger.debug("Directory '" + directoryName + "' does not exist - Continuing initialization.");
            }
        }
    }

    /**
     * Return a resource comparator
     */
    public static Comparator getResourceComparator() {
        return new ResourceComparator();
    }

    protected final static class ResourceComparator implements Comparator {

        /**
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(Object o1, Object o2) {
            if ( !(o1 instanceof Resource) || !(o2 instanceof Resource)) {
                return 0;
            }
            return ((Resource)o1).getFilename().compareTo(((Resource)o2).getFilename());
        }
    }
}
