/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.template.jxtg.script.event;

import java.util.Stack;

import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.cocoon.template.jxtg.script.Parser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class StartEval extends StartInstruction {
    private final JXTExpression value;

    public StartEval(StartElement raw, Attributes attrs, Stack stack)
        throws SAXException {

        super(raw);

        String select = attrs.getValue("select");
        this.value = Parser.compileExpr(select, "eval: \"select\":", getLocation());
    }

    public JXTExpression getValue() {
        return value;
    }
}
