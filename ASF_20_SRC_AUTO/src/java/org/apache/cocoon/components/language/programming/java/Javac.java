/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.language.programming.java;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.components.language.programming.CompilerError;
import org.apache.cocoon.util.ClassUtils;
import org.apache.log.Hierarchy;

/**
 * This class wraps the Sun's Javac Compiler.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: Javac.java,v 1.3 2004/03/05 13:02:48 bdelacretaz Exp $
 * @since 2.0
 */

public class Javac extends AbstractJavaCompiler {

  public final static String CLASSIC_CLASS = "sun.tools.javac.Main";
  public final static String MODERN_CLASS = "com.sun.tools.javac.Main";

  private boolean modern = false;

  public Javac() {

    // Use reflection to be able to build on all JDKs
    try {
        ClassUtils.loadClass(MODERN_CLASS);
        modern = true;
    } catch (ClassNotFoundException e) {
        Hierarchy.getDefaultHierarchy().getLoggerFor("cocoon").debug("Javac connector could not find modern compiler -- falling back to classic.");
        try {
            ClassUtils.loadClass(CLASSIC_CLASS);
            modern = false;
        } catch (Exception ex) {
            Hierarchy.getDefaultHierarchy().getLoggerFor("cocoon").error("No compiler found in your classpath. Make sure you added 'tools.jar'", ex);
            throw new CascadingRuntimeException("No compiler found in your classpath. Make sure you added 'tools.jar'", ex);
        }
    }
  }

    protected Javac(boolean modern) {
        this.modern = modern;
    }

  /**
   * Compile a source file yielding a loadable class file.
   *
   * <code>null</code> if it is the platform's default encoding
   * @exception IOException If an error occurs during compilation
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
   * @param input The error stream
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
   * @param input The error stream
   * @return The list of compiler error messages
   * @exception IOException If an error occurs during message collection
   */
  protected List parseModernStream(BufferedReader input) throws IOException {
    List errors = new ArrayList();
    String line = null;
    StringBuffer buffer = null;

    while (true) {
      // cleanup the buffer
      buffer = new StringBuffer(); // this is quicker than clearing it

      // most errors terminate with the '^' char
      do {
        if ((line = input.readLine()) == null)
        {
            if (buffer.length() > 0) {
                // There's an error which doesn't end with a '^'
                errors.add(new CompilerError("\n" + buffer.toString()));
            }
            return errors;
        }
        buffer.append(line);
        buffer.append('\n');
      } while (!line.endsWith("^"));

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
    try {
      String file = tokens.nextToken();
      if (file.length() == 1) file = new StringBuffer(file).append(":").append(tokens.nextToken()).toString();
      int line = Integer.parseInt(tokens.nextToken());

      String message = tokens.nextToken("\n").substring(1);
      String context = tokens.nextToken("\n");
      String pointer = tokens.nextToken("\n");
      int startcolumn = pointer.indexOf("^");
      int endcolumn = context.indexOf(" ", startcolumn);
      if (endcolumn == -1) endcolumn = context.length();
      return new CompilerError(file, false, line, startcolumn, line, endcolumn, message);
    } catch(NoSuchElementException nse) {
      return new CompilerError("no more tokens - could not parse error message: " + error);
    } catch(Exception nse) {
      return new CompilerError("could not parse error message: " + error);
    }
  }

  /**
   * Parse the compiler error stream to produce a list of
   * <code>CompilerError</code>s
   *
   * @param input The error stream
   * @return The list of compiler error messages
   * @exception IOException If an error occurs during message collection
   */
  protected List parseClassicStream(BufferedReader input) throws IOException {

    List errors = null;
    String line = null;
    StringBuffer buffer = null;

    while (true) {
      // cleanup the buffer
      buffer = new StringBuffer(); // this is faster than clearing it

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
    try {
      String file = tokens.nextToken();
      if (file.length() == 1) file = new StringBuffer(file).append(":").append(tokens.nextToken()).toString();
      int line = Integer.parseInt(tokens.nextToken());

      String last = tokens.nextToken();
      // In case the message contains ':', it should be reassembled
      while (tokens.hasMoreElements()) {
        last += tokens.nextToken();
      }
      tokens = new StringTokenizer(last.trim(), "\n");
      String message = tokens.nextToken();
      String context = tokens.nextToken();
      String pointer = tokens.nextToken();
      int startcolumn = pointer.indexOf("^");
      int endcolumn = context.indexOf(" ", startcolumn);
      if (endcolumn == -1) endcolumn = context.length();

      return new CompilerError(srcDir + File.separator + file, true, line, startcolumn, line, endcolumn, message);
    } catch(NoSuchElementException nse) {
      return new CompilerError("no more tokens - could not parse error message: " + error);
    } catch(Exception nse) {
      return new CompilerError("could not parse error message: " + error);
    }
  }

  public String toString() {
    return "Sun Javac Compiler";
  }
}
