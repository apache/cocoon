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
package org.apache.cocoon.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: XMLMulticaster.java,v 1.1 2003/03/09 00:09:46 pier Exp $
 */

public final class XMLMulticaster implements XMLConsumer {

    /**
     * The XMLMulticaster forwards incomming sax events to a list of
     * receiving objects.
     */
    private ContentHandler[] contentHandlerList;
    private LexicalHandler[] lexicalHandlerList;

    /**
     * Create a new XMLMulticaster with two consumers
     */
    public XMLMulticaster(XMLConsumer firstConsumer, XMLConsumer secondConsumer) {
        this.contentHandlerList = new ContentHandler[] {firstConsumer, secondConsumer};
        this.lexicalHandlerList = new LexicalHandler[] {firstConsumer, secondConsumer};
    }

    /**
     * Create a new XMLMulticaster from two contentHandler/lexicalHandler pairs
     */
    public XMLMulticaster(ContentHandler firstContentHandler,
                          LexicalHandler firstLexicalHandler,
                          ContentHandler secondContentHandler,
                          LexicalHandler secondLexicalHandler) {
        this.contentHandlerList = new ContentHandler[] {firstContentHandler, secondContentHandler};
        this.lexicalHandlerList = new LexicalHandler[] {firstLexicalHandler, secondLexicalHandler};
    }

    public XMLMulticaster(ContentHandler[] chList,
                          LexicalHandler[] lhList) {
        this.contentHandlerList = chList;
        this.lexicalHandlerList = lhList;
    }

    public void startDocument() throws SAXException {
        for(int i=0; i<this.contentHandlerList.length; i++) {
            this.contentHandlerList[i].startDocument();
        }
    }

    public void endDocument() throws SAXException {
        for(int i=0; i<this.contentHandlerList.length; i++) {
                this.contentHandlerList[i].endDocument();
        }
    }

    public void startPrefixMapping(java.lang.String prefix, java.lang.String uri) throws SAXException {
        for(int i=0; i<this.contentHandlerList.length; i++)
                this.contentHandlerList[i].startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(java.lang.String prefix) throws SAXException {
        for(int i=0; i<this.contentHandlerList.length; i++)
                this.contentHandlerList[i].endPrefixMapping(prefix);
    }

    public void startElement(java.lang.String namespaceURI, java.lang.String localName, java.lang.String qName, Attributes atts) throws SAXException {
        for(int i=0; i<this.contentHandlerList.length; i++)
                this.contentHandlerList[i].startElement(namespaceURI, localName, qName, atts);
    }

    public void endElement(java.lang.String namespaceURI, java.lang.String localName, java.lang.String qName) throws SAXException {
        for(int i=0; i<this.contentHandlerList.length; i++)
                this.contentHandlerList[i].endElement(namespaceURI, localName, qName);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        for(int i=0; i<this.contentHandlerList.length; i++)
                this.contentHandlerList[i].characters(ch, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        for(int i=0; i<this.contentHandlerList.length; i++)
                this.contentHandlerList[i].ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(java.lang.String target, java.lang.String data) throws SAXException {
        for(int i=0; i<this.contentHandlerList.length; i++)
                this.contentHandlerList[i].processingInstruction(target, data);
    }

    public void setDocumentLocator(Locator locator) {
        for(int i=0; i<this.contentHandlerList.length; i++)
                this.contentHandlerList[i].setDocumentLocator(locator);
    }

    public void skippedEntity(java.lang.String name) throws SAXException {
        for(int i=0; i<this.contentHandlerList.length; i++)
                this.contentHandlerList[i].skippedEntity(name);
    }

    public void startDTD(String name, String public_id, String system_id)
                        throws SAXException {
        for(int i=0; i<this.lexicalHandlerList.length; i++)
            if (this.lexicalHandlerList[i] != null)
                this.lexicalHandlerList[i].startDTD(name, public_id, system_id);
    }

    public void endDTD() throws SAXException {
        for(int i=0; i<this.lexicalHandlerList.length; i++)
            if (this.lexicalHandlerList[i] != null)
                this.lexicalHandlerList[i].endDTD();
    }

    public void startEntity(String name) throws SAXException {
        for(int i=0; i<this.lexicalHandlerList.length; i++)
            if (this.lexicalHandlerList[i] != null)
                 this.lexicalHandlerList[i].startEntity(name);
    }

    public void endEntity(String name) throws SAXException {
        for(int i=0; i<this.lexicalHandlerList.length; i++)
            if (this.lexicalHandlerList[i] != null)
                this.lexicalHandlerList[i].endEntity(name);
    }

    public void startCDATA() throws SAXException {
        for(int i=0; i<this.lexicalHandlerList.length; i++)
            if (this.lexicalHandlerList[i] != null)
                this.lexicalHandlerList[i].startCDATA();
    }

    public void endCDATA() throws SAXException {
        for(int i=0; i<this.lexicalHandlerList.length; i++)
            if (this.lexicalHandlerList[i] != null)
                this.lexicalHandlerList[i].endCDATA();
    }

    public void comment(char ary[], int start, int length)
                        throws SAXException {
        for(int i=0; i<this.lexicalHandlerList.length; i++)
            if (this.lexicalHandlerList[i] != null)
                this.lexicalHandlerList[i].comment(ary, start, length);
    }
}
