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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.template.jxtg.environment.ExecutionContext;
import org.apache.cocoon.template.jxtg.script.Invoker;
import org.apache.cocoon.template.jxtg.script.event.AttributeEvent;
import org.apache.cocoon.template.jxtg.script.event.Event;
import org.apache.cocoon.template.jxtg.script.event.StartElement;
import org.apache.cocoon.template.jxtg.script.event.StartInstruction;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class StartCall extends StartInstruction {
//    private JXTExpression macroName;
    private Map parameters;
    private StartElement body;
    private StartDefine definition;

    public StartCall(StartDefine definition, StartElement body)
            throws SAXException {
        super(body.getLocation());
        this.parameters = new HashMap();
        setBody(body);
        setDefinition(definition);

        Iterator i = this.body.getAttributeEvents().iterator();
        while (i.hasNext()) {
            AttributeEvent attrEvent = (AttributeEvent) i.next();
            addParameterInstance(attrEvent);
        }
    }

    public StartCall(StartElement raw, Attributes attrs, Stack stack)
            throws SAXException {
        super(raw);
//        Locator locator = getLocation();
//        String name = attrs.getValue("macro");
//        if (name == null) {
//            throw new SAXParseException("if: \"test\" is required", locator,
//                    null);
//        }
//        this.macroName = JXTExpression.compileExpr(name, "call: \"macro\": ",
//                locator);
        this.parameters = new HashMap();
    }

    public void setDefinition(StartDefine definition) {
        this.definition = definition;
        setEndInstruction(definition.getEndInstruction());
    }

    public void addParameterInstance(AttributeEvent attributeEvent)
            throws SAXException {
        StartParameterInstance parameter = new StartParameterInstance(
                attributeEvent);
        this.parameters.put(parameter.getName(), parameter);
    }

    public Event execute(XMLConsumer consumer,
            ExpressionContext expressionContext,
            ExecutionContext executionContext, StartElement macroCall,
            Event startEvent, Event endEvent) throws SAXException {
        Map attributeMap = new HashMap();
        Iterator i = parameters.keySet().iterator();
        while (i.hasNext()) {
            String parameterName = (String) i.next();
            StartParameterInstance parameter = (StartParameterInstance) parameters
                    .get(parameterName);
            Object parameterValue = parameter.getValue(expressionContext);
            attributeMap.put(parameterName, parameterValue);
        }
        ExpressionContext localExpressionContext = new ExpressionContext(
                expressionContext);
        HashMap macro = new HashMap();
        macro.put("body", this.body);
        macro.put("arguments", attributeMap);
        localExpressionContext.put("macro", macro);

        Iterator iter = this.definition.getParameters().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry e = (Map.Entry) iter.next();
            String key = (String) e.getKey();
            StartParameter startParam = (StartParameter) e.getValue();
            Object default_ = startParam.getDefaultValue();
            Object val = attributeMap.get(key);
            if (val == null) {
                val = default_;
            }
            localExpressionContext.put(key, val);
        }
        Invoker.call(getLocation(), this.body, consumer,
                localExpressionContext, executionContext, definition.getBody(),
                definition.getEndInstruction());
        // ev = startElement.getEndElement().getNext();
        return getNext();
    }

    /**
     * @param startElement
     */
    public void setBody(StartElement startElement) {
        this.body = startElement;
        setNext(startElement.getEndElement().getNext());
    }
}
