/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.generation;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.AbstractXMLProducer;
import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.apache.commons.jxpath.*;

public class JXPathTemplate extends AbstractGenerator {

    private static final JXPathContextFactory 
        jxpathContextFactory = JXPathContextFactory.newInstance();

    final static String JXPATH_NS = 
        "http://cocoon.apache.org/transformation/jxpath/1.0";

    final static String FOR_EACH = "for-each";
    final static String IF = "if";
    final static String CHOOSE = "choose";
    final static String WHEN = "when";
    final static String OTHERWISE = "otherwise";
    final static String VALUE_OF = "value-of";

    class Event {
	final Locator location;
	Event next;
	Event(Locator location) {
	    this.location = new LocatorImpl(location);
	}
    }

    class TextEvent extends Event {
	TextEvent(Locator location, 
		  char[] chars, int start, int length) {
	    super(location);
	    this.chars = new char[this.length = length];
	    System.arraycopy(chars, start, this.chars, 
			     this.start = 0, length);
	}
	final char[] chars;
	final int start;
	final int length;
    }

    class Characters extends TextEvent {
	Characters(Locator location, 
		  char[] chars, int start, int length) {
	    super(location, chars, start, length);
	}
    }

    class StartDocument extends Event {
	long compileTime;
	StartDocument(Locator location, long compileTime) {
	    super(location);
	    this.compileTime = compileTime;
	}
    }

    class EndDocument extends Event {
	EndDocument(Locator location) {
	    super(location);
	}
    }

    class EndPrefixMapping extends Event {
	EndPrefixMapping(Locator location, String prefix) {
	    super(location);
	    this.prefix = prefix;
	}
	final String prefix;
    }
    
    class IgnorableWhitespace extends TextEvent {
	IgnorableWhitespace(Locator location, 
			    char[] chars, int start, int length) {
	    super(location, chars, start, length);
	}
    }

    class SkippedEntity extends Event {
	SkippedEntity(Locator location, String name) {
	    super(location);
	    this.name = name;
	}
	final String name;
    }

    abstract class AttributeEvent {
	AttributeEvent(String name, String namespaceURI,
		       String type) {
	    this.name = name;
	    this.namespaceURI = namespaceURI;
	    this.type = type;
	}
	final String name;
	final String namespaceURI;
	final String type;
    }
    
    class CopyAttribute extends AttributeEvent {
	CopyAttribute(String name, String namespaceURI,
		      String type, String value) {
	    super(name, namespaceURI, type);
	    this.value = value;
	}
	final String value;
    }
    
    class Subst {
    }
    
    class Literal extends Subst {
	Literal(String val) {
	    this.value = val;
	}
	String value;
    }
    
    class Expression extends Subst {
	Expression(CompiledExpression expr) {
	    this.expr = expr;
	}
	CompiledExpression expr;
    }

    class SubstituteAttribute extends AttributeEvent {
	List substitutions = new LinkedList();
    }

    class StartElement extends Event {
	StartElement(Locator location, String namespaceURI,
		     String localName, String raw,
		     Attributes attrs) throws SAXException {
	    super(location);
	    this.namespaceURI = namespaceURI;
	    this.localName = localName;
	    this.raw = raw;
	    StringBuffer buf = new StringBuffer();
	    for (int i = 0, len = attrs.getLength();
		 i < len; i++) {
		String uri = attrs.getURI(i);
		String localName = attrs.getLocalName(i);
		String raw = attrs.getQName(i);
		String type = attrs.getType(i);
		String value = attrs.getValue(i);
		StringReader in = new StringReader(value);
		int ch;
		buf.setLength(0);
		boolean inExpr = false;
		while ((ch = in.read()) != -1) {
		    char c = (char)ch;
		    if (inExpr) {
			if (c == '}') {
			    String str = buf.toString();
			    buf.setLength(0);
			    CompiledExpression compiledExpression = 
				JXPathContext.compile(str);
			    attributeEvents.add(new Expression(compiledExpression));
			    inExpr = false;
			} else if (c == '\\') {
			    ch = in.read();
			    if (ch == -1) {
				buf.append('\\');
			    } else {
				buf.append((char)ch);
			    }
			} else {
			    buf.append(c);
			}
		    } else {
			if (c == '\\') {
			    ch = in.read();
			    if (ch == -1) {
				buf.append('\\');
			    } else {
				buf.append((char)ch);
			    }
			} else {
			    if (c == '{') {
				ch = in.read();
				if (ch != -1) {
				    if (buf.getLength() > 0) {
					attributeEvents.add(new Literal(buf.toString()));
					buf.setLength(0);
				    }
				    buf.append((char)ch);
				    inExpr = true;
				    continue;
				}
				buf.append('{');
			    }
			    if (ch != -1) {
				buf.append((char)ch);
			    }
			}
		    }
		}
		if (buf.getLength() > 0) {
		    attributeEvents.add(new Literal(buf.toString()));
		}
	    }
	}
	final String namespaceURI;
	final String localName;
	final String raw;
	final List attributeEvents = new LinkedList();
    }

