/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.programming.javascript;

import java.io.File;

import org.mozilla.javascript.*;
import org.mozilla.javascript.tools.jsc.Main;

import org.apache.cocoon.components.language.programming.java.*;

import org.apache.cocoon.components.language.LanguageException;

/**
 * The compiled Javascript (Rhino) programming language processor
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.5 $ $Date: 2000-12-08 20:39:27 $
 */
public class JavascriptLanguage extends JavaLanguage
{
  /**
   * Return the language name
   *
   * @return The language name
   */
  public String getName() {
    return "javascript";
  }

  /**
   * Return the language's canonical source file extension.
   *
   * @return The source file extension
   */
  public String getSourceExtension() {
    return "js";
  }

  /**
   * Compile a source file yielding a loadable class file.
   *
   * @param filename The object program base file name
   * @param baseDirectory The directory containing the object program file
   * @param encoding The encoding expected in the source file or
   * <code>null</code> if it is the platform's default encoding
   * @exception LanguageException If an error occurs during compilation
   */
  protected void compile(
    String name, File baseDirectory, String encoding
  ) throws LanguageException {
    try {
      Main compiler = (Main) this.compilerClass.newInstance();

      int pos = name.lastIndexOf(File.separatorChar);
      String filename = name.substring(pos + 1);
      String pathname =
        baseDirectory.getCanonicalPath() + File.separator +
        name.substring(0, pos).replace(File.separatorChar, '/');
      String packageName =
        name.substring(0, pos).replace(File.separatorChar, '.');

      String[] args = {
        "-extends",
        "org.apache.cocoon.components.language.markup.xsp.javascript.JSGenerator",
        "-nosource",
        "-O", "9",
        "-package", packageName,
        pathname + File.separator + filename + "." + this.getSourceExtension()
      };

      compiler.main(args);
    } catch (Exception e) {
      log.warn("JavascriptLanguage.compile", e);
      throw new LanguageException(e.getMessage());
    }
  }
}
