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
package org.apache.cocoon.components;

import org.apache.cocoon.xml.XMLConsumer;
import org.apache.commons.collections.ArrayStack;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * The stack for the processing environment.
 * This is a special implementation of a stack for the handling of the
 * cocoon protocol.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: EnvironmentStack.java,v 1.3 2004/03/05 13:02:45 bdelacretaz Exp $
 */
final class EnvironmentStack 
    extends ArrayStack 
    implements Cloneable {
    
    int offset;
    
    Object getCurrent() {
        return this.get(offset);
        //return this.peek(this.offset);
    }
    
    int getOffset() {
        return this.offset;
    }
  
    void setOffset(int value) {
        this.offset = value;  
    }
    
    public Object clone() {
        EnvironmentStack old = (EnvironmentStack) super.clone();
        old.offset = offset;
        return old;
    }
    
    XMLConsumer getEnvironmentAwareConsumerWrapper(XMLConsumer consumer, 
                                                   int oldOffset) {
        return new EnvironmentChanger(consumer, this, oldOffset, this.offset);
    }
}

/**
 * This class is an {@link XMLConsumer} that changes the current environment.
 * When a pipeline calls an internal pipeline, two environments are
 * established: one for the calling pipeline and one for the internal pipeline.
 * Now, if SAX events are send from the internal pipeline, they are
 * received by some component of the calling pipeline, so inbetween we
 * have to change the environment forth and back.
 */
final class EnvironmentChanger
implements XMLConsumer {

    final XMLConsumer consumer;
    final EnvironmentStack stack;
    final int oldOffset;
    final int newOffset;
    
    EnvironmentChanger(XMLConsumer consumer, EnvironmentStack es,
                       int oldOffset, int newOffset) {
        this.consumer = consumer;
        this.stack = es;
        this.oldOffset = oldOffset;
        this.newOffset = newOffset;
    }
    
    public void setDocumentLocator(Locator locator) {
        this.stack.setOffset(this.oldOffset);
        this.consumer.setDocumentLocator(locator);
        this.stack.setOffset(this.newOffset);
    }

    public void startDocument()
    throws SAXException {
        this.stack.setOffset(this.oldOffset);
        this.consumer.startDocument();
        this.stack.setOffset(this.newOffset);
    }

    public void endDocument()
    throws SAXException {
        this.stack.setOffset(this.oldOffset);
        this.consumer.endDocument();
        this.stack.setOffset(this.newOffset);
    }

    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        this.stack.setOffset(this.oldOffset);
        this.consumer.startPrefixMapping(prefix, uri);
        this.stack.setOffset(this.newOffset);
    }

    public void endPrefixMapping(String prefix)
    throws SAXException {
        this.stack.setOffset(this.oldOffset);
        this.consumer.endPrefixMapping(prefix);
        this.stack.setOffset(this.newOffset);
    }

    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        this.stack.setOffset(this.oldOffset);
        this.consumer.startElement(uri, loc, raw, a);
        this.stack.setOffset(this.newOffset);
    }


    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        this.stack.setOffset(this.oldOffset);
        this.consumer.endElement(uri, loc, raw);
        this.stack.setOffset(this.newOffset);
    }
    
    public void characters(char c[], int start, int len)
    throws SAXException {
        this.stack.setOffset(this.oldOffset);
        this.consumer.characters(c, start, len);
        this.stack.setOffset(this.newOffset);
    }

    public void ignorableWhitespace(char c[], int start, int len)
    throws SAXException {
        this.stack.setOffset(this.oldOffset);
        this.consumer.ignorableWhitespace(c, start, len);
        this.stack.setOffset(this.newOffset);
    }

    public void processingInstruction(String target, String data)
    throws SAXException {
        this.stack.setOffset(this.oldOffset);
        this.consumer.processingInstruction(target, data);
        this.stack.setOffset(this.newOffset);
    }

    public void skippedEntity(String name)
    throws SAXException {
        this.stack.setOffset(this.oldOffset);
        this.consumer.skippedEntity(name);
        this.stack.setOffset(this.newOffset);
    }

    public void startDTD(String name, String publicId, String systemId)
    throws SAXException {
        this.stack.setOffset(this.oldOffset);
        this.consumer.startDTD(name, publicId, systemId);
        this.stack.setOffset(this.newOffset);
    }

    public void endDTD()
    throws SAXException {
        this.stack.setOffset(this.oldOffset);
        this.consumer.endDTD();
        this.stack.setOffset(this.newOffset);
    }

    public void startEntity(String name)
    throws SAXException {
        this.stack.setOffset(this.oldOffset);
        this.consumer.startEntity(name);
        this.stack.setOffset(this.newOffset);
    }

    public void endEntity(String name)
    throws SAXException {
        this.stack.setOffset(this.oldOffset);
        this.consumer.endEntity(name);
        this.stack.setOffset(this.newOffset);
    }

    public void startCDATA()
    throws SAXException {
        this.stack.setOffset(this.oldOffset);
        this.consumer.startCDATA();
        this.stack.setOffset(this.newOffset);
    }

    public void endCDATA()
    throws SAXException {
        this.stack.setOffset(this.oldOffset);
        this.consumer.endCDATA();
        this.stack.setOffset(this.newOffset);
    }

    public void comment(char ch[], int start, int len)
    throws SAXException {
        this.stack.setOffset(this.oldOffset);
        this.consumer.comment(ch, start, len);
        this.stack.setOffset(this.newOffset);
    }
}
