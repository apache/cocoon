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
package org.apache.cocoon.xml.sax;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This abstract class provides utility methods to implement a producer
 * for SAX events..
 *
 * @version $Id$
 */
public abstract class AbstractSAXProducer  {

    /** Empty, do-nothing content handler */
    protected static final ContentHandler EMPTY_CONTENT_HANDLER = new DefaultHandler();

    /** The <code>ContentHandler</code> receiving SAX events. */
    protected ContentHandler contentHandler = EMPTY_CONTENT_HANDLER;

    /** The <code>LexicalHandler</code> receiving SAX events. */
    protected LexicalHandler lexicalHandler = DefaultLexicalHandler.NULL_HANDLER;

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
        if ( handler instanceof LexicalHandler ) {
            this.lexicalHandler = (LexicalHandler)handler;
        } else {
            this.lexicalHandler = DefaultLexicalHandler.NULL_HANDLER;
        }
    }

    /**
     * Recycle the producer by removing references, and resetting handlers to
     * null (empty) implementations.
     */
    public void recycle() {
        this.contentHandler = EMPTY_CONTENT_HANDLER;
        this.lexicalHandler = DefaultLexicalHandler.NULL_HANDLER;
    }
}