    class StartForEach extends Event {
	StartForEach(Locator location, CompiledExpression select) {
	    super(location);
	    this.select = select;
	}
	CompiledExpression select;
	EndForEach endForEach;
    }
    
    class EndForEach extends Event {
	EndForEach(Locator location) {
	    super(location);
	}
    }

    class StartIf extends Event {
	StartIf(Locator location, CompiledExpression test) {
	    super(location);
	    this.test = test;
	}
	final CompiledExpression test;
	EndIf endIf;
    }

    class EndIf extends Event {
	EndIf(Locator location) {
	    super(location);
	}
    }

    class StartChoose extends Event {
	StartChoose(Locator location) {
	    super(location);
	}
	StartWhen firstChoice;
	StartOtherwise otherwise;
	EndChoose endChoose;
    }

    class EndChoose extends Event {
	EndChoose(Locator location) {
	    super(location);
	}
    }

    class StartWhen extends Event {
	StartWhen(Locator location, CompiledExpression test) {
	    super(location);
	    this.test = test;
	}
	final CompiledExpression test;
	StartWhen nextChoice;
	EndWhen endWhen;
    }

    class EndWhen extends Event {
	EndWhen(Locator location) {
	    super(location);
	}
    }

    class StartOtherwise extends Event {
	StartOtherwise(Locator location) {
	    super(location);
	}
	EndOtherwise endOtherwise;
    }

    class EndOtherwise extends Event {
	EndOtherwise(Locator location) {
	    super(location);
	}
    }

    class StartPrefixMapping extends Event {
	StartPrefixMapping(Locator location, String prefix,
			   String uri) {
	    super(location);
	    this.prefix = prefix;
	    this.uri = uri;
	}
	String prefix;
	String uri;
    }

    class Comment extends TextEvent {
	Comment(Locator location, char[] chars,
		int start, int length) {
	    super(location, chars, start, length);
	}
    }

    class EndCDATA extends Event {
	EndCDATA(Locator location) {
	    super(location);
	}
    }

    class EndDTD extends Event {
	EndDTD(Locator location) {
	    super(location);
	}
    }

    class EndEntity extends Event {
	EndEntity(Locator location, String name) {
	    super(location);
	    this.name = name;
	}
	final String name;
    }

    class StartDTD extends Event {
	StartDTD(Locator location, String name, 
		 String publicId, String systemId) {
	    super(location);
	    this.name = name;
	    this.publicId = publicId;
	    this.systemId = systemId;
	}
	final String name;
	final String publicId;
	final String SystemId;
    }
    
    class StartEntity extends Event {
	public StartEntity(Locator location, String name) {
	    super(location);
	    this.name = name;
	}
	final String name;
    }


    class Parser implements LexicalHandler, ContentHandler {

	Event lastEvent;
	Stack stack = new Stack();
	Locator locator;

	private void addEvent(Event ev) {
	    lastEvent.next = ev;
	    lastEvent = ev;
	}

	public void characters(char[] ch, int start, int length) {
	    Characters chars = new Characters(locator,
					      ch, start, length);
	    addEvent(chars);
	}

