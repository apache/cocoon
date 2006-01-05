/* 
 * Copyright 2002-2005 The Apache Software Foundation
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
package org.apache.cocoon.components.classloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.matching.helpers.WildcardHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;

/**
 * Default implementation of {@link ClassLoaderFactory}. It accepts both class directory and jar
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
 */
public class DefaultClassLoaderFactory extends AbstractLogEnabled implements ClassLoaderFactory,
    Serviceable, ThreadSafe, Disposable {

    private ServiceManager manager;
    private SourceResolver resolver;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver)manager.lookup(SourceResolver.ROLE);
    }
    
    private void ensureIsDirectory(Source src, String location) throws ConfigurationException {
        if (!src.exists()) {
            throw new ConfigurationException(src.getURI() + " doesn't exist, at " + location);
        } else if (!(src instanceof TraversableSource) || !((TraversableSource)src).isCollection()) {
            throw new ConfigurationException(src.getURI() + " is not a directory, at " + location);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.core.ClassLoaderFactory#createClassLoader(java.lang.ClassLoader)
     */
    public ClassLoader createClassLoader(ClassLoader parent, Configuration config) throws ConfigurationException {
        List urlList = new ArrayList();
        List includeList = new ArrayList();
        List excludeList = new ArrayList();
        Configuration[] children = config.getChildren();
        for (int i = 0; i < children.length; i++) {
            Configuration child = children[i];
            String name = child.getName();
            Source src = null;
            try {
                // A class dir: simply add its URL
                if ("class-dir".equals(name)) {
                    src = resolver.resolveURI(child.getAttribute("src"));
                    ensureIsDirectory(src, child.getLocation());
                    urlList.add(new URL(src.getURI()));
                    resolver.release(src);
                    src = null;
                
                // A lib dir: scan for all jar and zip it contains
                } else if ("lib-dir".equals(name)) {
                    src = resolver.resolveURI(child.getAttribute("src"));
                    ensureIsDirectory(src, child.getLocation());
                    Iterator iter = ((TraversableSource)src).getChildren().iterator();
                    while (iter.hasNext()) {
                        Source childSrc = (Source)iter.next();
                        String childURI = childSrc.getURI();
                        resolver.release(childSrc);
                        if (childURI.endsWith(".jar") || childURI.endsWith(".zip")) {
                            urlList.add(new URL(childURI));
                        }
                    }
                    resolver.release(src);
                    src = null;
                } else if  ("include-classes".equals(name)) {
                    includeList.add(WildcardHelper.compilePattern(child.getAttribute("pattern")));
                } else if ("exclude-classes".equals(name)) {
                    excludeList.add(WildcardHelper.compilePattern(child.getAttribute("pattern")));
                } else {
                    throw new ConfigurationException("Unexpected element " + name + " at " + child.getLocation());
                }
            } catch(ConfigurationException ce) {
                resolver.release(src);
                throw ce;
            } catch(Exception e) {
                resolver.release(src);
                throw new ConfigurationException("Error loading " + name + " at " + child.getLocation(), e);
            }
        }
        
        URL[] urls = (URL[])urlList.toArray(new URL[urlList.size()]);
        int[][] includes = includeList.isEmpty() ? null : (int[][])includeList.toArray(new int[includeList.size()][]);
        int[][] excludes = excludeList.isEmpty() ? null : (int[][])excludeList.toArray(new int[excludeList.size()][]);
        
        return new DefaultClassLoader(urls, includes, excludes, parent);
    }

    public static class DefaultClassLoader extends URLClassLoader {
        
        private final int[][] includes;
        private final int[][] excludes;

        /**
         * Alternate constructor to define a parent and initial <code>URL</code>
         * s.
         */
        public DefaultClassLoader(URL[] urls, int[][] includes, int[][] excludes, final ClassLoader parent) {
            this(urls, includes, excludes, parent, null);
        }

        /**
         * Alternate constructor to define a parent, initial <code>URL</code>s,
         * and a default <code>URLStreamHandlerFactory</code>.
         */
        public DefaultClassLoader(final URL[] urls, int[][] includes, int[][] excludes, ClassLoader parent, URLStreamHandlerFactory factory) {
            super(urls, parent, factory);
            this.includes = includes;
            this.excludes = excludes;
        }
        
        private boolean tryClassHere(String name) {
            // Scan includes, then excludes
            boolean tryHere;
            
            // If no explicit includes, try here
            if (this.includes == null) {
                tryHere = true;
            } else {
                // See if it matches include patterns
                tryHere = false;
                for (int i = 0; i < this.includes.length; i++) {
                    if (WildcardHelper.match(null, name, includes[i])) {
                        tryHere = true;
                        break;
                    }
                }
            }
            
            // Go through the exclusion list
            if (tryHere && excludes != null) {
                for (int i = 0; i < this.excludes.length; i++) {
                    if (WildcardHelper.match(null, name, excludes[i])) {
                        tryHere = false;
                        break;
                    }
                }
            }
            
            return tryHere;
        }

        /**
         * Loads the class from this <code>ClassLoader</class>.  If the
         * class does not exist in this one, we check the parent.  Please
         * note that this is the exact opposite of the
         * <code>ClassLoader</code> spec.  We use it to work around
         * inconsistent class loaders from third party vendors.
         *
         * @param     name the name of the class
         * @param     resolve if <code>true</code> then resolve the class
         * @return    the resulting <code>Class</code> object
         * @exception ClassNotFoundException if the class could not be found
         */
        public final Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
            // First check if it's already loaded
            Class clazz = findLoadedClass(name);

            if (clazz == null) {
                
                ClassLoader parent = getParent();

                if (tryClassHere(name)) {
                    try {
                        clazz = findClass(name);
                        //System.err.println("Paranoid load : " + name);
                    } catch (ClassNotFoundException cnfe) {
                        if (parent == null) {
                            // Propagate exception
                            throw cnfe;                        
                        }
                    }
                }
                
                if (clazz == null) {
                    if (parent == null) {
                        throw new ClassNotFoundException(name);
                    } else {
                        // Will throw a CFNE if not found in parent
                        clazz = parent.loadClass(name);
                    }
                }
            }

            if (resolve) {
                resolveClass(clazz);
            }

            return clazz;
        }

        /**
         * Gets a resource from this <code>ClassLoader</class>.  If the
         * resource does not exist in this one, we check the parent.
         * Please note that this is the exact opposite of the
         * <code>ClassLoader</code> spec.  We use it to work around
         * inconsistent class loaders from third party vendors.
         *
         * @param name of resource
         */
        public final URL getResource(final String name) {

            URL resource = findResource(name);
            ClassLoader parent = this.getParent();
            if (resource == null && parent != null) {
                resource = parent.getResource(name);
            }

            return resource;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        this.manager.release(this.resolver);
    }
}
