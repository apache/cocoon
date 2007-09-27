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
import org.apache.cocoon.el.parsing.Subst;
import org.apache.cocoon.template.environment.ExecutionContext;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.script.Invoker;
import org.apache.cocoon.template.script.event.Event;
import org.apache.cocoon.template.script.event.StartElement;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.util.NamespacesTable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version SVN $Id$
 */
public class Set extends Instruction {

    private final Subst var;
    private final Subst value;

    public Set(ParsingContext parsingContext, StartElement raw, Attributes attrs, Stack stack)
        throws SAXException {

        super(raw);

        Locator locator = getLocation();
        String var = attrs.getValue("var");
        String value = attrs.getValue("value");
        Subst varExpr = null;
        Subst valueExpr = null;
        if (var != null) {
            varExpr = parsingContext.getStringTemplateParser().compileExpr(var, "set: \"var\":", locator);
        }
        if (value != null) {
            valueExpr = parsingContext.getStringTemplateParser().compileExpr(value, "set: \"value\":", locator);
        }
        this.var = varExpr;
        this.value = valueExpr;
    }

    public Event execute(final XMLConsumer consumer,
                         ObjectModel objectModel, ExecutionContext executionContext,
                         MacroContext macroContext, NamespacesTable namespaces, Event startEvent, Event endEvent) 
        throws SAXException {

        Object value = null;
        String var = null;
        try {
            if (this.var != null) {
                var = this.var.getStringValue(objectModel);
            }
            if (this.value != null) {
                value = this.value.getNode(objectModel);
            }
        } catch (Exception exc) {
            throw new SAXParseException(exc.getMessage(), getLocation(), exc);
        }
        if (this.value == null) {
            NodeList nodeList =
                Invoker.toDOMNodeList("set", this, objectModel, executionContext, macroContext, namespaces);
            // JXPath doesn't handle NodeList, so convert it to an array
            int len = nodeList.getLength();
            Node[] nodeArr = new Node[len];
            for (int i = 0; i < len; i++) {
                nodeArr[i] = nodeList.item(i);
            }
            value = nodeArr;
        }
        if (var != null) {
            objectModel.put(var, value);
        }
        return getEndInstruction().getNext();
    }
}
