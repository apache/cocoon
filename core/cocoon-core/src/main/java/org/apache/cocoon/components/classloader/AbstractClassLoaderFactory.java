/* 
 * Copyright 2002-2006 The Apache Software Foundation
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

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.TraversableSource;

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
 */
public abstract class AbstractClassLoaderFactory
    implements ClassLoaderFactory,
               Serviceable,
               ThreadSafe,
               Disposable {

    protected ServiceManager manager;
    protected SourceResolver resolver;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver)manager.lookup(SourceResolver.ROLE);
    }

    private void ensureIsDirectory(Source src) throws Exception {
        if (!src.exists()) {
            throw new Exception(src.getURI() + " doesn't exist");
        } else if (!(src instanceof TraversableSource) || !((TraversableSource)src).isCollection()) {
            throw new Exception(src.getURI() + " is not a directory");
        }
    }

    /**
     * @see org.apache.cocoon.components.classloader.ClassLoaderFactory#createClassLoader(java.lang.ClassLoader, org.apache.avalon.framework.configuration.Configuration)
     */
    public ClassLoader createClassLoader(ClassLoader parent, Configuration config)
    throws ConfigurationException {
        final ClassLoaderConfiguration configBean = new ClassLoaderConfiguration();
        final Configuration[] children = config.getChildren();
        for (int i = 0; i < children.length; i++) {
            final Configuration child = children[i];
            final String name = child.getName();
            if ("class-dir".equals(name)) {
                configBean.addClassDirectory(child.getAttribute("src"));
            } else if ("lib-dir".equals(name)) {
                configBean.addLibDirectory(child.getAttribute("src"));
            } else if ("include-classes".equals(name)) {
                configBean.addInclude(child.getAttribute("pattern"));
            } else if ("exclude-classes".equals(name)) {
                configBean.addExclude(child.getAttribute("pattern"));
            } else {
                throw new ConfigurationException("Unexpected element " + name + " at " + child.getLocation());
            }
        }
        try {
            return this.createClassLoader(parent, configBean);
        } catch(ConfigurationException ce) {
            throw ce;
        } catch(Exception e) {
            throw new ConfigurationException("Error creating class loader.", e);
        }
    }

    protected ClassLoader createClassLoader(ClassLoader parent, ClassLoaderConfiguration config)
    throws Exception {
        final List urlList = new ArrayList();
        Iterator i;
        // process class directories
        i = config.getClassDirectories().iterator();
        while ( i.hasNext() ) {
            // A class dir: simply add its URL
            final String directory = (String)i.next();
            Source src = null;
            try {
                src = resolver.resolveURI(directory);
                ensureIsDirectory(src);
                urlList.add(new URL(src.getURI()));
            } finally {
                this.resolver.release(src);
            }
        }

        // process lib directories
        i = config.getLibDirectories().iterator();
        while ( i.hasNext() ) {
            // A lib dir: scan for all jar and zip it contains
            final String directory = (String)i.next();
            Source src = null;
            try {
                src = resolver.resolveURI(directory);
                ensureIsDirectory(src);
                Iterator iter = ((TraversableSource)src).getChildren().iterator();
                while (iter.hasNext()) {
                    Source childSrc = (Source)iter.next();
                    String childURI = childSrc.getURI();
                    resolver.release(childSrc);
                    if (childURI.endsWith(".jar") || childURI.endsWith(".zip")) {
                        urlList.add(new URL(childURI));
                    }
                }
            } finally {
                this.resolver.release(src);
            }
        }

        URL[] urls = (URL[])urlList.toArray(new URL[urlList.size()]);
        
        return this.createClassLoader(urls, config.getIncludes(), config.getExcludes(), parent);
    }

    protected URL getUrl(ServletContext servletContext, String rootPath, String path) 
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
        return servletContext.getResource(this.getContextPath(rootPath, path));
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

    protected String getContextPath(String rootPath, String path) {
        if ( path.startsWith("context://") ) {
            return path.substring(9);
        }
        return rootPath + path;        
    }

    protected ClassLoader createClassLoader(ClassLoader              parent,
                                            ClassLoaderConfiguration config,
                                            ServletContext           servletContext,
                                            String                   rootPath)
    throws Exception {
        if ( rootPath == null ) {
            rootPath = "/";
        } else if ( !rootPath.endsWith("/") ) {
            rootPath = rootPath + "/";
        }
        final List urlList = new ArrayList();
        Iterator i;
        // process class directories
        i = config.getClassDirectories().iterator();
        while ( i.hasNext() ) {
            // A class dir: simply add its URL
            final String directory = (String)i.next();
            final URL url = this.getUrl(servletContext, rootPath, directory);
            // TODO Should we somehow check if this is a dir?
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

                for (int m = 0; m < libraries.length; m++) {
                    final URL lib = libraries[m].toURL();
                    urlList.add(lib);
                }                
            } else {
                // if this is an absolut url we can't handle it!
                if ( directory.indexOf(":/") > 0 ) {
                    throw new Exception("Can't handle absolute url as lib class path: " + directory);
                }
                final String contextPath = this.getContextPath(rootPath, directory);
                final Set resources = servletContext.getResourcePaths(contextPath + '/');
                if ( resources != null ) {
                    final Iterator iter = resources.iterator();
                    while ( iter.hasNext() ) {
                        final String path = (String)iter.next();
                        if (path.endsWith(".jar") || path.endsWith(".zip")) {
                            urlList.add(servletContext.getResource(path));
                        }
                    }
                }
            }
        }

        URL[] urls = (URL[])urlList.toArray(new URL[urlList.size()]);
        
        return this.createClassLoader(urls, config.getIncludes(), config.getExcludes(), parent);
    }

    protected abstract ClassLoader createClassLoader(URL[] urls, List includePatterns, List excludePatterns, ClassLoader parent);

    private class JarFileFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.endsWith(".zip") || name.endsWith(".jar");
        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.resolver);
            this.resolver = null;
            this.manager = null;
        }
    }
}
