/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap.patterns;

/**
 * This class is an utility class that perform wilcard-patterns matching.
 * <br>
 * The pattern is made up with the following wildcards:
 * <ul>
 *   <li>The '*' character means that zero or more characters (excluding the
 *       path separator '/') are to be matched.</li>
 *   <li>The '**' sequence means that zero or more characters (including the
 *       path separator '/') are to be matched.</li>
 *   <li>The '\' character is used as an escape sequence ('\*' is translated in
 *       '*', it does not represent a matching pattern). If an exact '\'
 *       character needs to be matched the source string must contain a '\\'
 *       sequence.</li>
 * </ul>
 * 
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-27 01:25:41 $
 */
public class PatternMatcher {

    /** The int representing '*' in the pattern <code>int []</code>. */
    protected static final int MATCH_FILE=-1;
    /** The int representing '**' in the pattern <code>int []</code>. */
    protected static final int MATCH_PATH=-2;
    /** The int value that terminates the pattern <code>int []</code>. */
    protected static final int MATCH_END=-3;

    /** The <code>int []</code> identifying the pattern to match. */
    protected int[] sourcePattern=null;

    /**
     * Construct a new <code>PatternMatcher</code> instance.
     */
    public PatternMatcher() {
        super();
    }

    /**
     * Construct a new <code>PatternMatcher</code> instance.
     */
    public PatternMatcher(String pattern)
    throws PatternException {
        this();
        this.setPattern(pattern);
    }

    /**
     * Match the specified <code>String</code> against the configured pattern.
     */
    public boolean match(String data) {
        if ((data==null)||(this.sourcePattern==null)) return(false);
        return(this.matchPattern(data.toCharArray(),this.sourcePattern));
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

    /**
     * Matches the specified <code>char []</code> against the given pattern
     * formatted as an <code>int []</code>.
     * <br>
     * This method returns <b>true</b> if the specified <code>char []</code>
     * can be matched by the pattern created by <code>convertPattern(...)</code>
     * be represented as an <code>int []</code>, <b>false</b> otherwise.
     *
     * @param buff The <code>char []</code> of the path to be matched.
     * @param expr The <code>int []</code> representing the pattern, as
     *             returned by <code>convertPattern(...)</code>.
     * @exception NullPointerException If a parameter is <b>null</b>.
     */
    protected boolean matchPattern(char buff[], int expr[])
    throws NullPointerException {
        // Set the current position in buffers to zero
        int k=0;
        int exprpos=0;
        int buffpos=0;
        // This value represent the type of the last whildcard matching. It
        // will store MATCH_FILE '*' and MATCH_PATH for '**'
        int exprchr=0;
        // Go in loop :)
        while(true) {
            // Search the first MATCH_FILE ('*'), MATCH_PATH ('**') or
            // MATCH_END (end-of-buffer) character
            int end=exprpos;
            while(expr[end]>=0) end++;

            // Get the offset of the token included between '?', '*' or '**'
            int off=this.matchArray(expr,exprpos,end,buff,buffpos);

            // If the substring was not found, we have no match
            if(off==-1) return(false);

            // If the previous wildcard was MATCH_FILE ('*') then we have to
            // check that the part that was ignored doesn't contain a path
            // separator '/'
            // While we check, we also copy the characters that need to be 
            // substituted to '*' in the target array
            if(exprchr==MATCH_FILE) for(int x=buffpos; x<off; x++) {
                if (buff[x]=='/') return(false);
            }

            // Check if we reached the end of the expression
            if(expr[end]==MATCH_END) {
                // If also the data buffer is finished we have a match
                if(off+end-exprpos!=buff.length) return(false);
                else return(true);
            }

            // Set the current data buffer position to the first character
            // after the matched token
            buffpos=off+end-exprpos;
            // Set the current expression buffer position to the first
            // character after the wildcard
            exprpos=end+1;
            // Update the last expression character ('*' or '**')
            exprchr=expr[end];
        }
    }

    /**
     * Get the offset of a part of an int array within a char array.
     * <br>
     * This method return the index in d of the first occurrence after dpos of
     * that part of array specified by r, starting at rpos and terminating at
     * rend.
     *
     * @param r The array containing the data that need to be matched in d.
     * @param rpos The index of the first character in r to look for.
     * @param rend The index of the last character in r to look for plus 1.
     * @param d The array of char that should contain a part of r.
     * @param dpos The starting offset in d for the matching.
     * @return The offset in d of the part of r matched in d or -1 if that was
     *         not found.
     */
    protected int matchArray(int r[], int rpos, int rend, char d[], int dpos) {
        // Check if pos and len are legal
        if(rend<rpos) throw new IllegalArgumentException("rend<rpos");
        // If we need to match a zero length string return current dpos
        if(rend==rpos) return(dpos);
        // If we need to match a 1 char length string do it simply
        if(rend-rpos==1) {
            // Search for the specified character
            for(int x=dpos; x<d.length; x++) if (r[rpos]==d[x]) return(x);
        }
        // Main string matching loop. It gets executed if the characters to
        // match are less then the characters left in the d buffer
        while(dpos+rend-rpos<=d.length) {
            // Set current startpoint in d
            int y=dpos;
            // Check every character in d for equity. If the string is matched
            // return dpos
            for(int x=rpos; x<=rend; x++) {
                if(x==rend) return(dpos);
                if(r[x]==d[y]) y++;
                else break;
            }
            // Increase dpos to search for the same string at next offset
            dpos++;
        }
        // The remaining chars in d buffer were not enough or the string 
        // wasn't matched
        return(-1);
    }
}
