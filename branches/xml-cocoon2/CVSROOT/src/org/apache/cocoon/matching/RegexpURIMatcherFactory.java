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

import org.w3c.dom.DocumentFragment;
 
/** 
 * This class generates source code which represents a specific pattern matcher
 * for request URIs
 * 
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a> 
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-07-19 22:19:55 $ 
 */ 

public class RegexpURIMatcherFactory implements MatcherFactory {
    public String generateClassLevel (String prefix, String pattern, DocumentFragment conf) throws Exception {
        StringBuffer sb = new StringBuffer ();
        RECompiler r = new RECompiler();
        String name         = prefix+"_re";
        String instructions = name + "PatternInstructions";
        sb.append("\n    // Pre-compiled regular expression '")
          .append(pattern).append("'\n")
          .append("    static char[] ");
        sb.append(instructions).append(" = \n    {");
        REProgram program = r.compile(pattern);
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
    }

    public String generateMethodLevel (String prefix, String pattern, DocumentFragment conf) throws Exception {
        StringBuffer sb = new StringBuffer ();
        String name         = prefix+"_re";
        String instructions = name + "PatternInstructions";
        sb.append("java.util.Stack stack = new java.util.Stack ();")
          .append("if (").append(name).append("Pattern.match(request.getURI())) {");
        // Count number of parens
        int i = 0;
        int j = -1;
        while ((j = pattern.indexOf('(', j+1)) != -1) {
            if (j == 0 || pattern.charAt(j-1) != '\\') {
                sb.append("stack.push (").append(name).append("Pattern.getParen(")
                  .append(++i).append("));");
            }
        }
        sb.append("return (java.util.Map) stack; } else { return null; }");
        return sb.toString();
    }
}
