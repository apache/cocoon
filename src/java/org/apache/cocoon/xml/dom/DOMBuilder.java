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
package org.apache.cocoon.xml.dom;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

/**
 * The <code>DOMBuilder</code> is a utility class that will generate a W3C
 * DOM Document from SAX events.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: DOMBuilder.java,v 1.5 2004/04/01 12:29:47 cziegeler Exp $
 */
public class DOMBuilder
extends AbstractXMLPipe {

    /** The transformer factory shared by all instances */
    protected static final SAXTransformerFactory factory = (SAXTransformerFactory)SAXTransformerFactory.newInstance();

    /** The listener */
    protected Listener listener;

    /** The result */
    protected DOMResult result;

    /** The parentNode */
    protected Node parentNode;

    /**
     * Construct a new instance of this DOMBuilder.
     */
    public DOMBuilder() {
        this( (Listener)null, (Node)null );
    }

    /**
     * Construct a new instance of this DOMBuilder.
     */
    public DOMBuilder( Listener listener ) {
        this(listener, null);
    }

    /**
     * Construct a new instance of this DOMBuilder.
     */
    public DOMBuilder( Listener listener, Node parentNode ) {
        super();
        this.listener = listener;
        try {
            TransformerHandler handler = factory.newTransformerHandler();
            this.setContentHandler(handler);
            this.setLexicalHandler(handler);
            this.parentNode = parentNode;
            if (parentNode != null) {
                this.result = new DOMResult( parentNode );
            } else {
                this.result = new DOMResult();
            }
            handler.setResult(this.result);
        } catch (javax.xml.transform.TransformerException local) {
            throw new CascadingRuntimeException("Fatal-Error: Unable to get transformer handler", local);
        }
    }

    /**
     * Constructs a new instance that appends nodes to the given parent node.<br/>
     * Note : you cannot use a <code>Listener<code> when appending to a
     * <code>Node</code>, because the notification occurs at <code>endDocument()</code>
     * which does not happen here.
     */
    public DOMBuilder( Node parentNode ) {
        this(null, parentNode);
    }

    /**
     * Recycling
     */
    public void recycle() {
        super.recycle();

        try {
            TransformerHandler handler = factory.newTransformerHandler();
            this.setContentHandler(handler);
            this.setLexicalHandler(handler);
            if (this.parentNode != null) {
                this.result = new DOMResult(this.parentNode);
            } else {
                this.result = new DOMResult();
            }
            handler.setResult(this.result);
        } catch (javax.xml.transform.TransformerException local) {
            throw new CascadingRuntimeException("Fatal-Error: Unable to get transformer handler", local);
        }
    }

    /**
     * Return the newly built Document.
     */
    public Document getDocument() {
        if ((this.result == null) || (this.result.getNode()==null))  {
            return null;
        } else if (this.result.getNode().getNodeType() == Node.DOCUMENT_NODE) {
            return ( (Document)this.result.getNode() );
        } else {
            return ( this.result.getNode().getOwnerDocument() );
        }
    }

    /**
     * Receive notification of the beginning of a document.
     *
     * @exception SAXException If this method was not called appropriately.
     */
    public void endDocument()
    throws SAXException {
        super.endDocument();

        // Notify the listener
        this.notifyListener();
    }

    /**
     * Receive notification of a successfully completed DOM tree generation.
     */
    protected void notifyListener()
    throws SAXException {
        if ( this.listener != null ) this.listener.notify( this.getDocument() );
    }

    /**
     * The Listener interface must be implemented by those objects willing to
     * be notified of a successful DOM tree generation.
     */
    public interface Listener {

        /**
         * Receive notification of a successfully completed DOM tree generation.
         */
        void notify(Document doc)
        throws SAXException;
    }
}
