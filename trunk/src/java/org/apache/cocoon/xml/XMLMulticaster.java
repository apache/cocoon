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
package org.apache.cocoon.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: XMLMulticaster.java,v 1.2 2004/03/08 14:04:00 cziegeler Exp $
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
