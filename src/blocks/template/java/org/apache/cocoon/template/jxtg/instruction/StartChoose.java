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
import org.apache.cocoon.template.jxtg.script.Invoker;
import org.apache.cocoon.template.jxtg.script.event.Event;
import org.apache.cocoon.template.jxtg.script.event.StartElement;
import org.apache.cocoon.template.jxtg.script.event.StartInstruction;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class StartChoose extends StartInstruction {

    private StartWhen firstChoice;
    private StartOtherwise otherwise;

    public StartChoose(StartElement raw, Attributes attrs, Stack stack) {
        super(raw);
    }

    public Event execute(final XMLConsumer consumer,
                         ExpressionContext expressionContext, ExecutionContext executionContext,
                         StartElement macroCall, Event startEvent, Event endEvent) 
        throws SAXException {
        StartWhen startWhen = this.firstChoice;
        while (startWhen != null) {
            Object val;
            try {
                val = startWhen.getTest().getValue(expressionContext);
            } catch (Exception e) {
                throw new SAXParseException(e.getMessage(), getLocation(), e);
            }
            boolean result;
            if (val instanceof Boolean) {
                result = ((Boolean) val).booleanValue();
            } else {
                result = (val != null);
            }
            if (result) {
                Invoker.execute(consumer, expressionContext, executionContext,
                                macroCall, startWhen.getNext(),
                                startWhen.getEndInstruction());
                break;
            }
            startWhen = startWhen.getNextChoice();
        }
        if (startWhen == null && this.otherwise != null) {
            Invoker.execute(consumer, expressionContext, executionContext,
                            macroCall, this.otherwise.getNext(),
                            this.otherwise.getEndInstruction());
        }
        return getEndInstruction().getNext();
    }

    public void setFirstChoice(StartWhen firstChoice) {
        this.firstChoice = firstChoice;
    }

    public StartWhen getFirstChoice() {
        return firstChoice;
    }

    public void setOtherwise(StartOtherwise otherwise) {
        this.otherwise = otherwise;
    }

    public StartOtherwise getOtherwise() {
        return otherwise;
    }
}
