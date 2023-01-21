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

import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.cocoon.el.parsing.Subst;
import org.apache.cocoon.template.environment.ExecutionContext;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.script.Invoker;
import org.apache.cocoon.template.script.event.Event;
import org.apache.cocoon.template.script.event.StartElement;
import org.apache.cocoon.template.xml.AttributeAwareXMLConsumer;
import org.apache.cocoon.xml.ContentHandlerWrapper;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.util.NamespacesTable;
import org.apache.commons.lang.StringUtils;
import org.apache.xml.serialize.TextSerializer;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @version $Id$
 */
public class Attribute extends Instruction {
    public static  final String XML_ATTR_NAME_BLANK = "parameter: \"name\" is required";
    public static final String XML_ATTR_NAME_INVALID = "parameter: \"name\" is an invalid XML attribute name";
    
    private Subst name;
    private Subst value;

    public Attribute(ParsingContext parsingContext, StartElement raw, Attributes attrs, Stack stack)
            throws SAXException {
        super(raw);
        this.name = getSubst("name", attrs, parsingContext, true);
        this.value = getSubst("value", attrs, parsingContext, false);
    }

    public Event execute(final XMLConsumer consumer, ObjectModel objectModel,
            ExecutionContext executionContext, MacroContext macroContext, NamespacesTable namespaces, Event startEvent, Event endEvent)
            throws SAXException {

        String nameStr = null;
        String valueStr = "";
        try {
            nameStr = this.name.getStringValue(objectModel);
          
            if (StringUtils.isBlank(nameStr))
                throw new SAXParseException(XML_ATTR_NAME_BLANK, getLocation());

            if (!nameStr.matches("[A-Za-z][^\\s:]*"))
                throw new SAXParseException(XML_ATTR_NAME_INVALID, getLocation());  
            
            if (this.value != null)
                valueStr = this.value.getStringValue(objectModel);
            else {
                TextSerializer serializer = new TextSerializer();
                StringWriter writer = new StringWriter();
                serializer.setOutputCharStream(writer);

                ContentHandlerWrapper contentHandler = new ContentHandlerWrapper(serializer, serializer);
                contentHandler.startDocument();

                Invoker.execute(contentHandler, objectModel, executionContext, macroContext, namespaces, this.getNext(), this
                        .getEndInstruction());
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
    
    private Subst getSubst(String attrName, Attributes attrs, ParsingContext parsingContext, boolean isRequired)
            throws SAXParseException {
        Locator locator = getLocation();
        String value = attrs.getValue(attrName);
        if (isRequired && value == null) {
            throw new SAXParseException("parameter: \"" + attrName + "\" is required", locator, null);
        } else if (!isRequired && value == null) {
            return null;
        }

        return parsingContext.getStringTemplateParser().compileExpr(value, "parameter: \"" + attrName + "\": ", locator);
    }     

}
