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
import org.apache.cocoon.template.jxtg.expression.Literal;
import org.apache.cocoon.template.jxtg.expression.Subst;
import org.apache.cocoon.template.jxtg.instruction.StartDefine;
import org.apache.cocoon.template.jxtg.instruction.StartParameter;
import org.apache.cocoon.template.jxtg.script.event.*;
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
                               ExecutionContext executionContext,
                               StartElement macroCall,
                               Event startEvent, Event endEvent)
            throws SAXException {

        Event ev = startEvent;
        LocatorFacade loc = new LocatorFacade(ev.getLocation());
        consumer.setDocumentLocator(loc);
        while (ev != endEvent) {
            loc.setDocumentLocator(ev.getLocation());

            // ContentHandler methods
            if (ev instanceof Characters) {
                TextEvent text = (TextEvent) ev;
                Iterator iter = text.getSubstitutions().iterator();
                while (iter.hasNext()) {
                    Subst subst = (Subst)iter.next();
                    char[] chars;
                    if (subst instanceof Literal) {
                        chars = ((Literal)subst).getCharArray();
			consumer.characters(chars, 0, chars.length);
                    } else {
                        JXTExpression expr = (JXTExpression) subst;
                        try {
                            Object val = expr.getNode(expressionContext);
			    executeNode(consumer, val);
                        } catch (Exception e) {
                            throw new SAXParseException(e.getMessage(),
                                                        ev.getLocation(), e);
                        } catch (Error err) {
                            throw new SAXParseException(err.getMessage(),
                                                        ev.getLocation(),
                                                        new ErrorHolder(err));
                        }
                    }
                }
            } else if (ev instanceof EndDocument) {
                consumer.endDocument();
            } else if (ev instanceof EndElement) {
                EndElement endElement = (EndElement) ev;
                StartElement startElement = endElement.getStartElement();
                consumer.endElement(startElement.getNamespaceURI(),
                        startElement.getLocalName(), startElement.getRaw());
            } else if (ev instanceof EndPrefixMapping) {
                EndPrefixMapping endPrefixMapping = (EndPrefixMapping) ev;
                consumer.endPrefixMapping(endPrefixMapping.getPrefix());
            } else if (ev instanceof IgnorableWhitespace) {
                TextEvent text = (TextEvent) ev;
                characters(expressionContext, executionContext, text,
                           new CharHandler() {
                                   public void characters(char[] ch, int offset, int len)
                                       throws SAXException {
                                       consumer.ignorableWhitespace(ch, offset, len);
                                   }
                               });
            } else if (ev instanceof ProcessingInstruction) {
                ProcessingInstruction pi = (ProcessingInstruction) ev;
                consumer.processingInstruction(pi.getTarget(), pi.getData());
            } else if (ev instanceof SkippedEntity) {
                SkippedEntity skippedEntity = (SkippedEntity) ev;
                consumer.skippedEntity(skippedEntity.getName());
            } else if (ev instanceof StartDocument) {
                if (((StartDocument) ev).getEndDocument() != null) {
                    // if this isn't a document fragment
                    consumer.startDocument();
                }
            } else if (ev instanceof StartElement) {
                StartElement startElement = (StartElement) ev;
                StartDefine def = (StartDefine) executionContext
                        .getDefinitions().get(startElement.getQname());
                if (def != null) {
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
                                    throw new SAXParseException(e.getMessage(),
                                                                ev.getLocation(), e);
                                } catch (Error err) {
                                    throw new SAXParseException(err.getMessage(),
                                                                ev.getLocation(),
                                                                new ErrorHolder(err));
                                }
                                attributeValue = val != null ? val : "";
                            } else {
                                StringBuffer buf = new StringBuffer();
                                Iterator iterSubst =
                                    substEvent.getSubstitutions().iterator();
                                while (iterSubst.hasNext()) {
                                    Subst subst = (Subst) iterSubst.next();
                                    if (subst instanceof Literal) {
                                        Literal lit = (Literal) subst;
                                        buf.append(lit.getValue());
                                    } else if (subst instanceof JXTExpression) {
                                        JXTExpression expr = (JXTExpression) subst;
                                        Object val;
                                        try {
                                            val = expr.getValue(expressionContext);
                                        } catch (Exception e) {
                                            throw new SAXParseException(e.getMessage(),
                                                                        ev.getLocation(), e);
                                        } catch (Error err) {
                                            throw new SAXParseException(err.getMessage(),
                                                                        ev.getLocation(),
                                                                        new ErrorHolder(err));
                                        }
                                        buf.append(val != null ? val.toString() : "");
                                    }
                                }
                                attributeValue = buf.toString();
                            }
                        } else {
                            throw new Error("this shouldn't have happened");
                        }
                        attributeMap.put(attributeName, attributeValue);
                    }
                    ExpressionContext localExpressionContext =
                        new ExpressionContext(expressionContext);
                    HashMap macro = new HashMap();
                    macro.put("body", startElement);
                    macro.put("arguments", attributeMap);
                    localExpressionContext.put("macro", macro);
                    Iterator iter = def.getParameters().entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry e = (Map.Entry) iter.next();
                        String key = (String) e.getKey();
                        StartParameter startParam =
                            (StartParameter) e.getValue();
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
                    continue;
                }
                Iterator i = startElement.getAttributeEvents().iterator();
                AttributesImpl attrs = new AttributesImpl();
                while (i.hasNext()) {
                    AttributeEvent attrEvent = (AttributeEvent) i.next();
                    if (attrEvent instanceof CopyAttribute) {
                        CopyAttribute copy = (CopyAttribute) attrEvent;
                        attrs.addAttribute(copy.getNamespaceURI(),
                                           copy.getLocalName(), copy.getRaw(),
                                           copy.getType(), copy.getValue());
                    } else if (attrEvent instanceof SubstituteAttribute) {
                        StringBuffer buf = new StringBuffer();
                        SubstituteAttribute substEvent = (SubstituteAttribute) attrEvent;
                        Iterator iterSubst = substEvent.getSubstitutions().iterator();
                        while (iterSubst.hasNext()) {
                            Subst subst = (Subst) iterSubst.next();
                            if (subst instanceof Literal) {
                                Literal lit = (Literal) subst;
                                buf.append(lit.getValue());
                            } else if (subst instanceof JXTExpression) {
                                JXTExpression expr = (JXTExpression) subst;
                                Object val;
                                try {
                                    val = expr.getValue(expressionContext);
                                } catch (Exception e) {
                                    throw new SAXParseException(e.getMessage(),
                                                                ev.getLocation(), e);
                                } catch (Error err) {
                                    throw new SAXParseException(err.getMessage(),
                                                                ev.getLocation(),
                                                                new ErrorHolder(err));
                                }
                                buf.append(val != null ? val.toString() : "");
                            }
                        }
                        attrs.addAttribute(attrEvent.getNamespaceURI(),
                                           attrEvent.getLocalName(), attrEvent.getRaw(),
                                           attrEvent.getType(), buf.toString());
                    }
                }
                consumer.startElement(startElement.getNamespaceURI(),
                                      startElement.getLocalName(), startElement.getRaw(),
                                      attrs);
            } else if (ev instanceof StartPrefixMapping) {
                StartPrefixMapping startPrefixMapping = (StartPrefixMapping) ev;
                consumer.startPrefixMapping(startPrefixMapping.getPrefix(),
                                            startPrefixMapping.getUri());

                // LexicalHandler methods
            } else if (ev instanceof EndCDATA) {
                consumer.endCDATA();
            } else if (ev instanceof EndDTD) {
                consumer.endDTD();
            } else if (ev instanceof EndEntity) {
                consumer.endEntity(((EndEntity) ev).getName());
            } else if (ev instanceof StartCDATA) {
                consumer.startCDATA();
            } else if (ev instanceof StartDTD) {
                StartDTD startDTD = (StartDTD) ev;
                consumer.startDTD(startDTD.getName(), startDTD.getPublicId(),
                                  startDTD.getSystemId());
            } else if (ev instanceof StartEntity) {
                consumer.startEntity(((StartEntity) ev).getName());

                // Instructions
            } else if (ev instanceof StartInstruction) {
                ev = ((StartInstruction)ev).execute(consumer,
                                                    expressionContext, executionContext,
                                                    macroCall, startEvent, endEvent);
                continue;
            }
            ev = ev.getNext();
        }
    }

    interface CharHandler {
        public void characters(char[] ch, int offset, int length)
                throws SAXException;
    }

    private static void characters(ExpressionContext expressionContext,
                                   ExecutionContext executionContext,
                                   TextEvent event, CharHandler handler)
        throws SAXException {
        Iterator iter = event.getSubstitutions().iterator();
        while (iter.hasNext()) {
            Object subst = iter.next();
            char[] chars;
            if (subst instanceof Literal) {
                chars = ((Literal) subst).getCharArray();
            } else {
                JXTExpression expr = (JXTExpression) subst;
                try {
                    Object val = expr.getValue(expressionContext);
                    chars = val != null ? val.toString().toCharArray()
                            : ArrayUtils.EMPTY_CHAR_ARRAY;
                } catch (Exception e) {
                    throw new SAXParseException(e.getMessage(), event
                            .getLocation(), e);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(), event
                            .getLocation(), new ErrorHolder(err));
                }
            }
            handler.characters(chars, 0, chars.length);
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
	    char[] ch =
		val == null ? ArrayUtils.EMPTY_CHAR_ARRAY
		: val.toString().toCharArray();
	    consumer.characters(ch, 0, ch.length);
	}
    }

    /**
     * dump a DOM document, using an IncludeXMLConsumer to filter out start/end
     * document events
     */
    public static void executeDOM(final XMLConsumer consumer, Node node)
            throws SAXException {
        IncludeXMLConsumer includer = new IncludeXMLConsumer(consumer);
        DOMStreamer streamer = new DOMStreamer(includer);
        streamer.stream(node);
    }

    private static void call(Locator location, StartElement macroCall,
                             final XMLConsumer consumer,
                             ExpressionContext expressionContext,
                             ExecutionContext executionContext,
                             Event startEvent, Event endEvent)
        throws SAXException {
        try {
            execute(consumer, expressionContext, executionContext,
                    macroCall, startEvent, endEvent);
        } catch (SAXParseException exc) {
            throw new SAXParseException(macroCall.getLocalName() + ": "
                                        + exc.getMessage(), location, exc);
        }
    }

    public static NodeList toDOMNodeList(String elementName,
                                         StartInstruction si,
                                         ExpressionContext expressionContext,
                                         ExecutionContext executionContext,
                                         StartElement macroCall)
            throws SAXException {
        DOMBuilder builder = new DOMBuilder();
        builder.startDocument();
        builder.startElement(JXTemplateGenerator.NS, elementName, elementName,
                             EMPTY_ATTRS);
        execute(builder, expressionContext, executionContext,
                macroCall, si.getNext(), si.getEndInstruction());
        builder.endElement(JXTemplateGenerator.NS, elementName, elementName);
        builder.endDocument();
        Node node = builder.getDocument().getDocumentElement();
        return node.getChildNodes();
    }

}
