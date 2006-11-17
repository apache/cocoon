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

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;


/**
 * This class loader reverses the search order for classes.  It checks this classloader
 * before it checks its parent.
 *
 * @version $Id$
 */
public class ShieldedClassLoader extends URLClassLoader {

    /**
     * Alternate constructor to define a parent and initial <code>URL</code>s.
     */
    public ShieldedClassLoader(URL[] urls, final ClassLoader parent) {
        this(urls, parent, null);
    }

    /**
     * Alternate constructor to define a parent, initial <code>URL</code>s,
     * and a default <code>URLStreamHandlerFactory</code>.
     */
    public ShieldedClassLoader(final URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    protected boolean tryClassHere(String name) {
        // don't include classes in the java or javax.servlet package
        if ( name != null && (name.startsWith("java.") || name.startsWith("javax.servlet") ) ) {
            return false;
        }
        
        return true;
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
            
            final ClassLoader parent = getParent();

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

