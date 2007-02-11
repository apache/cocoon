/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components.treeprocessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.sitemap.PatternException;

/**
 * Utility class for handling {...} pattern substitutions from a List of Maps.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: MapStackResolver.java,v 1.2 2004/03/01 03:50:58 antonio Exp $
 * @deprecated use {@link org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory}
 */

public abstract class MapStackResolver {

    public static final Map EMPTY_MAP = Collections.unmodifiableMap(new java.util.HashMap(0));

    /**
     * Resolve all {...} patterns using the values given in the list of maps.
     */
    public abstract String resolve(List mapStack) throws PatternException;

    /**
     * Does an expression need resolving (i.e. contain {...} patterns) ?
     */
    public static boolean needsResolve(String expression) {
        if (expression == null || expression.length() == 0) {
            return false;
        }

        // Is the first char a '{' ?
        if (expression.charAt(0) == '{') {
            return true;
        }

        if (expression.length() < 2) {
            return false;
        }

        // Is there any unescaped '{' ?
        int pos = 1;
        while ( (pos = expression.indexOf('{', pos)) != -1) {
            // Found a '{' : is it escaped ?
            if (expression.charAt(pos - 1) != '\\') {
                // No : need to resolve
                return true;
            }
            pos++;
        }
        // Nothing found...
        return false;
    }

    /**
     * Unescape an expression that doesn't need to be resolved, but may contain
     * escaped '{' characters.
     *
     * @param expression the expression to unescape.
     * @return the unescaped result, or <code>expression</code> if unescaping isn't necessary.
     */
    public static String unescape(String expression) {
        // Does it need escaping ?
        if (expression == null || expression.indexOf("\\{") == -1) {
            return expression;
        }

        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);
            if (ch != '\\' || i >= (expression.length() - 1) || expression.charAt(i+1) != '{') {
                buf.append(ch);
            }
        }

        return buf.toString();
    }

    /**
     * Get a resolver for a given expression. Chooses the most efficient implementation
     * depending on <code>expression</code>.
     */
    public static MapStackResolver getResolver(String expression) throws PatternException {
        if (needsResolve(expression)) {
//            return new RealResolver(expression);
            return new CompiledResolver(expression);
        } else {
            return new NullResolver(expression);
        }
    }

    /**
     * Build a <code>Parameters</code> object from a Map of named <code>ListOfMapResolver</code>s and
     * a list of Maps used for resolution.
     *
     * @return a fully resolved <code>Parameters</code>.
     */
    public static Parameters buildParameters(Map expressions, List mapStack) throws PatternException {
        if (expressions == null || expressions.size() == 0) {
            return Parameters.EMPTY_PARAMETERS;
        }

        Parameters result = new Parameters();

        Iterator iter = expressions.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            result.setParameter(
                ((MapStackResolver)entry.getKey()).resolve(mapStack),
                ((MapStackResolver)entry.getValue()).resolve(mapStack)
            );
        }

        return result;
    }

    /**
     * Resolve all values of a <code>Map</code> from a Map of named <code>ListOfMapResolver</code>s and
     * a list of Maps used for resolution.
     *
     * @return a fully resolved <code>Map</code>.
     */
    public static Map resolveMap(Map expressions, List mapStack) throws PatternException {
        int size;
        if (expressions == null || (size = expressions.size()) == 0) {
            return EMPTY_MAP;
        }

        Map result = new HashMap(size);

        Iterator iter = expressions.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            result.put(
                ((MapStackResolver)entry.getKey()).resolve(mapStack),
                ((MapStackResolver)entry.getValue()).resolve(mapStack)
            );
        }

        return result;
    }

    //-------------------------------------------------------------------------
    /**
     * No-op resolver for expressions that don't need to be resolved.
     */
    private static class NullResolver extends MapStackResolver {
        private String originalExpr = null;
        private String expression = null;

        public NullResolver(String expression) {
            if (expression != null) {
                this.originalExpr = expression;
                this.expression = unescape(expression);
            }
        }

        public String toString() {
            return this.originalExpr;
        }

        public final String resolve(List mapStack) {
            return this.expression;
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Compiled form for faster substitution
     */
    private static class CompiledResolver extends MapStackResolver {
        private String originalExpr;

        private String[] strings;
        private int[] levels;

        public CompiledResolver(String expression) throws PatternException {
            this.originalExpr = expression;
            compile(expression);
        }

        public String toString() {
            return this.originalExpr;
        }

        private void compile(String expr) throws PatternException {
            // We're sure here that expr *contains* some substitutions

            List stringList = new ArrayList();
            List levelList  = new ArrayList();

            int length = expr.length();
            int prev = 0; // position after last closing brace

            comp : while(prev < length) {
                // find next unescaped '{'
                int pos = prev;
                while(pos < length &&
                      (pos = expr.indexOf('{', pos)) != -1 &&
                      (pos != 0 && expr.charAt(pos - 1) == '\\')) {
                    pos++;
                }

                if (pos >= length || pos == -1) {
                    // no more braces
                    if (prev < length) {
                        stringList.add(unescape(expr.substring(prev)));
                        levelList.add(new Integer(-1));
                    }
                    break comp;
                }

                // Pass closing brace
                pos++;

                // Add litteral strings between closing and next opening brace
                if (prev < pos-1) {
                    stringList.add(unescape(expr.substring(prev, pos - 1)));
                    levelList.add(new Integer(-1));
                }

                // Determine subst level
                int level = 1; // Start at 1 since it will be substracted from list.size()
                while(expr.startsWith("../", pos)) {
                    level++;
                    pos += "../".length();
                }

                int end = expr.indexOf('}', pos);
                if (end == -1) {
                    throw new PatternException("Unmatched '{' in " + expr);
                }

                stringList.add(expr.substring(pos, end));
                levelList.add(new Integer(level));

                prev = end + 1;
            }

            this.strings = new String[stringList.size()];
            this.levels = new int[stringList.size()];
            for (int i = 0; i < strings.length; i++) {
                this.strings[i] = (String)stringList.get(i);
                this.levels[i] = ((Integer)levelList.get(i)).intValue();
            }
        }

        public final String resolve(List mapStack) throws PatternException {
            StringBuffer result = new StringBuffer();
            int stackSize = mapStack.size();

            for (int i = 0; i < this.strings.length; i++) {
                int level = this.levels[i];
                if (level == -1) {
                    result.append(this.strings[i]);

                } else {
                    if (level > stackSize) {
                        throw new PatternException("Error while evaluating '" + this.originalExpr +
                            "' : not so many levels");
                    }

                    Object value = ((Map)mapStack.get(stackSize - level)).get(this.strings[i]);
                    if (value != null) {
                        result.append(value);
                    }
                }
            }

            return result.toString();
        }

//        public void dump() {
//            System.out.println(this.originalExpr + " compiled in :");
//            for (int i = 0; i < this.strings.length; i++) {
//                System.out.print("[" + this.levels[i] + ":'" + this.strings[i] + "'] ");
//            }
//            System.out.println();
//            System.out.println();
//        }
    }

//    public static void main(String [] args) throws Exception {
//
//        new CompiledResolver("&{../../blah}").dump();
//        new CompiledResolver("{t1}tt{t2}x").dump();
//        new CompiledResolver("\\{t1}tt{t2}xx").dump();
//        new CompiledResolver("{t1}tt\\{t2}xx").dump();
//        new CompiledResolver("{t1}tt{t2}xx").dump();
//        new CompiledResolver("xx{../t1}{../../../t2}zz").dump();
//        new CompiledResolver("xx{../t1}\\{../../../t2}zz").dump();
//
//    }
}
