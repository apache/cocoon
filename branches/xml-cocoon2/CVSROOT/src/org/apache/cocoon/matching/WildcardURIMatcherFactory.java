/***************************************************************************** 
 * Copyright (C) The Apache Software Foundation. All rights reserved.        * 
 * ------------------------------------------------------------------------- * 
 * This software is published under the terms of the Apache Software License * 
 * version 1.1, a copy of which has been included  with this distribution in * 
 * the LICENSE file.                                                         * 
 *****************************************************************************/
 
package org.apache.cocoon.matching; 

import org.apache.avalon.ConfigurationException;

import org.w3c.dom.DocumentFragment;
 
/** 
 * This class generates source code which represents a specific pattern matcher
 * for request URIs
 * 
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a> 
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a> 
 * @version CVS $Revision: 1.1.2.12 $ $Date: 2000-09-27 16:07:29 $ 
 */ 

public class WildcardURIMatcherFactory implements MatcherFactory {

    /** The int representing '*' in the pattern <code>int []</code>. */
    protected static final int MATCH_FILE	= -1;
    /** The int representing '**' in the pattern <code>int []</code>. */
    protected static final int MATCH_PATH	= -2;
    /** The int representing begin in the pattern <code>int []</code>. */
    protected static final int MATCH_BEGIN	= -4;
    /** The int representing end in pattern <code>int []</code>. */
    protected static final int MATCH_THEEND	= -5;
    /** The int value that terminates the pattern <code>int []</code>. */
    protected static final int MATCH_END	= -3;

    /** The <code>int []</code> identifying the pattern to match. */
    protected int[] sourcePattern = null;

    /**
     * Generates the matcher method level source code
     */
    public String generateMethodSource (String prefix, String pattern, 
                                        DocumentFragment conf) 
    throws ConfigurationException {
        StringBuffer result = new StringBuffer();
        return result.append ("java.util.ArrayList list = new ArrayList();")
                     .append ("if (org.apache.cocoon.matching.helpers.WildcardURIMatcher.match (list, environment.getUri(), ")
                     .append(prefix).append("_expr))")
                     .append ("return list;")
                     .append ("else return null;").toString();
    }

    /**
     * Generates the matcher class level source code
     */
    public String generateClassSource (String prefix, String pattern, 
                                       DocumentFragment conf) 
    throws ConfigurationException {
        StringBuffer result = new StringBuffer();
        try {
            this.setPattern (pattern);
   
            result.append ("// wildcard pattern = \"" + pattern + "\"\n\t")
                  .append ("static int[] ").append(prefix).append("_expr = {");

            int j = sourcePattern.length - 1;
            char c;
            for (int i = 0; i < j; i++) {
                result.append (sourcePattern[i])
                      .append (',');
            }
            return result.append (sourcePattern[j])
                         .append ("};").toString();
        } catch (NullPointerException pe) {
            throw new ConfigurationException (pe.getMessage(), null);
        }
    }

    /**
     * Set the pattern for matching.
     */
    public void setPattern(String pattern)
    throws NullPointerException {
        if (pattern == null) throw new NullPointerException("Pattern cannot be null");
        this.sourcePattern = this.convertPattern(pattern);
    }

    /**
     * Translate the given <code>String</code> into a <code>int []</code>
     * representing the pattern matchable by this class.
     * <br>
     * This function translates a <code>String</code> into an int array
     * converting the special '*' and '\' characters.
     * <br>
     * Here is how the conversion algorithm works:
     * <ul>
     *   <li>The '*' character is converted to MATCH_FILE, meaning that zero
     *        or more characters (excluding the path separator '/') are to
     *        be matched.</li>
     *   <li>The '**' sequence is converted to MATCH_PATH, meaning that zero
     *       or more characters (including the path separator '/') are to
     *        be matched.</li>
     *   <li>The '\' character is used as an escape sequence ('\*' is
     *       translated in '*', not in MATCH_FILE). If an exact '\' character
     *       is to be matched the source string must contain a '\\'.
     *       sequence.</li>
     * </ul>
     * When more than two '*' characters, not separated by another character,
     * are found their value is considered as '**' (MATCH_PATH).
     * <br>
     * The array is always terminated by a special value (MATCH_END).
     * <br>
     * All MATCH* values are less than zero, while normal characters are equal
     * or greater.
     *
     * @parameter data The string to translate.
     * @return The encoded string as an int array, terminated by the MATCH_END
     *         value (don't consider the array length).
     * @exception NullPointerException If data is null.
     */
    protected int[] convertPattern(String data)
    throws NullPointerException {
        
        // Prepare the arrays
        int expr[] = new int[data.length() + 2];
        char buff[] = data.toCharArray();
        
        // Prepare variables for the translation loop
        int y = 0;
        boolean slash = false;

        // Must start from beginning
        expr[y++] = MATCH_BEGIN;
        
        if (buff.length > 0) {
            if (buff[0]=='\\') {
                slash = true;
            } else if (buff[0] == '*') {
                expr[y++] = MATCH_FILE;
            }  else {
                expr[y++] = buff[0];
            }
        
            // Main translation loop
            for (int x = 1; x < buff.length; x++) {
                // If the previous char was '\' simply copy this char.
                if (slash) {
                    expr[y++] = buff[x];
                    slash = false;
                // If the previous char was not '\' we have to do a bunch of checks
                } else {
                    int prev = (y - 1);
                    // If this char is '\' declare that and continue
                    if (buff[x] == '\\') {
                        slash = true;
                    // If this char is '*' check the previous one
                    } else if (buff[x] == '*') {
                        // If the previous character als was '*' match a path
                        if (expr[y-1] <= MATCH_FILE) {
                            expr[y-1] = MATCH_PATH;
                        } else {
                            expr[y++] = MATCH_FILE;
                        }
                    } else {
                        expr[y++]=buff[x];
                    }
                }
            }
        }

        // Must match end at the end
        expr[y] = MATCH_THEEND;
        return expr;
    }        
}
