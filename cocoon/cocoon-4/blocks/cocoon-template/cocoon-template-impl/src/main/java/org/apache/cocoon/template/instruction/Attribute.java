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

import java.io.StringWriter;
import java.util.Stack;

import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.template.JXTemplateGenerator;
import org.apache.cocoon.template.environment.ExecutionContext;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.expression.JXTExpression;
import org.apache.cocoon.template.script.Invoker;
import org.apache.cocoon.template.script.event.Event;
import org.apache.cocoon.template.script.event.StartElement;
import org.apache.cocoon.template.xml.AttributeAwareXMLConsumer;
import org.apache.cocoon.xml.ContentHandlerWrapper;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.xml.serialize.TextSerializer;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @version $Id$
 */
public class Attribute extends Instruction {

    private JXTExpression name;
    private JXTExpression value;

    public Attribute(ParsingContext parsingContext, StartElement raw, Attributes attrs, Stack stack)
            throws SAXException {
        super(raw);

        Locator locator = getLocation();
        String name = attrs.getValue("name");
        if (name == null) {
            throw new SAXParseException("parameter: \"name\" is required", locator, null);
        }
        this.name = parsingContext.getStringTemplateParser().compileExpr(name, "parameter: \"name\": ", locator);

        String value = attrs.getValue("value");

        this.value = parsingContext.getStringTemplateParser().compileExpr(value, "parameter: \"value\": ", locator);
    }

    public Event execute(final XMLConsumer consumer, ExpressionContext expressionContext,
            ExecutionContext executionContext, MacroContext macroContext, Event startEvent, Event endEvent)
            throws SAXException {

        String nameStr = null;
        String valueStr = "";
        try {
            nameStr = this.name.getStringValue(expressionContext);

            if (this.value != null)
                valueStr = this.value.getStringValue(expressionContext);
            else {
                final Attributes EMPTY_ATTRS = new AttributesImpl();
                String elementName = "attribute";

                TextSerializer serializer = new TextSerializer();
                StringWriter writer = new StringWriter();
                serializer.setOutputCharStream(writer);

                ContentHandlerWrapper contentHandler = new ContentHandlerWrapper(serializer, serializer);
                contentHandler.startDocument();

                // TODO is root element necessary for TextSerializer?
                contentHandler.startElement(JXTemplateGenerator.NS, elementName, elementName, EMPTY_ATTRS);
                Invoker.execute(contentHandler, expressionContext, executionContext, macroContext, this.getNext(), this
                        .getEndInstruction());
                contentHandler.endElement(JXTemplateGenerator.NS, elementName, elementName);
                contentHandler.endDocument();
                valueStr = writer.toString();
            }
        } catch (Exception exc) {
            throw new SAXParseException(exc.getMessage(), getLocation(), exc);
        }
        if (consumer instanceof AttributeAwareXMLConsumer) {
            AttributeAwareXMLConsumer c = (AttributeAwareXMLConsumer) consumer;
            c.attribute("", nameStr, nameStr, "CDATA", valueStr == null ? "" : valueStr);
        } else
            throw new SAXParseException("consumer is not attribute aware", getLocation());
        return getEndInstruction().getNext();
    }

}
