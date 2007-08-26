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

import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.template.environment.ExecutionContext;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.script.Invoker;
import org.apache.cocoon.template.script.event.Event;
import org.apache.cocoon.template.script.event.StartElement;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.util.NamespacesTable;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version SVN $Id$
 */
public class Choose extends Instruction {

    private When firstChoice;
    private Otherwise otherwise;

    public Choose(ParsingContext parsingContext, StartElement raw, Attributes attrs, Stack stack) {
        super(raw);
    }

    public Event execute(final XMLConsumer consumer,
                         ObjectModel objectModel, ExecutionContext executionContext,
                         MacroContext macroContext, NamespacesTable namespaces, Event startEvent, Event endEvent) 
        throws SAXException {
        When startWhen = this.firstChoice;
        while (startWhen != null) {
            Object val;
            try {
                val = startWhen.getTest().getValue(objectModel);
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
                Invoker.execute(consumer, objectModel, executionContext,
                                macroContext, namespaces, startWhen.getNext(), startWhen.getEndInstruction());
                break;
            }
            startWhen = startWhen.getNextChoice();
        }
        if (startWhen == null && this.otherwise != null) {
            Invoker.execute(consumer, objectModel, executionContext,
                            macroContext, namespaces, this.otherwise.getNext(),
                            this.otherwise.getEndInstruction());
        }
        return getEndInstruction().getNext();
    }

    public void setFirstChoice(When firstChoice) {
        this.firstChoice = firstChoice;
    }

    public When getFirstChoice() {
        return firstChoice;
    }

    public void setOtherwise(Otherwise otherwise) {
        this.otherwise = otherwise;
    }

    public Otherwise getOtherwise() {
        return otherwise;
    }
}
