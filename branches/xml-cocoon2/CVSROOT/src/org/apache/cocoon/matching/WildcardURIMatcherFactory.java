/***************************************************************************** 
 * Copyright (C) The Apache Software Foundation. All rights reserved.        * 
 * ------------------------------------------------------------------------- * 
 * This software is published under the terms of the Apache Software License * 
 * version 1.1, a copy of which has been included  with this distribution in * 
 * the LICENSE file.                                                         * 
 *****************************************************************************/ 
package org.apache.cocoon.matching; 

import org.apache.cocoon.sitemap.patterns.PatternTranslator; 
import org.apache.cocoon.sitemap.patterns.PatternException; 

import org.w3c.dom.DocumentFragment;
 
/** 
 * This class generates source code which represents a specific pattern matcher
 * for request URIs
 * 
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a> 
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-07-17 21:06:12 $ 
 */ 

public class WildcardURIMatcherFactory /*extends PatternTranslator*/ implements MatcherFactory {

    /** The int representing '*' in the pattern <code>int []</code>. */
    protected static final int MATCH_FILE=-1;
    /** The int representing '**' in the pattern <code>int []</code>. */
    protected static final int MATCH_PATH=-2;
    /** The int value that terminates the pattern <code>int []</code>. */
    protected static final int MATCH_END=-3;

    /** The <code>int []</code> identifying the pattern to match. */
    protected int[] sourcePattern=null;

    /**
     * Generates the matcher method source code
     */
    public String generate (String pattern, DocumentFragment conf) 
    throws PatternException {
        StringBuffer result = new StringBuffer();
        this.setPattern (pattern);
   
        result.append ("Stack stack = new Stack();");
        result.append ("/* pattern=\""+pattern+"\" */");
        result.append ("int expr[] = {");

        int j = sourcePattern.length-1;
        char c;
        for (int i = 0; i < j; i++) {
            result.append (sourcePattern[i]);
            result.append (',');
        }
        result.append (sourcePattern[j]);
        result.append ("};");
        result.append ("if (org.apache.cocoon.matching.helpers.WildcardURIMatcher.match (stack, request.getUri(), expr))");
        result.append ("return (Map) stack;");
        result.append ("else return null;");
        return result.toString();
    }

    /**
     * Set the pattern for matching.
     */
    public void setPattern(String pattern)
    throws PatternException {
        if (pattern==null) throw new PatternException("Null pattern");
        this.sourcePattern=this.convertPattern(pattern);
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
        int expr[]=new int[data.length()+1];
        char buff[]=data.toCharArray();
        // Prepare variables for the translation loop
        int y=0;
        boolean slash=false;
        if(buff[0]=='\\') slash=true;
        else if(buff[0]=='*') expr[y++]=MATCH_FILE;
        else expr[y++]=buff[0];
        // Main translation loop
        for (int x=1; x<buff.length; x++) {
            // If the previous char was '\' simply copy this char.
            if (slash) {
                expr[y++]=buff[x];
                slash=false;
            // If the previous char was not '\' we have to do a bunch of checks
            } else {
                int prev=(y-1);
                // If this char is '\' declare that and continue
                if(buff[x]=='\\') {
                    slash=true;
                // If this char is '*' check the previous one
                } else if(buff[x]=='*') {
                    // If the previous character als was '*' match a path
                    if(expr[y-1]<=MATCH_FILE) expr[y-1]=MATCH_PATH;
                    else expr[y++]=MATCH_FILE;
                } else expr[y++]=buff[x];
            }
        }
        // Declare the end of the array and return it
        expr[y]=MATCH_END;
        return(expr);
    }        
     
    /** Testing */
    public static void main(String argv[]) {
        try {
            if (argv.length<1) return;
            System.out.println("Matching Expr.    \""+argv[0]+"\"");
            WildcardURIMatcherFactory wm = new WildcardURIMatcherFactory();
            System.out.println(wm.generate (argv[0], null));
        } catch (Exception e) {
            System.out.println(e.getClass().getName());
            System.out.println(e.getMessage());
        }
    }

}
