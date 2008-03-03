/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.util;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * A special purpose <code>XMLConsumer</code> which can:
 * <ul>
 * <li>Ignore startDocument, endDocument events.
 * <li>Ignore startDTD, endDTD, and all comments within DTD.
 * </ul>
 *
 * @version $Id$
 */
public class IncludeXMLConsumer implements ContentHandler, LexicalHandler {

    private final ContentHandler contentHandler;
    private final LexicalHandler lexicalHandler;

    private boolean inDTD;

    /**
     * Constructor
     */
    public IncludeXMLConsumer (ContentHandler contentHandler) {
        this(contentHandler, contentHandler instanceof LexicalHandler ? (LexicalHandler)contentHandler : null);
    }

    /**
     * Constructor
     */
    public IncludeXMLConsumer (ContentHandler contentHandler, LexicalHandler lexicalHandler) {
        this.contentHandler = contentHandler;
        this.lexicalHandler = lexicalHandler;
    }

    //
    // ContentHandler interface
    //

    public void setDocumentLocator(Locator loc) {
        this.contentHandler.setDocumentLocator(loc);
    }

    public void startDocument() throws SAXException {
        // Ignored
    }

    public void endDocument() throws SAXException {
        // Ignored
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        this.contentHandler.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        this.contentHandler.endPrefixMapping(prefix);
    }

    public void startElement(String uri, String local, String qName, Attributes attr) throws SAXException {
        this.contentHandler.startElement(uri,local,qName,attr);
    }

    public void endElement(String uri, String local, String qName) throws SAXException {
        this.contentHandler.endElement(uri, local, qName);
    }

    public void characters(char[] ch, int start, int end) throws SAXException {
        this.contentHandler.characters(ch, start, end);
    }

    public void ignorableWhitespace(char[] ch, int start, int end) throws SAXException {
        this.contentHandler.ignorableWhitespace(ch, start, end);
    }

    public void processingInstruction(String name, String value) throws SAXException {
        this.contentHandler.processingInstruction(name, value);
    }

    public void skippedEntity(String ent) throws SAXException {
        this.contentHandler.skippedEntity(ent);
    }

    //
    // LexicalHandler interface
    //

    public void startDTD(String name, String public_id, String system_id)
    throws SAXException {
        // Ignored
        this.inDTD = true;
    }

    public void endDTD() throws SAXException {
        // Ignored
        this.inDTD = false;
    }

    public void startEntity(String name) throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.startEntity(name);
        }
    }

    public void endEntity(String name) throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.endEntity(name);
        }
    }

    public void startCDATA() throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.startCDATA();
        }
    }

    public void endCDATA() throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.endCDATA();
        }
    }

    public void comment(char ary[], int start, int length) throws SAXException {
        if (!inDTD && lexicalHandler != null) {
            lexicalHandler.comment(ary,start,length);
        }
    }
}
