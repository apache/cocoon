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

import java.io.IOException;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

import org.apache.cocoon.util.NetUtils;
import org.apache.cocoon.util.IOUtils;
import org.apache.cocoon.util.ClassUtils;

import org.apache.avalon.logger.Loggable;
import org.apache.log.Logger;

/**
 * A class loader with a growable list of path search directories.
 * BL: Changed to extend URLClassLoader for both maintenance and
 *     compatibility reasons.  It doesn't hurt that it runs quicker
 *     now as well.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.23 $ $Date: 2001-04-20 20:49:49 $
 */
public class RepositoryClassLoader extends URLClassLoader implements Loggable {

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

  public void setLogger(Logger logger) {
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
   * @param directoryName The path directory
   * @exception IOException Non-existent, non-readable or non-directory
   * repository
   */
  public void addDirectory(File repository) throws IOException {
      try {
          this.addURL(repository.getCanonicalFile().toURL());
      } catch (MalformedURLException mue) {
          log.error("The repository had a bad URL", mue);
          throw new IOException("Could not add repository");
      }
  }

  /**
   * Create a Class from a byte array
   */
  public Class defineClass(byte [] b) throws ClassFormatError {
      return super.defineClass(null, b, 0, b.length);
  }
}
