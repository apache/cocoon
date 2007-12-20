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
package org.apache.cocoon.template.expression;

import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

/**
 * @version  $Id$
 */
public class DefaultStringTemplateParser extends AbstractStringTemplateParser {

    /**
     * @see AbstractStringTemplateParser#parseSubstitutions(Reader)
     */
    protected List parseSubstitutions(Reader in) throws Exception {
        LinkedList substitutions = new LinkedList();
        StringBuffer buf = new StringBuffer();

        int ch;
        boolean inExpr = false;
        top: while ((ch = in.read()) != -1) {
            // column++;
            char c = (char) ch;
            processChar: while (true) {
                if (inExpr) {
                    if (c == '\\') {
                        ch = in.read();
                        buf.append(ch == -1 ? '\\' : (char) ch);
                    } else if (c == '}') {
                        String str = buf.toString();
                        substitutions.add(compile(str));
                        buf.setLength(0);
                        inExpr = false;
                    } else {
                        buf.append(c);
                    }
                } else if (c == '{') {
                    ch = in.read();
                    if (ch != '{') {
                        inExpr = true;
                        if (buf.length() > 0) {
                            substitutions.add(new Literal(buf.toString()));
                            buf.setLength(0);
                        }
                        buf.append((char) ch);
                        continue top;
                    }
                    buf.append(c);
                    if (ch != -1) {
                        c = (char) ch;
                        continue processChar;
                    }
                } else {
                    buf.append(c);
                }
                break;
            }
        }

        if (inExpr) {
            throw new Exception("Unterminated {");
        }

        if (buf.length() > 0) {
            substitutions.add(new Literal(buf.toString()));
        }

        return substitutions;
    }
}
