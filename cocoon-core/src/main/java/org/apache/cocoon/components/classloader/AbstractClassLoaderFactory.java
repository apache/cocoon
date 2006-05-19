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
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.util.WildcardHelper;
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
    extends AbstractLogEnabled
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

    private void ensureIsDirectory(Source src, String location) throws ConfigurationException {
        if (!src.exists()) {
            throw new ConfigurationException(src.getURI() + " doesn't exist, at " + location);
        } else if (!(src instanceof TraversableSource) || !((TraversableSource)src).isCollection()) {
            throw new ConfigurationException(src.getURI() + " is not a directory, at " + location);
        }
    }

    /**
     * @see org.apache.cocoon.components.classloader.ClassLoaderFactory#createClassLoader(java.lang.ClassLoader, org.apache.avalon.framework.configuration.Configuration)
     */
    public ClassLoader createClassLoader(ClassLoader parent, Configuration config) throws ConfigurationException {
        final List urlList = new ArrayList();
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
                } else if (!"include-classes".equals(name) && !"exclude-classes".equals(name) ) {
                    throw new ConfigurationException("Unexpected element " + name + " at " + child.getLocation());
                }
            } catch(ConfigurationException ce) {
                throw ce;
            } catch(Exception e) {
                throw new ConfigurationException("Error loading " + name + " at " + child.getLocation(), e);
            } finally {
                resolver.release(src);
                src = null;                
            }
        }
        
        URL[] urls = (URL[])urlList.toArray(new URL[urlList.size()]);
        int[][] includes = compilePatterns(config.getChildren("include-classes"));
        int[][] excludes = compilePatterns(config.getChildren("exclude-classes"));
        
        return this.createClassLoader(urls, includes, excludes, parent);
    }

    protected abstract ClassLoader createClassLoader(URL[] urls, int[][] includes, int[][] excludes, ClassLoader parent);

    private int[][] compilePatterns(Configuration[] patternConfigs) throws ConfigurationException {
        if (patternConfigs.length == 0) {
            return null;
        }

        int[][] patterns = new int[patternConfigs.length][];

        for (int i = 0; i < patternConfigs.length; i++) {
            patterns[i] = WildcardHelper.compilePattern(patternConfigs[i].getAttribute("pattern"));
        }

        return patterns;
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
