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

import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.template.jxtg.JXTemplateGenerator;
import org.apache.cocoon.template.jxtg.environment.ExecutionContext;
import org.apache.cocoon.template.jxtg.environment.LocatorFacade;
import org.apache.cocoon.template.jxtg.instruction.MacroContext;
import org.apache.cocoon.template.jxtg.instruction.StartCall;
import org.apache.cocoon.template.jxtg.instruction.StartDefine;
import org.apache.cocoon.template.jxtg.script.event.Event;
import org.apache.cocoon.template.jxtg.script.event.StartElement;
import org.apache.cocoon.template.jxtg.script.event.StartInstruction;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.commons.lang.ArrayUtils;
import org.apache.excalibur.xml.sax.XMLizable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class Invoker {
    private static final Attributes EMPTY_ATTRS = new AttributesImpl();

    public static void execute(final XMLConsumer consumer,
            ExpressionContext expressionContext,
            ExecutionContext executionContext, MacroContext macroContext,
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
                            executionContext, macroContext, startEvent, endEvent);
                    continue;
                }

                StartCall call = new StartCall( def, startElement );
                ev = call.execute(consumer, expressionContext,
                        executionContext, macroContext, startEvent, endEvent);
            } else
                ev = ev.execute(consumer, expressionContext, executionContext,
                        macroContext, startEvent, endEvent);
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

    public static NodeList toDOMNodeList(String elementName,
            StartInstruction si, ExpressionContext expressionContext,
            ExecutionContext executionContext, MacroContext macroContext)
            throws SAXException {
        DOMBuilder builder = new DOMBuilder();
        builder.startDocument();
        builder.startElement(JXTemplateGenerator.NS, elementName, elementName,
                EMPTY_ATTRS);
        execute(builder, expressionContext, executionContext, macroContext, si
                .getNext(), si.getEndInstruction());
        builder.endElement(JXTemplateGenerator.NS, elementName, elementName);
        builder.endDocument();
        Node node = builder.getDocument().getDocumentElement();
        return node.getChildNodes();
    }

}
