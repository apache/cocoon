/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.language.markup;

import java.io.File;
import java.net.URL;
import java.util.Date;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import org.xml.sax.InputSource;
import org.xml.sax.EntityResolver;

import org.apache.avalon.Composer;
import org.apache.avalon.Component;
import org.apache.avalon.ComponentManager;

import org.apache.avalon.utils.Parameters;
import org.apache.avalon.AbstractNamedComponent;

import org.apache.avalon.Configuration;
import org.apache.avalon.ConfigurationException;

import org.apache.cocoon.util.IOUtils;
import org.apache.cocoon.components.store.MemoryStore;

import org.apache.cocoon.components.language.programming.ProgrammingLanguage;

import java.io.IOException;
import org.xml.sax.SAXException;
import java.net.MalformedURLException;

/**
 * Base implementation of <code>MarkupLanguage</code>. This class uses
 * logicsheets as the only means of code generation. Code generation should
 * be decoupled from this context!!! Moreover, this class uses DOM documents
 * (as opposed to Cocoon2's standard SAX events)
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-07-22 20:41:32 $
 */
public abstract class AbstractMarkupLanguage
  extends AbstractNamedComponent
  implements MarkupLanguage, Composer
{
  /**
   * The supported language table
   */
  protected Hashtable languages;

  /**
   * The in-memory code-generation logicsheet cache
   */
  protected MemoryStore logicsheetCache;

  /**
   * The markup language's namespace uri
   */
  protected String uri;

  /**
   * The markup language's namespace prefix
   */
  protected String prefix;

  /** The component manager */
  protected ComponentManager manager;

  /**
   * Set the global component manager.
   *
   * @param manager The sitemap-specified component manager
   */
  public void setComponentManager(ComponentManager manager) {
    this.manager = manager;
  }

  /**
   * The defualt constructor.
   */
  public AbstractMarkupLanguage() throws SAXException, IOException {
    // Initialize language table
    this.languages = new Hashtable();

    // Initialize logicsheet cache
    this.logicsheetCache = new MemoryStore();
  }

  /**
   * Initialize the (required) markup language namespace definition.
   *
   * @param params The sitemap-supplied parameters
   * @exception Exception Not actually thrown
   */
  protected void setParameters(Parameters params) throws Exception
  {
    this.uri = getRequiredParameter(params, "uri");
    this.prefix = getRequiredParameter(params, "prefix");
  }

  /**
   * Process additional configuration. Load supported programming language
   * definitions
   *
   * @param conf The language configuration
   * @exception ConfigurationException If an error occurs loading logichseets
   */
  protected void setAdditionalConfiguration(Configuration conf)
    throws ConfigurationException
  {
    try {
      Enumeration l = conf.getConfigurations("target-language");
      while (l.hasMoreElements()) {
        Configuration lc = (Configuration) l.nextElement();

        LanguageDescriptor language = new LanguageDescriptor();
        language.setName(lc.getAttribute("name"));

	Parameters lcp = Parameters.fromConfiguration(lc);
        String logicsheetLocation =
	  getRequiredParameter(lcp, "core-logicsheet");

        URL logicsheetURL = IOUtils.getURL(logicsheetLocation);
        String logicsheetName = logicsheetURL.toExternalForm();
        Logicsheet logicsheet = new Logicsheet();
        logicsheet.setInputSource(new InputSource(logicsheetURL.openStream()));
        CachedURL entry = new CachedURL(logicsheetURL, logicsheet);
        this.logicsheetCache.store(logicsheetName, entry);
        language.setLogicsheet(logicsheetName);

        Enumeration n = lc.getConfigurations("builtin-logicsheet");
        while (n.hasMoreElements()) {
          Configuration nc = (Configuration) n.nextElement();
	  Parameters ncp = Parameters.fromConfiguration(nc);

          String namedLogicsheetPrefix = getRequiredParameter(ncp, "prefix");
          String namedLogicsheetUri = getRequiredParameter(ncp, "uri");
          String namedLogicsheetLocation = getRequiredParameter(ncp, "href");

          // FIXME: This is repetitive; add method for both cases
          URL namedLogicsheetURL = IOUtils.getURL(namedLogicsheetLocation);
          String namedLogicsheetName = namedLogicsheetURL.toExternalForm();
          NamedLogicsheet namedLogicsheet = new NamedLogicsheet();
          namedLogicsheet.setInputSource(
            new InputSource(namedLogicsheetURL.openStream())
          );
          namedLogicsheet.setPrefix(namedLogicsheetPrefix);
          namedLogicsheet.setUri(namedLogicsheetUri);
          CachedURL namedEntry =
            new CachedURL(namedLogicsheetURL, namedLogicsheet);
          this.logicsheetCache.store(namedLogicsheetName, namedEntry);
          language.addNamedLogicsheet(
            namedLogicsheetPrefix, namedLogicsheetName
          );
        }

        this.languages.put(language.getName(), language);
      }
    } catch (Exception e) {
e.printStackTrace();
      throw new ConfigurationException(e.getMessage(), conf);
    }
  }

  /**
   * Return the source document's encoding. This can be <code>null</code> for
   * the platform's default encoding. The default implementation returns
   * <code>null, but derived classes may override it if encoding applies to
   * their concrete languages. FIXME: There should be a way to get the
   * XML document's encoding as seen by the parser; unfortunately, this
   * information is not returned by current DOM or SAX parsers...
   *
   * @param document The input document
   * @return The document-specified encoding
   */
  public String getEncoding(Document document) {
    return null;
  }

  /**
   * Prepare the document for logicsheet processing and code generation. The
   * default implementation does nothing, but derived classes should (at least)
   * use the passed programming language to quote <code>Strings</code>
   *
   * @param document The input document
   * @param filename The input source filename
   * @param language The target programming language
   * @return The augmented document
   */
  protected Document preprocessDocument(
    Document document, String filename, ProgrammingLanguage language
  )
  {
    return document;
  }

  /**
   * Returns a list of logicsheets to be applied to this document for source
   * code generation.
   *
   * @param document The input document
   * @return An array of logicsheet <i>names</i>
   */
  protected abstract String[] getLogicsheets(Document document);

  /**
   * Add a dependency on an external file to the document for inclusion in
   * generated code. This is used by <code>AbstractServerPagesGenerator</code>
   * to populate a list of <code>File</code>'s tested for change on each
   * invocation; this information, in turn, is used by
   * <code>ServerPagesLoaderImpl</code> to assert whether regeneration is
   * necessary.
   *
   * @param PARAM_NAME Param description
   * @return the value
   * @exception EXCEPTION_NAME If an error occurs
   * @see ServerPages <code>AbstractServerPagesGenerator</code>
   *      and <code>ServerPagesLoaderImpl</code>
   */
  protected abstract void addDependency(Document document, String location);

  /**
   * Generate source code from the input document for the target
   * <code>ProgrammingLanguage</code>. After preprocessing the input document,
   * this method applies logicsheets in the following order:
   * <ul>
   *   <li>User-defined logicsheets</li>
   *   <li>Namespace-mapped logicsheets</li>
   *   <li>Language-specific logicsheet</li>
   * </ul>
   *
   * @param document The input document
   * @param filename The input document's original filename
   * @param programmingLanguage The target programming language
   * @return The generated source code
   * @exception Exception If an error occurs during code generation
   */
  public String generateCode(
    Document document, String filename, ProgrammingLanguage programmingLanguage
  ) throws Exception {
    String languageName = programmingLanguage.getName();

    LanguageDescriptor language =
      (LanguageDescriptor) this.languages.get(languageName);

    if (language == null) {
      throw new IllegalArgumentException(
        "Unsupported programming language: " + languageName
      );
    }

    // Preprocess document as needed
    document = this.preprocessDocument(document, filename, programmingLanguage);

    // Create code generator
    LogicsheetCodeGenerator codeGenerator = new LogicsheetCodeGenerator(); 

    // Add user-defined logicsheets
    String[] logicsheetNames = this.getLogicsheets(document);
    for (int i = 0; i < logicsheetNames.length; i++) {
      this.addLogicsheet(codeGenerator, logicsheetNames[i], document);
    }

    // Add namespace-mapped logicsheets
    Element root = document.getDocumentElement();
    NamedNodeMap attrs = root.getAttributes();
    int attrCount = attrs.getLength();
    for (int i = 0; i < attrCount; i++) {
      Attr attr = (Attr) attrs.item(i);
      String name = attr.getName();

      if (name.startsWith("xmlns:")) {
        String prefix = name.substring(6);
        String namedLogicsheetName = language.getNamedLogicsheet(prefix);

        if (namedLogicsheetName != null) {
          this.addLogicsheet(codeGenerator, namedLogicsheetName, document);
        }
      }
    }

    // Add language-specific logicsheet (always last!)
    this.addLogicsheet(codeGenerator, language.getLogicsheet(), document);

    return codeGenerator.generateCode(document, filename);
  }

  /**
   * Add a logicsheet to the code generator.
   *
   * @param codeGenerator The code generator
   * @param logicsheetLocation Location of the logicsheet to be added
   * @param document The input document
   * @exception MalformedURLException If location is invalid
   * @exception IOException IO Error
   * @exception SAXException Logicsheet parse error
   */
  protected void addLogicsheet(
    LogicsheetCodeGenerator codeGenerator,
    String logicsheetLocation,
    Document document
  ) throws MalformedURLException, IOException, SAXException
  {
    String systemId = null;
    InputSource inputSource = null;

    if (logicsheetLocation.indexOf(":/") < 0) { // Relative to Cocoon root
      EntityResolver entityResolver =
        (EntityResolver) this.manager.getComponent("cocoon");
      inputSource = entityResolver.resolveEntity(null, logicsheetLocation);
      systemId = inputSource.getSystemId();
    } else { // Fully resolved URL
      systemId = logicsheetLocation;
      inputSource = new InputSource(systemId);
    }

    URL url = new URL(systemId);
    String logicsheetName = url.toExternalForm();
    CachedURL entry = (CachedURL) this.logicsheetCache.get(logicsheetName);

    Logicsheet logicsheet = null;

    if (entry == null) {
      logicsheet = new Logicsheet();
      logicsheet.setInputSource(inputSource);
      entry = new CachedURL(url, logicsheet);
      this.logicsheetCache.store(logicsheetName, entry);
    }

    logicsheet = entry.getLogicsheet();

    if (entry.hasChanged()) {
      logicsheet.setInputSource(inputSource);
    }

    if (entry.isFile()) {
      this.addDependency(document, IOUtils.getFullFilename(entry.getFile()));
    }

    codeGenerator.addLogicsheet(logicsheet);
  }

  // Inner classes

  /**
   * This class holds transient information about a target programming
   * language.
   *
   */
  protected class LanguageDescriptor {
    /**
     * The progamming language name
     */
    protected String name;

    /**
     * The progamming language core logicsheet
     */
    protected String logicsheet;

    /**
     * The list of built-in logicsheets defined for this target language
     */
    protected Hashtable namedLogicsheets;

    /**
     * The default constructor
     */
    protected LanguageDescriptor() {
      this.namedLogicsheets = new Hashtable();
    }

    /**
     * Set the programming language's name
     *
     * @param name The programming language's name
     */
    protected void setName(String name) {
      this.name = name;
    }

    /**
     * Return the programming language's name
     *
     * @return The programming language's name
     */
    protected String getName() {
      return this.name;
    }

    /**
     * Set the programming language's core logichseet location
     *
     * @param logicsheet The programming language's core logichseet location
     */
    protected void setLogicsheet(String logicsheet) {
      this.logicsheet = logicsheet;
    }

    /**
     * Return the programming language's core logichseet location
     *
     * @return The programming language's core logichseet location
     */
    protected String getLogicsheet() {
      return this.logicsheet;
    }

    /**
     * Add a namespace-mapped logicsheet to this language
     *
     * @param prefix The logichseet's namespace prefix
     * @param uri The logichseet's namespace uri
     * @param namedLogicsheet The logichseet's location
     */
    protected void addNamedLogicsheet(String prefix, String namedLogicsheet) {
      this.namedLogicsheets.put(
        prefix,
        namedLogicsheet
      );
    }

    /**
     * Return a namespace-mapped logicsheet given its name
     *
     * @return The namespace-mapped logicsheet
     */
    protected String getNamedLogicsheet(String prefix) {
      return (String) this.namedLogicsheets.get(prefix);
    }
  }

  /**
   * This class holds a cached URL entry associated with a logicsheet
   *
   */
  protected class CachedURL {
    /**
     * The logicsheet URL
     */
    protected URL url;
    /**
     * The logicsheet's <code>File</code> if it's actually a file.
     * This is used to provide last modification information not
     * otherwise available for URL's in Java :-(
     */
    protected File file;
    /**
     * The cached logicsheet 
     */
    protected Logicsheet logicsheet;
    /**
     * The las time this logicsheet was changed/loaded
     */
    protected long lastModified;

    /**
     * The constructor.
     */
    protected CachedURL(URL url, Logicsheet logicsheet) throws IOException {
      this.url = url;
      this.logicsheet = logicsheet;

      if (this.isFile()) {
        this.file = new File(url.getFile());
      }

      this.lastModified = (new Date()).getTime();
    }

    /**
     * Return this entry's URL
     *
     * @return The cached logicsheet's URL
     */
    protected URL getURL() {
      return this.url;
    }

    protected boolean isFile() {
      return this.url.getProtocol().equals("file");
    }

    /**
     * Return this entry's <code>File</code>
     *
     * @return The cached logicsheet's <code>File</code>
     */
    protected File getFile() {
      return this.file;
    }

    /**
     * Return this entry's cached logicsheet
     *
     * @return The cached logicsheet
     */
    protected Logicsheet getLogicsheet() {
      return this.logicsheet;
    }

    /**
     * Assert whether this entry's logicsheet should be reloaded
     *
     * @return Whether the cached logicsheet has changed
     */
    protected boolean hasChanged() {
      if (this.file == null) {
        return false;
      }

      return this.lastModified < this.file.lastModified();
    }
  }
}
