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
import org.apache.avalon.NamedComponentManager;

import org.apache.avalon.utils.Parameters;

import org.apache.cocoon.Cocoon;

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
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;

/**
 * The default implementation of <code>ProgramGenerator</code>
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.9 $ $Date: 2000-08-31 15:48:14 $
 */
public class ProgramGeneratorImpl
  implements ProgramGenerator, Composer, Configurable
{
  /** The in-memory store */
  protected MemoryStore cache = new MemoryStore();

  /** The component manager */
  protected ComponentManager manager;

  /** The named component manager */
  protected NamedComponentManager factory;

  /** The filesystem-based store */
  protected String repositoryName = null;

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

    this.factory = (NamedComponentManager) this.manager.getComponent("factory");
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

    //this.repositoryName = params.getParameter("repository");
    if (this.repositoryName == null) {
        this.repositoryName = System.getProperty(Cocoon.TEMPDIR_PROPERTY, Cocoon.DEFAULT_TEMP_DIR);
    }
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
            normalizedName, this.repositoryName, null
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
          document, normalizedName, programmingLanguage, resolver
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

        // Store generated program in cache
        this.cache.store(filename, program);
      }

      // Instantiate
      programInstance = programmingLanguage.instantiate(program);
    }

    return programInstance;
  }
}
