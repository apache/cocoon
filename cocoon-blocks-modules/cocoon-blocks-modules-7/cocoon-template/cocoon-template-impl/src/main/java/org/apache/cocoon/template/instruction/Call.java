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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.el.parsing.Subst;
import org.apache.cocoon.template.environment.ErrorHolder;
import org.apache.cocoon.template.environment.ExecutionContext;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.script.Invoker;
import org.apache.cocoon.template.script.event.AttributeEvent;
import org.apache.cocoon.template.script.event.Characters;
import org.apache.cocoon.template.script.event.Event;
import org.apache.cocoon.template.script.event.IgnorableWhitespace;
import org.apache.cocoon.template.script.event.StartElement;
import org.apache.cocoon.template.script.event.TextEvent;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.util.NamespacesTable;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version $Id$
 */
public class Call extends Instruction {

    private Object macro;
    private Subst targetNamespace;
    private Map parameters;
    private Event body;

    public Call(Define definition, StartElement startElement)
            throws SAXException {
        super(startElement);
        this.parameters = new HashMap();
        setBody(startElement);
        setNext(startElement.getNext());
        setDefinition(definition);

        Iterator i = startElement.getAttributeEvents().iterator();
        while (i.hasNext()) {
            AttributeEvent attrEvent = (AttributeEvent) i.next();
            addParameterInstance(attrEvent);
        }
    }

    public Call(ParsingContext parsingContext, StartElement raw, Attributes attrs, Stack stack)
            throws SAXException {
        super(raw);
        this.parameters = new HashMap();
        Locator locator = getLocation();

        String name = attrs.getValue("macro");
        if (name == null) {
            throw new SAXParseException("if: \"test\" is required", locator,
                    null);
        }
        this.macro = parsingContext.getStringTemplateParser().compileExpr(name, "call: \"macro\": ",
                locator);

        String namespace = StringUtils.defaultString(attrs
                .getValue("targetNamespace"));
        this.targetNamespace = parsingContext.getStringTemplateParser().compileExpr(namespace,
                "call: \"targetNamespace\": ", locator);
    }

    public void setDefinition(Define definition) {
        this.macro = definition;
    }

    public void addParameterInstance(AttributeEvent attributeEvent)
            throws SAXException {
        ParameterInstance parameter = new ParameterInstance(
                attributeEvent);
        this.parameters.put(parameter.getName(), parameter);
    }

    public Event execute(XMLConsumer consumer,
            ObjectModel objectModel,
            ExecutionContext executionContext, MacroContext macroContext,
            NamespacesTable namespaces, Event startEvent, Event endEvent) throws SAXException {
        Map attributeMap = new HashMap();
        Iterator i = parameters.keySet().iterator();
        while (i.hasNext()) {
            String parameterName = (String) i.next();
            ParameterInstance parameter = (ParameterInstance) parameters
                    .get(parameterName);
            Object parameterValue = parameter.getValue(objectModel);
            attributeMap.put(parameterName, parameterValue);
        }
        objectModel.markLocalContext();
        HashMap macro = new HashMap();
        macro.put("body", this.body);
        macro.put("arguments", attributeMap);
        objectModel.put("macro", macro);

        Define definition = resolveMacroDefinition(objectModel,
                executionContext);
        Iterator iter = definition.getParameters().entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry e = (Map.Entry) iter.next();
            String key = (String) e.getKey();
            Parameter startParam = (Parameter) e.getValue();
            Object default_ = startParam.getDefaultValue();
            Object val = attributeMap.get(key);
            if (val == null) {
                val = default_;
            }
            objectModel.put(key, val);
        }

        Event macroBodyStart = getNext();
        Event macroBodyEnd = null;

        if (getEndInstruction() != null)
            macroBodyEnd = getEndInstruction();
        else
            macroBodyEnd = getStartElement().getEndElement();
        
        MacroContext newMacroContext = new MacroContext(definition.getQname(),
                macroBodyStart, macroBodyEnd);
        try {
            Invoker.execute(consumer, objectModel, executionContext,
                    newMacroContext, namespaces,definition.getBody(), definition.getEndInstruction());
        } catch (SAXParseException exc) {
            throw new SAXParseException(newMacroContext.getMacroQName() + ": "
                    + exc.getMessage(), location, exc);
        }
        objectModel.cleanupLocalContext();

        if (getEndInstruction() != null)
            return getEndInstruction().getNext();
        else
            return getStartElement().getEndElement().getNext();
    }

    /**
     * @param executionContext
     * @throws SAXParseException
     */
    private Define resolveMacroDefinition(
            ObjectModel objectModel,
            ExecutionContext executionContext) throws SAXParseException {
        if (this.macro instanceof Define)
            return (Define) macro;

        Object macroName;
        Object namespace;
        Subst macroNameExpression = (Subst) macro;
        try {
            macroName = macroNameExpression.getValue(objectModel);
            namespace = targetNamespace.getValue(objectModel);
            if (namespace == null)
                namespace = "";
        } catch (Exception e) {
            throw new SAXParseException(e.getMessage(), getLocation(), e);
        } catch (Error err) {
            throw new SAXParseException(err.getMessage(), getLocation(),
                    new ErrorHolder(err));
        }
        Define definition = (Define) executionContext
                .getDefinitions()
                .get("{" + namespace.toString() + "}" + macroName.toString());
        if (definition == null)
            throw new SAXParseException("no macro definition: " + macroName,
                    getLocation());
        return definition;
    }

    /**
     * @param body
     */
    public void setBody(Event body) {
        this.body = body;

    }

    public void endNotify() throws SAXException {
        // FIXME: copy/pasted from StartDefine (almost)
        Event e = next;
        boolean params = true;
        while (e != this.getEndInstruction()) {
            if (e instanceof ParameterInstance) {
                ParameterInstance startParamInstance = (ParameterInstance) e;
                if (!params) {
                    throw new SAXParseException(
                            "<parameter value> not allowed here: \""
                                    + startParamInstance.name + "\"",
                            startParamInstance.getLocation(), null);
                }
                Object prev = this.parameters.put(startParamInstance.name,
                        startParamInstance);
                if (prev != null) {
                    throw new SAXParseException("duplicate parameter value: \""
                            + startParamInstance.name + "\"", location, null);
                }
                e = startParamInstance.getEndInstruction();
            } else if (e instanceof IgnorableWhitespace) {
                // EMPTY
            } else if (e instanceof Characters) {
                // check for whitespace
                char[] ch = ((TextEvent) e).getRaw();
                int len = ch.length;
                for (int i = 0; i < len; i++) {
                    if (!Character.isWhitespace(ch[i])) {
                        if (params) {
                            params = false;
                            this.body = e;
                        }
                        break;
                    }
                }
            } else {
                if (params) {
                    params = false;
                    this.body = e;
                }
            }
            e = e.getNext();
        }
        if (this.body == null) {
            this.body = this.getEndInstruction();
        }
        setNext(this.body);
    }
}
