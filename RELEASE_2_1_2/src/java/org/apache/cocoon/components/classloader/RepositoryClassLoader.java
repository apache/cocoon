/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: RepositoryClassLoader.java,v 1.1 2003/03/09 00:08:48 pier Exp $
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