	public void endDocument() {
	    StartDocument startDoc = (StartDocument)stack.pop();
	    EndDocument endDoc = new EndDocument(locator);
	    addEvent(endDoc);
	}

	public void endElement(String namespaceURI,
			       String localName,
			       String raw) 
	    throws SAXException {
	    Event start = (Event)stack.pop();
	    Event newEvent = null;
	    if (JXPATH_NS.equals(namespaceURI)) {
		if (start instanceof StartForEach) {
		    StartForeach startForEach = 
			(StartForEach)start;
		    newEvent = startForEach.endForEach = 
			new EndForEach(locator);
		    
		} else if (start instanceof StartIf) {
		    StartIf startIf = (StartIf)start;
		    newEvent = startIf.endIf = 
			new EndIf(locator);
		} else if (start instanceof StartWhen) {
		    StartWhen startWhen = (StartWhen)start;
		    StartChoose startChoose = (StartChoose)stack.peek();
		    if (startChoose.firstWhen != null) {
			StartWhen w = startChoose.firstWhen;
			while (w.nextWhen != null) {
			    w = w.nextWhen;
			}
			w.nextWhen = startWhen;
		    } else {
			startChoose.firstWhen = startWhen;
		    }
		    newEvent = startWhen.endWhen = 
			new EndWhen(locator);
		} else if (start instanceof StartOtherwise) {
		    StartOtherwise startOtherwise = 
			(StartOtherwise)start;
		    StartChoose startChoose = (StartChoose)stack.peek();
		    newEvent = startOtherwise.endOtherwise = 
			new EndOtherwise(locator);
		    startChoose.otherwise = startOtherwise;
		}
	    } else {
		StartElement startElement = (StartElement)start;
		newEvent = new EndElement(locator, startElement);
	    }
	    addEvent(newEvent);
	}
	
	public void endPrefixMapping(String prefix) {
	    EndPrefixMapping endPrefixMapping = 
		new EndPrefixMapping(locator, prefix);
	    addEvent(endPrefixMapping);
	}

	public void ignorableWhitespace(char[] ch, int start, int length) {
	    Event chars = new IgnorableWhitespace(locator,
						  ch, start, length);
	    addEvent(chars);
	}

	public void processingInstruction(String target,
					  String data) {
	    Event pi = new ProcessingInstruction(locator, target,
						 data);
	    addEvent(pi);
	}

	public void setDocumentLocator(Locator locator) {
	    this.locator = locator;
	}

	public void skippedEntity(String name) {
	    addEvent(new SkippedEntity(locator, name));
	}


	public void startDocument() {
	    startEvent = new StartDocument(locator);
	    lastEvent = startEvent;
	    stack.push(lastEvent);
	}

	public void startElement(String namespaceURI,
				 String localName,
				 String raw,
				 Attributes attrs) 
	    throws SAXException {
	    Event newEvent = null;
	    if (JXPATH_NS.equals(namespaceURI)) {
		if (localName.equals(FOR_EACH)) {
		    String select = attrs.getValue("select");
		    if (select == null) {
			throw new SAXParseException("for-each: \"select\" is rrequired", locator, null);
		    }
		    CompiledExpression expr = 
			JXPathContext.compile(select);
		    StartForEach startForEach = 
			new StartForEach(locator, expr);
		    newEvent = startForEach;
		} else if (localName.equals(CHOOSE)) {
		} else if (localName.equals(WHEN)) {
		} else if (localName.equals(OTHERWISE)) {
		} else if (localName.equals(IF)) {
		    String test = attrs.getValue("test");
		    if (test == null) {
			throw new SAXParseException("if: \"test\" is rrequired", locator, null);
		    }
		    CompiledExpression expr = 
			JXPathContext.compile(test);
		    StartIf startIf = 
			new StartIf(locator, expr);
		    newEvent = startIf;
		} else if (localName.equals(VALUE_OF)) {
		}
	    } else {
		StartElement startElem = 
		    new StartElement(locator, namespaceURI,
				     localName, raw, attrs);
		newEvent = startElem;
		stack.push(startElem);
	    }
	    addEvent(newEvent);
	}
	
