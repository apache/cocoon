/***************************************************************************** 
 * Copyright (C) The Apache Software Foundation. All rights reserved.        * 
 * ------------------------------------------------------------------------- * 
 * This software is published under the terms of the Apache Software License * 
 * version 1.1, a copy of which has been included  with this distribution in * 
 * the LICENSE file.                                                         * 
 *****************************************************************************/ 
package org.apache.cocoon.matching; 

import org.apache.regexp.RECompiler;
import org.apache.regexp.REProgram;
import org.apache.regexp.RESyntaxException;

import org.apache.avalon.ConfigurationException;

import org.w3c.dom.DocumentFragment;
 
/** 
 * This class generates source code which represents a specific pattern matcher
 * for request URIs
 * 
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a> 
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2000-10-02 11:07:31 $ 
 */ 

public class RegexpURIMatcherFactory implements MatcherFactory {
    public String generateClassSource (String prefix, String pattern, 
                                       DocumentFragment conf) 
    throws ConfigurationException {
        StringBuffer sb = new StringBuffer ();
        try {
            RECompiler r = new RECompiler();
            String name         = prefix;
            String instructions = name + "PatternInstructions";
            String pat = correctPattern (pattern);
            sb.append("\n    // Pre-compiled regular expression '")
              .append(pat).append("'\n")
              .append("    static char[] ");
            sb.append(instructions).append(" = \n    {");
            REProgram program = r.compile(pat);
            int numColumns = 7;
            char[] p = program.getInstructions();
            for (int j = 0; j < p.length; j++) {
                if ((j % numColumns) == 0) {
                    sb.append("\n        ");
                }
                String hex = Integer.toHexString(p[j]);
                while (hex.length() < 4) {
                    hex = "0" + hex;
                }
                sb.append("0x").append(hex).append(", ");
            }
            sb.append("\n    };")
              .append("\n    static org.apache.regexp.RE ") 
              .append(name)
              .append("Pattern = new org.apache.regexp.RE(new org.apache.regexp.REProgram(")
              .append(instructions)
              .append("));");
            return sb.toString();
        } catch (RESyntaxException rse) {
            throw new ConfigurationException (rse.getMessage(), null);
        }
    }

    public String generateMethodSource (String prefix, String pattern, 
                                        DocumentFragment conf) 
    throws ConfigurationException {
        StringBuffer sb = new StringBuffer ();
        String name         = prefix;
        String instructions = name + "PatternInstructions";
        String pat = correctPattern (pattern);
        sb.append("java.util.ArrayList list = new java.util.ArrayList ();")
          .append("if(").append(name).append("Pattern.match(((javax.servlet.http.HttpServletRequest)objectModel.get(\"request\")).getRequestURI())) {");
        // Count number of parens
        int i = 0;
        int j = -1;
        while ((j = pat.indexOf('(', j+1)) != -1) {
            if (j == 0 || pat.charAt(j-1) != '\\') {
                sb.append("list.add (").append(name).append("Pattern.getParen(")
                  .append(++i).append("));");
            }
        }
        sb.append("return list; } else { return null; }");
        return sb.toString();
    }

    private String correctPattern (String pattern) {
        char[] pat = new char[pattern.length()];
        pattern.getChars (0, pattern.length(), pat, 0);
        int j = 0;
        int i = 0;
        while (i < pat.length-1) {
            if (pat[i] == '\\') {
                if (pat[i+1] == '\\') {
                    i++;
                }
            }
            pat[j++]=pat[i++];
        }
        pat[j]=pat[i];
        return new String(pat,0,j+1);
    }
}
