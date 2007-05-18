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

import java.io.ByteArrayInputStream;
import java.util.Stack;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.template.environment.ExecutionContext;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.expression.JXTExpression;
import org.apache.cocoon.template.script.Invoker;
import org.apache.cocoon.template.script.event.Event;
import org.apache.cocoon.template.script.event.StartElement;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.commons.lang.BooleanUtils;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.excalibur.xml.sax.XMLizable;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version SVN $Id$
 */
public class Out extends Instruction {
    private final JXTExpression compiledExpression;
    private Boolean xmlize;
    private Boolean stripRoot;

    public Out(ParsingContext parsingContext, StartElement raw, Attributes attrs, Stack stack)
        throws SAXException {

        super(raw);
        Locator locator = getLocation();
        String value = attrs.getValue("value");
        if (value == null)
            throw new SAXParseException("out: \"value\" is required", locator, null);

        this.compiledExpression = parsingContext.getStringTemplateParser().compileExpr(value, "out: \"value\": ", locator);
        String lenientValue = attrs.getValue("lenient");
        Boolean lenient = lenientValue == null ? null : Boolean.valueOf(lenientValue);

        // Why can out be lenient?
        if (lenient != null)
            this.compiledExpression.setLenient(lenient);
        
        String xmlize = attrs.getValue("xmlize");
        this.xmlize = ( xmlize == null ) ? null : Boolean.valueOf( xmlize );
        
        String stripRoot = attrs.getValue("strip-root");
        this.stripRoot = ( stripRoot == null ) ? null : Boolean.valueOf( stripRoot );
    }

    public Event execute(final XMLConsumer consumer,
                         ExpressionContext expressionContext, ExecutionContext executionContext,
                         MacroContext macroContext, Event startEvent, Event endEvent) 
        throws SAXException {
        Object val;
        try {
            val = this.compiledExpression.getNode(expressionContext);
            
            boolean stripRoot = BooleanUtils.toBoolean(this.stripRoot);
            //TODO: LG, I do not see a good way to do this.
            if (BooleanUtils.isTrue(this.xmlize)) {
                if (val instanceof Node || val instanceof Node[] || val instanceof XMLizable)
                    Invoker.executeNode(consumer, val, stripRoot);
                else {
                    ServiceManager serviceManager = executionContext.getServiceManager();
                    SAXParser parser = null;
                    try {
                        parser = (SAXParser) serviceManager.lookup(SAXParser.ROLE);
                        InputSource source = new InputSource(new ByteArrayInputStream(val.toString().getBytes()));
                        IncludeXMLConsumer includeConsumer = new IncludeXMLConsumer(consumer);
                        includeConsumer.setIgnoreRootElement(stripRoot);
                        parser.parse(source, includeConsumer);
                    } finally {
                        serviceManager.release(parser);
                    }
                }
            } else
                Invoker.executeNode(consumer, val, stripRoot);
        } catch (Exception e) {
            throw new SAXParseException(e.getMessage(), getLocation(), e);
        }
        return getNext();
    }
}
