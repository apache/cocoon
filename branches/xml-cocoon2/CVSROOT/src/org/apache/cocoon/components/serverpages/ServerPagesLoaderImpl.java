/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.serverpages;

import java.io.File;
import java.io.FileReader;

import org.apache.arch.Composer;
import org.apache.arch.ComponentManager;

import org.apache.arch.config.Configurable;
import org.apache.arch.config.Configuration;
import org.apache.arch.config.ConfigurationException;

import org.apache.arch.named.NamedComponentManager;

import org.apache.cocoon.Parameters;

import org.apache.cocoon.components.store.MemoryStore;
import org.apache.cocoon.components.store.FilesystemStore;

import org.apache.cocoon.generators.ServerPagesGenerator;

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
 * The default implementation of <code>ServerPagesGeneratorLoader</code>
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-05-23 23:10:09 $
 */
public class ServerPagesLoaderImpl
  implements ServerPagesLoader, Composer, Configurable
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
   * Load a <code>Generator</code> built from an XML document written in a
   * <code>MarkupLanguage</code>
   *
   * @param file The input document's <code>File</code>
   * @param markupLanguage The <code>MarkupLanguage</code> in which the input
   * document is written
   * @param programmingLanguage The <code>ProgrammingLanguage</code> in which
   * the generator must be written
   * @return The loaded generator
   * @exception Exception If an error occurs during generation or loading
   */
  public ServerPagesGenerator load(
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
    Object generatorProgram = null;
    ServerPagesGenerator generator = null;

    synchronized(filename.intern()) {
      // Attempt to load generator class from cache
      generatorProgram = this.cache.get(filename);
  
      if (generatorProgram == null) {
        try {
          // FIXME: Why pass null as encoding?
          generatorProgram = programmingLanguage.load(
            normalizedName, this.repositoryName, null
          );
  
	  // Instantiate generator
          generator = (ServerPagesGenerator)
	    programmingLanguage.instantiate(generatorProgram);

          // Store loaded program in cache
          this.cache.store(filename, generatorProgram);
        } catch (LanguageException e) {
	}
      } else {
        generator = (ServerPagesGenerator)
	  programmingLanguage.instantiate(generatorProgram);
      }
      
      if (
	  this.autoReload &&
          generator != null && (
            generator.dateCreated() < file.lastModified() ||
            generator.hasChanged()
          )
      )
      {
        // Unload program
        programmingLanguage.unload(
	  generatorProgram, normalizedName, this.repositoryName
        );
  
        // Invalidate previous generator
	generator = null;
        generatorProgram = null;
      }
  
      if (generatorProgram == null) {
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
        generatorProgram = programmingLanguage.load(
	  normalizedName, this.repositoryName, encoding
	);

	// Instantiate generator
        generator = (ServerPagesGenerator)
	  programmingLanguage.instantiate(generatorProgram);

        // Store generated program in cache
        this.cache.store(filename, generatorProgram);
      }
    }

    return generator;
  }
}
