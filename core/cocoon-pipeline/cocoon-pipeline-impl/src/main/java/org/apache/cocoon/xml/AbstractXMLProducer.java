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

import org.apache.avalon.excalibur.pool.Recyclable;

import org.apache.cocoon.util.AbstractLogEnabled;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This abstract class provides default implementation of the methods specified
 * by the <code>XMLProducer</code> interface.
 *
 * @version $Id$
 */
public abstract class AbstractXMLProducer extends AbstractLogEnabled
                                          implements XMLProducer, Recyclable {

    /** Empty, do-nothing content handler */
    protected static final ContentHandler EMPTY_CONTENT_HANDLER = new DefaultHandler();

    /** The <code>XMLConsumer</code> receiving SAX events. */
    protected XMLConsumer xmlConsumer;

    /** The <code>ContentHandler</code> receiving SAX events. */
    protected ContentHandler contentHandler = EMPTY_CONTENT_HANDLER;

    /** The <code>LexicalHandler</code> receiving SAX events. */
    protected LexicalHandler lexicalHandler = DefaultLexicalHandler.NULL_HANDLER;

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     *
     * <p>This method will call <code>setContentHandler(consumer)</code>
     * and <code>setLexicalHandler(consumer)</code> since {@link XMLConsumer}
     * interface implements both {@link ContentHandler} and {@link LexicalHandler}.
     *
     * @param consumer xml consumer, should never be null.
     */
    public void setConsumer(XMLConsumer consumer) {
        this.xmlConsumer = consumer;
        setContentHandler(consumer);
        setLexicalHandler(consumer);
    }

    /**
     * Set the <code>ContentHandler</code> that will receive XML data.
     *
     * <p>Subclasses may retrieve this <code>ContentHandler</code> instance
     * accessing the protected <code>super.contentHandler</code> field.
     *
     * @param handler content handler, should never be null.
     */
    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }

    /**
     * Set the <code>LexicalHandler</code> that will receive XML data.
     *
     * <p>Subclasses may retrieve this <code>LexicalHandler</code> instance
     * accessing the protected <code>super.lexicalHandler</code> field.
     *
     * @param handler lexical handler, should never be null.
     */
    public void setLexicalHandler(LexicalHandler handler) {
        this.lexicalHandler = handler;
    }

    /**
     * Recycle the producer by removing references, and resetting handlers to
     * null (empty) implementations.
     */
    public void recycle() {
        this.xmlConsumer = null;
        this.contentHandler = EMPTY_CONTENT_HANDLER;
        this.lexicalHandler = DefaultLexicalHandler.NULL_HANDLER;
    }
}
