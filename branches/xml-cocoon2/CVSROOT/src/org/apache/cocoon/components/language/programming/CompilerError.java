/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
 
package org.apache.cocoon.components.language.programming;

/**
 * This class encapsulates an error message produced by a programming language
 * processor (whether interpreted or compiled)
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version $Revision: 1.1.2.2 $ $Date: 2000-07-22 20:41:39 $
 * @since 2.0
 */

public class CompilerError {
  /**
   * Is this a severe error or a warning?
   */
  private boolean error;
  /**
   * The start line number of the offending program text
   */
  private int startline;
  /**
   * The start column number of the offending program text
   */
  private int startcolumn;
  /**
   * The end line number of the offending program text
   */
  private int endline;
  /**
   * The end column number of the offending program text
   */
  private int endcolumn;
  /**
   * The name of the file containing the offending program text
   */
  private String file;
  /**
   * The actual error text produced by the language processor
   */
  private String message;
    
  /**
   * The error message constructor.
   *
   * @param file The name of the file containing the offending program text
   * @param error The actual error text produced by the language processor
   * @param startline The start line number of the offending program text
   * @param startcolumn The start column number of the offending program text
   * @param endline The end line number of the offending program text
   * @param endcolumn The end column number of the offending program text
   * @param message The actual error text produced by the language processor
   */
  public CompilerError(
    String file,
    boolean error,
    int startline,
    int startcolumn,
    int endline,
    int endcolumn,
    String message
  ) 
  {
    this.file = file;
    this.error = error;
    this.startline = startline;
    this.startcolumn = startcolumn;
    this.endline = endline;
    this.endcolumn = endcolumn;
    this.message = message;
  }

  /**
   * Return the filename associated with this compiler error.
   *
   * @return The filename associated with this compiler error
   */
  public String getFile() {
    return file;
  }

  /**
   * Assert whether this is a severe error or a warning
   *
   * @return Whether the error is severe
   */
  public boolean isError() {
    return error;
  }

  /**
   * Return the starting line number of the program text originating this error
   *
   * @return The starting line number of the program text originating this error
   */
  public int getStartLine() {
    return startline;
  }

  /**
   * Return the starting column number of the program text originating this
   * error
   *
   * @return The starting column number of the program text originating this
   * error
   */
  public int getStartColumn() {
    return startcolumn;
  }

  /**
   * Return the ending line number of the program text originating this error
   *
   * @return The ending line number of the program text originating this error
   */
  public int getEndLine() {
    return endline;
  }

  /**
   * Return the ending column number of the program text originating this
   * error
   *
   * @return The ending column number of the program text originating this
   * error
   */
  public int getEndColumn() {
    return endcolumn;
  }

  /**
   * Return the message produced by the language processor
   *
   * @return The message produced by the language processor
   */
  public String getMessage() {
    return message;
  }
}
