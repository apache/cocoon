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
package org.apache.cocoon.template.jxtg.instruction;

import java.util.Stack;

import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.template.jxtg.environment.ExecutionContext;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.cocoon.template.jxtg.script.event.Event;
import org.apache.cocoon.template.jxtg.script.event.StartElement;
import org.apache.cocoon.template.jxtg.script.event.StartInstruction;
import org.apache.cocoon.template.jxtg.script.Invoker;
import org.apache.cocoon.xml.XMLConsumer;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class StartSet extends StartInstruction {

    private final JXTExpression var;
    private final JXTExpression value;

    public StartSet(StartElement raw, Attributes attrs, Stack stack)
        throws SAXException {

        super(raw);

        Locator locator = getLocation();
        String var = attrs.getValue("var");
        String value = attrs.getValue("value");
        JXTExpression varExpr = null;
        JXTExpression valueExpr = null;
        if (var != null) {
            varExpr = JXTExpression.compileExpr(var, "set: \"var\":", locator);
        }
        if (value != null) {
            valueExpr = JXTExpression.compileExpr(value, "set: \"value\":", locator);
        }
        this.var = varExpr;
        this.value = valueExpr;
    }

    public Event execute(final XMLConsumer consumer,
                         ExpressionContext expressionContext, ExecutionContext executionContext,
                         StartElement macroCall, Event startEvent, Event endEvent) 
        throws SAXException {

        Object value = null;
        String var = null;
        try {
            if (this.var != null) {
                var = this.var.getStringValue(expressionContext);
            }
            if (this.value != null) {
                value = this.value.getNode(expressionContext);
            }
        } catch (Exception exc) {
            throw new SAXParseException(exc.getMessage(), getLocation(), exc);
        }
        if (value == null) {
            NodeList nodeList =
                Invoker.toDOMNodeList("set", this,
                                      expressionContext, executionContext,
                                      macroCall);
            // JXPath doesn't handle NodeList, so convert it to an array
            int len = nodeList.getLength();
            Node[] nodeArr = new Node[len];
            for (int i = 0; i < len; i++) {
                nodeArr[i] = nodeList.item(i);
            }
            value = nodeArr;
        }
        if (var != null) {
            expressionContext.put(var, value);
        }
        return getEndInstruction().getNext();
    }
}
