/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap.patterns;

/**
 * This class is an utility class that perform wilcard-patterns matching and
 * translation.
 * <br>
 * The pattern rules follow the one defined by <code>PatternMatcher</code> and
 * the translation is done in the following way (in example):
 * if the source pattern is &quot;test/*.xml&quot; and the target pattern is
 * &quot;sources/data/*&quot;, the <code>String</code> &quot;test/my.xml&quot;
 * will be translated into &quot;sources/data/my&quot;
 * 
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.4 $ $Date: 2000-02-27 07:15:11 $
 */
public class PatternTranslator extends PatternMatcher {

    /** Wether the source and the target patterns are equal. */
    private boolean identityTranslation=false;

    /** The int array identifying the pattern to match. */
    protected int[] targetPattern=null;


    /**
     * Construct a new <code>PatternTranslator</code> instance.
     */
    public PatternTranslator() {
        super();
    }

    /**
     * Construct a new <code>PatternTranslator</code> instance.
     */
    public PatternTranslator(String pattern)
    throws PatternException {
        this();
        this.setPattern(pattern);
    }

    /**
     * Construct a new <code>PatternTranslator</code> instance.
     */
    public PatternTranslator(String source, String target)
    throws PatternException {
        this();
        this.setPattern(source,target);
    }

    /**
     * Translate the specified <code>String</code> according to the rules
     * specified by the source and target patterns.
     * <br>
     * If the specified <code>String</code> is not matched by the source
     * pattern <b>null</b> is returned.
     * <br>
     * If the source and the target patterns are qual the same
     * <code>String</code> is returned.
     */
    public String translate(String data) {
        if (data==null) throw new NullPointerException("Null data");
        if (this.identityTranslation) return(data);
        if ((this.targetPattern==null) || (super.sourcePattern==null))
            throw new IllegalStateException("Null internals");
        if (!super.match(data)) throw new IllegalArgumentException("No match");
        char x[]=data.toCharArray();
        
        return(this.translatePattern(x,super.sourcePattern,this.targetPattern));
    }

    /**
     * Set the source and target patterns as two identical patterns.
     * <br>
     * No translation will be performed, as this specifies the identity
     * transformation.
     */
    public void setPattern(String pattern)
    throws PatternException {
        if (pattern==null) throw new PatternException("Null pattern");
        this.setPattern(pattern,pattern);
    }

    /**
     * Set the source and target patterns.
     * <br>
     * If the source pattern is equal to the target one, no translation will
     * be performed as same patterns specify an identity transformation.
     */
    public void setPattern(String source, String target)
    throws PatternException {
        if (source==null) throw new PatternException("Null source pattern");
        if (target==null) throw new PatternException("Null target pattern");
        if (source.equals(target)) {
            this.targetPattern=super.convertPattern(target);
            super.sourcePattern=this.targetPattern;
            this.identityTranslation=true;
            return;
        }
        int t[]=super.convertPattern(target);
        int s[]=super.convertPattern(source);
        int y=0;
        for(int x=0; x<t.length; x++) if(t[x]<0) {
            while(s[y]>=0) y++;
            if(t[x]!=s[y]) throw new PatternException("Unbalanced expression");
            else y++;
            if(t[x]==MATCH_END) break;
        }
        this.targetPattern=t;
        super.sourcePattern=s;
    }

    /** 
     * Internal routines for translation
     * <br>
     * <b>FIXME: This routine doesn't match when the pattern ends with a
     *    wildcard. Need to fix together with <code>matchPattern()</code>,
     *    in <code>PatternMatcher</code> otherwise it won't work.</b>
     */
    protected String translatePattern(char buff[], int expr[], int trns[])
    throws NullPointerException {
        char rslt[]=new char[expr.length+trns.length+buff.length];
        // Set the current position in buffers to zero
        int k=0;
        int rsltpos=0;
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
            int off=super.matchArray(expr,exprpos,end,buff,buffpos);

            // If the substring was not found, we have no match
            if(off==-1) return(null);

            // If the previous wildcard was MATCH_FILE ('*') then we have to
            // check that the part that was ignored doesn't contain a path
            // separator '/'
            // While we check, we also copy the characters that need to be 
            // substituted to '*' in the target array
            if(exprchr==MATCH_FILE) for(int x=buffpos; x<off; x++) {
                if (buff[x]=='/') return(null);
                else rslt[rsltpos++]=buff[x];
            }

            // If the previous wildcard was MATCH_FILE ('*') then we just copy
            // the characters that need to be substituted to '**' in the target
            // array
            else if(exprchr==MATCH_PATH) {
                for(int x=buffpos; x<off; x++) rslt[rsltpos++]=buff[x];
            }                

            // Copy the current token of characters from the source array
            while(trns[k]>=0) rslt[rsltpos++]=(char)trns[k++]; k++;

            // Check if we reached the end of the expression
            if(expr[end]==MATCH_END) {
                // If the data buffer is finished we have a match
                if(off+end-exprpos==buff.length)
                    return(new String(rslt,0,rsltpos));
                // Otherwise return the null string
                return(null);
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
}
