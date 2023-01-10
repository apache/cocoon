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
package org.apache.cocoon.util;

import org.apache.regexp.RE;
import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;

import java.util.HashMap;
import java.util.Map;


/**
 * This class is an utility class that perform wildcard-patterns matching and isolation.
 *
 * @version $Id$
 */
public class WildcardMatcherHelper {
    //~ Static fields/initializers -----------------------------------------------------------------

    /** Default path separator: "/" */
    public static final char ESC = '\\';

    /** Default path separator: "/" */
    public static final char PATHSEP = '/';

    /** Default path separator: "/" */
    public static final char STAR = '*';

    //~ Methods ------------------------------------------------------------------------------------

    /**
     * Match a pattern agains a string and isolates wildcard replacement into a <code>Map</code>.
     * <br>
     * Here is how the matching algorithm works:
     *
     * <ul>
     *   <li>
     *     The '*' character, meaning that zero or more characters (excluding the path separator '/')
     *     are to be matched.
     *   </li>
     *   <li>
     *     The '**' sequence, meaning that zero or more characters (including the path separator '/')
     *     are to be matched.
     *   </li>
     *   <li>
     *     The '\*' sequence is honored as a litteral '*' character, not a wildcard
     *   </li>
     * </ul>
     * <br>
     * When more than two '*' characters, not separated by another character, are found their value is
     * considered as '**' and immediate succeeding '*' are skipped.
     * <br>
     * The '**' wildcard is greedy and thus the following sample matches as {"foo/bar","baz","bug"}:
     * <dl>
     *   <dt>pattern</dt>
     *   <dd>STAR,STAR,PATHSEP,STAR,PATHSEP,STAR,STAR (why can't I express it litterally?)</dt>
     *   <dt>string</dt>
     *   <dd>foo/bar/baz/bug</dt>
     * </dl>
     * The first '**' in the pattern will suck up as much as possible without making the match fail.
     * 
     * @param pat The pattern string.
     * @param str The string to math agains the pattern
     *
     * @return a <code>Map</code> containing the representation of the extracted pattern. The extracted patterns are
     *         keys in the <code>Map</code> from left to right beginning with "1" for te left most, "2" for the next,
     *         a.s.o. The key "0" is the string itself. If the return value is null, string does not match to the
     *         pattern .
     */
    public static Map match(final String pat,
                            final String str) {
        Matcher matcher;
        synchronized (cache) {
            matcher = (Matcher) cache.get(pat);
            if ( matcher == null ) {
                matcher = new Matcher(pat);
                cache.put(pat, matcher);
            }
        }

        String[] list = matcher.getMatches(str);
        if ( list == null )
            return null;

        int n = list.length;
        Map map = new HashMap(n * 2 + 1);
        for ( int i = 0; i < n; i++ ) {
            map.put(String.valueOf(i), list[i]);
        }

        return map;
    }

    /** Cache for compiled pattern matchers */
    private static final Map cache = new HashMap();

    //~ Inner Classes ------------------------------------------------------------------------------

    /**
     * The private matcher class
     */
    private static class Matcher {

        /** Regexp to split constant parts from front and back leaving wildcards in the middle. */
        private static final REProgram splitter;

        static {
            final String fixedRE = "([^*\\\\]*)";
            final String wcardRE = "(.*[*\\\\])";
            final String splitRE = "^" + fixedRE + wcardRE + fixedRE + "$";
            splitter = new RECompiler().compile(splitRE);
        }

        /** Wildcard types to short-cut simple '*' and "**' matches. */
        private static final int WC_CONST = 0;
        private static final int WC_STAR = 1;
        private static final int WC_STARSTAR = 2;
        private static final int WC_REGEXP = 3;

        //~ Instance fields ------------------------------------------------------------------------

        // All fields declared final to emphasize requirement to be thread-safe.

        /** Fixed text at start of pattern. */
        private final String prefix;

        /** Fixed text at end of pattern. */
        private final String suffix;
        
        /** Length of prefix and suffix. */
        private final int fixlen;
        
        /** Wildcard type of pattern. */
        private final int wctype;
        
