/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.util;

import java.util.StringTokenizer;

/**
 * A collection of <code>String</code> handling utility methods.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-07-29 18:30:42 $
 */
public class StringUtils {
  /**
   * Split a string as an array using whitespace as separator
   *
   * @param line The string to be split
   * @return An array of whitespace-separated tokens
   */
  public static String[] split(String line) {
    return split(line, " \t\n\r");
  }

  /**
   * Split a string as an array using a given set of separators
   *
   * @param line The string to be split
   * @param delimiter A string containing token separators
   * @return An array of token
   */
  public static String[] split(String line, String delimiter) {
    Tokenizer tokenizer = new Tokenizer(line, delimiter);
    int tokenCount = tokenizer.countTokens();
    String[] result = new String[tokenCount];

    for (int i = 0; i < tokenCount; i++) {
      result[i] = tokenizer.nextToken();
    }

    return result;
  }

  /**
   * Tests whether a given character is alphabetic, numeric or
   * underscore
   *
   * @param c The character to be tested
   * @return whether the given character is alphameric or not
   */
  public static boolean isAlphaNumeric(char c) {
    return c == '_' ||
           (c >= 'a' && c <= 'z') ||
           (c >= 'A' && c <= 'Z') ||
           (c >= '0' && c <= '9');
  }
}
