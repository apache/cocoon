/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.maven.deployer.servlet;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;


/**
 * This class creates a singleton instance of the shielded class loader.
 *
 * It can be configured through context paramters:
 * <ul>
 * <li><code>shielded-classloader-debug</code> Can be used to turn debug messages on.</li>
 * </ul>
 *
 * @version $Id$
 */
public class ShieldedClassLoaderManager {


    public static final String SHIELDED_CLASSLOADER_DEBUG = "shielded-classloader-debug";

    public static final String SHIELDED_CLASSLOADER_USE_REPOSITORY = "shieled-classloader-use-repository";

    public static final String WEB_INF_SHIELDED_LIB = "shielded/lib";

    public static final String WEB_INF_SHIELDED_CLASSES = "shielded/classes";

    protected static final String SHIELDED_LIB = "/WEB-INF/" + WEB_INF_SHIELDED_LIB;

    protected static final String SHIELDED_CLASSES = "/WEB-INF/" + WEB_INF_SHIELDED_CLASSES;

    protected static ClassLoader shieldedClassLoader;

    /**
     * Create the class loader.
     * @param servletContext
     * @return
     * @throws ServletException
     */
    public static synchronized ClassLoader getClassLoader(ServletContext servletContext)
    throws ServletException {
        if ( shieldedClassLoader == null ) {
            try {
                shieldedClassLoader = createClassLoader(ShieldedClassLoaderManager.class.getClassLoader(), servletContext);
            } catch (IOException ioe) {
                throw new ServletException("Unable to create shielded class loader.", ioe);                
            }
        }
        return shieldedClassLoader;
    }

    /**
     * Log a debug message to the log of the servlet context.
     * This method first checks if the init parameter "shielded-classloader-debug" has the value
     * true before it logs.
     * @param servletContext The servlet context.
     * @param message        The message to log.
     */
    public static void logDebug(ServletContext servletContext, String message) {
        if ( servletContext.getInitParameter(SHIELDED_CLASSLOADER_DEBUG) != null
             && servletContext.getInitParameter(SHIELDED_CLASSLOADER_DEBUG).equalsIgnoreCase("true") ) { 
            servletContext.log(message);
        }
    }

    /**
     * Create the shielded class loader.
     */
    protected static ClassLoader createClassLoader(ClassLoader    parent,
                                                   ServletContext servletContext)
    throws IOException {
        String classesDirectory = ShieldedClassLoaderManager.SHIELDED_CLASSES;
        String jarDirectory = ShieldedClassLoaderManager.SHIELDED_LIB;
        if ( servletContext.getInitParameter(SHIELDED_CLASSLOADER_USE_REPOSITORY) != null ) {
            boolean useShieldedRepository = Boolean.valueOf(servletContext.getInitParameter(SHIELDED_CLASSLOADER_USE_REPOSITORY)).booleanValue();
            if ( !useShieldedRepository ) {
                classesDirectory = "/WEB-INF/classes";
                jarDirectory = "/WEB-INF/libs";
            }
        }
        final List urlList = new ArrayList();
        // add url for classes dir
        if (servletContext.getResource(classesDirectory) != null) {
            urlList.add(servletContext.getResource(classesDirectory));
        }

        // add url for lib dir
        if (servletContext.getResource(jarDirectory) != null) {
            final Set resources = servletContext.getResourcePaths(jarDirectory + '/');
            if (resources != null) {
                // we add all urls into a temporary list first to sort them
                // before we add them
                final List temporaryList = new ArrayList();
                final Iterator iter = resources.iterator();
                while (iter.hasNext()) {
                    final String path = (String) iter.next();
                    if (path.endsWith(".jar") || path.endsWith(".zip")) {
                        temporaryList.add(servletContext.getResource(path));
                    }
                }
                // let's sort before adding
                Collections.sort(temporaryList, new UrlComparator());
                urlList.addAll(temporaryList);
            }
        }

        URL[] urls = (URL[]) urlList.toArray(new URL[urlList.size()]);

        return new ShieldedClassLoader(urls, parent);
    }

    /**
     * Simple comparator for comparing url objects.
     */
    protected final static class UrlComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            if (o1 instanceof URL && o2 instanceof URL) {
                return ((URL) o1).toExternalForm().compareTo(
                        ((URL) o2).toExternalForm());
            }
            return 0;
        }
    }
}
