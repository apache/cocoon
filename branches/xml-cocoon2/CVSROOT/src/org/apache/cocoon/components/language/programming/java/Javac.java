/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.components.language.programming.java;

import java.io.*;
import java.util.*;
import org.apache.cocoon.components.language.programming.*;

/**
 * This class wraps the Sun's Javac Compiler.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.2.4 $ $Date: 2000-08-21 17:41:46 $
 * @since 2.0
 */

public class Javac extends AbstractJavaCompiler {

  public final static String CLASSIC_CLASS = "sun.tools.javac.Main";
  public final static String MODERN_CLASS = "com.sun.tools.javac.Main";

  private boolean modern = false;

  public Javac() {

    // Use reflection to be able to build on all JDKs
    try {
        Class.forName(MODERN_CLASS);
        modern = true;
    } catch (ClassNotFoundException e) {
        try {
            Class.forName(CLASSIC_CLASS);
            modern = false;
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException("No compiler found in your classpath. Make sure you added 'tools.jar'");
        }
    }
  }

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
    boolean result = compiler.compile(toStringArray(fillArguments(new ArrayList())));
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
  protected List parseStream(BufferedReader input) throws IOException {
    if (modern) {
        return parseModernStream(input);
    } else {
        return parseClassicStream(input);
    }
  }

  /**
   * Parse the compiler error stream to produce a list of
   * <code>CompilerError</code>s
   *
   * @param errors The error stream
   * @return The list of compiler error messages
   * @exception IOException If an error occurs during message collection
   */
  protected List parseModernStream(BufferedReader input) throws IOException {
    List errors = null;
    String line = null;
    StringBuffer buffer = new StringBuffer();

    while (true) {
      // cleanup the buffer
      buffer.delete(0, buffer.length());

      // each error terminates with the '^' char
      do {
        if ((line = input.readLine()) == null) return errors;
        buffer.append(line);
        buffer.append('\n');
      } while (!line.endsWith("^"));

      // if error is found create the vector
      if (errors == null) errors = new ArrayList();

      // add the error bean
      errors.add(parseModernError(buffer.toString()));
    }
  }

  /**
   * Parse an individual compiler error message with modern style.
   *
   * @param error The error text
   * @return A messaged <code>CompilerError</code>
   */
  private CompilerError parseModernError(String error) {
    StringTokenizer tokens = new StringTokenizer(error, ":");
    String file = tokens.nextToken();
    if (file.length() == 1) file += ":" + tokens.nextToken();
    int line = Integer.parseInt(tokens.nextToken());

    // FIXME (SM) finish writing a decent parser for modern errors
    /*String message = tokens.nextToken();
    String context = tokens.nextToken();
    String pointer = tokens.nextToken();
    int startcolumn = pointer.indexOf("^");
    int endcolumn = context.indexOf(" ", startcolumn);
    if (endcolumn == -1) endcolumn = context.length();*/
    
    return new CompilerError(error);
  }

  /**
   * Parse the compiler error stream to produce a list of
   * <code>CompilerError</code>s
   *
   * @param errors The error stream
   * @return The list of compiler error messages
   * @exception IOException If an error occurs during message collection
   */
  protected List parseClassicStream(BufferedReader input) throws IOException {

    List errors = null;
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
      if (errors == null) errors = new ArrayList();

      // add the error bean
      errors.add(parseClassicError(buffer.toString()));
    }
  }

  /**
   * Parse an individual compiler error message with classic style.
   *
   * @param error The error text
   * @return A messaged <code>CompilerError</code>
   */
  private CompilerError parseClassicError(String error) {

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

    return new CompilerError(srcDir + File.separator + file, true, line, startcolumn, line, endcolumn, message);
  }

  public String toString() {
    return "Sun Javac Compiler";
  }
}
