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
import org.apache.cocoon.template.jxtg.script.Invoker;
import org.apache.cocoon.template.jxtg.script.event.Event;
import org.apache.cocoon.template.jxtg.script.event.StartElement;
import org.apache.cocoon.template.jxtg.script.event.StartInstruction;
import org.apache.cocoon.xml.XMLConsumer;
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
            Invoker.executeNode(consumer, val);
        } catch (Exception e) {
            throw new SAXParseException(e.getMessage(), getLocation(), e);
        }
        return getNext();
    }
}
