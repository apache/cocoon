/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.components.language.programming;

import java.util.List;
import org.apache.avalon.component.Component;

import java.io.IOException;

/**
 * This interface defines a compiler's functionality for all
 * (Java-based) compiled languages
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.2.6 $ $Date: 2001-04-20 20:49:57 $
 * @since 2.0
 */
public interface LanguageCompiler extends Component {
  /**
   * Set the name of the file containing the source program
   *
   * @param file The name of the file containing the source program
   */
  void setFile(String file);

  /**
   * Set the name of the directory containing the source program file
   *
   * @param srcDir The name of the directory containing the source program file
   */
  void setSource(String srcDir);

  /**
   * Set the name of the directory to contain the resulting object program file
   *
   * @param destDir The name of the directory to contain the resulting object
   * program file
   */
  void setDestination(String destDir);

  /**
   * Set the classpath to be used for this compilation
   *
   * @param classpath The classpath to be used for this compilation
   */
  void setClasspath(String classpath);

  /**
   * Set the encoding of the input source file or <code>null</code> to use the
   * platform's default encoding
   *
   * @param encoding The encoding of the input source file or <code>null</code>
   * to use the platform's default encoding
   */
  void setEncoding(String encoding);

  /**
   * Compile a source file yielding a loadable program file.
   *
   * @param filename The object program base file name
   * @param baseDirectory The directory containing the object program file
   * @param encoding The encoding expected in the source file or
   * <code>null</code> if it is the platform's default encoding
   * @exception LanguageException If an error occurs during compilation
   */
  boolean compile() throws IOException;

  /**
   * Return the list of errors generated by this compilation
   *
   * @return The list of errors generated by this compilation
   * @exception IOException If an error occurs during message collection
   */
  List getErrors() throws IOException;
}
