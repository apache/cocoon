/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.matching.helpers;

import java.util.Stack;

/**
 * This class is an utility class that perform wilcard-patterns matching and
 * isolation.
 * <br>
 * 
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-07-22 20:41:51 $
 */
public class WildcardURIMatcher {

    /** The int representing '*' in the pattern <code>int []</code>. */
    protected static final int MATCH_FILE=-1;
    /** The int representing '**' in the pattern <code>int []</code>. */
    protected static final int MATCH_PATH=-2;
    /** The int value that terminates the pattern <code>int []</code>. */
    protected static final int MATCH_END=-3;

    /**
     * match a pattern agains a string and isolates wildcard replacement into a 
     * <code>Stack</code>.
     */
    public static boolean match (Stack stack, String data, int[] expr) 
    throws NullPointerException {
        if (stack == null) throw new NullPointerException ("No Stack provided");
        if (data == null) throw new NullPointerException ("No data provided");
        if (expr == null) throw new NullPointerException ("No pattern expression provided");
        
        char buff[] = data.toCharArray ();
        // Allocate the result buffer
        char rslt[]=new char[expr.length+buff.length];
        
        // The previous and current position of the expression character
        // (MATCH_*)
        int charpos = 0;
        // Search the fist expression character
        while (expr[charpos] >= 0) charpos++;
        // The expression charater (MATCH_*)
        int exprchr = expr[charpos];

        // The position in the expression, input, translation and result arrays
        int exprpos = 0;
        int buffpos = 0;
        int trnspos = 0;
        int rsltpos = 0;
        int offset = -1;
        
        while (true) {
            // Check if the data in the expression array before the current
            // expression character matches the data in the input buffer
            offset = matchArray (expr, exprpos, charpos, buff, buffpos);
            if (offset < 0) return(false);

            // Copy the data from the translation buffer into the result buffer
            // up to the next expression character (arriving to the current in the
            // expression buffer)        
            // while (trns[trnspos] >= 0) rslt[rsltpos++] = (char)trns[trnspos++];
            // trnspos++;
                
            if (exprchr == MATCH_END) {
                if (rsltpos > 0)
                    stack.push (new String(rslt, 0, rsltpos));
                return (true);
            }
            
            // Search the next expression character
            buffpos += (charpos-exprpos);
            exprpos = ++charpos;
            while (expr[charpos] >= 0) charpos++;
            int prevchr = exprchr;
            exprchr = expr[charpos];
   
            offset = matchArray (expr, exprpos, charpos, buff, buffpos);
            if (offset < 0) return (false);
    
            // Copy the data from the source buffer into the result buffer
            // to substitute the expression character
            if (prevchr == MATCH_PATH) {
                while (buffpos < offset) rslt[rsltpos++] = buff[buffpos++];
            } else {
                // Matching file, don't copy '/'
                while (buffpos < offset) {
                    if (buff[buffpos] == '/') return (false);
                    rslt[rsltpos++] = buff[buffpos++];
                }
            stack.push (new String (rslt, 0, rsltpos));
            rsltpos = 0;
            }
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
    protected static int matchArray (int r[], int rpos, int rend, char d[], int dpos) {
        // Check if pos and len are legal
        if (rend < rpos) throw new IllegalArgumentException ("rend < rpos");
        // If we need to match a zero length string return current dpos
        if (rend == rpos) return (d.length); //?? dpos?
        // If we need to match a 1 char length string do it simply
        if ((rend - rpos) == 1) {
            // Search for the specified character
            for (int x = dpos; x < d.length; x++) 
                if (r[rpos] == d[x]) return (x);
        }
        // Main string matching loop. It gets executed if the characters to
        // match are less then the characters left in the d buffer
        while ((dpos + rend - rpos) <= d.length) {
            // Set current startpoint in d
            int y = dpos;
            // Check every character in d for equity. If the string is matched
            // return dpos
            for (int x = rpos; x <= rend; x++) {
                if (x == rend) return (dpos);
                if (r[x] == d[y]) y++;
                else break;
            }
            // Increase dpos to search for the same string at next offset
            dpos++;
        }
        // The remaining chars in d buffer were not enough or the string 
        // wasn't matched
        return (-1);
    }

    protected static int[] convertPattern(String data)
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
            if (argv.length<2) return;
            Stack stack = new Stack();
            int expr[] = convertPattern (argv[0]);
            System.out.println("Matching Expr.    \""+argv[0]+"\"");
            System.out.println("Uri String. \""+argv[1]+"\"");
            if (WildcardURIMatcher.match (stack, argv[1], expr)) {
                System.out.println("Matched");
                for (int i = 0; i < stack.size(); i++)
                    System.out.println(i+" "+(String)stack.elementAt (i));
            } else
                System.out.println("Not matched");
        } catch (Exception e) {
            System.out.println(e.getClass().getName());
            System.out.println(e.getMessage());
        }
    }
}
