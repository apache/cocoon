/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.components.language.programming.java;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import org.apache.avalon.Parameters;

import org.apache.avalon.Composer;
import org.apache.avalon.Component;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.ThreadSafe;

import org.apache.cocoon.Roles;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.JavaArchiveFilter;
import org.apache.cocoon.components.classloader.ClassLoaderManager;
import org.apache.cocoon.components.language.programming.*;
import org.apache.cocoon.components.language.LanguageException;

/**
 * The Java programming language processor
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.16 $ $Date: 2000-12-11 15:06:02 $
 */
public class JavaLanguage extends CompiledProgrammingLanguage implements ThreadSafe {

  /** The class loader */
  private ClassLoaderManager classLoaderManager;

  /**
   * Return the language name
   *
   * @return The language name
   */
  public String getName() {
    return "java";
  }

  /**
   * Return the language's canonical source file extension.
   *
   * @return The source file extension
   */
  public String getSourceExtension() {
    return "java";
  }

  /**
   * Return the language's canonical object file extension.
   *
   * @return The object file extension
   */
  public String getObjectExtension() {
    return "class";
  }

  /**
   * Set the configuration parameters. This method instantiates the
   * sitemap-specified <code>ClassLoaderManager</code>
   *
   * @param params The configuration parameters
   * @exception Exception If the class loader manager cannot be instantiated
   */
  protected void setParameters(Parameters params) throws Exception {
    super.setParameters(params);

    String compilerClass = params.getParameter("class-loader", "org.apache.cocoon.components.classloader.ClassLoaderManagerImpl");
    if (compilerClass != null) {
        this.classLoaderManager = (ClassLoaderManager) ClassUtils.newInstance(compilerClass);
    }
  }

  /**
   * Set the global component manager. This methods initializes the class
   * loader manager if it was not (successfully) specified in the language
   * parameters
   *
   * @param manager The global component manager
   */
  public void compose(ComponentManager manager) {
    super.compose(manager);

    if (this.classLoaderManager == null) {
      try {
          log.debug("Looking up " + Roles.CLASS_LOADER);
          this.classLoaderManager =
            (ClassLoaderManager) this.manager.lookup(Roles.CLASS_LOADER);
      } catch (Exception e) {
          log.error("Could not find component", e);
      }
    }
  }

  /**
   * Actually load an object program from a class file.
   *
   * @param filename The object program base file name
   * @param baseDirectory The directory containing the object program file
   * @return The loaded object program
   * @exception LanguageException If an error occurs during loading
   */
  protected Object loadProgram(String name, File baseDirectory)
    throws LanguageException
  {
    try {
      this.classLoaderManager.addDirectory(baseDirectory);
      return
        this.classLoaderManager.loadClass(name.replace(File.separatorChar, '.'));
    } catch (Exception e) {
      log.warn("Could not load class for program '" + name + "'", e);
      throw new LanguageException("Could not load class for program '" + name + "' due to a " + e.getClass().getName() + ": " + e.getMessage());
    }
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

      AbstractJavaCompiler compiler = (AbstractJavaCompiler) this.compilerClass.newInstance();

      int pos = name.lastIndexOf(File.separatorChar);
      String filename = name.substring(pos + 1);
      String pathname =
        baseDirectory.getCanonicalPath() + File.separator +
        name.substring(0, pos).replace(File.separatorChar, '/');

      compiler.setFile(
        pathname + File.separator +
        filename + "." + this.getSourceExtension()
      );

      compiler.setSource(pathname);

      compiler.setDestination(baseDirectory.getCanonicalPath());

      String systemClasspath = System.getProperty("java.class.path");
      String systemExtDirs = System.getProperty("java.ext.dirs");
      compiler.setClasspath(
        baseDirectory.getCanonicalPath() +
        ((classpath != null) ? File.pathSeparator + classpath : "") +
        ((systemClasspath != null) ? File.pathSeparator + systemClasspath : "") +
        ((systemExtDirs != null) ? File.pathSeparator + expandDirs(systemExtDirs) : "")
      );

      if (encoding != null) {
        compiler.setEncoding(encoding);
      }

      if (!compiler.compile()) {
        StringBuffer message = new StringBuffer("Error compiling " + filename + ":\n");

        List errors = compiler.getErrors();
        int count = errors.size();
        for (int i = 0; i < count; i++) {
          CompilerError error = (CompilerError) errors.get(i);
          message.append("Line " + error.getStartLine()
            + ", column " + error.getStartColumn()
            + ": " + error.getMessage());
        }

        throw new LanguageException(message.toString());
      }

    } catch (InstantiationException e) {
      log.warn("Could not instantiate the compiler", e);
      throw new LanguageException("Could not instantiate the compiler: " + e.getMessage());
    } catch (IllegalAccessException e) {
      log.warn("Could not access the compiler class", e);
      throw new LanguageException("Could not access the compiler class: " + e.getMessage());
    } catch (IOException e) {
      log.warn("Error during compilation", e);
      throw new LanguageException("Error during compilation: " + e.getMessage());
    }
  }

  /**
   * Create a new instance for the given class
   *
   * @param program The Java class
   * @return A new class instance
   * @exception LanguageException If an instantiation error occurs
   */
  public Object instantiate(Object program) throws LanguageException {
    try {
      return ((Class) program).newInstance();
    } catch (Exception e) {
      log.warn("Could not instantiate program instance", e);
      throw new LanguageException("Could not instantiate program instance due to a " + e.getClass().getName() + ": " + e.getMessage());
    }
  }

  /**
   * Unload a previously loaded class. This method simply reinstantiates the
   * class loader to ensure that a new version of the same class will be
   * correctly loaded in a future loading operation
   *
   * @param program A previously loaded class
   * @exception LanguageException If an error occurs during unloading
   */
  public void doUnload(Object program) throws LanguageException {
    this.classLoaderManager.reinstantiate();
  }

  /**
   * Escape a <code>String</code> according to the Java string constant
   * encoding rules.
   *
   * @param constant The string to be escaped
   * @return The escaped string
   */
  public String quoteString(String constant) {
    char chr[] = constant.toCharArray();
    StringBuffer buffer = new StringBuffer();

    for (int i = 0; i < chr.length; i++) {
      switch (chr[i]) {
        case '\t':
          buffer.append("\\t");
          break;
        case '\r':
          buffer.append("\\r");
          break;
        case '\n':
          buffer.append("\\n");
          break;
        case '"':
        case '\\':
          buffer.append('\\');
          // Fall through
        default:
          buffer.append(chr[i]);
          break;
      }
    }

    return buffer.toString();
  }

  private String expandDirs(String d) {
    File dir = new File(d);
    File[] files = dir.listFiles(new JavaArchiveFilter());
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < files.length; i++) {
        buffer.append(files[i] + File.pathSeparator);
    }
    return buffer.toString();
  }
}
