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

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * This abstract class provides default implementation of the methods specified
 * by the <code>XMLProducer</code> interface.
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @version CVS $Id: AbstractXMLProducer.java,v 1.3 2004/03/18 18:33:13 joerg Exp $
 */
public abstract class AbstractXMLProducer
extends AbstractLogEnabled
implements XMLProducer, Recyclable {

    /** The <code>XMLConsumer</code> receiving SAX events. */
    protected XMLConsumer xmlConsumer;

    /** The <code>ContentHandler</code> receiving SAX events. */
    protected ContentHandler contentHandler;

    /** The <code>LexicalHandler</code> receiving SAX events. */
    protected LexicalHandler lexicalHandler;

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     * <br>
     * This method will simply call <code>setContentHandler(consumer)</code>
     * and <code>setLexicalHandler(consumer)</code>.
     */
    public void setConsumer(XMLConsumer consumer) {
        this.xmlConsumer = consumer;
        setContentHandler(consumer);
        setLexicalHandler(consumer);
    }

    /**
     * Set the <code>ContentHandler</code> that will receive XML data.
     * <br>
     * Subclasses may retrieve this <code>ContentHandler</code> instance
     * accessing the protected <code>super.contentHandler</code> field.
     */
    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }

    /**
     * Set the <code>LexicalHandler</code> that will receive XML data.
     * <br>
     * Subclasses may retrieve this <code>LexicalHandler</code> instance
     * accessing the protected <code>super.lexicalHandler</code> field.
     */
    public void setLexicalHandler(LexicalHandler handler) {
        this.lexicalHandler = handler;
    }

    /**
     * Recycle the producer by removing references
     */
    public void recycle() {
        this.xmlConsumer = null;
        this.contentHandler = null;
        this.lexicalHandler = null;
    }
}
