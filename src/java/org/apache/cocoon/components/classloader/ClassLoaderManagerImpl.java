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
package org.apache.cocoon.components.classloader;

import org.apache.avalon.framework.thread.ThreadSafe;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * A singleton-like implementation of <code>ClassLoaderManager</code>
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Id: ClassLoaderManagerImpl.java,v 1.2 2004/03/05 13:02:46 bdelacretaz Exp $
 */
public class ClassLoaderManagerImpl implements ClassLoaderManager, ThreadSafe {
  /**
   * The single class loader instance
   */
  protected static RepositoryClassLoader instance = null;

  protected static Set fileSet = Collections.synchronizedSet(new HashSet());

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
    if ( ! ClassLoaderManagerImpl.fileSet.contains(directoryName)) {
        ClassLoaderManagerImpl.fileSet.add(directoryName);
        ClassLoaderManagerImpl.instance.addDirectory(directoryName);
    }
  }

  /**
   * Load a class through the proxied class loader
   *
   * @param className The name of the class to be loaded
   * @return The loaded class
   * @exception ClassNotFoundException If the class is not found
   */
  public Class loadClass(String className) throws ClassNotFoundException {
    return ClassLoaderManagerImpl.instance.loadClass(className);
  }

  /**
   * Reinstantiate the proxied class loader to allow for class reloading
   *
   */
  public void reinstantiate() {
    if ( ClassLoaderManagerImpl.fileSet.isEmpty()) {
      ClassLoaderManagerImpl.instance = new RepositoryClassLoader();
    } else {
      ClassLoaderManagerImpl.instance = new RepositoryClassLoader(new Vector(ClassLoaderManagerImpl.fileSet));
    }
  }
}
