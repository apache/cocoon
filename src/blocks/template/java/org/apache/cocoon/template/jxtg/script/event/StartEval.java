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
import org.apache.cocoon.template.jxtg.environment.ErrorHolder;
import org.apache.cocoon.template.jxtg.environment.ExecutionContext;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.cocoon.template.jxtg.script.Invoker;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class StartEval extends StartInstruction {
    private final JXTExpression value;

    public StartEval(StartElement raw, Attributes attrs, Stack stack)
        throws SAXException {

        super(raw);

        String select = attrs.getValue("select");
        this.value = JXTExpression.compileExpr(select, "eval: \"select\":", getLocation());
    }

    public Event execute(final XMLConsumer consumer,
                         ExpressionContext expressionContext, ExecutionContext executionContext,
                         StartElement macroCall, Event startEvent, Event endEvent) 
        throws SAXException {
        try {
            Object val = this.value.getNode(expressionContext);
            if (!(val instanceof StartElement)) {
                throw new Exception("macro invocation required instead of: " + val);
            }
            StartElement call = (StartElement) val;
            Invoker.execute(consumer, expressionContext, executionContext,
                            call, call.getNext(), call.getEndElement());
        } catch (Exception exc) {
            throw new SAXParseException(exc.getMessage(), getLocation(), exc);
        } catch (Error err) {
            throw new SAXParseException(err.getMessage(), getLocation(), new ErrorHolder(err));
        }
        return getEndInstruction().getNext();
    }
}
