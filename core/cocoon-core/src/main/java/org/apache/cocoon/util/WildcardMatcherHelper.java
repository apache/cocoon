/*
 * Copyright 1999-2006 The Apache Software Foundation.
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

import java.util.HashMap;
import java.util.Map;


/**
 * This class is an utility class that perform wilcard-patterns matching and isolation.
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
     *
     * When more than two '*' characters, not separated by another character, are found their value is
     * considered as '**'.
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
        final Matcher map = new Matcher(pat, str);

        if(map.isMatch()) {
            return map.getMap();
        }

        return null;
    }

    //~ Inner Classes ------------------------------------------------------------------------------

    /**
     * The private matcher class
     */
    private static class Matcher {
        //~ Instance fields ------------------------------------------------------------------------

        /** The character array of the pattern */
        private final char[] apat;

        /** The character array of the string */
        private final char[] astr;

        /** The <code>Map</code> to be filled */
        private Map map = new HashMap();

        /** The pattern */
        private final String pat;

        /**The string */
        private final String str;

        /** Whether string matched to pattern */
        private final boolean matched;

        /** map index */
        private int idx = 0;

        /** index into pattern */
        private int ipat;

        /** index into string */
        private int istr;

        //~ Constructors ---------------------------------------------------------------------------

        /**
         * Creates a new Matcher object.
         *
         * @param aPat The pattern
         * @param aStr The string
         */
        public Matcher(final String aPat,
                       final String aStr) {
            this.str = aStr;
            this.pat = aPat;
            ipat = pat.length() - 1;
            istr = str.length() - 1;
            apat = pat.toCharArray();
            astr = str.toCharArray();
            add(str);
            matched = match();
        }

        //~ Methods --------------------------------------------------------------------------------

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public Map getMap() {
            // reverse the map 
            final Map newmap = new HashMap();
            int j = map.size() - 1;
            newmap.put("0", map.get("0"));

            for(int i = 1; i < map.size(); i++) {
                final Object o = map.get("" + i);
                newmap.put("" + j, o);
                j--;
            }

            return newmap;
        }

        /**
         * Has it matched?
         *
         * @return whether it has matched
         */
        public boolean isMatch() {
            return matched;
        }

        /**
         * Add a etracted substring to the map
         *
         * @param aStr The extracted substring
         */
        private void add(final String aStr) {
            map.put(String.valueOf(idx++), aStr);
        }

        /**
         * Scans the pattern and the search string from the end toward the start
         *
         * @return wether the pstring matches the pattern
         */
        private boolean match() {
            // scan a common literal suffix
            scanLiteralSuffix();

            // if we are already at the start of both strings 
            // than the pattern matched
            if((ipat < 0) && (istr < 0)) return true;

            // if hole string has matched the pattern so far and the rest of the pattern only has wildcard(s)
            // we match too otherwise we clearly don't match
            if((ipat >= 0) && (istr < 0)) {
                while((ipat >= 0) && (apat[ipat] == STAR)) ipat--;

                if(ipat < 0) {
                    add("");

                    return true;
                } else {
                    return false;
                }
            }

            // if hole pattern has matched the string so far but the string has more characters left
            // we don't match
            if((ipat < 0) && (istr >= 0)) return false;

            // if we have not stopped at a wildcard character 
            // a character doesn't match and thus we do not match at all
            if(apat[ipat] != STAR) return false;

            // if it is a double (or more) wildcard pattern
            if((ipat > 0) && (apat[ipat - 1] == STAR)) {
                // skip to first non star charater in the pattern
                while((--ipat >= 0) && (apat[ipat] == STAR));

                // if we are at the start of the pattern we've matched and are finish scanning
                if(ipat < 0) {
                    add(str.substring(0, istr + 1));

                    return true;
                }

                // Now we need to scan for the start of the literal characters in the pattern
                final int eipat = ipat + 1; // end position of a literal character used for substring operations

                while((ipat >= 0) && ((apat[ipat] != STAR) || ((ipat > 0) && (apat[ipat - 1] == ESC)))) ipat--;

                // if we reached the start of the pattern just do a string compare with the corresponding part from 
                // the start of the string
                if(ipat < 0) {
                    // if the remaining length of the string isn't the same as that found in the pattern 
                    // we do not match
                    if(strncmp(apat, 0, astr, 0, eipat)) {
                        add(str.substring(eipat, istr + 1));

                        return true;
                    }

                    // otherwise we do not match
                    return false;
                }

                // Now we need to check whether the litteral substring of the pattern 
                // is contained in the string somewhere
                final int l = eipat - ipat - 1;
                final int eistr = istr + 1;

                while(((istr - l) >= 0) && ! strncmp(apat, ipat + 1, astr, istr - l + 1, l)) istr--;

                if((istr - l) < 0) return false;

                add(str.substring(istr + 1, eistr));
                istr -= l;
            } else // if it is a single star pattern
             {
                // skip the star
                --ipat;

                // if we are at the beginning of the pattern we have to check there is not PATH_SEP in string
                if(ipat < 0) {
                    final int eistr = istr + 1;

                    while((istr >= 0) && (astr[istr] != PATHSEP)) istr--;

                    if(istr < 0) {
                        add(str.substring(0, eistr));

                        return true;
                    }

                    // otherwise we do not match
                    return false;
                }

                // Now we need to search for the start of either a path sparator or another wildcard characters 
                // in the pattern
                final int eipat = ipat + 1;

                while((ipat >= 0) &&
                      ((apat[ipat] != STAR) || ((ipat > 0) && (apat[ipat - 1] == ESC))) &&
                      (apat[ipat] != PATHSEP)) {
                    ipat--;
                }

                // if we reached the beginning of the pattern just do a String compare with the corresponding part from 
                // the beginning of the string
                if(ipat < 0) {
                    if(strncmp(apat, 0, astr, 0, eipat)) {
                        add(str.substring(eipat, istr + 1));

                        return true;
                    }

                    // otherwise we do not match
                    return false;
                }

                // Now we need to check whether the litteral substring of the pattern 
                // is contained in the string somewhere
                if(apat[ipat] != PATHSEP) {
                    ipat++;
                }

                final int l = eipat - ipat;
                final int eistr = ++istr; // move one up over the end 

                while(((istr - l) >= 0) && ! strncmp(apat, ipat, astr, istr - l, l)) istr--;

                if((istr - l) < 0) return false;

                add(str.substring(istr, eistr)); // TODO check this asd
                istr -= (l + 1);
                --ipat;
            }

            return match();
        }

        /**
         * Scan a possible common suffix
         */
        private void scanLiteralSuffix() {
            // scan a common literal suffix
            while((ipat >= 0) &&
                  (istr >= 0) &&
                  ((apat[ipat] != STAR) || ((ipat > 0) && (apat[ipat - 1] == ESC))) &&
                  (apat[ipat] == astr[istr])) {
                ipat--;

                if((ipat >= 0) && (apat[ipat] == ESC))  ipat--;

                istr--;
            }
        }

        /**
         * Compare two charater array from  individual offsets
         *
         * @param a1 The first character array
         * @param o1 The offset into the first character array
         * @param a2 The second character array
         * @param o2 The offset into the second character array
         * @param l The length to compare
         *
         * @return Whether the all the mentioned characters match each other
         */
        private boolean strncmp(final char[] a1,
                                final int o1,
                                final char[] a2,
                                final int o2,
                                final int l) {
            int i = 0;

            for(i = 0; (i < l) && (a1[o1 + i] == a2[o2 + i]); i++);

            return i == l;
        }
    }
}