	void startPrefixMapping(String prefix, String uri) {
	    addEvent(new StartPrefixMapping(locator, prefix, uri));
	}
    }

    private StartDocument startEvent;
    private XMLConsumer consumer;
    private JXPathContext rootContext;
    private Variables variables;
    private static Map cache = new HashMap();
    private Source inputSource;

    public void setup(SourceResolver resolver, Map objectModel,
                      String src, Parameters parameters)
        throws ProcessingException, SAXException, IOException {

        super.setup(resolver, objectModel, src, parameters);
        if (src != null) {
            try {
                this.inputSource = resolver.resolveURI(src);
            } catch (SourceException se) {
                throw SourceUtil.handle("Error during resolving of '" + src + "'.", se);
            }
        }
	String uri = inputSource.getURI();
	int lastMod = inputSource.getLastModified();
	synchronized (cache) {
	    StartDocument startEvent = (StartDocument)cache.get(uri);
	    if (startEvent != null &&
		lastMod > startEvent.compileTime) {
		cache.remove(uri);
	    }
	}
        // FIX ME: When we decide proper way to pass "bean" and "kont"
        Object bean = ((Environment)resolver).getAttribute("bean-dict");
        kont =
            (WebContinuation)((Environment)resolver).getAttribute("kont");
        variables = new MyVariables(bean, 
                                    kont,
                                    ObjectModelHelper.getRequest(objectModel),
                                    ObjectModelHelper.getResponse(objectModel),
                                    ObjectModelHelper.getContext(objectModel),
                                    parameters);
	rootContext = jxpathContextFactory.newContext(null, bean);
	rootContext.setVariables(variables);
    }

    public void setConsumer(XMLConsumer consumer) {
	this.consumer = consumer;
    }

    public void generate() 
        throws IOException, SAXException, ProcessingException {
	StartDocument startEvent;
	synchronized (cache) {
	    startEvent = cache.get(inputSource.getURI());
	}
	if (startEvent == null) {
	    long compileTime = inputSource.getLastModified();
	    Parser parser = new Parser();
	    this.resolver.toSAX(this.inputSource, parser);
	    startEvent = parser.startEvent;
	    startEvent.compileTime = compileTime;
	    synchronized (cache) {
		cache.put(inputSource.getURI(), startEvent);
	    }
	}
	execute(rootContext, startEvent, null);
    }

