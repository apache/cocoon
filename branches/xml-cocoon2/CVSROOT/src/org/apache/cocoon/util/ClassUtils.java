/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.util;

import java.io.File;
import java.net.URL;

import java.io.IOException;

/**
 * A collection of class management utility methods.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-07-22 20:42:02 $
 */
public class ClassUtils {
  /**
   * Create a new instance given a class name
   *
   * @param className A class name
   * @return A new instance
   * @exception Exception If an instantiation error occurs
   */
  public static Object newInstance(String className) throws Exception {
    return getClass(className).newInstance();
  }

  /**
   * Load a class given its name
   *
   * @param className A class name
   * @return The class pointed to by <code>className</code>
   * @exception Exception If a loading error occurs
   */
  public static Class getClass(String className) throws Exception {
    return ClassUtils.class.getClassLoader().loadClass(className);
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
    throws IOException, IllegalArgumentException
  {
    URL url =
      aClass.
      getProtectionDomain().
      getCodeSource().
      getLocation();

    if (!url.getProtocol().equals("file")) {
      throw new IllegalArgumentException("Class was not loaded from a file url");
    }

    File directory = new File(url.getFile());
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException("Class was not loaded from a directory");
    }

    String className = aClass.getName();
    String basename = className.substring(className.lastIndexOf(".") + 1);

    File file = new File(
      directory.getCanonicalPath() +
      File.separator +
      basename +
      ".class"
    );

    return file.lastModified();
  }
}
