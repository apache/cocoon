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
package org.apache.cocoon.template.script.event;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.template.environment.ExecutionContext;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.expression.Substitutions;
import org.apache.cocoon.template.instruction.MacroContext;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @version SVN $Id$
 */
public class StartElement extends Event {
    public StartElement(ParsingContext parsingContext, Locator location, String namespaceURI,
            String localName, String raw, Attributes attrs) throws SAXException {
        super(location);
        this.namespaceURI = namespaceURI;
        this.localName = localName;
        this.raw = raw;
        this.qname = "{" + namespaceURI + "}" + localName;
        int len = attrs.getLength();
        for (int i = 0; i < len; i++) {
            String uri = attrs.getURI(i);
            String local = attrs.getLocalName(i);
            String qname = attrs.getQName(i);
            String type = attrs.getType(i);
            String value = attrs.getValue(i);
            Substitutions substitutions = new Substitutions(parsingContext, getLocation(), value);
            if (substitutions.hasSubstitutions()) {
                getAttributeEvents().add(
                        new SubstituteAttribute(uri, local, qname, type,
                                substitutions));
            } else {
                getAttributeEvents().add(
                        new CopyAttribute(uri, local, qname, type, value));
            }
        }
        this.attributes = new AttributesImpl(attrs);
    }

    final String namespaceURI;
    final String localName;
    final String raw;
    private final String qname;
    private final List attributeEvents = new LinkedList();
    final Attributes attributes;
    EndElement endElement;

    public EndElement getEndElement() {
        return endElement;
    }

    public String getLocalName() {
        return localName;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public String getRaw() {
        return raw;
    }

    public String getQname() {
        return qname;
    }

    public List getAttributeEvents() {
        return attributeEvents;
    }

    public void setEndElement(EndElement endElement) {
        this.endElement = endElement;

    }

    public Event execute(XMLConsumer consumer,
            ExpressionContext expressionContext,
            ExecutionContext executionContext, MacroContext macroContext,
            Event startEvent, Event endEvent) throws SAXException {
        Iterator i = getAttributeEvents().iterator();
        AttributesImpl attrs = new AttributesImpl();
        while (i.hasNext()) {
            AttributeEvent attrEvent = (AttributeEvent) i.next();
            if (attrEvent instanceof CopyAttribute) {
                CopyAttribute copy = (CopyAttribute) attrEvent;
                attrs.addAttribute(copy.getNamespaceURI(), copy.getLocalName(),
                        copy.getRaw(), copy.getType(), copy.getValue());
            } else if (attrEvent instanceof SubstituteAttribute) {
                SubstituteAttribute substEvent = (SubstituteAttribute) attrEvent;
                String attributeValue = substEvent.getSubstitutions().toString(
                        getLocation(), expressionContext);
                attrs.addAttribute(attrEvent.getNamespaceURI(), attrEvent
                        .getLocalName(), attrEvent.getRaw(), attrEvent
                        .getType(), attributeValue);
            }
        }
        
        // Send any pending startPrefixMapping events
        expressionContext.getNamespaces().enterScope(consumer);
        consumer.startElement(getNamespaceURI(), getLocalName(), getRaw(),
                attrs);
        return getNext();
    }
}
