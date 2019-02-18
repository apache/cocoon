/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.cocoon.components.language.programming.CompilerError;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

/**
 * This class wraps the Sun's Javac Compiler.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id$
 * @since 2.0
 */

public class Javac extends AbstractJavaCompiler {

  public Javac() {
      // EMPTY
  }

  /**
   * Compile a source file yielding a loadable class file.
   *
   * <code>null</code> if it is the platform's default encoding
   * @exception IOException If an error occurs during compilation
   */
  public boolean compile() throws IOException {

    ByteArrayOutputStream err = new ByteArrayOutputStream();
    String[] args = toStringArray(fillArguments(new ArrayList<String>()));

    JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
    int rc = javac.run(null, null, err, args);
    boolean result = rc == 0;
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
  protected List<CompilerError> parseStream(BufferedReader input) throws IOException {
    List<CompilerError> errors = new ArrayList<CompilerError>();

    while (true) {
      StringBuilder buffer = new StringBuilder();

      // most errors terminate with the '^' char
      String line;
      do {
        if ((line = input.readLine()) == null) {
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
      if (file.length() == 1)
        file = file + ":" + tokens.nextToken();
      int line = Integer.parseInt(tokens.nextToken());

      String message = tokens.nextToken("\n").substring(1);
      String context = tokens.nextToken("\n");
      String pointer = tokens.nextToken("\n");
      int startcolumn = pointer.indexOf("^");
      int endcolumn = context.indexOf(" ", startcolumn);
      if (endcolumn == -1) {
          endcolumn = context.length();
      }
      return new CompilerError(file, false, line, startcolumn, line, endcolumn, message);
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
