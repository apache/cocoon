/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.programming;

import java.io.File;

import org.apache.avalon.utils.Parameters;

import org.apache.avalon.Composer;
import org.apache.avalon.Component;
import org.apache.avalon.ComponentManager;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.components.language.LanguageException;

/**
 * A compiled programming language. This class extends
 * <code>AbstractProgrammingLanguage</code> adding support for compilation
 * and object program files
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.7 $ $Date: 2000-09-19 00:27:14 $
 */
public abstract class CompiledProgrammingLanguage
  extends AbstractProgrammingLanguage
  implements Composer
{
  /** The compiler */
  protected Class compilerClass;

  /** The component manager */
  protected ComponentManager manager;

  /** The local classpath */
  protected String classpath;

  /** The source deletion option */
  protected boolean deleteSources = false;

  /**
   * Set the configuration parameters. This method instantiates the
   * sitemap-specified language compiler
   *
   * @param params The configuration parameters
   * @exception Exception If the language compiler cannot be loaded
   */
  protected void setParameters(Parameters params) throws Exception {
    super.setParameters(params);

    String compilerClass = params.getParameter("compiler", null);
    if (compilerClass == null) {
      throw new LanguageException(
        "Missing 'compiler' parameter for compiled language '" +
	this.getName() + "'"
      );
    }
    this.compilerClass = ClassUtils.loadClass(compilerClass);

    this.deleteSources = params.getParameterAsBoolean("delete-sources", false);
  }

  /**
   * Set the global component manager
   *
   * @param manager The global component manager
   */
  public void setComponentManager(ComponentManager manager) {
    this.manager = manager;
    this.classpath = ((Cocoon) this.manager.getComponent("cocoon")).getClasspath();
  }

  /**
   * Return the language's canonical object file extension.
   *
   * @return The object file extension
   */
  public abstract String getObjectExtension();

  /**
   * Unload a previously loaded program
   *
   * @param program A previously loaded object program
   * @exception LanguageException If an error occurs during unloading
   */
  public abstract void doUnload(Object program) throws LanguageException;

  /**
   * Unload a previously loaded program given its original filesystem location
   *
   * @param program The previously loaded object program
   * @param filename The base filename of the object program
   * @param baseDirectory The directory contaning the object program file
   * @return the value
   * @exception EXCEPTION_NAME If an error occurs
   */
  protected final void doUnload(
    Object program, String filename, String baseDirectory
  )
    throws LanguageException
  {
    File file = new File (
      baseDirectory + File.separator +
      filename + "." + this.getObjectExtension()
    );

    file.delete();

    this.doUnload(program);
  }

  /**
   * Actually load an object program from a file.
   *
   * @param filename The object program base file name
   * @param baseDirectory The directory containing the object program file
   * @return The loaded object program
   * @exception LanguageException If an error occurs during loading
   */
  protected abstract Object loadProgram(String filename, String baseDirectory)
    throws LanguageException;

  /**
   * Compile a source file yielding a loadable object file.
   *
   * @param filename The object program base file name
   * @param baseDirectory The directory containing the object program file
   * @param encoding The encoding expected in the source file or
   * <code>null</code> if it is the platform's default encoding
   * @exception LanguageException If an error occurs during compilation
   */
  protected abstract void compile(
    String filename, String baseDirectory, String encoding
  ) throws LanguageException;

  /**
   * Load an object program from a file. This method compiled the
   * corresponding source file if necessary
   *
   * @param filename The object program base file name
   * @param baseDirectory The directory containing the object program file
   * @param encoding The encoding expected in the source file or
   * <code>null</code> if it is the platform's default encoding
   * @return The loaded object program
   * @exception LanguageException If an error occurs during compilation
   */
  public Object load(String filename, String baseDirectory, String encoding)
    throws LanguageException
  {

    // Does object file exist? Load and return instance
    File objectFile = new File(
      baseDirectory + File.separator +
      filename + "." + this.getObjectExtension()
    );

    if (objectFile.exists() && objectFile.isFile() && objectFile.canRead()) {
      return this.loadProgram(filename, baseDirectory);
    }

    // Does source file exist?
    File sourceFile = new File(
      baseDirectory + File.separator +
      filename + "." + this.getSourceExtension()
    );

    if (sourceFile.exists() && sourceFile.isFile() && sourceFile.canRead()) {
      this.compile(filename, baseDirectory, encoding);

      if (this.deleteSources) {
        sourceFile.delete();
      }

      return this.loadProgram(filename, baseDirectory);
    }

    throw new LanguageException(
      "Can't load program: " +
      baseDirectory + File.separator + filename
    );
  }
}
