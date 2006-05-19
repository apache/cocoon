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
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

import org.apache.cocoon.util.WildcardHelper;

/**
 * @version $Id$
 */
public class DefaultClassLoader extends URLClassLoader {

    protected final int[][] includes;
    protected final int[][] excludes;

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

    protected boolean tryClassHere(String name) {
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

    protected Class getClass(String name)
    throws ClassNotFoundException {
        return findClass(name);
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
                    clazz = this.getClass(name);
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

