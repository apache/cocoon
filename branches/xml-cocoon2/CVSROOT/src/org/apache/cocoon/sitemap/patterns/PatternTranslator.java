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
 * @version CVS $Revision: 1.1.2.5 $ $Date: 2000-02-27 12:56:21 $
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
     * <br>
     * <i>Seems ok now</i>
     */
    protected String translatePattern(char buff[], int expr[], int trns[])
    throws NullPointerException {
        // Allocate the result buffer
        char rslt[]=new char[expr.length+trns.length+buff.length];
        
        // The previous and current position of the expression character
        // (MATCH_*)
        int charpos=0;
        // Search the fist expression character
        while (expr[charpos]>=0) charpos++;
        // The expression charater (MATCH_*)
        int exprchr=expr[charpos];

        // The position in the expression, input, translation and result arrays
        int exprpos=0;
        int buffpos=0;
        int trnspos=0;
        int rsltpos=0;
        int offset=-1;
        
        while (true) {
            // Check if the data in the expression array before the current
            // expression character matches the data in the input buffer
            offset=super.matchArray(expr,exprpos,charpos,buff,buffpos);
            if (offset<0) return(null);

            // Copy the data from the translation buffer into the result buffer
            // up to the next expression character (arriving to the current in the
            // expression buffer)        
            while (trns[trnspos]>=0) rslt[rsltpos++]=(char)trns[trnspos++];
            trnspos++;
                
            if (exprchr==MATCH_END) return(new String(rslt,0,rsltpos));
            
            // Search the next expression character
            buffpos+=(charpos-exprpos);
            exprpos=++charpos;
            while (expr[charpos]>=0) charpos++;
            int prevchr=exprchr;
            exprchr=expr[charpos];
   
            offset=super.matchArray(expr,exprpos,charpos,buff,buffpos);
            if (offset<0) return(null);
    
            // Copy the data from the source buffer into the result buffer
            // to substitute the expression character
            if (prevchr==MATCH_PATH) {
                while (buffpos<offset) rslt[rsltpos++]=buff[buffpos++];
            } else {
                // Matching file, don't copy '/'
                while (buffpos<offset) {
                    if (buff[buffpos]=='/') return(null);
                    rslt[rsltpos++]=buff[buffpos++];
                }
            }
        }
    }
    
    /** Testing */
    public static void main(String argv[]) {
        try {
            if (argv.length<3) return;
            PatternTranslator t=new PatternTranslator(argv[0],argv[1]);
            System.out.println("Matching Expr.    \""+argv[0]+"\"");
            System.out.println("Translation Expr. \""+argv[1]+"\"");
            System.out.println("Source Data       \""+argv[2]+"\"");
            System.out.println(" - Math returned \""+t.match(argv[2])+"\"");
            System.out.println(" - Translation   \""+t.translate(argv[2])+"\"");
        } catch (Exception e) {
            System.out.println(e.getClass().getName());
            System.out.println(e.getMessage());
        }
    }
}
