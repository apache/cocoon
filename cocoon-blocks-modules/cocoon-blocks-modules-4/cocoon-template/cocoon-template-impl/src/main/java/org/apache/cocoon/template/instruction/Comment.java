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

import java.util.Properties;
import java.util.Stack;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.template.environment.ExecutionContext;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.script.Invoker;
import org.apache.cocoon.template.script.event.Event;
import org.apache.cocoon.template.script.event.StartElement;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version SVN $Id$
 */
public class Comment extends Instruction {
    public Comment(ParsingContext parsingContext, StartElement raw, Attributes attrs, Stack stack) {
        // <jx:comment>This will be parsed</jx:comment>
        super(raw);
    }

    public Event execute(final XMLConsumer consumer,
                         ExpressionContext expressionContext, ExecutionContext executionContext,
                         MacroContext macroContext, Event startEvent, Event endEvent) 
        throws SAXException {
        // Parse the body of the comment
        NodeList nodeList =
            Invoker.toDOMNodeList("comment", this,
                                  expressionContext, executionContext,
                                  macroContext);
        // JXPath doesn't handle NodeList, so convert it to an array
        int len = nodeList.getLength();
        final StringBuffer buf = new StringBuffer();
        Properties omit = XMLUtils.createPropertiesForXML(true);
        for (int i = 0; i < len; i++) {
            try {
                buf.append(XMLUtils.serializeNode(nodeList.item(i), omit));
            } catch (ProcessingException e) {
                throw new SAXParseException(e.getMessage(), getLocation(), e);
            }
        }
        char[] chars = new char[buf.length()];
        buf.getChars(0, chars.length, chars, 0);
        consumer.comment(chars, 0, chars.length);
        return getEndInstruction().getNext();
    }
}
