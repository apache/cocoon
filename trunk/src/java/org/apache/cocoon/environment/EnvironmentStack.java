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
package org.apache.cocoon.environment;

import org.apache.cocoon.xml.XMLConsumer;
import org.apache.commons.collections.ArrayStack;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * The stack for the processing environment.
 * This is an internal class, and it might change in an incompatible way over time.
 * For developing your own components/applications based on Cocoon, you shouldn't 
 * really need it.
 * This is a special implementation of a stack for the handling of the
 * cocoon protocol and the sitemap source resolving.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: EnvironmentStack.java,v 1.4 2004/01/05 13:28:01 cziegeler Exp $
 */
final class EnvironmentStack 
    extends ArrayStack 
    implements Cloneable {
    
    int offset;
    
    EnvironmentInfo getCurrentInfo() {
        return (EnvironmentInfo)this.get(offset);
    }
    
    void pushInfo(EnvironmentInfo info) {
        this.push(info);
    }
    
    EnvironmentInfo popInfo() {
        return (EnvironmentInfo)this.pop();
    }
    
    EnvironmentInfo peekInfo() {
        return (EnvironmentInfo)this.peek();
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
