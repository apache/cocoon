/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import org.apache.cocoon.Job;
import org.apache.cocoon.sax.XMLConsumer;
import org.apache.cocoon.sax.XMLSource;
import org.apache.cocoon.framework.ConfigurationException;
import org.apache.cocoon.filters.Filter;
import org.apache.cocoon.producers.Producer;
import org.apache.cocoon.serializers.Serializer;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-02-09 08:35:07 $
 */
public class Processor {
    /** The int representing '*' */
    private static final int MATCH_FILE=-1;
    /** The int representing '**' */
    private static final int MATCH_PATH=-2;
    /** The int used to terminate strings */
    private static final int MATCH_END=-3;
    /** The pattern string generating the target space */
    private String targetString=null;
    /** The pattern string generating the source space */
    private String sourceString=null;
    /** The target space pattern */
    private int target[]=null;
    /** The source space pattern */
    private int source[]=null;
    /** The Partition containig this Processor. */
    private Partition partition=null;
    /** Deny empty construction */
    private Processor() {}

    /** This Processor Producer. */
    protected Producer producer=null;
    /** This Processor array of Filter. */
    protected Filter filters[]=null;
    /** This Processor Serializer. */
    protected Serializer serializer=null;

    /**
     * Create a new Processor instance.
     */
    public Processor(Partition part, String tgt, String src)
    throws ConfigurationException {
        super();
        this.partition=part;
        if (tgt==null) throw new ConfigurationException("Null target specified");
        if (src==null) throw new ConfigurationException("Null source specified");
        int t[]=this.arrayBuild(tgt);
        int s[]=this.arrayBuild(src);
        int y=0;
        for(int x=0; x<t.length; x++) if(t[x]<0) {
            while(s[y]>=0) y++;
            if(t[x]!=s[y])
                throw new ConfigurationException("Unbalanced target/source expression");
            else y++;
            if(t[x]==MATCH_END) break;
        }
        this.targetString=tgt;
        this.sourceString=src;
        this.target=t;
        this.source=s;
    }

    /**
     * Handle a given Job.
     */
    public boolean handle(Job job, OutputStream out)
    throws IOException, SAXException {
        String uri=job.getUri();
        if(!this.matchTarget(uri)) return(false);
        String source=this.translateTarget(uri);
        XMLConsumer consumer=this.serializer.getXMLConsumer(job,source,out);
        for (int x=0; x<this.filters.length; x++) {
            consumer=this.filters[x].getXMLConsumer(job,source,consumer);
        }
        this.producer.getXMLSource(job,source).produce(consumer);
        return(true);
    }

    /**
     * Return true if the specified string can be matched by the target space
     * matching rule.
     */
    public boolean matchTarget(String data) {
        return(this.match(this.target,data.toCharArray()));
    }

    /**
     * Return true if the specified string can be matched by the source space
     * matching rule.
     */
    public boolean matchSource(String data) {
        return(this.match(this.source,data.toCharArray()));
    }

    /**
     * Translate a string in the target space and get its equivalent in
     * the source space.
     * <br>
     * Example: If this object was constructed calling <code>new Processor(...,
     * &quot;*.html&quot;, &quot;sources/*.xml&quot;)</code>
     * calling <code>translateTarget(&quot;foobar.html&quot;)</code> will
     * return <code>&quot;/sources/foobar.xml&quot;</code>.
     *
     * @param tgtpath The target path to be translated into the source space.
     * @return The translation of the target path in the source space or null
     *         if the specified string cannot be matched in the target space.
     * @throws NullPointerException If the specified target path is null.
     */
    public String translateTarget(String tgtpath) {
        return(this.translate(this.target,this.source,tgtpath.toCharArray()));
    }

    /**
     * Translate a string in the source space and get its equivalent in
     * the target space.
     * <br>
     * Example: If this object was constructed calling <code>new Processor(...,
     * &quot;*.html&quot;, &quot;sources/*.xml&quot;)</code>
     * calling <code>translateTarget(&quot;/sources/foobar.xml&quot;)</code>
     * will return <code>&quot;foobar.html&quot;</code>.
     *
     * @param srcpath The source path to be translated into the target space.
     * @return The translation of the source path in the target space or null
     *         if the specified string cannot be matched in the source space.
     * @throws NullPointerException If the specified source path is null.
     */
    public String translateSource(String srcpath) {
        return(this.translate(this.source,this.target,srcpath.toCharArray()));
    }

    /** Internal routines for matching */
    private boolean match(int expr[], char buff[])
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
            int off=arrayMatch(expr,exprpos,end,buff,buffpos);

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

    /** Internal routines for translation */
    private String translate(int expr[], int trns[], char buff[])
    throws NullPointerException {
        char rslt[]=new char[1024];
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
            int off=arrayMatch(expr,exprpos,end,buff,buffpos);

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
                // If also the data buffer is finished we have a match
                if(off+end-exprpos!=buff.length) return(null);
                else return(new String(rslt,0,rsltpos));
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
    private int arrayMatch(int r[], int rpos, int rend, char d[], int dpos) {
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

    /**
     * This function translates a string into an int array converting the
     * special '*' and '\' characters.
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
     * All MATCH* values are smaller than zero, while normal characters are
     * greater.
     *
     * @parameter data The string to translate.
     * @return The encoded string as an int array, terminated by the MATCH_END
     *         value (don't consider the array length).
     * @exception NullPointerException If data is null.
     */
    private int[] arrayBuild(String data)
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
}
