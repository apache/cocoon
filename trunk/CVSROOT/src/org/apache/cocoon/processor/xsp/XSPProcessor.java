/*-- $Id: XSPProcessor.java,v 1.36 2001-01-09 18:30:36 greenrd Exp $ --

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
import org.xml.sax.SAXException;

import javax.servlet.*;
import javax.servlet.http.*;

import org.apache.cocoon.*;
import org.apache.cocoon.store.*;
import org.apache.cocoon.logger.*;
import org.apache.cocoon.parser.*;
import org.apache.cocoon.transformer.*;
import org.apache.cocoon.processor.*;
import org.apache.cocoon.framework.*;

import org.apache.cocoon.processor.xsp.language.*;

// (SM) there should not be direct dependency on Turbine!!!!
import org.apache.turbine.services.resources.TurbineResourceService;

/**
 * This class implements the XSP engine.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version $Revision: 1.36 $ $Date: 2001-01-09 18:30:36 $
 */
public class XSPProcessor extends AbstractActor
  implements Processor, Configurable, Status, Cacheable
{
  public static final String DEFAULT_LANGUAGE = "java";
  public static final String LOGICSHEET_PI = "xml-logicsheet";

  protected Factory factory;
  protected Parser parser;
  protected Transformer transformer;

  protected Hashtable languages;
  protected File repositoryFile;

  protected String encoding;

  protected Store store;
  protected Monitor monitor;
  protected Hashtable byNamespace;
  
  protected Logger logger;

  protected XSPGlobal global;
  protected ServletContext servletContext;

  /** Version of XSP spec in use. */
  public static String version () {
    return "1.0";
  }

  public XSPProcessor() {
    this.global = new XSPGlobal();
  }

  public void init(Director director) {
    super.init(director);

    // Initialize Cocoon factory
    this.factory = (Factory) director.getActor("factory");

    // Initialize Cocoon cache
    this.store = (Store) director.getActor("store");

    // Initialize Cocoon monitor
    this.monitor = new Monitor(8);

    // Initialize DOM parser
    this.parser = (Parser) director.getActor("parser");

    // Initialize Servlet Context
    this.servletContext = (ServletContext) director.getActor("context");

    // Initialize XSLT processor
    this.transformer = (Transformer) director.getActor("transformer");

    // Initialize XSLT processor
    this.logger = (Logger) director.getActor("logger");

    // Initialize the logicsheet hashtable indexed by language
    this.byNamespace = new Hashtable(5);

    // Initialize the logicsheet hashtable indexed by language
    this.languages = new Hashtable(1);
  }

  public void init(Configurations conf) {

    // create a repository for xsp logicsheets
    Hashtable xspLogicsheets = new Hashtable(10);

    // Set default encoding
    this.encoding = (String) conf.get("encoding");

    // Set properties and actors for each supported language
    Tokenizer t = new Tokenizer((String) conf.get("languages"));
    while (t.hasMoreTokens()) {
        try {
            String languageName = t.nextToken();
            Configurations c = conf.getConfigurations(languageName);
            XSPLogicsheet logicsheet = new XSPLogicsheet(transformer, parser, null);

            // Make XSP Processor class configurable via cocoon.properties (i.e. compiler)
            Configurations cc = c.getConfigurations("processor");

            String processorName = (String) c.get("processor");
                XSPLanguageProcessor languageProcessor =
                    (XSPLanguageProcessor) this.factory.create(processorName, cc);
            this.languages.put(languageName, languageProcessor);

            String logicsheetName = (String) c.get("logicsheet");
            InputStream logicsheetInputStream = this.getClass().getResourceAsStream(logicsheetName);
            if (logicsheetInputStream == null) 
                throw new Exception("Resource '" + logicsheetName + "' could not be found.");
            logicsheet.setStylesheet(this.parser.parse(new InputSource(logicsheetInputStream)));
   
            String preprocessorName = (String) c.get("preprocessor");
            if (processorName != null) {
                XSPPreprocessor domPreprocessor = (XSPPreprocessor) this.factory.create(preprocessorName);
                logicsheet.setPreprocessor(domPreprocessor);
            }
    
            xspLogicsheets.put(languageName, logicsheet);
//        } catch (SAXException e) {
//            throw new RuntimeException(Utils.getStackTraceAsString(e.getException()));
        } catch (Exception e) {
            throw new RuntimeException("Error while initializing XSP engine: " +
                                       Utils.getStackTraceAsString(e));
        }
    }

    // Add to namespace logicsheet list
    this.byNamespace.put("xsp", xspLogicsheets);

    String repositoryName = (String) conf.get("repository");
    this.repositoryFile = new File(repositoryName);
    if (!this.repositoryFile.exists()) {
      if (!this.repositoryFile.mkdirs()) {
        throw new RuntimeException("Can't create store repository: "
          + repositoryFile
          + ". Make sure it's there or you have writing permissions.<br>"
          + "In case this path is relative we highly suggest you to"
          + " change this to an absolute path so you can control its location directly"
          + " and provide valid access rights." );
      }
    }

    if (!(this.repositoryFile.canRead() && this.repositoryFile.canWrite())) {
      throw new RuntimeException("Can't access store repository: "
        + repositoryFile.getAbsolutePath()
        + ". Make sure you have writing permissions.");
    }

    // Languages other than Java may also need to use the classpath to link to Java code
    // Try to get the classpath from conf or Catalina or Tomcat or Resin first;
    // if that all fails, use the standard one.
    String classpath = (String) conf.get ("localclasspath");
    if (classpath == null && this.servletContext != null) {
      classpath = (String)
        this.servletContext.getAttribute("org.apache.catalina.jsp_classpath");
      if (classpath == null) {
        classpath = (String)
          this.servletContext.getAttribute("org.apache.tomcat.jsp_classpath");
        if (classpath == null) {
          classpath = (String)
            this.servletContext.getAttribute ("caucho.class.path");
        }
      }
    }
    if (classpath == null) 
      classpath = "";
    else
      classpath += File.pathSeparator;

    classpath += System.getProperty("java.class.path");

    // Set parameters for each language processor
    Enumeration enum = this.languages.elements();
    while (enum.hasMoreElements()) {
      XSPLanguageProcessor languageProcessor =
        (XSPLanguageProcessor) enum.nextElement();

      try {
        languageProcessor.setEncoding(this.encoding);
        languageProcessor.setRepository(this.repositoryFile);
	languageProcessor.setClassPath(classpath);
      } catch (Exception e) {
        throw new RuntimeException(
          "Error setting repository for language processor: " +
          e.getMessage()
        );
      }
    }

    // Load namespace-mapped logicsheets
    Configurations lsConf = conf.getConfigurations("logicsheet");
    Enumeration e = lsConf.keys();

    while (e.hasMoreElements()) {
      String str = (String) e.nextElement();
      String namespace = str.substring(0, str.indexOf('.'));
      String language = str.substring(str.indexOf('.') + 1);
      String location = (String) lsConf.get(str);

      if (this.languages.get(language) == null) {
        throw new RuntimeException(
          "Unsupported language '" + language + "' " +
          "in logicsheet '" + namespace + "'"
        );
      }

      Hashtable byLanguage = (Hashtable) this.byNamespace.get(namespace);
      if (byLanguage == null) byLanguage = new Hashtable(1);

      try {
        Object resource = Utils.getLocationResource(location);
        if (resource == null) throw new Exception("Resource not found or retrieving error.");
       
        XSPPreprocessor preprocessor = null;
        String preprocessorName =
          (String) lsConf.get(namespace + "." + language + ".preprocessor");
        if (preprocessorName != null) {
          preprocessor = (XSPPreprocessor) this.factory.create(preprocessorName);
        }

        this.refreshLogicsheet(resource, preprocessor);
        byLanguage.put(language, this.store.get(resource.toString()));
        this.byNamespace.put(namespace, byLanguage);
      }
      catch (Exception ex) {
        throw new RuntimeException ("Error loading logicsheet at " + location + " due to " + ex);
      }
    }
    
    Configurations poolConf = conf.getConfigurations("pool");
    Properties poolProperties = poolConf.getProperties();
    poolProperties.put("services.TurbineResourceService.classname", "org.apache.turbine.services.resources.TurbineResourceService");

/*
    Enumeration en = poolConf.keys();
    
    while (en.hasMoreElements()) {
        String key = (String) en.nextElement();
        String value = (String) poolConf.get(key);
        logger.log("@@@"+key+"="+value+"@@@", Logger.DEBUG);
    }
*/    

    try {
        TurbineResourceService.setProperties(poolProperties);
        logger.log("TurbineResources all set!!", Logger.DEBUG);
    } catch (IOException ioexp) {
        // should we consider this fatal and throw an exception? (SM)
        logger.log(this,
                   "Setting of TurbineResource properties failed due to " +
                   ioexp,
                   Logger.WARNING);
    }
  }

  public Document process(Document document, Dictionary parameters)
    throws Exception
  {
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

    // Retrieve servletContext and request objects from parameters
    HttpServletRequest request =
      (HttpServletRequest) parameters.get("request");

    HttpServletResponse response =
      (HttpServletResponse) parameters.get("response");

    // XXX: This is a mess - keys all over the place! - RDG

    // Determine source document's absolute pathname
    String filename = Utils.getBasename(request, servletContext);
    File sourceFile = new File(filename);
    filename = getStoreKey (request);

    // Get page from Cocoon store
    PageEntry pageEntry = (PageEntry) this.store.get(filename);

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

      pageEntry = new PageEntry(sourceFile, targetFile);
      pageEntry.setLogicsheets(
        this.getLogicsheets(document, request)
      );

      // Was page created during a previous incarnation?
      if (targetFile.exists() && !pageEntry.hasChanged()) {
        this.loadPage(languageProcessor, pageEntry, targetFilename);
      }

      // Remember page entry
      this.store.hold(filename, pageEntry);
    }

    // Get page from Cocoon store AGAIN to ensure proper synchronization
    pageEntry = (PageEntry) this.store.get(filename);

    synchronized (pageEntry) {
      // [Re]create page if necessary
      if (pageEntry.hasChanged()) {
        // Update logicsheets in page entry
        pageEntry.setLogicsheets(
          this.getLogicsheets(document, request)
        );
  
        // Build XSP parameters for logicsheet
        Hashtable logicsheetParameters = new Hashtable();
        logicsheetParameters.put("filename", filename);
        logicsheetParameters.put("language", languageName);
        logicsheetParameters.put("XSP-ENVIRONMENT", Cocoon.version ());
        logicsheetParameters.put("XSP-VERSION", version());
  
        // Apply each logicsheet in sequence
        Vector logicsheetList = pageEntry.getLogicsheets();
        int logicsheetCount = logicsheetList.size();
        for (int i = 0; i < logicsheetCount; i++) {
          Object resource = logicsheetList.elementAt(i);
          XSPLogicsheet logicsheet =
            (XSPLogicsheet) this.store.get(resource.toString());
          document = logicsheet.apply(document, logicsheetParameters);
        }
  
        // Apply namespace-defined logicsheets
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
  
            // Not xsp yet, it comes forecefully last
            if (!namespace.equals("xsp")) {
              Hashtable byLanguage = (Hashtable) this.byNamespace.get(namespace);
              if (byLanguage != null) { // Is such a namespace mapped?
                document =
                  ((XSPLogicsheet) byLanguage.get(languageName)).
                apply(document, logicsheetParameters);
              }
            }
          }
        }
  
        // Now is the time... apply the implied, built-in logicsheet
        document =
          (
           (XSPLogicsheet)
           ((Hashtable) this.byNamespace.get("xsp")).get(languageName)
          ).apply(document, logicsheetParameters);
  
  
        // Retrieve and format generated source code
        Element sourceElement = document.getDocumentElement();
        // sourceElement.normalize();
        // String sourceCode = sourceElement.getFirstChild().getNodeValue();
        /*
          For large XSP pages normalize() can be
         _really_ slow so we do it ourselves
        */
        StringBuffer buffer = new StringBuffer();
        Node node = sourceElement.getFirstChild();
        while (node != null) {
          switch(node.getNodeType()) {
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
              buffer.append(((Text)node).getData());
              break;
          }
          node = node.getNextSibling();
        }
        String sourceCode = languageProcessor.formatCode(buffer.toString());      
  
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
        byte[] bytes = null;
        if (this.encoding == null) {
          bytes = sourceCode.getBytes();
        } else {
          bytes = sourceCode.getBytes(this.encoding);
        }
        FileOutputStream fileWriter = new FileOutputStream(sourceFilename); 
        fileWriter.write(bytes);
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
    }

    return pageEntry.getPage().getDocument(request, response);
  }

  // FIXME: pageEntry.loadPage(languageProcessor, filename)
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

  private void refreshLogicsheet(Object resource) throws Exception {
    this.refreshLogicsheet(resource, null);
  }

  // FIXME: A common class with XSLProcessor?
  private void refreshLogicsheet(Object resource, XSPPreprocessor preprocessor)
    throws Exception
  {
        String name = resource.toString();
    XSPLogicsheet logicsheet = (XSPLogicsheet) this.store.get(name);

    // Parse logicsheet
        if (logicsheet == null) {
      logicsheet = new XSPLogicsheet(this.transformer, this.parser, name);
        }

    logicsheet.setStylesheet(getDocument(resource));
    logicsheet.setPreprocessor(preprocessor);

    // Hold logicsheet in store
    this.store.hold(name, logicsheet);
    this.monitor.invalidate(name);
    this.monitor.watch(name, resource);
  }

  // FIXME: Utils.java: pass pi type as arg. Provide for sheet building
  private Vector getLogicsheets(Document document, HttpServletRequest request)
    throws Exception
  {
    Vector vector = new Vector();

    Enumeration pis = Utils.getAllPIs(document, LOGICSHEET_PI).elements();
    while (pis.hasMoreElements()) {
      Hashtable attributes =
            Utils.getPIPseudoAttributes((ProcessingInstruction) pis.nextElement());

      String location = (String) attributes.get("href");
      if (location != null) {
        try {
          XSPPreprocessor preprocessor = null;
          String preprocessorName = (String) attributes.get("dom-preprocessor");
          if (preprocessorName != null) {
            preprocessor =
              (XSPPreprocessor) Class.forName(preprocessorName).newInstance();
          }

          Object resource = Utils.getLocationResource(location, request, servletContext);

          this.refreshLogicsheet(resource, preprocessor);

          vector.addElement(resource);

        } catch (MalformedURLException e) {
          throw new ProcessorException(
            "Could not associate logicsheet to document: " +
            location + " is a malformed URL."
          );
        }
      }
    }

    return vector;
  }

  // FIXME: Utils.java: return InputStream (getDocumentStream)
  private Document getDocument(Object resource) throws Exception {
    InputSource input = new InputSource();
    input.setSystemId(resource.toString());

    if (resource instanceof File) {
      input.setCharacterStream(new FileReader(((File) resource)));
    } else if (resource instanceof URL) {
      input.setCharacterStream(
        new InputStreamReader(((URL) resource).openStream())
      );
    } else {
      // should never happen
      throw new Error(
        "Fatal error: Could not elaborate given resource: " + resource
      );
    }

    // do not validate stylesheets
    return this.parser.parse(input, false);
  }

  protected ServletContext getServletContext() {
    return this.servletContext;
  }

  protected XSPGlobal getGlobal() {
    return this.global;
  }

  protected String getStoreKey (Object context) {
    if (!(context instanceof HttpServletRequest)) {
      return null; // can't interpret context
    }

    HttpServletRequest request = (HttpServletRequest) context;
 
    // Determine source document's absolute pathname
    String filename = Utils.getBasename(request, servletContext);
    File sourceFile = new File(filename);
    try {
      return sourceFile.getCanonicalPath();
    } catch (IOException e) {
      return sourceFile.getAbsolutePath();
    }
  }

  protected PageEntry getPageEntry (Object context) {
    String storeKey = getStoreKey (context);
    if (storeKey == null) return null;
    return (PageEntry) store.get (storeKey);
  }

  public boolean hasChanged(Object context) {
    // Get page from Cocoon store
    PageEntry pageEntry = getPageEntry (context);

    // New page?
    if (pageEntry == null) return true;

    // NOT pageEntry.hasChanged ()! We are calling the hasChanged method
    // of the XSP page itself.
    return pageEntry.getPage().hasChanged(context);
  }

  public boolean isCacheable(HttpServletRequest request) {
    // Get page from Cocoon store
    PageEntry pageEntry = getPageEntry (request);

    // New page?
    if (pageEntry == null) return true;

    // NOT pageEntry.isCacheable ()! We are calling the isCacheable method
    // of the XSP page itself.
    return pageEntry.getPage().isCacheable(request);
  }

  public String getStatus() {
    return "eXtensible Server Pages Processor";
  }

  public class PageEntry {
    protected File source;
    protected File target;
    protected XSPPage page;
    protected Vector logicsheets;

    public PageEntry(File source, File target) {
      this.source = source;
      this.target = target;
    }

    public void setLogicsheets(Vector logicsheets) throws Exception {
      this.logicsheets = logicsheets;

      int logicsheetCount = this.logicsheets.size();
      for (int i = 0; i < logicsheetCount; i++) {
        Object resource = this.logicsheets.elementAt(i);
        Object object = store.get(resource.toString());

        if (object == null) {
          refreshLogicsheet(resource);
        }
      }
    }

    public Vector getLogicsheets() {
      return this.logicsheets;
    }

    /** Does the page need recompiling? */
    public boolean hasChanged() throws Exception {
      if (
        !this.target.exists() ||
        this.target.lastModified() < this.source.lastModified()
      ) {
        return true;
      }

      if (this.logicsheets != null) {
        int changeCount = 0;
        int logicsheetCount = this.logicsheets.size();
        for (int i = 0; i < logicsheetCount; i++) {
          Object resource = this.logicsheets.elementAt(i);

          if (
            monitor.hasChanged(resource.toString()) ||
            this.target.lastModified() < monitor.timestamp(resource)
          ) {
            changeCount++;
            refreshLogicsheet(resource);
          }
        }

        return changeCount > 0;
      }

      return false;
    }

    public void setPage(XSPPage page) {
      this.page = page;
    }

    public XSPPage getPage() {
      return this.page;
    }
  }
}
