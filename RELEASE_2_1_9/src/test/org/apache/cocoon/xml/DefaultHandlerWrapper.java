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

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Wrap a ContentHandler in a DefaultHandler
 *
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 */


public final class DefaultHandlerWrapper extends DefaultHandler {
    private final ContentHandler handler;

    public DefaultHandlerWrapper( ContentHandler handler ) {
        this.handler = handler;
    }

    public void setDocumentLocator(Locator locator) {
        handler.setDocumentLocator(locator);
    }

    public void startDocument() throws SAXException {
        handler.startDocument();
    }

    public void endDocument() throws SAXException {
        handler.endDocument();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        handler.startPrefixMapping(prefix,uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        handler.endPrefixMapping(prefix);
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        handler.startElement(namespaceURI,localName,qName,atts);
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        handler.endElement(namespaceURI,localName,qName);
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        handler.characters(ch,start,length);
    }

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
        handler.ignorableWhitespace(ch,start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        handler.processingInstruction(target,data);
    }

    public void skippedEntity(String name) throws SAXException {
        handler.skippedEntity(name);
    }


}
