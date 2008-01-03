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
package org.apache.cocoon.xml;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.w3c.dom.Node;

/**
 * A special purpose <code>XMLConsumer</code> which can:
 * <ul>
 * <li>Trim empty characters if
 *     {@link #setIgnoreEmptyCharacters(boolean) ignoreEmptyCharacters} is set.
 * <li>Ignore root element if
 *     {@link #setIgnoreRootElement(boolean) ignoreRootElement} is set.
 * <li>Ignore startDocument, endDocument events.
 * <li>Ignore startDTD, endDTD, and all comments within DTD.
 * </ul>
 *
 * <p>It is more complicated version of {@link EmbeddedXMLPipe} which, except
 * being used to include other files into the SAX events stream, can perform
 * optional operations described above.</p>
 *
 * @see EmbeddedXMLPipe
 * @version $Id$
 */
public class IncludeXMLConsumer implements XMLConsumer {

    /** The TrAX factory for serializing xml */
    private static final TransformerFactory FACTORY = TransformerFactory.newInstance();

    private final ContentHandler contentHandler;
    private final LexicalHandler lexicalHandler;

    private boolean ignoreEmptyCharacters;
    private boolean ignoreRootElement;
    private int     ignoreRootElementCount;
    private boolean inDTD;

    /**
     * Constructor
     */
    public IncludeXMLConsumer (XMLConsumer consumer) {
        this(consumer, consumer);
    }

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

    /**
     * Utility method to stream a DOM node into the provided content handler,
     * lexical handler.
     *
     * @param node The DOM Node to be included
     * @param contentHandler The SAX ContentHandler receiving the information
     * @param lexicalHandler The SAX LexicalHandler receiving the information (optional)
     */
    public static void includeNode(Node           node,
                                   ContentHandler contentHandler,
                                   LexicalHandler lexicalHandler)
    throws SAXException {
        if (node != null) {
            if (node.getNodeType() == Node.TEXT_NODE){
                String value = node.getNodeValue();
                contentHandler.characters(value.toCharArray(), 0, value.length());
            } else {
                try {
                    IncludeXMLConsumer filter = new IncludeXMLConsumer(contentHandler, lexicalHandler);
                    Transformer transformer = FACTORY.newTransformer();
                    DOMSource source = new DOMSource(node);
                    SAXResult result = new SAXResult(filter);
                    result.setLexicalHandler(filter);
                    transformer.transform(source, result);
                } catch (TransformerConfigurationException e) {
                    throw new SAXException("TransformerConfigurationException", e);
                } catch (TransformerException e) {
                    throw new SAXException("TransformerException", e);
                }
            }
        }
    }

    /**
     * Control SAX event handling.
     * If set to <code>true</code> all empty characters events are ignored.
     * The default is <code>false</code>.
     */
    public void setIgnoreEmptyCharacters(boolean value) {
        this.ignoreEmptyCharacters = value;
    }

    /**
     * Control SAX event handling.
     * If set to <code>true</code> the root element is ignored.
     * The default is <code>false</code>.
     */
    public void setIgnoreRootElement(boolean value) {
        this.ignoreRootElement = value;
        this.ignoreRootElementCount = 0;
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
        if (this.ignoreRootElement == false ||
            this.ignoreRootElementCount > 0) {
            this.contentHandler.startElement(uri,local,qName,attr);
        }
        this.ignoreRootElementCount++;
    }

    public void endElement(String uri, String local, String qName) throws SAXException {
        this.ignoreRootElementCount--;
        if (!this.ignoreRootElement  || this.ignoreRootElementCount > 0) {
            this.contentHandler.endElement(uri, local, qName);
        }
    }

    public void characters(char[] ch, int start, int end) throws SAXException {
        if (this.ignoreEmptyCharacters) {
            String text = new String(ch, start, end).trim();
            if (text.length() > 0) {
                this.contentHandler.characters(text.toCharArray(), 0, text.length());
            }
        } else {
            this.contentHandler.characters(ch, start, end);
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int end) throws SAXException {
        if (!this.ignoreEmptyCharacters) {
            this.contentHandler.ignorableWhitespace(ch, start, end);
        }
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
