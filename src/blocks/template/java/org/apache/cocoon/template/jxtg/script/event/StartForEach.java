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
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class StartForEach extends StartInstruction {

    final JXTExpression items;
    final JXTExpression var;
    final JXTExpression varStatus;
    final JXTExpression begin;
    final JXTExpression end;
    final JXTExpression step;
    final Boolean lenient;

    public StartForEach(StartElement raw, Attributes attrs, Stack stack)
        throws SAXException {

        super(raw);

        String name = raw.getLocalName();
        Locator locator = getLocation();

        String items = attrs.getValue("items");
        String select = attrs.getValue("select");
        this.var = Parser.compileExpr(attrs.getValue("var"), null, locator);
        this.varStatus = Parser.compileExpr(attrs.getValue("varStatus"), null, locator);
        this.begin = Parser.compileInt(attrs.getValue("begin"), name, locator);
        this.end = Parser.compileInt(attrs.getValue("end"), name, locator);
        this.step = Parser.compileInt(attrs.getValue("step"), name, locator);
        String lenientValue = attrs.getValue("lenient");
        this.lenient = (lenientValue == null) ? null : Boolean.valueOf(lenientValue);

        if (items == null) {
            if (select == null && (begin == null || end == null)) {
                throw new SAXParseException("forEach: \"select\", \"items\", or both \"begin\" and \"end\" must be specified",
                                            locator, null);
            }
        } else if (select != null) {
            throw new SAXParseException("forEach: only one of \"select\" or \"items\" may be specified",
                                        locator, null);
        }
        this.items = Parser.compileExpr(items == null ? select : items, null, locator);
    }

    public JXTExpression getBegin() {
        return begin;
    }

    public JXTExpression getEnd() {
        return end;
    }

    public JXTExpression getItems() {
        return items;
    }

    public Boolean getLenient() {
        return lenient;
    }

    public JXTExpression getStep() {
        return step;
    }

    public JXTExpression getVar() {
        return var;
    }

    public JXTExpression getVarStatus() {
        return varStatus;
    }
}
