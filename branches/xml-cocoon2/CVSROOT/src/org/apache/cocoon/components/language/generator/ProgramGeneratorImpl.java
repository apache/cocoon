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

import org.apache.arch.Modifiable;

import org.apache.arch.Composer;
import org.apache.arch.ComponentManager;

import org.apache.arch.config.Configurable;
import org.apache.arch.config.Configuration;
import org.apache.arch.config.ConfigurationException;

import org.apache.arch.named.NamedComponentManager;

import org.apache.cocoon.Parameters;

import org.apache.cocoon.components.store.MemoryStore;
import org.apache.cocoon.components.store.FilesystemStore;


import org.apache.cocoon.components.language.LanguageException;
import org.apache.cocoon.components.language.markup.MarkupLanguage;
import org.apache.cocoon.components.language.programming.CodeFormatter;
import org.apache.cocoon.components.language.programming.ProgrammingLanguage;

import org.apache.cocoon.util.IOUtils;
import org.apache.cocoon.util.DOMUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Document;


import org.xml.sax.InputSource;
import org.apache.arch.Component;

import java.io.IOException;
import org.xml.sax.SAXException;
import java.io.FileNotFoundException;

/**
 * The default implementation of <code>ProgramGenerator</code>
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-05-24 20:17:03 $
 */
public class ProgramGeneratorImpl
  implements ProgramGenerator, Composer, Configurable
{
  /** The in-memory store */
  protected MemoryStore cache = new MemoryStore();

  /** The filesystem-based store */
  protected String repositoryName;

  /** The component manager */
  protected ComponentManager manager;

  /** The named component manager */
  protected NamedComponentManager factory;

  /** The auto-reloading option */
  protected boolean autoReload = true;

  /**
   * Set the global component manager. This method also sets the
   * <code>NamedComponentManager</code> used as language factory for both
   * markup and programming languages.
   *
   * @param manager The global component manager
   */
  public void setComponentManager(ComponentManager manager) {
    this.manager = manager;

    this.factory =
      (NamedComponentManager) this.manager.getComponent("factory");
  }

  /**
   * Set the sitemap-provided configuration. This method sets the persistent
   * code repository and the auto-reload option
   *
   * @param conf The configuration information
   * @exception ConfigurationException Not thrown here
   */
  public void setConfiguration(Configuration conf)
    throws ConfigurationException
  {
    Parameters params = Parameters.fromConfiguration(conf);

    this.repositoryName = params.getParameter("repository", "./repository");
    this.autoReload = params.getParameterAsBoolean("auto-reload", true);
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
  public Modifiable load(
    File file, String markupLanguageName, String programmingLanguageName
  ) throws Exception {
    // Get markup and programming languages
    MarkupLanguage markupLanguage = (MarkupLanguage)
      this.factory.getComponent("markup-language", markupLanguageName);

    ProgrammingLanguage programmingLanguage = (ProgrammingLanguage)
      this.factory.getComponent("programming-language", programmingLanguageName);

    // Create filesystem store
    FilesystemStore repository = new FilesystemStore(this.repositoryName);

    // Set filenames
    String filename = IOUtils.getFullFilename(file);
    String normalizedName = repository.normalizedFilename(filename);
    String sourceExtension = programmingLanguage.getSourceExtension();

    // Ensure no 2 requests for the same file overlap
    Object program = null;
    Modifiable programInstance = null;

    synchronized(filename.intern()) {
      // Attempt to load program object from cache
      program = this.cache.get(filename);
  
      try {
        if (program == null) {
          // FIXME: Why pass null as encoding?
          program = programmingLanguage.load(
            normalizedName, this.repositoryName, null
          );
  
	  // Instantiate program
          programInstance =
	    (Modifiable) programmingLanguage.instantiate(program);
	}

        programInstance = (Modifiable) programmingLanguage.instantiate(program);

        // Store loaded program in cache
        this.cache.store(filename, program);
      } catch (LanguageException e) {}
      
      if (
	  this.autoReload &&
          programInstance != null &&
	  programInstance.modifiedSince(file.lastModified())
      )
      {
        // Unload program
        programmingLanguage.unload(
	  program, normalizedName, this.repositoryName
        );
  
        // Invalidate previous program/instance pair
        program = null;
	programInstance = null;
      }
  
      if (program == null) {
        // Generate code
        Document document =
	  DOMUtils.DOMParse(new InputSource(new FileReader(file)));
        String encoding = markupLanguage.getEncoding(document);
        String code = markupLanguage.generateCode(
          document, normalizedName, programmingLanguage
        );
  
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
	  normalizedName, this.repositoryName, encoding
	);

	// Instantiate
        programInstance = (Modifiable) programmingLanguage.instantiate(program);

        // Store generated program in cache
        this.cache.store(filename, program);
      }
    }

    return programInstance;
  }
}