        /** Compiled regexp equivalent to wildcard pattern between prefix and suffix. */
        private final REProgram regexp;

        //~ Constructors ---------------------------------------------------------------------------

        /**
         * Creates a new Matcher object.
         *
         * @param pat The pattern
         * @param str The string
         */
        Matcher(final String pat) {
            RE re = new RE(splitter);

            if ( re.match(pat) ) {

                // Split pattern into (foo/)(*)(/bar).

                prefix = re.getParen(1);
                String wildcard = re.getParen(2);
                String tail = re.getParen(3);

                // If wildcard ends with \ then add the first char of postfix to wildcard.
                if ( tail.length() != 0 && wildcard.charAt(wildcard.length() - 1) == ESC ) {
                    wildcard = wildcard + tail.substring(0, 1);
                    suffix = tail.substring(1);
                }
                else {
                    suffix = tail;
                }

                // Use short-cuts for single * or ** wildcards

                if ( wildcard.equals("*") ) {
                    wctype = WC_STAR;
                    regexp = null;
                }
                else if ( wildcard.equals("**") ) {
                    wctype = WC_STARSTAR;
                    regexp = null;
                }
                else {
                    wctype = WC_REGEXP;
                    regexp = compileRegexp(wildcard);
                }
            }
            else {
                // Pattern is a constant without '*' or '\'.
                prefix = pat;
                suffix = "";
                wctype = WC_CONST;
                regexp = null;
            }

            fixlen = prefix.length() + suffix.length();
        }

        //~ Methods --------------------------------------------------------------------------------

        /**
         * Match string against pattern.
         * 
         * @param str The string
         * @return list of wildcard matches, null if match failed
         */
        String[] getMatches(final String str) {

            // Protect against 'foo' matching 'foo*foo'.
            if ( str.length() < fixlen )
                return null;

            if ( !str.startsWith(prefix) )
                return null;

            if ( !str.endsWith(suffix) )
                return null;

            String infix = str.substring(prefix.length(), str.length() - suffix.length());

            if ( wctype == WC_REGEXP ) {
                RE re = new RE(regexp);
                if ( !re.match(infix) )
                    return null;

                int n = re.getParenCount();
                String[] list = new String[n];
                list[0] = str;
                for ( int i = 1; i < n; i++ )
                    list[i] = re.getParen(i);
                return list;
            }

            if ( wctype == WC_CONST ) {
                if ( infix.length() != 0 )
                    return null;
                return new String[] {
                    str
                };
            }

            if ( wctype == WC_STAR ) {
                if ( infix.indexOf(PATHSEP) != -1 )
                    return null;
            }

            return new String[] {
                str, infix
            };
        }
    }

    /**
     * Compile wildcard pattern into regexp pattern.
     * 
     * @param pat The wildcard pattern
     * @return compiled regexp program.
     */
    private static REProgram compileRegexp(String pat) {
        StringBuffer repat = new StringBuffer(pat.length() * 6);
        repat.append('^');

        // Add an extra character to allow unchecked wcpat[i+1] accesses.
        // Unterminated ESC sequences are silently handled as '\\'.
        char[] wcpat = (pat + ESC).toCharArray();
        for ( int i = 0, n = pat.length(); i < n; i++ ) {
            char ch = wcpat[i];

            if ( ch == STAR ) {
                if ( wcpat[i + 1] != STAR ) {
                    repat.append("([^/]*)");
                    continue;
                }

                // Handle two and more '*' as single '**'.
                while ( wcpat[i + 1] == STAR )
                    i++;
                repat.append("(.*)");
                continue;
            }

            // Match ESC+ESC and ESC+STAR as literal ESC and STAR which needs to be escaped
            // in regexp. Match ESC+other as two characters ESC+other where other may also
            // need to be escaped in regexp.
            if ( ch == ESC ) {
                ch = wcpat[++i];
                if ( ch != ESC && ch != STAR )
                    repat.append("\\\\");
            }

            if ( ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9'
                 || ch == '/' ) {
                repat.append(ch);
                continue;
            }

            repat.append('\\');
            repat.append(ch);
        }
        repat.append('$');

        return new RECompiler().compile(repat.toString());
    }
}
