/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.components.language.generator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;

import org.apache.avalon.Modifiable;

import org.apache.avalon.Component;
import org.apache.avalon.Composer;
import org.apache.avalon.ComponentManager;
import org.apache.avalon.Configurable;
import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;
import org.apache.avalon.ComponentSelector;

import org.apache.avalon.Parameters;

import org.apache.cocoon.Cocoon;
import org.apache.cocoon.Roles;

import org.apache.cocoon.components.store.MemoryStore;
import org.apache.cocoon.components.store.FilesystemStore;

import org.apache.cocoon.components.language.LanguageException;
import org.apache.cocoon.components.language.markup.MarkupLanguage;
import org.apache.cocoon.components.language.programming.CodeFormatter;
import org.apache.cocoon.components.language.programming.ProgrammingLanguage;

import org.apache.cocoon.util.IOUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Document;

import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * The default implementation of <code>ProgramGenerator</code>
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.14 $ $Date: 2000-10-19 20:21:15 $
 */
public class ProgramGeneratorImpl
  implements ProgramGenerator, Composer, Configurable
{
  /** The auto-reloading option */
  protected boolean autoReload = true;

  /** The in-memory store */
  protected MemoryStore cache = new MemoryStore();

  /** The component manager */
  protected ComponentManager manager;

  /** The markup language component selector */
  protected ComponentSelector markupSelector;

  /** The programming language component selector */
  protected ComponentSelector languageSelector;

  /** The working directory */
  protected String workDir;

  /**
   * Set the global component manager. This method also sets the
   * <code>ComponentSelector</code> used as language factory for both
   * markup and programming languages.
   *
   * @param manager The global component manager
   */
  public void compose(ComponentManager manager) {
    this.manager = manager;
    this.markupSelector = (ComponentSelector) this.manager.lookup(Roles.MARKUP_LANGUAGE);
    this.languageSelector = (ComponentSelector) this.manager.lookup(Roles.PROGRAMMING_LANGUAGE);
    this.workDir = ((Cocoon) this.manager.lookup(Roles.COCOON)).getWorkDir();
  }

  /**
   * Set the sitemap-provided configuration. This method sets the persistent
   * code repository and the auto-reload option
   *
   * @param conf The configuration information
   * @exception ConfigurationException Not thrown here
   */
  public void configure(Configuration conf)
    throws ConfigurationException
  {
    Parameters params = Parameters.fromConfiguration(conf);
    this.autoReload = params.getParameterAsBoolean("auto-reload", autoReload);
  }

  /**
   * Load a program built from an XML document written in a
   * <code>MarkupLanguage</code>
   *
   * @param file The input document's <code>File</code>
   * @param markupLanguage The <code>MarkupLanguage</code> in which the input
   * document is written
   * @param programmingLanguage The <code>ProgrammingLanguage</code> in which
   * the program must be written
   * @return The loaded program instance
   * @exception Exception If an error occurs during generation or loading
   */
  public Object load(
    File file, String markupLanguageName, String programmingLanguageName,
    EntityResolver resolver
  ) throws Exception {
    // Get markup and programming languages
    MarkupLanguage markupLanguage = (MarkupLanguage)
      this.markupSelector.select(markupLanguageName);

    ProgrammingLanguage programmingLanguage = (ProgrammingLanguage)
      this.languageSelector.select(programmingLanguageName);

    programmingLanguage.setLanguageName(programmingLanguageName);

    if (markupLanguage instanceof Composer) {
        ((Composer) markupLanguage).compose(this.manager);
    }

    if (programmingLanguage instanceof Composer) {
        ((Composer) programmingLanguage).compose(this.manager);
    }

    // Create filesystem store
    FilesystemStore repository = new FilesystemStore(this.workDir);

    // Set filenames
    String filename = IOUtils.getFullFilename(file);
    String normalizedName = repository.normalizedFilename(filename);
    String sourceExtension = programmingLanguage.getSourceExtension();

    // Ensure no 2 requests for the same file overlap
    Object program = null;
    Object programInstance = null;

    synchronized(filename.intern()) {
      // Attempt to load program object from cache
      program = this.cache.get(filename);

      try {
        if (program == null) {
          /*
             FIXME: Passing null as encoding may result in invalid
             recompilation under certain circumstances!
          */
          program = programmingLanguage.load(
            normalizedName, this.workDir, null
          );

          // Store loaded program in cache
          this.cache.store(filename, program);
        }

        // Instantiate program
        programInstance = programmingLanguage.instantiate(program);
      } catch (LanguageException e) { }

      /*
         FIXME: It's the program (not the instance) that must
         be queried for changes!!!
      */
      if (
          this.autoReload &&
          programInstance != null &&
          programInstance instanceof Modifiable &&
          ((Modifiable) programInstance).modifiedSince(file.lastModified())
      )
      {
        // Unload program
        programmingLanguage.unload(
          program, normalizedName, this.workDir
        );

        // Invalidate previous program/instance pair
        program = null;
        programInstance = null;
      }

      if (program == null) {
        // Generate code
        String code = markupLanguage.generateCode(
          new InputSource(new FileReader(file)), normalizedName, programmingLanguage, resolver
        );

        String encoding = markupLanguage.getEncoding();

        // Format source code if applicable
        CodeFormatter codeFormatter = programmingLanguage.getCodeFormatter();
        if (codeFormatter != null) {
          code = codeFormatter.format(code, encoding);
        }

        // Store generated code
        String sourceFilename = filename + "." + sourceExtension;
        repository.store(sourceFilename, code);

        // Verify source file generation was successful
        File sourceFile = (File) repository.get(sourceFilename);
        if (sourceFile == null) {
          throw new IOException(
            "Error creating source file: " + sourceFilename
          );
        }

        // [Compile]/Load generated program
        program = programmingLanguage.load(
          normalizedName, this.workDir, encoding
        );

        // Store generated program in cache
        this.cache.store(filename, program);
      }

      // Instantiate
      programInstance = programmingLanguage.instantiate(program);
    }

    return programInstance;
  }
}
