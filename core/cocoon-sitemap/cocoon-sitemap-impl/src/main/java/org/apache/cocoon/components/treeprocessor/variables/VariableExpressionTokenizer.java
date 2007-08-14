/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.treeprocessor.variables;

import org.apache.cocoon.sitemap.PatternException;

/**
 * Parses "Text {module:{module:attribute}} more text {variable}" types of
 * expressions. Supports escaping of braces with '\' character, and nested
 * expressions.
 *
 * @version $Id$
 */
public final class VariableExpressionTokenizer {

    /**
     * Callback for tokenizer
     */
    public interface TokenReciever {
        int OPEN = -2;
        int CLOSE = -3;
        int COLON = -4;
        int TEXT = -5;
        int MODULE = -6;
        int VARIABLE = -8;
        int NEW_EXPRESSION = -12;

        /**
         * Reports parsed tokens.
         */
        void addToken(int type, String value) throws PatternException;
    }

    /**
     * Tokenizes specified expression. Passes tokens to the
     * reciever.
     *
     * @throws PatternException if expression is not valid
     */
    public static void tokenize(String expression, TokenReciever reciever) throws PatternException {

        int lastTokenType = 0;

        int openCount = 0;
        int closeCount = 0;

        int pos = 0;
        int i;
        boolean escape = false;

        for (i = 0; i < expression.length(); i++) {
            final char c = expression.charAt(i);

            if (escape) {
                escape = false;
            } else if (c == '\\' && i < expression.length()) {
                char nextChar = expression.charAt(i + 1);
                if (nextChar == '{' || nextChar == '}' || nextChar == '$') {
                    expression = expression.substring(0, i) + expression.substring(i + 1);
                    escape = true;
                    i--;
                }
            } else if (c == '$') {
                if (expression.charAt(i+1) != '{')
                    //it's not an expression like ${cocoon.request}, skipping
                    continue;
                
                if (i > pos) {
                    reciever.addToken(lastTokenType = TokenReciever.TEXT, expression.substring(pos, i));
                }
                
                i++;
                openCount++;
                reciever.addToken(lastTokenType = TokenReciever.OPEN, null);
                
                int closePos = indexOf(expression, '}', i);

                //expression conforming cocoon-expression-language
                String newExpression = expression.substring(i+1, closePos);
                reciever.addToken(lastTokenType = TokenReciever.NEW_EXPRESSION, newExpression);
                i = closePos - 1;
                
            } else if (c == '{') {
                if (i > pos) {
                    reciever.addToken(lastTokenType = TokenReciever.TEXT, expression.substring(pos, i));
                }

                openCount++;
                reciever.addToken(lastTokenType = TokenReciever.OPEN, null);

                int colonPos = indexOf(expression, ':', i);
                int closePos = indexOf(expression, '}', i);
                int openPos = indexOf(expression, '{', i);

                if (openPos < colonPos && openPos < closePos) {
                    throw new PatternException("Invalid '{' at position " + i +
                                               " in expression \"" + expression + "\"");
                }

                if (colonPos < closePos) {
                    // we've found a module
                    String module = expression.substring(i + 1, colonPos);
                    reciever.addToken(lastTokenType = TokenReciever.MODULE, module);
                    i = colonPos - 1;
                } else {
                    // Unprefixed name: variable
                    reciever.addToken(lastTokenType = TokenReciever.VARIABLE, expression.substring(i + 1, closePos));
                    i = closePos - 1;
                }

                pos = i + 1;
            } else if (c == '}') {
                if (i > 0 && expression.charAt(i - 1) == '\\') {
                    continue;
                }
                if (i > pos) {
                    reciever.addToken(lastTokenType = TokenReciever.TEXT, expression.substring(pos, i));
                }

                closeCount++;
                reciever.addToken(lastTokenType = TokenReciever.CLOSE, null);

                pos = i + 1;
            } else if (c == ':') {
                if (lastTokenType != TokenReciever.MODULE || i != pos) {
                    // this colon isn't part of a module reference
                    continue;
                }

                reciever.addToken(lastTokenType = TokenReciever.COLON, null);
                pos = i + 1;
            }
        }

        if (i > pos) {
            reciever.addToken(lastTokenType = TokenReciever.TEXT, expression.substring(pos, i));
        }

        if (openCount != closeCount) {
            throw new PatternException("Mismatching braces in expression \"" + expression + "\"");
        }
    }

    private static int indexOf(String expression, char chr, int pos) {
        int location;
        return (location = expression.indexOf(chr, pos + 1)) != -1? location : expression.length();
    }
}
