package org.apache.cocoon.interpreter.ecmascript;

import java.io.*;
import java.net.*;
import java.util.*;
import FESI.jslib.*;
import org.w3c.dom.*;
import org.apache.cocoon.interpreter.*;

/**
 * @author <a href="mailto:rrocha@plenix.org">Ricardo Rocha</a>
 * @version $Revision: 1.1.1.1 $ $Date: 1999-11-09 01:51:16 $
 */

public class EcmaScriptModule implements Module {
  // Should be a parameter
  private static String[] extensions = {
    "FESI.Extensions.BasicIO",
    "FESI.Extensions.FileIO",
    "FESI.Extensions.JavaAccess",
    "FESI.Extensions.Database",
    "FESI.Extensions.OptionalRegExp",
  };

  private static String initScript = null;
  private static URL url = null;
  private static final String INIT_SCRIPT = "org/apache/cocoon/interpreter/ecmascript/initScript.es";

  long lastModified;
  private File scriptFile;

  private String scriptName;
  private String scriptText;

  private Vector evaluatorPool;
  private JSGlobalObject globalEvaluator;

  public EcmaScriptModule(String theModule) throws LanguageException {
    this.scriptFile = null;
    this.scriptName = theModule;

    // Load init script from resource file
    try {
        if (initScript == null) {
            initScript = getFileContents(EcmaScriptModule.class.getClassLoader().getResource(INIT_SCRIPT).openStream());
        }
    } catch (Exception e) {
        throw new LanguageException("Could not find ECMAScript initialization file. The archive is probably damaged: " + e.toString());
    }
  }

  public Instance createInstance(Document document, Dictionary parameters) throws LanguageException {
    try {
      // Load script relative to path
      if (this.scriptFile == null) {
       String path = (String) parameters.get("path");

       if (path != null) {
         this.scriptName = path + this.scriptName;
       }

        this.scriptFile = new File(this.scriptName);
        this.lastModified = this.scriptFile.lastModified();

        if (!this.scriptFile.canRead()) {
          throw new LanguageException("Can't open file " + scriptName);
        }

        loadScript();
      }

      // Reload script if changed
      if (this.scriptFile.lastModified() != this.lastModified) {
       loadScript();
        this.lastModified = this.scriptFile.lastModified();
      }

      // Acquire/create a free evaluator
      EcmaScriptEvaluator requestEvaluator = null;
      int poolSize = this.evaluatorPool.size();

      int count;
      for (count = 0; count < poolSize; count++) {
        requestEvaluator = (EcmaScriptEvaluator) evaluatorPool.elementAt(count);

        if (requestEvaluator.acquire()) {
          break;
        }
      }

      if (count == poolSize) {
        requestEvaluator = new EcmaScriptEvaluator(this.globalEvaluator, this.extensions);

       requestEvaluator.acquire();
       this.evaluatorPool.addElement(requestEvaluator);
      }

      // Initialize acquired evaluator
      JSGlobalObject globalObject = requestEvaluator.getGlobalObject();

      globalObject.setMember("global", globalObject.makeObjectWrapper(globalEvaluator));

      Object request = parameters.get("request");
      globalObject.setMember("request", globalObject.makeObjectWrapper(request));

      globalObject.setMember("document", globalObject.makeObjectWrapper(document));

      System.out.println("Ending...");

      return new EcmaScriptInstance(requestEvaluator, document);
    } catch (Exception e) {
      throw new LanguageException(e.getMessage());
    }
  }

  private void loadScript() throws LanguageException {
    // Global evaluator initialization
    try {
      this.globalEvaluator = JSUtil.makeEvaluator(extensions);

      this.scriptText = getFileContents(scriptName);
      this.globalEvaluator.eval(initScript);

      this.globalEvaluator.eval(this.scriptText);

      // Evaluator pool initialization
      this.evaluatorPool = new Vector();
      this.evaluatorPool.addElement(new EcmaScriptEvaluator(this.globalEvaluator, extensions));
    } catch (Exception e) {
      throw new LanguageException("Error creating EcmaScript evaluator: " + e.getMessage());
    }
  }

  private static String getFileContents(String fileName) throws IOException {
    return getFileContents(new FileInputStream(fileName));
  }

  private static String getFileContents(InputStream inputStream) throws IOException {
    InputStreamReader reader = new InputStreamReader(inputStream);

    int len;
    char chr[] = new char[4096]; // Big enough chunk
    StringBuffer buffer = new StringBuffer();

    while ((len = reader.read(chr)) > 0) {
      buffer.append(chr, 0, len);
    }

    return buffer.toString();
  }
}