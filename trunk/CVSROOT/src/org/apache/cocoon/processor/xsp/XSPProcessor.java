/*-- $Id: XSPProcessor.java,v 1.10 2000-02-13 18:29:34 stefano Exp $ --

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) @year@ The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Cocoon" and  "Apache Software Foundation"  must not be used to
    endorse  or promote  products derived  from this  software without  prior
    written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

 */

package org.apache.cocoon.processor.xsp;

import java.io.*;
import java.net.*;
import java.util.*;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.cocoon.*;
import org.apache.cocoon.parser.*;
import org.apache.cocoon.transformer.*;
import org.apache.cocoon.processor.*;
import org.apache.cocoon.framework.*;

import org.apache.cocoon.processor.xsp.language.*;

/**
 * This class implements the XSP engine.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version $Revision: 1.10 $ $Date: 2000-02-13 18:29:34 $
 */
public class XSPProcessor extends AbstractActor
  implements Processor, Configurable, Status
{
  public static final String DEFAULT_LANGUAGE = "java";

  protected Parser parser;
  protected Transformer transformer;

  protected Hashtable languages;
  protected Hashtable libraries;
  protected File repositoryFile;
  protected Hashtable pageCache;

  protected XSPLibrary xspLibrary;

  protected XSPGlobal global;
  protected ServletContext servletContext;

  public XSPProcessor() {
    this.global = new XSPGlobal();
  }

  public void init(Director director) {
    super.init(director);

    // Initialize DOM parser
    this.parser = (Parser) director.getActor("parser");

    // Initialize Servlet Context
    this.servletContext = (ServletContext) director.getActor("context");

    // Initialize XSLT processor
    this.transformer = (Transformer) director.getActor("transformer");

    // Initialize page cache
    this.pageCache = new Hashtable();

    // Initialize the library container
    this.libraries = new Hashtable();

    try {
      // Load configuration
      Document document = this.parser.parse(
        new InputSource(
          new InputStreamReader(
            this.getClass().getResourceAsStream("xsp.xml")
          )
        ), false
      );

      Element root = document.getDocumentElement();

      // Load languages and XSP built-in library
      this.xspLibrary = new XSPLibrary();
      this.xspLibrary.setNamespace("xsp");

      this.languages = new Hashtable();
      NodeList languageList = root.getElementsByTagName("language");
      int languageCount = languageList.getLength();

      // Don't forget to set repository later, on init(conf)!
      for (int i = 0; i < languageCount; i++) {
        Element languageElement = (Element) languageList.item(i);

        // Do language
        String languageName = languageElement.getAttribute("name");

        String processorName = languageElement.getAttribute("processor");
        XSPLanguageProcessor languageProcessor =
          (XSPLanguageProcessor) Class.forName(processorName).newInstance();

        boolean formatOption =
          languageElement.getAttribute("format-code").equals("true");
        languageProcessor.setFormatOption(formatOption);

        this.languages.put(languageName, languageProcessor);

        // Do template
/* START: Build XSP parameter dictionary */
/* Add manager role key/value pairs */
/* Add global XSP objects */
/* END: Build XSP parameter dictionary */

/* START: Create a new XSP core library given URI  */
/* Pass global XSP parameter dictionary */
        XSPTemplate template = new XSPTemplate(transformer, parser, null);
        template.setLanguageName(languageName);

        String templateName = languageElement.getAttribute("template");
        Document templateStylesheet = this.parser.parse(
          new InputSource(
            new InputStreamReader(
              this.getClass().getResourceAsStream(templateName)
            )
          ), false
        );
        template.setStylesheet(templateStylesheet);

        String preprocessorName =
          languageElement.getAttribute("dom-preprocessor");
        if (preprocessorName.length() > 0) {
          XSPPreprocessor domPreprocessor =
            (XSPPreprocessor) Class.forName(preprocessorName).newInstance();
          template.setPreprocessor(domPreprocessor);
        }

        this.xspLibrary.addTemplate(template);
/* END: Create a new XSP core library given URI  */
      }
    } catch (Exception e) {
      throw new RuntimeException("Error during initialization: " + e);
    }
  }

  public void init(Configurations conf) {

    // Initialize repository
    conf = conf.getConfigurations("xsp");
        
/* How to use Cocoon's Object Store for compiled classes? */
    // FIXME: the XSP processor should use the Cocoon internal object store
    // rather than providing its own. This is a quick and dirty hack to 
    // make it work with Ricardo's code. But we'll create smoother integration
    // in future versions (SM)
    
    String repositoryName = (String) conf.get("repository");
    this.repositoryFile = new File(repositoryName);
    if (!this.repositoryFile.exists()) {
      if (!this.repositoryFile.mkdirs()) {
        throw new RuntimeException("Can't create store repository: "
          + repositoryFile.getAbsolutePath()
          + ". Make sure it's there or you have writing permissions.");
      }
    }

    if (!(this.repositoryFile.canRead() && this.repositoryFile.canWrite())) {
      throw new RuntimeException("Can't access store repository: "
        + repositoryFile.getAbsolutePath()
        + ". Make sure you have writing permissions.");
    }

    // Set repository for each language processor
    Enumeration enum = this.languages.elements();
    while (enum.hasMoreElements()) {
      XSPLanguageProcessor languageProcessor =
        (XSPLanguageProcessor) enum.nextElement();

      try {
        languageProcessor.setRepository(this.repositoryFile);
      } catch (Exception e) {
        throw new RuntimeException(
          "Error setting repository for language processor: " +
          e.getMessage()
        );
      }
    }

/* START: Load external libraries */
    // Load external libraries
    conf = conf.getConfigurations("library");

    Enumeration e = conf.keys();
    while (e.hasMoreElements()) {
      String str = (String) e.nextElement();
      String namespace = str.substring(0, str.indexOf('.'));
      String language = str.substring(str.indexOf('.') + 1);
      String location = (String) conf.get(str);

      if (this.languages.get(language) == null) {
        throw new RuntimeException(
          "Unsupported language '" + language + "' " +
          "in library '" + namespace + "'"
        );
      }

      XSPLibrary library = (XSPLibrary) this.libraries.get(namespace);
      if (library == null) {
        library = new XSPLibrary();
        library.setNamespace(namespace);
      }

      XSPTemplate template = new XSPTemplate(transformer, parser, location);
      template.setLanguageName(language);

      try {
        InputStream stream;

        if (location.startsWith("resource://")) {
            URL x = ClassLoader.getSystemResource(location.substring("resource://".length()));
            if (x == null) throw new IOException("Resource not found: " + location);
            else stream = x.openStream();
        } else {
            stream = new FileInputStream(location);
        }

        Document stylesheet = this.parser.parse(new InputSource(stream), false);

        template.setStylesheet(stylesheet);

        String preprocessorName = (String) this.libraries.get(namespace + "." + language + ".preprocessor");
        if (preprocessorName != null) {
          XSPPreprocessor preprocessor = (XSPPreprocessor) Class.forName(preprocessorName).newInstance();
          template.setPreprocessor(preprocessor);
        }

        library.addTemplate(template);
      } catch (Exception ex) {
        throw new RuntimeException ("Error loading logicsheet: " + location + ". " + ex);
      }

      this.libraries.put(namespace, library);
    }
/* END: Load external libraries */
  }

  public Document process(Document document, Dictionary parameters)
    throws Exception
  {
    // Retrieve servletContext and request objects from parameters
    HttpServletRequest request =
      (HttpServletRequest) parameters.get("request");

    HttpServletResponse response =
      (HttpServletResponse) parameters.get("response");

    // Determine source document's absolute pathname
    String filename = Utils.getBasename(request, servletContext);

    File sourceFile = new File(filename);
    filename = sourceFile.getCanonicalPath();

    // Determine page language
    Element root = document.getDocumentElement();

    String languageName = root.getAttribute("language");
    if (languageName.length() == 0) {
      languageName = DEFAULT_LANGUAGE;
    }

    XSPLanguageProcessor languageProcessor =
      (XSPLanguageProcessor) languages.get(languageName);

    if (languageProcessor == null) {
      throw new Exception("Unsupported language: " + languageName);
    }

    // Get page from page
    PageEntry pageEntry = (PageEntry) this.pageCache.get(filename);

    // New page?
    if (pageEntry == null) {
      String targetFilename =
        XSPUtil.normalizedBaseName(filename);

      String objectExtension = languageProcessor.getObjectExtension();
      if (objectExtension != null) {
        targetFilename += "." + objectExtension;
      }

      File targetFile = new File(
        this.repositoryFile.getCanonicalPath() +
        File.separator +
        targetFilename
      );

      pageEntry = new PageEntry(sourceFile,targetFile);

      // Was page created during a previous incarnation?
      if (targetFile.exists() && !pageEntry.hasChanged()) {
        this.loadPage(languageProcessor, pageEntry, targetFilename);
      }

      this.pageCache.put(filename, pageEntry);
    }

    // [Re]create page if necessary
    if (pageEntry.hasChanged()) {
      // Collect used libraries
      Vector libraryList = new Vector();
/* START: Locate libraries used */
      NamedNodeMap attributes = root.getAttributes();
      int attrCount = attributes.getLength();
      for (int i = 0; i < attrCount; i++) {
        Attr attr = (Attr) attributes.item(i);
        String attrName = attr.getName();

        // Is this attribute a namespace definition?
        if (
          attrName.length() >= 6 &&
          attrName.substring(0, 6).equals("xmlns:")
        ) {
          String namespace = attrName.substring(6);

          // Not xsp, it's implied
          if (!namespace.equals("xsp")) {
            XSPLibrary library =
              (XSPLibrary) this.libraries.get(attrName.substring(6));
/* [Re]Load library given URI here */

            if (library != null) {
              libraryList.addElement(library);
            }
          }
        }
      }
/* END: Locate libraries used */

      // The XSP built-in library always comes last
      libraryList.addElement(this.xspLibrary);

      // Apply each library template
      Hashtable templateParameters = new Hashtable();
/* START: Build XSP parameters for library */
      templateParameters.put("filename", filename);
      templateParameters.put("language", languageName);
/* END: Build XSP parameters for library */

      int libraryCount = libraryList.size();
      for (int i = 0; i < libraryCount; i++) {
        XSPLibrary library = (XSPLibrary) libraryList.elementAt(i);
/* START: Load/Reload library if necessary */
/* Use Cocoon Object Store! */
        XSPTemplate template = library.getTemplate(languageName);
/* END: Load/Reload library if necessary */
        if (template != null) document = template.apply(document, templateParameters);
      }

      // Retrieve and format generated source code
      Element sourceElement = document.getDocumentElement();
      sourceElement.normalize();
      String sourceCode = sourceElement.getFirstChild().getNodeValue();

      sourceCode = languageProcessor.formatCode(sourceCode);

      // Store source code in repository
      String baseName = XSPUtil.normalizedBaseName(filename);

      String sourceExtension = languageProcessor.getSourceExtension();
      if (sourceExtension != null) {
        baseName += "." + sourceExtension;
      }

      String sourceFilename =
        repositoryFile.getCanonicalPath() +
        File.separator +
          baseName;

      // Create repository subdirectories as needed
      String subdirName = XSPUtil.pathComponent(sourceFilename);
      File subdirFile = new File(subdirName);
      if (!subdirFile.exists()) {
        if (!subdirFile.mkdirs()) {
          throw new Exception("Can't create subdirectory: " + subdirName);
        }
      }

      // Dump to file
      FileWriter fileWriter = new FileWriter(sourceFilename);
      fileWriter.write(sourceCode);
      fileWriter.flush();
      fileWriter.close();

      // Compile generated code
      XSPPage page = pageEntry.getPage();
      if (page != null) {
        languageProcessor.unload(page);
      }

      languageProcessor.compile(baseName);
      this.loadPage(languageProcessor, pageEntry, baseName);
    }

    return pageEntry.getPage().getDocument(request, response);
  }

  protected XSPPage loadPage(
    XSPLanguageProcessor languageProcessor,
    PageEntry pageEntry,
    String filename
  )
    throws Exception
  {
    Hashtable pageParameters = new Hashtable();
    pageParameters.put("director", this.director);
    pageParameters.put("global", this.global);

    XSPPage page = languageProcessor.load(filename);
    page.init(pageParameters);

    pageEntry.setPage(page);

    return page;
  }

  protected ServletContext getServletContext() {
    return this.servletContext;
  }

  protected XSPGlobal getGlobal() {
    return this.global;
  }

  public boolean hasChanged(Object context) {
    return true;
  }

  public String getStatus() {
    return "eXtensible Server Pages Processor";
  }

  public class PageEntry {
    protected File source;
    protected File target;
    protected XSPPage page;

    public PageEntry(File source, File target) {
      this.source = source;
      this.target = target;
    }

    public boolean hasChanged() {
      return
        !this.target.exists() ||
        this.target.lastModified() < this.source.lastModified();
    }

    public void setPage(XSPPage page) {
      this.page = page;
    }

    public XSPPage getPage() {
      return this.page;
    }
  }
}