    private void execute(JXPathContext context,
			 Event startEvent, Event endEvent) 
	throws SAXException {
	Event ev = startEvent;
	while (ev != endEvent) {
	    consumer.setDocumentLocator(ev.location);
	    if (ev instanceof Characters) {
		TextEvent text = (Text)ev;
		consumer.characters(text.chars, text.start,
				    text.length);
	    } else if (ev instanceof EndDocument) {
		consumer.startDocument();
	    } else if (ev instanceof EndElement) {
		EndElement endElement = (EndElement)ev;
		StartElement startElement = (StartElement)endElement.startElement;
		consumer.endElement(startElement.namespaceURI,
				    startElement.localName,
				    startElement.raw);
	    } else if (ev instanceof EndPrefixMapping) {
		EndPrefixMapping endPrefixMapping = 
		    (EndPrefixMapping)ev;
		consumer.endPrefixMapping(endPrefixMapping.prefix);
	    } else if (ev instanceof IgnorableWhitespace) {
		TextEvent text = (Text)ev;
		consumer.ignorableWhitespace(text.chars, text.start,
					     text.length);
	    } else if (ev instanceof ProcessingInstruction) {
		ProcessingInstruction pi = (ProcessingInstruction)ev;
		consumer.processingInstruction(pi.target, pi.data);
	    } else if (ev instanceof SkippedEntity) {
		SkippedEntity skippedEntity = (SkippedEntity)event;
		consumer.skippedEntity(skippedEntity.name);
	    } else if (ev instanceof StartDocument) {
		consumer.startDocument();
	    } else if (ev instanceof StartIf) {
		StartIf startIf = (StartIf)ev;
		Object val = startIf.test.getValue(context);
		boolean result = false;
		if (val instanceof Boolean) {
		    result = ((Boolean)val).booleanValue();
		}
		if (!result) {
		    ev = startIf.endIf.next;
		    continue;
		}
	    } else if (ev instanceof StartForeach) {
		StartForEach startForEach = (StartForEach)ev;
		Iterator iter = 
		    startForEach.select.iteratePointers(context);
		while (iter.hasNext()) {
		    Object contextObject = iter.next();
		    JXPathContext newContext = 
			jxpathContextFactory.newContext(null, 
							contextObject);
		    newContext.setVariables(variables);
		    execute(newContext,
			    startForEach.startEvent,
			    startForEach.endForEach);
		}
		ev = startForEach.endForEach.next;
		continue;
	    } else if (ev instanceof StartChoose) {
		StartChoose choose = (StartChoose)ev;
		StartWhen when = choose.firstWhen; 
		for (;when != null; when = when.nextChoice) {
		    Object val = when.test.getValue(context);
		    boolean result = false;
		    if (val instanceof Boolean) {
			result = ((Boolean)val).booleanValue();
		    }
		    if (result) {
			execute(context, when.next, when.endWhen);
			break;
		    }
		}
		if (when == null) {
		    if (startChoose.otherwise != null) {
			execute(context, startChoose.otherwise.next,
				startChoose.otherwise.endOtherwise);
		    }
		}
		ev = choose.endChoose.next;
		continue;
	    } else if (ev instanceof StartElement) {
		StartElement startElement = (StartElement)ev;
		Iterator i = startElement.attributeEvents.iterator();
		AttributesImpl attrs = new AttributesImpl();
		while (i.hasNext()) {
		    AttributeEvent attrEvent = (AttributeEvent)
			i.next();
		    if (attrEvent instanceof CopyAttribute) {
			CopyAttribute copy =
			    (CopyAttribute)attrEvent;
			attrs.addAttribute(copy.name,
					   copy.namespaceURI,
					   copy.type,
					   copy.value);
		    } else if (attrEvent instanceof 
			       SubstituteAttribute) {
			StringBuffer buf = new StringBuffer();
			SubstituteAttribute substEvent =
			    (SubstituteAttribute)attrEvent;
			Iterator ii = substEvent.substitutions.iterator();
			while (ii.hasNext()) {
			    Subst subst = (Subst)ii.next();
			    if (subst instanceof Literal) {
				Literal lit = (Literal)subst;
				buf.append(lit);
			    } else if (subst instanceof Expression) {
				Expression expr = (Expression)subst;
				Object val = 
				    expr.compiledExpression.getValue(getContext());
				if (val == null) {
				    val = "";
				}
				buf.append(val.toString());
			    }
			}
			attrs.addAttribute(subst.name,
					   subst.namespaceURI,
					   subst.type,
					   buf.toString());
		    }
		}
		consumer.startElement(startElement.namespaceURI,
				      startElement.localName,
				      startElement.raw,
				      attrs); 
	    } else if (ev instanceof StartPrefixMapping) {
		StartPrefixMapping startPrefixMapping = 
		    (StartPrefixMapping)ev;
		consumer.startPrefixMapping(ev.prefix, ev.uri);
	    } else if (ev instanceof Comment) {
		TextEvent text = (Text)ev;
		consumer.comment(text.chars, text.start,
				 text.length);
	    } else if (ev instanceof EndCDATA) {
		consumer.endCDATA();
	    } else if (ev instanceof EndDTD) {
		consumer.endDTD();
	    } else if (ev instanceof EndEntity) {
		consumer.endEntity(((EndEntity)ev).name);
	    } else if (ev instanceof startCDATA) {
		consumer.startCDATA();
	    } else if (ev instanceof StartDTD) {
		StartDTD startDTD = (StartDTD)ev;
		consumer.startDTD(startDTD.name,
				  startDTD.publicId,
				  startDTD.systemId);
	    } else if (ev instanceof StartEntity) {
		consumer.startEntity(((StartEntity)ev).name);
	    }
	    ev = ev.next;
	}
    }

}
