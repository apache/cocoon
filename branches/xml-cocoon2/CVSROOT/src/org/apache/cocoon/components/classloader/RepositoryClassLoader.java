/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.classloader;

import java.util.Vector;

import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;

import org.apache.cocoon.util.IOUtils;
import org.apache.cocoon.util.ClassUtils;

import org.apache.log.Logger;
import org.apache.log.LogKit;

/**
 * A class loader with a growable list of path search directories
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.12 $ $Date: 2000-12-13 14:46:24 $
 */
class RepositoryClassLoader extends ClassLoader {
  /**
   * The logger
   */
  protected Logger log = LogKit.getLoggerFor("cocoon");
  /**
   * The list of searchable directories
   */
  protected Vector repositories;

  /**
   * Create an empty new class loader.
   */
  public RepositoryClassLoader() {
    super(RepositoryClassLoader.class.getClassLoader());
    this.repositories = new Vector();
  }

  /**
   * Create a class loader from a list of directories
   *
   * @param repositories List of searchable directories
   */
  protected RepositoryClassLoader(Vector repositories) {
    this.repositories = repositories;
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
    String fullFilename = null;

    if (repository == null) {
        throw new IOException("You cannot add a null directory");
    }

    // Ensure the same directory isn't specified twice
    try {
        int count = this.repositories.size();
        fullFilename = IOUtils.getFullFilename(repository);

        for (int i = 0; i < count; i++) {
            File directory = (File) this.repositories.elementAt(i);
            if (fullFilename.equals(IOUtils.getFullFilename(directory))) {
                return;
            }
        }

        if (!repository.exists()) {
          throw new IOException("Non-existent: " + fullFilename);
        }

        if (!repository.isDirectory()) {
          throw new IOException("Not a directory: " + fullFilename);
        }

        if (!(repository.canRead() && repository.canWrite())) {
          throw new IOException("Not readable/writable: " + fullFilename);
        }

        this.repositories.addElement(repository);
    } catch (SecurityException se) {
        log.debug("RepositoryClassLoader:SecurityException", se);
        throw new IOException("Cannot access directory");
    }
  }

  /**
   * Load a class using the parent class loader or, failing that,
   * from one of the stored repositories in definition order
   *
   * @param name The class name
   * @param resolve Whether the class name must be resolved
   * @return The loaded class
   * @exception ClassNotFoundException If the class is not found in any of the
   * repositories
   */
  protected Class loadClass (String name, boolean resolve)
    throws ClassNotFoundException
  {
    Class c = findLoadedClass(name);

    if (c == null) {
      try {
        c = findSystemClass(name);
      } catch (ClassNotFoundException e) {
        log.debug("Could not load class " + name + "trying to load from the repository");
        byte[] bits = this.loadClassData (name);

        if (bits == null) {
          ClassLoader cl = ClassUtils.getClassLoader();

          if (cl != null)  {
            c = cl.loadClass (name);
          }
        } else {
          c = defineClass (null, bits, 0, bits.length);

          if (resolve) {
            resolveClass (c);
          }
        }
      }

      if (c == null) {
        throw new ClassNotFoundException (name);
      }
    }

    return c;
  }

    /**
    * Load class from a file contained in a repository.
    *
    * @param className The class name
    * @return An array of byes containing the class data or <code>null</code> if
    * not founfd
    */
    protected byte[] loadClassData (String className) {
        int count = this.repositories.size();
        for (int i = 0; i < count; i++) {
            File repository = (File) this.repositories.elementAt(i);
            File file = new File(repository, this.getClassFilename(className));

            if (file.exists() && file.isFile() && file.canRead()) {
                byte[] buffer = null;
                FileInputStream in = null;

                int n = 0;
                int pos = 0;
                buffer = new byte [(int) file.length ()];

                try {
                    boolean process = true;
                    log.debug("Loading file: " + file.getCanonicalPath());

                    in = new FileInputStream(file);

                    while (process) {
                        if (pos < buffer.length) {
                            n = in.read (buffer, pos, buffer.length - pos);
                            if (n != -1) {
                                pos += n;
                            } else {
                                process = false;
                            }
                        } else {
                            process = false;
                        }
                    }

                    log.debug(n + " Bytes read.");

                    return buffer;
                } catch (IOException e) {
                    log.warn("RepositoryClassLoader.IOException", e);
                } finally {
                    if (in != null) {
                        try { in.close(); }
                        catch (IOException e) { log.warn("Could not close stream", e); }
                    }
                }
            }
        }

        return null;
    }

  /**
   * Return the filename associated with a given class name in a given
   * directory
   *
   * @param className The class name for which a filename is to be generated
   * @param repository The directory containing the class file
   * @return The filename associated with a given class name in a given
   * directory
   */
  protected String getClassFilename(String className) {
    return className.replace('.', File.separatorChar) + ".class";
  }
}
