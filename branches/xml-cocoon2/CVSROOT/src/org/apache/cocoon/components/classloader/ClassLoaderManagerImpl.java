/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.classloader;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import org.apache.cocoon.util.ClassUtils;

import org.apache.avalon.ThreadSafe;

/**
 * A singleton-like implementation of <code>ClassLoaderManager</code>
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.7 $ $Date: 2001-01-31 05:21:39 $
 */
public class ClassLoaderManagerImpl implements ClassLoaderManager, ThreadSafe {
  /**
   * The single class loader instance
   */
  protected static RepositoryClassLoader instance = null;

  /**
   * A constructor that ensures only a single class loader instance exists
   *
   */
  public ClassLoaderManagerImpl() {
    if (instance == null) {
      this.reinstantiate();
    }
  }

  /**
   * Add a directory to the proxied class loader
   *
   * @param directoryName The repository name
   * @exception IOException If the directory is invalid
   */
  public void addDirectory(File directoryName) throws IOException {
    instance.addDirectory(directoryName);
  }

  /**
   * Load a class through the proxied class loader
   *
   * @param className The name of the class to be loaded
   * @return The loaded class
   * @exception ClassNotFoundException If the class is not found
   */
  public Class loadClass(String className) throws ClassNotFoundException {
    return instance.loadClass(className);
  }

  /**
   * Reinstantiate the proxied class loader to allow for class reloading
   *
   */
  public synchronized void reinstantiate() {
    instance = new RepositoryClassLoader();
  }
}
