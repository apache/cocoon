/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.classloader;

import java.util.Vector;
import java.util.Iterator;

import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

import org.apache.cocoon.util.IOUtils;
import org.apache.cocoon.util.ClassUtils;

import org.apache.log.Logger;
import org.apache.log.LogKit;

/**
 * A class loader with a growable list of path search directories
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.14 $ $Date: 2000-12-15 17:57:54 $
 */
class RepositoryClassLoader extends URLClassLoader {
  /**
   * The logger
   */
  protected Logger log = LogKit.getLoggerFor("cocoon");

  /**
   * Create an empty new class loader.
   */
  public RepositoryClassLoader() {
    super(new URL[] {}, ClassUtils.getClassLoader());
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
   * @param directoryName The path directory
   * @exception IOException Non-existent, non-readable or non-directory
   * repository
   */
  public void addDirectory(File repository) throws IOException {
      try {
          this.addURL(repository.toURL());
      } catch (MalformedURLException mue) {
          log.error("The repository had a bad URL", mue);
          throw new IOException("Could not add repository");
      }
  }
}
