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

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * This class implements a ContentHandler for embedding a full SAX
 * event stream into an existing stream of SAX events. An instance of
 * this class will pass unmodified all the SAX events to the linked
 * ContentHandler, but it will ignore the startDocument/endDocument
 * and startDTD/endDTD events, as well as all comment events within
 * the DTD.
 *
 * @version $Id$
 */
public class EmbeddedXMLPipe extends AbstractXMLPipe {

    private boolean inDTD;

    /**
     * Creates an EmbeddedXMLPipe that writes into the given XMLConsumer.
     */
    public EmbeddedXMLPipe(XMLConsumer consumer) {
        setConsumer(consumer);
    }

    /**
     * Creates an EmbeddedXMLPipe that writes into the given ContentHandler.
     */
    public EmbeddedXMLPipe(ContentHandler handler) {
        setContentHandler(handler);
        if (handler instanceof LexicalHandler) {
            setLexicalHandler((LexicalHandler) handler);
        }
    }

    /**
     * Creates an EmbeddedXMLPipe that writes into the given ContentHandler.
     */
    public EmbeddedXMLPipe(ContentHandler contentHandler, LexicalHandler lexicalHandler) {
        setContentHandler(contentHandler);
        setLexicalHandler(lexicalHandler);
    }

    /**
     * Ignore the <code>startDocument</code> event: this method does nothing.
     *
     * @exception SAXException if an error occurs
     */
    public void startDocument() throws SAXException {
    }

    /**
     * Ignore the <code>endDocument</code> event: this method does nothing.
     *
     * @exception SAXException if an error occurs
     */
    public void endDocument() throws SAXException {
    }

    /**
     * Ignore the <code>startDTD</code> event: this method does nothing.
     *
     * @exception SAXException if an error occurs
     */
    public void startDTD(String name, String publicId, String systemId)
    throws SAXException {
        // Ignored
        this.inDTD = true;
    }

    /**
     * Ignore the <code>endDTD</code> event: this method does nothing.
     *
     * @exception SAXException if an error occurs
     */
    public void endDTD() throws SAXException {
        // Ignored
        this.inDTD = false;
    }

    /**
     * Ignore all <code>comment</code> events if between
     * startDTD/endDTD events.
     *
     * @exception SAXException if an error occurs
     */
    public void comment(char ch[], int start, int len)
    throws SAXException {
        if (!inDTD) {
            super.comment(ch, start, len);
        }
    }
}
