/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A collection of class management utility methods.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.13 $ $Date: 2001-04-25 17:09:11 $
 */
public class ClassUtils {

    /**
     * Reference for global ClassLoader that Cocoon uses throughout.
     */
    private static ClassLoader loader = null;

    /**
     * Reference for global Cocoon Jar URL that Cocoon uses throughout.
     */
    private static URL cocoonURL = null;
    /**
     * Create a new instance given a class name
     *
     * @param className A class name
     * @return A new instance
     * @exception Exception If an instantiation error occurs
     */
    public static Object newInstance(String className) throws Exception {
        return ClassUtils.loadClass(className).newInstance();
    }

    /**
     * Load a class given its name.
     * BL: We wan't to use a known ClassLoader--hopefully the heirarchy
     *     is set correctly.
     *
     * @param className A class name
     * @return The class pointed to by <code>className</code>
     * @exception ClassNotFoundException If a loading error occurs
     */
    public static Class loadClass(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    /**
     * Return a resource URL.
     * BL: if this is command line operation, the classloading issues
     *     are more sane.  During servlet execution, we explicitly set
     *     the ClassLoader.
     *
     * @return The context classloader.
     * @exception MalformedURLException If a loading error occurs
     */
    public static URL getResource(String resource) throws MalformedURLException {
        return ClassUtils.getClassLoader().getResource(resource);
    }

    /**
     * Return the context classloader.
     * BL: if this is command line operation, the classloading issues
     *     are more sane.  During servlet execution, we explicitly set
     *     the ClassLoader.
     *
     * @return The context classloader.
     */
    public static ClassLoader getClassLoader() {
        if (ClassUtils.loader == null) {
            setClassLoader(ClassUtils.class.getClassLoader());
        }

        return ClassUtils.loader;
    }

    /**
     * Set the context classloader
     * BL: if this is command line operation, the classloading issues
     *     are more sane.  During servlet execution, we explicitly set
     *     the ClassLoader.
     */
    public static void setClassLoader(ClassLoader loader) {
        if (ClassUtils.loader == null) {
            ClassUtils.loader = loader;
        }
    }

    /**
     * Return the context classloader.
     * BL: if this is command line operation, the classloading issues
     *     are more sane.  During servlet execution, we explicitly set
     *     the ClassLoader.
     *
     * @return The context classloader.
     */
    public static URL getCocoonURL() throws MalformedURLException {
        if (ClassUtils.cocoonURL == null) {
            throw new MalformedURLException("The cocoon jar URL was never set.");
        }

        return ClassUtils.cocoonURL;
    }

    /**
     * Set the context CocoonURL
     * BL: if this is command line operation, the classloading issues
     *     are more sane.  During servlet execution, we explicitly set
     *     the CocoonURL.
     */
    public static void setCocoonURL(URL location) {
        if (ClassUtils.cocoonURL == null) {
            ClassUtils.cocoonURL = location;
        }
    }

    /**
     * Tests if a class implements a given interface
     *
     * @return true if class implements given interface.
     */
    public static boolean implementsInterface(String className, String iface) throws Exception {
        Class class1 = ClassUtils.loadClass (className);
        Class class2 = ClassUtils.loadClass (iface);
        return ClassUtils.implementsInterface(class1, class2);
    }

    /**
     * Tests if a class implements a given interface
     *
     * @return true if class implements given interface.
     */
    public static boolean implementsInterface(Class class1, Class iface) {
        return iface.isAssignableFrom (class1);
    }

    /**
     * Determine the last modification date for this
     * class file or its enclosing library
     *
     * @param aClass A class whose last modification date is queried
     * @return The time the given class was last modified
     * @exception IOException IOError
     * @exception IllegalArgumentException The class was not loaded from a file
     * or directory
     */
    public static long lastModified(Class aClass)
        throws IOException, IllegalArgumentException  {
        URL url = aClass.getProtectionDomain().getCodeSource().getLocation();

        if (!url.getProtocol().equals("file")) {
            throw new IllegalArgumentException("Class was not loaded from a file url");
        }

        File directory = new File(url.getFile());
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Class was not loaded from a directory");
        }

        String className = aClass.getName();
        String basename = className.substring(className.lastIndexOf(".") + 1);

        File file = new File(directory.getCanonicalPath() + File.separator + basename + ".class");

        return file.lastModified();
    }
}
