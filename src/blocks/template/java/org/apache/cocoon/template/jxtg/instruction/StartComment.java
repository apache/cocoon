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

import java.util.Properties;
import java.util.Stack;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.template.jxtg.environment.ExecutionContext;
import org.apache.cocoon.template.jxtg.script.Invoker;
import org.apache.cocoon.template.jxtg.script.event.Event;
import org.apache.cocoon.template.jxtg.script.event.StartElement;
import org.apache.cocoon.template.jxtg.script.event.StartInstruction;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class StartComment extends StartInstruction {
    public StartComment(StartElement raw, Attributes attrs, Stack stack) {
        // <jx:comment>This will be parsed</jx:comment>
        super(raw);
    }

    public Event execute(final XMLConsumer consumer,
                         ExpressionContext expressionContext, ExecutionContext executionContext,
                         StartElement macroCall, Event startEvent, Event endEvent) 
        throws SAXException {
        // Parse the body of the comment
        NodeList nodeList =
            Invoker.toDOMNodeList("comment", this,
                                  expressionContext, executionContext,
                                  macroCall);
        // JXPath doesn't handle NodeList, so convert it to an array
        int len = nodeList.getLength();
        final StringBuffer buf = new StringBuffer();
        Properties omit = XMLUtils.createPropertiesForXML(true);
        for (int i = 0; i < len; i++) {
            try {
                String str = XMLUtils.serializeNode(nodeList.item(i), omit);
                buf.append(StringUtils.substringAfter(str, ">")); // cut
                // the XML header
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
