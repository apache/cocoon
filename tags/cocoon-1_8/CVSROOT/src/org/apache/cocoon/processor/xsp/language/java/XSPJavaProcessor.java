/*-- $Id: XSPJavaProcessor.java,v 1.11 2000-08-18 22:43:59 stefano Exp $ --

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

package org.apache.cocoon.processor.xsp.language.java;

import java.io.*;
import org.w3c.dom.*;
import javax.servlet.http.*;

import org.apache.cocoon.framework.*;
import org.apache.cocoon.processor.xsp.*;
import org.apache.cocoon.processor.xsp.language.*;

/**
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version $Revision: 1.11 $ $Date: 2000-08-18 22:43:59 $
 */
public class XSPJavaProcessor implements XSPLanguageProcessor, Configurable {
  // Create class loader
  protected File repository;
  protected String encoding;
  protected XSPClassLoader classLoader;
  protected JavaCompiler javac;

  protected boolean format;

  public XSPJavaProcessor() {
    this.format = false;
  }

  public void init(Configurations conf) {
    String javacClassName = (String) conf.get("compiler", "org.apache.cocoon.processor.xsp.language.java.SunJavaCompiler");
    try {
        this.javac = (JavaCompiler) Class.forName(javacClassName).newInstance();
    } catch (Exception e) {
    }
  }

  public String getSourceExtension() {
    return "java";
  }

  public String getObjectExtension() {
    return "class";
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public void setRepository(File repository) throws Exception {
    this.repository = repository;
    this.classLoader = new XSPClassLoader(this.repository);
  }

  public void setFormatOption(boolean format) {
    this.format = format;
  }


  public String formatCode(String code) throws Exception {
    // FIXME: do nothing for now. In the future, we should hook some
    // open source java pretty printer or write our own using JavaCC stuff
    // or similar to allow easier XSP code debugging (SM)
    return code;
  }

  public void compile(String filename) throws Exception {
    String repositoryName = this.repository.getCanonicalPath();
    String fullFilename = repositoryName + File.separator + filename;

    ByteArrayOutputStream err = new ByteArrayOutputStream(256);
    
    javac.setEncoding(this.encoding);
    javac.setClasspath(repositoryName + File.pathSeparator + System.getProperty("java.class.path"));
    javac.setOutputDir(repositoryName);
    javac.setMsgOutput(err);

    boolean compilationResult = javac.compile(fullFilename);

    if (!compilationResult) {
      // Massage message
      int pos = fullFilename.length() + 1;
      StringBuffer buffer = new StringBuffer();
      String[] errorLines = XSPUtil.split(err.toString(), "\r\n");
      for (int i = 0; i < errorLines.length; i++) {
        if (errorLines[i].startsWith(fullFilename)) {
          errorLines[i] = errorLines[i].substring(pos);
        }
        buffer.append(errorLines[i] + "\n");
      }

      throw new Exception(
        "XSP Java Compiler: Compilation failed for " +
        XSPUtil.fileComponent(filename) + "\n" +
        buffer.toString()
      );
    }
/* 
 int pos = fullFilename.length() + 1;
 StringBuffer buffer = new StringBuffer();
 String[] errorLines = XSPUtil.split(err.toString(), "\r\n");
 for (int i = 0; i < errorLines.length; i++) {
   if (errorLines[i].startsWith(fullFilename)) {
     errorLines[i] = errorLines[i].substring(pos);
   }
   buffer.append(errorLines[i] + "\n");
 }
 System.err.println(buffer.toString());
*/
  }

  public XSPPage load(String filename) throws Exception {
    return
      (XSPPage)
      this.classLoader.loadClass(
        filename.substring(0, filename.lastIndexOf("."))
        .replace(File.separatorChar, '.')
      )
      .newInstance();
  }

  public void unload(XSPPage page) throws Exception {
    this.classLoader = new XSPClassLoader(this.repository);
  }

  public static String className(String filename) {
    return
      XSPUtil.normalizedBaseName(
        XSPUtil.fileComponent(filename)
      );
  }

  public static String packageName(String filename) {
    return
      XSPUtil.normalizedName(
        XSPUtil.pathComponent(filename)
      ).replace(File.separatorChar, '.');
  }

  public String stringEncode(String string) {
    char chr[] = string.toCharArray();
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
}
