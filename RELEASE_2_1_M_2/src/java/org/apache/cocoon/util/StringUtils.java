/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

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

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
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
package org.apache.cocoon.util;



/**
 * A collection of <code>String</code> handling utility methods.
 *
 * @author <a href="mailto:ricardo@apache.org">Ricardo Rocha</a>
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Id: StringUtils.java,v 1.1 2003/03/09 00:09:43 pier Exp $
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
