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

import org.w3c.dom.traversal.NodeIterator;

/**
 * This class generates source code which represents a specific pattern matcher
 * for request URIs
 *
 * @author <a href="mailto:Giacomo.Pati@pwr.ch">Giacomo Pati</a>
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-11-30 21:41:46 $
 */

public class RegexpTargetHostMatcherFactory implements MatcherFactory {
    public String generateParameterSource (NodeIterator conf)
    throws ConfigurationException {
        return "RE";
    }

    public String generateClassSource (String prefix, String pattern,
                                       NodeIterator conf)
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
              .append("\n    static RE ")
              .append(name)
              .append("_expr = new RE(new REProgram(")
              .append(instructions)
              .append("));");
            return sb.toString();
        } catch (RESyntaxException rse) {
            throw new ConfigurationException (rse.getMessage(), rse);
        }
    }

    public String generateMethodSource (NodeIterator conf)
    throws ConfigurationException {
        StringBuffer sb = new StringBuffer ();
        sb.append("ArrayList list = new ArrayList ();")
          .append("String uri = ((HttpServletRequest)objectModel.get(Cocoon.REQUEST_OBJECT)).getHeader(\"Host\");")
          .append("if(uri.startsWith(\"/\")) uri = uri.substring(1);")
          .append("if(pattern.match(uri)) {");
        /* Handle parenthesised subexpressions. XXX: could be faster if we count
         * parens *outside* the generated code.
         * Note: *ONE* based, not zero.
         */
        sb.append("int parenCount = pattern.getParenCount();")
          .append("for (int paren = 1; paren <= parenCount; paren++) {")
          .append("list.add(pattern.getParen(paren));")
          .append("}");
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
