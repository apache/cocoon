/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: DOMBuilder.java,v 1.3 2003/12/06 21:22:09 cziegeler Exp $
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
     * @deprecated Use DOMBuilder() instead.
     */
    public DOMBuilder(DOMFactory factory) {
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
     * @deprecated Use DOMBuilder(listener) instead.
     */
    public DOMBuilder( DOMFactory factory, Listener listener ) {
        this(listener, null);
    }

    /**
     * Construct a new instance of this DOMBuilder.
     * @deprecated Use DOMBuilder(listener, parentNode) instead.
     */
    public DOMBuilder( DOMFactory domFactory, Listener listener, Node parentNode ) {
        this(listener, parentNode);
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
        this(null, null, parentNode);
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
