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
package org.apache.cocoon.classloader;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

/**
 * Abstract implementation of {@link ClassLoaderFactory}. It accepts both class directory and jar
 * directory configurations.
 * <p>
 * Wildcard patterns can also be specified to include or exclude some classes to be loaded in the
 * classloader. In such case, the class is directly loaded from the parent classloader. The default
 * is to include all classes.
 * <p>
 * Example:
 * <pre>
 * &lt;classpath&gt;
 *   &lt;class-dir src="BLOCK-INF/classes"/&gt;
 *   &lt;lib-dir src="BLOCK-INF/lib"/&gt;
 *   &lt;include-classes pattern="org.apache.cocoon.**"/&gt;
 *   &lt;exclude-classes pattern="org.apache.cocoon.transformation.**"/&gt;
 * &/lt;classpath&gt;
 * </pre>
 *
 * @version $Id$
 * @since 2.2
 */
public abstract class AbstractClassLoaderFactory
    implements ClassLoaderFactory {

    protected URL getUrl(ServletContext servletContext, String path) 
    throws MalformedURLException {
        // check for files
        final File file = this.getFile(path);
        if ( file != null ) {
            return file.toURL();
        }
        // check for absolut url
        if ( path.indexOf(":/") > 0 ) {
            return new URL(path);
        }
        return servletContext.getResource(this.getContextPath(path));
    }

    protected File getFile(String path) 
    throws MalformedURLException {
        // check for file url
        if ( path.startsWith("file:") ) {
            return new File(path.substring(5));
        }
        // check for absolut files on unix/windows
        if ( path.startsWith("/") || (path.length() > 2 && path.charAt(1) == ':' )) {
            return new File(path);
        }
        return null;
    }

    protected String getContextPath(String path) {
        if ( path.startsWith("context://") ) {
            return path.substring(9);
        }
        return "/" + path;        
    }

    public ClassLoader createClassLoader(ClassLoader              parent,
                                         ClassLoaderConfiguration config,
                                         ServletContext           servletContext)
    throws Exception {
        final List urlList = new ArrayList();
        Iterator i;
        // process class directories
        i = config.getClassDirectories().iterator();
        while ( i.hasNext() ) {
            // A class dir: simply add its URL
            final String directory = (String)i.next();
            URL url = this.getUrl(servletContext, directory);
            if ( url == null ) {
                throw new Exception("Directory not found for classpath: " + directory);
            }
            // TODO Should we somehow check if this is a dir?
            if ( !url.toExternalForm().endsWith("/") ) {
                url = new URL(url.toExternalForm() + "/");
            }
            urlList.add(url);
        }

        // process lib directories
        i = config.getLibDirectories().iterator();
        while ( i.hasNext() ) {
            // A lib dir: scan for all jar and zip it contains
            final String directory = (String)i.next();
            // Test for file
            final File libDir = this.getFile(directory);
            if ( libDir != null ) {
                if ( !libDir.exists() ) {
                    throw new Exception("Directory for lib class path does not exists: " + libDir);
                }
                if ( !libDir.isDirectory() ) {
                    throw new Exception("Configuration for lib class path is not a directory: " + libDir);
                }
                File[] libraries = libDir.listFiles(new JarFileFilter());
                // sort the files to provide a consistent search order
                Arrays.sort(libraries);
                for (int m = 0; m < libraries.length; m++) {
                    final URL lib = libraries[m].toURL();
                    urlList.add(lib);
                }                
            } else {
                // if this is an absolut url we can't handle it!
                if ( directory.indexOf(":/") > 0 ) {
                    throw new Exception("Can't handle absolute url as lib class path: " + directory);
                }
                final String contextPath = this.getContextPath(directory);
                final Set resources = servletContext.getResourcePaths(contextPath + '/');
                if ( resources != null ) {
                    // we add all urls into a temporary list first to sort them
                    // before we add them
                    final List temporaryList = new ArrayList();
                    final Iterator iter = resources.iterator();
                    while ( iter.hasNext() ) {
                        final String path = (String)iter.next();
                        if (path.endsWith(".jar") || path.endsWith(".zip")) {
                            temporaryList.add(servletContext.getResource(path));
                        }
                    }
                    // let's sort before adding
                    Collections.sort(temporaryList, new UrlComparator());
                    urlList.addAll(temporaryList);
                }
            }
        }

        URL[] urls = (URL[])urlList.toArray(new URL[urlList.size()]);
        
        return this.createClassLoader(urls, config.getIncludes(), config.getExcludes(), parent);
    }

    protected abstract ClassLoader createClassLoader(URL[] urls, List includePatterns, List excludePatterns, ClassLoader parent);

    protected final static class JarFileFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.endsWith(".zip") || name.endsWith(".jar");
        }
    }

    protected final static class UrlComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            if ( o1 instanceof URL && o2 instanceof URL ) {
                return ((URL)o1).toExternalForm().compareTo(((URL)o2).toExternalForm());
            }
            return 0;
        }        
    }
}
