/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.servlet;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

/**
 * The <code>ParanoidClassLoader</code> reverses the search order for classes.
 * It checks this classloader before it checks its parent.
 * 
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch </a>
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez </a>
 * @version CVS $Id: ParanoidClassLoader.java 30932 2004-07-29 17:35:38Z
 *          vgritsenko $
 */

public class ParanoidClassLoader extends URLClassLoader {

    /**
     * Default constructor has no parents or initial <code>URL</code>s.
     */
    public ParanoidClassLoader() {
        this(null, null, null);
    }

    /**
     * Alternate constructor to define a parent.
     */
    public ParanoidClassLoader(final ClassLoader parent) {
        this(new URL[0], parent, null);
    }

    /**
     * Alternate constructor to define initial <code>URL</code>s.
     */
    public ParanoidClassLoader(final URL[] urls) {
        this(urls, null, null);
    }

    /**
     * Alternate constructor to define a parent and initial <code>URL</code>
     * s.
     */
    public ParanoidClassLoader(final URL[] urls, final ClassLoader parent) {
        this(urls, parent, null);
    }

    /**
     * Alternate constructor to define a parent, initial <code>URL</code>s,
     * and a default <code>URLStreamHandlerFactory</code>.
     */
    public ParanoidClassLoader(final URL[] urls, final ClassLoader parent, final URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    /**
     * Extends <code>URLClassLoader</code>'s initialization methods so we
     * return a <code>ParanoidClassLoad</code> instead.
     */
    public static final URLClassLoader newInstance(final URL[] urls) {
        return new ParanoidClassLoader(urls);
    }

    /**
     * Extends <code>URLClassLoader</code>'s initialization methods so we
     * return a <code>ParanoidClassLoad</code> instead.
     */
    public static final URLClassLoader newInstance(final URL[] urls, final ClassLoader parent) {
        return new ParanoidClassLoader(urls, parent);
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

            try {
                clazz = findClass(name);
                //System.err.println("Paranoid load : " + name);
            } catch (ClassNotFoundException cnfe) {
                ClassLoader parent = getParent();
                if (parent != null) {
                    // Ask to parent ClassLoader (can also throw a CNFE).
                    clazz = parent.loadClass(name);
                } else {
                    // Propagate exception
                    throw cnfe;
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

    /**
     * Adds a new directory of class files.
     * 
     * @param file
     *            for jar or directory
     * @throws IOException
     */
    public final void addDirectory(File file) throws IOException {
        this.addURL(file.getCanonicalFile().toURL());
    }

    /**
     * Adds a new URL
     */

    public void addURL(URL url) {
        super.addURL(url);
    }
}