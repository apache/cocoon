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
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class StartIf extends StartInstruction {
    final JXTExpression test;

    public StartIf(StartElement raw, Attributes attrs, Stack stack) 
        throws SAXException {

        super(raw);

        Locator locator = getLocation();
        String test = attrs.getValue("test");
        if (test != null) {
            this.test = JXTExpression.compileExpr(test, "if: \"test\": ", locator);
        } else {
            throw new SAXParseException("if: \"test\" is required", locator, null);
        }
    }

    public JXTExpression getTest() {
        return test;
    }
}
