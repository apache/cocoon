/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.      *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                      *
 *****************************************************************************/
 
package org.apache.cocoon.components.language.programming.java;

import java.io.*;
import java.util.*;
import org.apache.cocoon.components.language.programming.*;

/**
 * This class wraps IBM's <i>Jikes</i> Java compiler
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.2.2 $ $Date: 2000-07-22 20:41:40 $
 * @since 2.0
 */

public class Jikes extends AbstractJavaCompiler {
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
      Vector args = new Vector(12);
      // command line name
      args.add("jikes");
      // indicate Emacs output mode must be used
      args.add("+D");

      Process p = Runtime.getRuntime().exec(toStringArray(fillArguments(args)));

      errors = p.getInputStream();
      
      try {
        p.waitFor();
        return (p.exitValue() == 0);
      } catch(InterruptedException somethingHappened) {
        return false;
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
   protected Vector parseStream(BufferedReader input) throws IOException {
      Vector errors = null;
      String line = null;
      StringBuffer buffer = new StringBuffer();

      while (true) {
        // cleanup the buffer
        buffer.delete(0, buffer.length());

        // first line is not space-starting        
        if (line == null) line = input.readLine();
        if (line == null) return errors;
        buffer.append(line);

        // all other space-starting lines are one error
        while (true) {        
           line = input.readLine();
           if ((line == null) || (line.charAt(0) != ' ')) break;
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
      int startline = Integer.parseInt(tokens.nextToken());
      int startcolumn = Integer.parseInt(tokens.nextToken());
      int endline = Integer.parseInt(tokens.nextToken());
      int endcolumn = Integer.parseInt(tokens.nextToken());
      String type = tokens.nextToken().trim().toLowerCase();
      String message = tokens.nextToken().trim();
      
      return new CompilerError(file, type.equals("error"), startline, startcolumn, endline, endcolumn, message);
   }
   
   public String getStatus() {
      return "IBM Jikes";
   }
}
