/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.programming;

import java.io.File;

import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;

import org.apache.avalon.utils.Parameters;
import org.apache.avalon.AbstractNamedComponent;

import org.apache.cocoon.components.language.LanguageException;

/**
 * Base implementation of <code>ProgrammingLanguage</code>. This class sets the
 * <code>CodeFormatter</code> instance and deletes source program files after
 * unloading.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2000-07-29 18:30:30 $
 */
public abstract class AbstractProgrammingLanguage
  extends AbstractNamedComponent
  implements ProgrammingLanguage
{
  /** The source code formatter */
  protected Class codeFormatter;

  /**
   * Set the configuration parameters. This method instantiates the
   * sitemap-specified source code formatter
   *
   * @param params The configuration parameters
   * @exception Exception If the language compiler cannot be loaded
   */
  protected void setParameters(Parameters params) throws Exception
  {
    try {
      String className = params.getParameter("code-formatter", null);
      if (className != null) {
        this.codeFormatter =
	  this.getClass().getClassLoader().loadClass(className);
      }
    } catch (Exception e) { }
  }

  /**
   * Return this language's source code formatter. A new formatter instance is
   * created on each invocation.
   *
   * @return The language source code formatter
   */
  public CodeFormatter getCodeFormatter() {
    if (this.codeFormatter != null) {
      try {
        return (CodeFormatter) this.codeFormatter.newInstance();
      } catch (Exception e) { }
    }

    return null;
  }

  /**
   * Unload a previously loaded program
   *
   * @param program A previously loaded object program
   * @exception LanguageException If an error occurs during unloading
   */
  protected abstract void doUnload(
    Object program, String filename, String baseDirectory
  )
    throws LanguageException;

  public final void unload(
    Object program, String filename, String baseDirectory
  )
    throws LanguageException
  {
    File file = new File (
      baseDirectory + File.separator +
      filename + "." + this.getSourceExtension()
    );

    file.delete();

    this.doUnload(program, filename, baseDirectory);
  }
}
