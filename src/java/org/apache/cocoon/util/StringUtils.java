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
package org.apache.cocoon.util;

/**
 * A collection of <code>String</code> handling utility methods.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: StringUtils.java,v 1.4 2004/05/05 22:28:22 ugo Exp $
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
        return Tokenizer.tokenize(line, delimiter, false);
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
     * @deprecated Use {@link org.apache.commons.lang.StringUtils#countMatches(String, String)}
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
     * @deprecated Use {@link org.apache.commons.lang.StringUtils#indexOfDifference(String, String)}
     */
    public static int matchStrings(String a, String b) {
        int i;
        char[] ca = a.toCharArray();
        char[] cb = b.toCharArray();
        int len = ( ca.length < cb.length ) ? ca.length : cb.length;

        for (i = 0; i < len; i++) {
            if (ca[i] != cb[i]) break;
        }

        return i;
    }

    /**
     * Replaces tokens in input with Value present in System.getProperty
     */
    public static String replaceToken(String s) {
        int startToken = s.indexOf("${");
        int endToken = s.indexOf("}",startToken);
        String token = s.substring(startToken+2,endToken);
        StringBuffer value = new StringBuffer();
        value.append(s.substring(0,startToken));
        value.append(System.getProperty(token));
        value.append(s.substring(endToken+1));
        return value.toString();
    }
}
