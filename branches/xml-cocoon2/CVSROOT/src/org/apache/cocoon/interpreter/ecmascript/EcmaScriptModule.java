/*-- $Id: EcmaScriptModule.java,v 1.3 1999-11-09 02:30:27 dirkx Exp $ -- 

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================
 
    Copyright (C) 1999 The Apache Software Foundation. All rights reserved.
 
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
package org.apache.cocoon.interpreter.ecmascript;

import java.io.*;
import java.net.*;
import java.util.*;
import FESI.jslib.*;
import org.w3c.dom.*;
import org.apache.cocoon.interpreter.*;

/**
 * @author <a href="mailto:rrocha@plenix.org">Ricardo Rocha</a>
 * @version $Revision: 1.3 $ $Date: 1999-11-09 02:30:27 $
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