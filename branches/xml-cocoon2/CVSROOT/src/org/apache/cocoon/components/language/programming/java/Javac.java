/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                             *
 *****************************************************************************/

package org.apache.cocoon.components.language.programming.java;

import java.io.*;
import java.util.*;
import org.apache.cocoon.components.language.programming.*;

/**
 * This class wraps the Sun's built-in Java compiler.
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.2.2 $ $Date: 2000-07-22 20:41:40 $
 * @since 2.0
 */

public class Javac extends AbstractJavaCompiler {
  /**
   * Compile a source file yielding a loadable class file.
   *
   * @param filename The object program base file name
   * @param baseDirectory The directory containing the object program file
   * @param encoding The encoding expected in the source file or
   * <code>null</code> if it is the platform's default encoding
   * @exception LanguageException If an error occurs during compilation
   */
  public boolean compile() throws IOException {
    ByteArrayOutputStream err = new ByteArrayOutputStream();
    sun.tools.javac.Main compiler = new sun.tools.javac.Main(err, "javac");
    boolean result =
      compiler.compile(toStringArray(fillArguments(new Vector(10))));
    this.errors = new ByteArrayInputStream(err.toByteArray());
    return result;
  }
  
  /**
   * Parse the compiler error stream to produce a list of
   * <code>CompilerError</code>s
   *
   * @param errors The error stream
   * @return The list of compiler error messages
   * @exception IOException If an error occurs during message collection
   */
  protected Vector parseStream(BufferedReader input) throws IOException {
    Vector errors = null;
    String line = null;
    StringBuffer buffer = new StringBuffer();

    while (true) {
      // cleanup the buffer
      buffer.delete(0, buffer.length());

      // each error has 3 lines
      for (int i = 0; i < 3 ; i++) {
        if ((line = input.readLine()) == null) return errors;
        buffer.append(line);
        buffer.append('\n');
      }

      // if error is found create the vector
      if (errors == null) errors = new Vector(10);
      
      // add the error bean
      errors.addElement(parseError(buffer.toString()));
    }
  }
  
  /**
   * Parse an individual compiler error message
   *
   * @param error The error text
   * @return A mssaged <code>CompilerError</code>
   */
  private CompilerError parseError(String error) {
    StringTokenizer tokens = new StringTokenizer(error, ":");
    String file = tokens.nextToken();
    int line = Integer.parseInt(tokens.nextToken());
    
    tokens = new StringTokenizer(tokens.nextToken().trim(), "\n");
    String message = tokens.nextToken();
    String context = tokens.nextToken();
    String pointer = tokens.nextToken();
    int startcolumn = pointer.indexOf("^");
    int endcolumn = context.indexOf(" ", startcolumn);
    if (endcolumn == -1) endcolumn = context.length();
    
    String type = "error";
    
    return new CompilerError(srcDir + File.separator + file, type.equals("error"), line, startcolumn, line, endcolumn, message);
  }
  
  public String getStatus() {
    return "Sun Classic JavaC";
  }
}
