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
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2000-10-06 21:25:33 $
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

    /**
     * Counts the occurrence of the given char in the string.
     *
     * @param str The string to be tested
     * @param c the char to be counted
     * @return the occurrence of the character in the string.
     */
    public static int count(String str, char c) {
        int index = 0;
        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == c) index++;
        }
        return index;
    }

    /**
     * Matches two strings.
     *
     * @param a The first string
     * @param b The second string
     * @return the index where the two strings stop matching starting from 0
     */
    public static int matchStrings(String a, String b) {
        int i;
        char[] ca = a.toCharArray();
        char[] cb = b.toCharArray();
        for (i = 0; (i < ca.length) || (i < cb.length); i++) {
            if (ca[i] != cb[i]) break;
        }
        return i;
    }
}
