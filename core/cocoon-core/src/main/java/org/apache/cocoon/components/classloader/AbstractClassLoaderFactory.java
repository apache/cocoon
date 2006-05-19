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

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    protected abstract ClassLoader createClassLoader(URL[] urls, List includePatterns, List excludePatterns, ClassLoader parent);

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
