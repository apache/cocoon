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
package org.apache.cocoon.template.jxtg.script;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.template.jxtg.JXTemplateGenerator;
import org.apache.cocoon.template.jxtg.environment.ErrorHolder;
import org.apache.cocoon.template.jxtg.environment.ExecutionContext;
import org.apache.cocoon.template.jxtg.environment.LocatorFacade;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.cocoon.template.jxtg.instruction.StartDefine;
import org.apache.cocoon.template.jxtg.instruction.StartParameter;
import org.apache.cocoon.template.jxtg.script.event.AttributeEvent;
import org.apache.cocoon.template.jxtg.script.event.CopyAttribute;
import org.apache.cocoon.template.jxtg.script.event.Event;
import org.apache.cocoon.template.jxtg.script.event.StartElement;
import org.apache.cocoon.template.jxtg.script.event.StartInstruction;
import org.apache.cocoon.template.jxtg.script.event.SubstituteAttribute;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.excalibur.xml.sax.XMLizable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

public class Invoker {
    private static final Attributes EMPTY_ATTRS = new AttributesImpl();

    public static void execute(final XMLConsumer consumer,
            ExpressionContext expressionContext,
            ExecutionContext executionContext, StartElement macroCall,
            Event startEvent, Event endEvent) throws SAXException {

        Event ev = startEvent;
        LocatorFacade loc = new LocatorFacade(ev.getLocation());
        consumer.setDocumentLocator(loc);
        while (ev != endEvent) {
            loc.setDocumentLocator(ev.getLocation());

            // ContentHandler methods
            if (ev instanceof StartElement) {
                StartElement startElement = (StartElement) ev;
                StartDefine def = (StartDefine) executionContext
                        .getDefinitions().get(startElement.getQname());
                if (def == null) {
                    ev = ev.execute(consumer, expressionContext,
                            executionContext, macroCall, startEvent, endEvent);
                    continue;
                }

                // this is a macro call
                Map attributeMap = new HashMap();
                Iterator i = startElement.getAttributeEvents().iterator();
                while (i.hasNext()) {
                    String attributeName;
                    Object attributeValue;
                    AttributeEvent attrEvent = (AttributeEvent) i.next();
                    attributeName = attrEvent.getLocalName();
                    if (attrEvent instanceof CopyAttribute) {
                        CopyAttribute copy = (CopyAttribute) attrEvent;
                        attributeValue = copy.getValue();
                    } else if (attrEvent instanceof SubstituteAttribute) {
                        SubstituteAttribute substEvent = (SubstituteAttribute) attrEvent;
                        if (substEvent.getSubstitutions().size() == 1
                                && substEvent.getSubstitutions().get(0) instanceof JXTExpression) {
                            JXTExpression expr = (JXTExpression) substEvent
                                    .getSubstitutions().get(0);
                            Object val;
                            try {
                                val = expr.getNode(expressionContext);
                            } catch (Exception e) {
                                throw new SAXParseException(e.getMessage(), ev
                                        .getLocation(), e);
                            } catch (Error err) {
                                throw new SAXParseException(err.getMessage(),
                                        ev.getLocation(), new ErrorHolder(err));
                            }
                            attributeValue = val != null ? val : "";
                        } else {
                            attributeValue = substEvent.getSubstitutions()
                                    .toString(ev.getLocation(),
                                            expressionContext);
                        }
                    } else {
                        throw new Error("this shouldn't have happened");
                    }
                    attributeMap.put(attributeName, attributeValue);
                }
                ExpressionContext localExpressionContext = new ExpressionContext(
                        expressionContext);
                HashMap macro = new HashMap();
                macro.put("body", startElement);
                macro.put("arguments", attributeMap);
                localExpressionContext.put("macro", macro);
                Iterator iter = def.getParameters().entrySet().iterator();
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
                call(ev.getLocation(), startElement, consumer,
                        localExpressionContext, executionContext,
                        def.getBody(), def.getEndInstruction());
                ev = startElement.getEndElement().getNext();
            } else 
                ev = ev.execute(consumer, expressionContext, executionContext,
                        macroCall, startEvent, endEvent);
        }
    }

    public static void executeNode(final XMLConsumer consumer, Object val)
            throws SAXException {

        if (val instanceof Node) {
            executeDOM(consumer, (Node) val);
        } else if (val instanceof NodeList) {
            NodeList nodeList = (NodeList) val;
            int len = nodeList.getLength();
            for (int i = 0; i < len; i++) {
                Node n = nodeList.item(i);
                executeDOM(consumer, n);
            }
        } else if (val instanceof Node[]) {
            Node[] nodeList = (Node[]) val;
            int len = nodeList.length;
            for (int i = 0; i < len; i++) {
                Node n = nodeList[i];
                executeDOM(consumer, n);
            }
        } else if (val instanceof XMLizable) {
            ((XMLizable) val).toSAX(new IncludeXMLConsumer(consumer));
        } else {
            char[] ch = val == null ? ArrayUtils.EMPTY_CHAR_ARRAY : val
                    .toString().toCharArray();
            consumer.characters(ch, 0, ch.length);
        }
    }

    /**
     * dump a DOM document, using an IncludeXMLConsumer to filter out start/end document events
     */
    public static void executeDOM(final XMLConsumer consumer, Node node)
            throws SAXException {
        IncludeXMLConsumer includer = new IncludeXMLConsumer(consumer);
        DOMStreamer streamer = new DOMStreamer(includer);
        streamer.stream(node);
    }

    private static void call(Locator location, StartElement macroCall,
            final XMLConsumer consumer, ExpressionContext expressionContext,
            ExecutionContext executionContext, Event startEvent, Event endEvent)
            throws SAXException {
        try {
            execute(consumer, expressionContext, executionContext, macroCall,
                    startEvent, endEvent);
        } catch (SAXParseException exc) {
            throw new SAXParseException(macroCall.getLocalName() + ": "
                    + exc.getMessage(), location, exc);
        }
    }

    public static NodeList toDOMNodeList(String elementName,
            StartInstruction si, ExpressionContext expressionContext,
            ExecutionContext executionContext, StartElement macroCall)
            throws SAXException {
        DOMBuilder builder = new DOMBuilder();
        builder.startDocument();
        builder.startElement(JXTemplateGenerator.NS, elementName, elementName,
                EMPTY_ATTRS);
        execute(builder, expressionContext, executionContext, macroCall, si
                .getNext(), si.getEndInstruction());
        builder.endElement(JXTemplateGenerator.NS, elementName, elementName);
        builder.endDocument();
        Node node = builder.getDocument().getDocumentElement();
        return node.getChildNodes();
    }

}
