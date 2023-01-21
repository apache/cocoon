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
package org.apache.cocoon.taglib;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * @version $Id$
 */
public abstract class XMLConsumerTagSupport extends VarTagSupport implements XMLConsumerTag {

    /*
     * @see ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        // nothing to do here
    }

    /*
     * @see ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
        // nothing to do here
    }

    /*
     * @see ContentHandler#endElement(String, String, String)
     */
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        // nothing to do here
    }

    /*
     * @see ContentHandler#endPrefixMapping(String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        // nothing to do here
    }

    /*
     * @see ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        // nothing to do here
    }

    /*
     * @see ContentHandler#processingInstruction(String, String)
     */
    public void processingInstruction(String target, String data) throws SAXException {
        // nothing to do here
    }

    /*
     * @see ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator locator) {
        // nothing to do here
    }

    /*
     * @see ContentHandler#skippedEntity(String)
     */
    public void skippedEntity(String name) throws SAXException {
        // nothing to do here
    }

    /*
     * @see ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
        // nothing to do here
    }

    /*
     * @see ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        // nothing to do here
    }

    /*
     * @see ContentHandler#startPrefixMapping(String, String)
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        // nothing to do here
    }

    /*
     * @see LexicalHandler#comment(char[], int, int)
     */
    public void comment(char[] ch, int start, int length) throws SAXException {
        // nothing to do here
    }

    /*
     * @see LexicalHandler#endCDATA()
     */
    public void endCDATA() throws SAXException {
        // nothing to do here
    }

    /*
     * @see LexicalHandler#endDTD()
     */
    public void endDTD() throws SAXException {
        // nothing to do here
    }

    /*
     * @see LexicalHandler#endEntity(String)
     */
    public void endEntity(String name) throws SAXException {
        // nothing to do here
   }

    /*
     * @see LexicalHandler#startCDATA()
     */
    public void startCDATA() throws SAXException {
        // nothing to do here
    }

    /*
     * @see LexicalHandler#startDTD(String, String, String)
     */
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        // nothing to do here
    }

    /*
     * @see LexicalHandler#startEntity(String)
     */
    public void startEntity(String name) throws SAXException {
        // nothing to do here
   }

}
