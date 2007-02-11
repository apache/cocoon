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
package org.apache.cocoon.components.sax;

import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLPipe;
import org.apache.cocoon.xml.XMLProducer;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/**
 * This is a simple Tee Component.
 * The incoming events are forwarded to two other components.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: XMLTeePipe.java,v 1.1 2003/03/09 00:09:11 pier Exp $
 */

public final class XMLTeePipe
implements XMLPipe {

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     */
    public void setConsumer(XMLConsumer consumer) {
        ((XMLProducer)this.firstConsumer).setConsumer(consumer);
    }

    private XMLConsumer firstConsumer;
    private XMLConsumer secondConsumer;

    /**
     * Create a new XMLTeePipe with two consumers
     */
    public XMLTeePipe(XMLConsumer firstPipe,
                      XMLConsumer secondConsumer) {
        this.firstConsumer = firstPipe;
        this.secondConsumer = secondConsumer;
    }

    public void recycle() {
        this.firstConsumer = null;
        this.secondConsumer = null;
    }

    public void startDocument() throws SAXException {
        this.firstConsumer.startDocument();
        this.secondConsumer.startDocument();
    }

    public void endDocument() throws SAXException {
        this.firstConsumer.endDocument();
        this.secondConsumer.endDocument();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        this.firstConsumer.startPrefixMapping(prefix, uri);
        this.secondConsumer.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        this.firstConsumer.endPrefixMapping(prefix);
        this.secondConsumer.endPrefixMapping(prefix);
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
    throws SAXException {
        this.firstConsumer.startElement(namespaceURI, localName, qName, atts);
        this.secondConsumer.startElement(namespaceURI, localName, qName, atts);
    }

    public void endElement(String namespaceURI, String localName, String qName)
    throws SAXException {
        this.firstConsumer.endElement(namespaceURI, localName, qName);
        this.secondConsumer.endElement(namespaceURI, localName, qName);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        this.firstConsumer.characters(ch, start, length);
        this.secondConsumer.characters(ch, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        this.firstConsumer.ignorableWhitespace(ch, start, length);
        this.secondConsumer.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        this.firstConsumer.processingInstruction(target, data);
        this.secondConsumer.processingInstruction(target, data);
    }

    public void setDocumentLocator(Locator locator) {
        this.firstConsumer.setDocumentLocator(locator);
        this.secondConsumer.setDocumentLocator(locator);
    }

    public void skippedEntity(String name) throws SAXException {
        this.firstConsumer.skippedEntity(name);
        this.secondConsumer.skippedEntity(name);
    }

    public void startDTD(String name, String public_id, String system_id)
    throws SAXException {
        this.firstConsumer.startDTD(name, public_id, system_id);
        this.secondConsumer.startDTD(name, public_id, system_id);
    }

    public void endDTD() throws SAXException {
        this.firstConsumer.endDTD();
        this.secondConsumer.endDTD();
    }

    public void startEntity(String name) throws SAXException {
        this.firstConsumer.startEntity(name);
        this.secondConsumer.startEntity(name);
    }

    public void endEntity(String name) throws SAXException {
        this.firstConsumer.endEntity(name);
        this.secondConsumer.endEntity(name);
    }

    public void startCDATA() throws SAXException {
        this.firstConsumer.startCDATA();
        this.secondConsumer.startCDATA();
    }

    public void endCDATA() throws SAXException {
        this.firstConsumer.endCDATA();
        this.secondConsumer.endCDATA();
    }

    public void comment(char ary[], int start, int length)
    throws SAXException {
        this.firstConsumer.comment(ary, start, length);
        this.secondConsumer.comment(ary, start, length);
    }

}
