/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.programming;

import java.io.File;

import org.apache.avalon.Component;

import org.apache.cocoon.components.language.LanguageException;

/**
 * This interface states the functionality of a programming language processor
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2000-12-07 17:10:49 $
 */
public interface ProgrammingLanguage extends Component {
  /**
   * Return the programming language's source file extension
   *
   * @return The canonical source file extension
   */
  public String getSourceExtension();

  /**
   * Load a program from a file
   *
   * @param filename The program base file name
   * @param baseDirectory The directory containing the program file
   * @param encoding The encoding expected in the source file or
   * <code>null</code> if it is the platform's default encoding
   * @return The loaded program
   * @exception LanguageException If an error occurs during loading
   */
  public Object load(String filename, File baseDirectory, String encoding)
    throws LanguageException;

  /**
   * Create a new instance for the given program type
   *
   * @param program The program type
   * @return A new program type instance
   * @exception LanguageException If an instantiation error occurs
   */
  public Object instantiate(Object program) throws LanguageException;

  /**
   * Unload from memory and invalidate a given program
   *
   * @param program The program
   * @param filename The name of the file this program was loaded from
   * @param baseDirectory The directory containing the program file
   * @exception LanguageException If an error occurs
   */
  public void unload (Object program, String filename, File baseDirectory)
    throws LanguageException;

  /**
   * Return the <code>CodeFormatter</code> associated with this programming
   * language
   *
   * @return The code formatter object or <code>null</code> if none is
   * available
   */
  public CodeFormatter getCodeFormatter();

  /**
   * Escape a <code>String</code> according to the programming language's
   * string constant encoding rules.
   *
   * @param constant The string to be escaped
   * @return The escaped string
   */
  public String quoteString(String constant);

  /**
   * Set Language Name
   *
   * @param name The name of the language
   */
  public void setLanguageName(String name);

  /**
   * Get Language Name
   *
   * @return The name of the language
   */
  public String getLanguageName();
}
