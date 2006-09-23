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
package org.apache.cocoon.template.instruction;

import java.util.Stack;

import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.template.environment.ErrorHolder;
import org.apache.cocoon.template.environment.ExecutionContext;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.expression.JXTExpression;
import org.apache.cocoon.template.script.Invoker;
import org.apache.cocoon.template.script.event.Event;
import org.apache.cocoon.template.script.event.StartElement;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version SVN $Id$
 */
public class Eval extends Instruction {
    private final JXTExpression value;

    public Eval(ParsingContext parsingContext, StartElement raw, Attributes attrs, Stack stack)
        throws SAXException {

        super(raw);

        String select = attrs.getValue("select");
        this.value = parsingContext.getStringTemplateParser().compileExpr(select, "eval: \"select\":", getLocation());
    }

    public Event execute(final XMLConsumer consumer,
                         ExpressionContext expressionContext, ExecutionContext executionContext,
                         MacroContext macroContext, Event startEvent, Event endEvent) 
        throws SAXException {
        try {
            Object val = this.value.getNode(expressionContext);
            if (!(val instanceof StartElement)) {
                throw new Exception("macro invocation required instead of: " + val);
            }
            StartElement call = (StartElement) val;
            
            //FIXME: eval does not allow to call macro providing macro body
            MacroContext newMacroContext = new MacroContext( call.getQname(), null, null );
            Invoker.execute(consumer, expressionContext, executionContext,
                            newMacroContext, call.getNext(), call.getEndElement());
        } catch (Exception exc) {
            throw new SAXParseException(exc.getMessage(), getLocation(), exc);
        } catch (Error err) {
            throw new SAXParseException(err.getMessage(), getLocation(), new ErrorHolder(err));
        }
        return getEndInstruction().getNext();
    }
}
