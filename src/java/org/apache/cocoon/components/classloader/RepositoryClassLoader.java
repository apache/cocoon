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

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.util.ClassUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Vector;

/**
 * A class loader with a growable list of path search directories.
 * BL: Changed to extend URLClassLoader for both maintenance and
 *     compatibility reasons.  It doesn't hurt that it runs quicker
 *     now as well.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Id: RepositoryClassLoader.java,v 1.2 2004/03/08 13:57:37 cziegeler Exp $
 */
public class RepositoryClassLoader extends URLClassLoader implements LogEnabled {

  /**
   * The logger
   */
  protected Logger log;

  /**
   * Create an empty new class loader.
   */
  public RepositoryClassLoader() {
    super(new URL[] {}, ClassUtils.getClassLoader());
  }

  /**
   * Create an empty new class loader.
   */
  public RepositoryClassLoader(URL[] urls) {
    super(urls, ClassUtils.getClassLoader());
  }

  /**
   * Create an empty new class loader.
   */
  public RepositoryClassLoader(URL[] urls, ClassLoader parentClassLoader) {
    super(urls, parentClassLoader);
  }

  /**
   * Provide component with a logger.
   * 
   * @param logger the logger
   */
  public void enableLogging(Logger logger) {
    if (this.log == null) {
      this.log = logger;
    }
  }


  /**
   * Create a class loader from a list of directories
   *
   * @param repositories List of searchable directories
   */
  protected RepositoryClassLoader(Vector repositories) {
      this();
      Iterator i = repositories.iterator();
      while (i.hasNext()) {
          try {
              this.addDirectory((File) i.next());
          } catch (IOException ioe) {
              log.error("Repository could not be added", ioe);
          }
      }
  }

  /**
   * Add a directory to the list of searchable repositories.
   * This methods ensures that no directory is specified more than once.
   *
   * @param repository The directory path
   * @exception IOException Non-existent, non-readable or non-directory
   * repository
   */
  public void addDirectory(File repository) throws IOException {
      try {
          this.addURL(repository.getCanonicalFile().toURL());
      } catch (MalformedURLException mue) {
          log.error("The repository had a bad URL", mue);
          throw new CascadingIOException("Could not add repository", mue);
      }
  }

  /**
   * Add a directory to the list of searchable repositories.
   * This methods ensures that no directory is specified more than once.
   *
   * @param repository The directory path
   * @exception IOException Non-existent, non-readable or non-directory
   * repository
   */
  public void addDirectory(String repository) throws IOException {
      try {
          File file = new File(repository);
          this.addURL(file.getCanonicalFile().toURL());
      } catch (MalformedURLException mue) {
          log.error("The repository had a bad URL", mue);
          throw new CascadingIOException("Could not add repository", mue);
      }
  }

  /**
   * Add a url to the list of searchable repositories
   */
  public void addURL(URL url) {
      super.addURL(url);
  }

  /**
   * Create a Class from a byte array
   */
  public Class defineClass(byte [] b) throws ClassFormatError {
      return super.defineClass(null, b, 0, b.length);
  }

}
