/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.xml;

import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

import org.apache.avalon.Loggable;
import org.apache.log.Logger;
import org.apache.avalon.Recyclable;

/**
 * This abstract class provides default implementation of the methods specified
 * by the <code>XMLProducer</code> interface.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2001-02-12 13:30:47 $
 */
public abstract class AbstractXMLProducer implements XMLProducer, Loggable, Recyclable {

    protected Logger log;

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
        this.contentHandler = consumer;
        this.lexicalHandler = consumer;
    }

    public void setLogger(Logger logger) {
        if (this.log == null) {
            this.log = logger;
        }
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
     *
     * @exception IllegalStateException If the <code>LexicalHandler</code> or
     *                                  the <code>XMLConsumer</code> were
     *                                  already set.
     */
    public void setLexicalHandler(LexicalHandler handler) {
        this.lexicalHandler = handler;
    }

    /**
     * Recycle the producer by removing references
     */
    public void recycle() {
        this.contentHandler = null;
        this.lexicalHandler = null;
    }
}
