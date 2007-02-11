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
package org.apache.cocoon.taglib;

import org.apache.cocoon.xml.XMLConsumer;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id: TransformerTagSupport.java,v 1.2 2003/03/16 17:49:08 vgritsenko Exp $
 */
public class TransformerTagSupport extends TagSupport implements TransformerTag {
    protected XMLConsumer xmlConsumer;

    /*
     * @see ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator locator) {
        xmlConsumer.setDocumentLocator(locator);
    }

    /*
     * @see ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
    }

    /*
     * @see ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
    }

    /*
     * @see ContentHandler#startPrefixMapping(String, String)
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        xmlConsumer.startPrefixMapping(prefix, uri);
    }

    /*
     * @see ContentHandler#endPrefixMapping(String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        xmlConsumer.endPrefixMapping(prefix);
    }

    /*
     * @see ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        xmlConsumer.startElement(namespaceURI, localName, qName, atts);
    }

    /*
     * @see ContentHandler#endElement(String, String, String)
     */
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        xmlConsumer.endElement(namespaceURI, localName, qName);
    }

    /*
     * @see ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        xmlConsumer.characters(ch, start, length);
    }

    /*
     * @see ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        xmlConsumer.ignorableWhitespace(ch, start, length);
    }

    /*
     * @see ContentHandler#processingInstruction(String, String)
     */
    public void processingInstruction(String target, String data) throws SAXException {
        xmlConsumer.processingInstruction(target, data);
    }

    /*
     * @see ContentHandler#skippedEntity(String)
     */
    public void skippedEntity(String name) throws SAXException {
        xmlConsumer.skippedEntity(name);
    }

    /*
     * @see LexicalHandler#startDTD(String, String, String)
     */
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        xmlConsumer.startDTD(name, publicId, systemId);
    }

    /*
     * @see LexicalHandler#endDTD()
     */
    public void endDTD() throws SAXException {
        xmlConsumer.endDTD();
    }

    /*
     * @see LexicalHandler#startEntity(String)
     */
    public void startEntity(String name) throws SAXException {
        xmlConsumer.startEntity(name);
    }

    /*
     * @see LexicalHandler#endEntity(String)
     */
    public void endEntity(String name) throws SAXException {
        xmlConsumer.endEntity(name);
    }

    /*
     * @see LexicalHandler#startCDATA()
     */
    public void startCDATA() throws SAXException {
        xmlConsumer.startCDATA();
    }

    /*
     * @see LexicalHandler#endCDATA()
     */
    public void endCDATA() throws SAXException {
        xmlConsumer.endCDATA();
    }

    /*
     * @see LexicalHandler#comment(char[], int, int)
     */
    public void comment(char[] ch, int start, int length) throws SAXException {
        xmlConsumer.comment(ch, start, length);
    }

    /*
     * @see XMLProducer#setConsumer(XMLConsumer)
     */
    public void setConsumer(XMLConsumer consumer) {
        this.xmlConsumer = consumer;
    }

    /*
     * @see Recyclable#recycle()
     */
    public void recycle() {
        this.xmlConsumer = null;
        super.recycle();
    }

}
