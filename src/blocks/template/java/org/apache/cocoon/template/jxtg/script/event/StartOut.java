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

import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.template.jxtg.environment.ExecutionContext;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.cocoon.template.jxtg.script.Invoker;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.excalibur.xml.sax.XMLizable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class StartOut extends StartInstruction {
    private final JXTExpression compiledExpression;

    public StartOut(StartElement raw, Attributes attrs, Stack stack)
        throws SAXException {

        super(raw);
        Locator locator = getLocation();
        String value = attrs.getValue("value");
        if (value != null) {
            this.compiledExpression =
                JXTExpression.compileExpr(value, "out: \"value\": ", locator);
            String lenientValue = attrs.getValue("lenient");
            Boolean lenient = lenientValue == null ? null : Boolean.valueOf(lenientValue);
            // Why can out be lenient?
            this.compiledExpression.setLenient(lenient);
        } else {
            throw new SAXParseException("out: \"value\" is required", locator, null);
        }
    }

    public Event execute(final XMLConsumer consumer,
                         ExpressionContext expressionContext, ExecutionContext executionContext,
                         StartElement macroCall, Event startEvent, Event endEvent) 
        throws SAXException {
        Object val;
        try {
            val = this.compiledExpression.getNode(expressionContext);
            if (val instanceof Node) {
                Invoker.executeDOM(consumer, (Node) val);
            } else if (val instanceof NodeList) {
                NodeList nodeList = (NodeList) val;
                int len = nodeList.getLength();
                for (int i = 0; i < len; i++) {
                    Node n = nodeList.item(i);
                    Invoker.executeDOM(consumer, n);
                }
            } else if (val instanceof Node[]) {
                Node[] nodeList = (Node[]) val;
                int len = nodeList.length;
                for (int i = 0; i < len; i++) {
                    Node n = nodeList[i];
                    Invoker.executeDOM(consumer, n);
                }
            } else if (val instanceof XMLizable) {
                ((XMLizable) val).toSAX(new IncludeXMLConsumer(consumer));
            } else {
                char[] ch =
                    val == null ? ArrayUtils.EMPTY_CHAR_ARRAY
                    : val.toString().toCharArray();
                consumer.characters(ch, 0, ch.length);
            }
        } catch (Exception e) {
            throw new SAXParseException(e.getMessage(), getLocation(), e);
        }
        return getNext();
    }
}
