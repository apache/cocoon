/*
 * Copyright 1999-2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
 * @version SVN $Id: JXTGStringTemplateParser.java 325986 2005-10-17 21:35:18Z
 *          lgawron $
 */
public class JXTGStringTemplateParser extends AbstractStringTemplateParser {
    public final static String JXPATH = "jxpath";
    public final static String JEXL = "jexl";
    public final static String JAVASCRIPT = "js";

    public List parseSubstitutions(Reader in) throws Exception {
        LinkedList substitutions = new LinkedList();
        StringBuffer buf = new StringBuffer();
        buf.setLength(0);
        int ch;
        boolean inExpr = false;
        String lang = null;
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
                        substitutions.add(compile(str, lang));
                        buf.setLength(0);
                        inExpr = false;
                    } else {
                        buf.append(c);
                    }
                } else if (c == '$' || c == '#' || c == '@') {
                    ch = in.read();
                    if (ch == '{') {

                        lang = (c == '#') ? JXPATH : ((c == '$') ? JEXL : JAVASCRIPT);
                        inExpr = true;
                        if (buf.length() > 0) {
                            substitutions.add(new Literal(buf.toString()));
                            buf.setLength(0);
                        }
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
        if (inExpr)
            throw new Exception("Unterminated {");

        if (buf.length() > 0)
            substitutions.add(new Literal(buf.toString()));
        return substitutions;
    }

}
