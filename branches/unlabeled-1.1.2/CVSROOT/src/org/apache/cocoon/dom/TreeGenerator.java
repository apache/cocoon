/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.dom;

import org.apache.cocoon.XMLConsumer;
import org.apache.cocoon.dom.DocumentFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.AttributeList;
import org.xml.sax.DocumentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Create a DOM tree from a serie of SAX events.
 * 
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-07 15:35:36 $
 */
public class TreeGenerator implements XMLConsumer {
    /** The current Document Factory */
    private DocumentFactory fac=null;
    /** The current Document */
    private Document doc=null;
    /** The current Node */
    private Node cur=null;
    /** The current Locator */
    private Locator loc=null;
    /** The current DocumentListener */
    private Listener lis=null;

    /**
     * Construct a new instance of this TreeGenerator.
     */
    public TreeGenerator() {
        this(null,null);
    }

    /**
     * Construct a new instance of this TreeGenerator.
     */
    public TreeGenerator(DocumentFactory fac) {
        this(fac,null);
    }

    /**
     * Construct a new instance of this TreeGenerator.
     */
    public TreeGenerator(Listener lis) {
        this(null,lis);
    }

    /**
     * Construct a new instance of this TreeGenerator.
     */
    public TreeGenerator(DocumentFactory fac, Listener lis) {
        super();
        this.setDocumentFactory(fac);
        this.setListener(lis);
    }

    /**
     * Set the DocumentFactory that will be used for building.
     */
    public void setDocumentFactory(DocumentFactory fac) {
        this.fac=fac;
    }

    /**
     * Return the DocumentFactory used for building.
     */
    public DocumentFactory getDocumentFactory() {
        return(this.fac);
    }

    /**
     * Return the newly built Document.
     */
    public Document document() {
        return(this.doc);
    }

    /**
     * Set the DocumentListener that will be notified when the Document
     * is successfully built.
     */
    public void setListener(Listener lis) {
        this.lis=lis;
    }

    /**
     * Get the current DocumentListener.
     */
    public Listener getListener() {
        return(this.lis);
    }

    /**
     * Set the SAX Document Locator.
     *
     * @param loc The SAX Locator.
     */
    public void setDocumentLocator (Locator loc) {
        this.loc=loc;
    }

    /**
     * Receive notification of the beginning of a document.
     *
     * @exception SAXException If this method was not called appropriately.
     */
    public void startDocument ()
    throws SAXException {
        if (this.fac==null)
            throw new SAXException("Document factory not specified");
        if (this.cur!=null)
            throw new SAXException("Document already started "+getLocation());
    }

    /**
     * Receive notification of the beginning of a document.
     *
     * @exception SAXException If this method was not called appropriately.
     */
    public void endDocument ()
    throws SAXException {
        if (this.cur!=this.doc)
            throw new SAXException("Current node differs from document "+
                                   getLocation());
        this.cur=null;
        if (this.lis!=null) this.lis.notify(this.doc);
    }

    /**
     * Receive notification of the beginning of an element.
     *
     * @param name The element tag name.
     * @param atts The element attributes list.
     * @exception SAXException If this method was not called appropriately.
     */
    public void startElement (String name, AttributeList atts)
    throws SAXException {
        if (this.doc==null) {
            this.doc=this.fac.newDocument(name);
            this.cur=this.doc;
        } else if (this.cur==null)
            throw new SAXException("No current node "+getLocation());
        Element e=this.doc.createElement(name);
        for(int x=0;x<atts.getLength();x++)
            e.setAttribute(atts.getName(x),atts.getValue(x));
        this.cur.appendChild(e);
        this.cur=e;
    }

    /**
     * Receive notification of the end of an element.
     *
     * @param name The element tag name.
     * @exception SAXException If this method was not called appropriately.
     */
    public void endElement (String name)
    throws SAXException {
        // Check if we have a current node.
        if (this.cur==null)
            throw new SAXException("No current node "+getLocation());
        // Check if the current node is an element
        if (this.cur.getNodeType()!=Node.ELEMENT_NODE)
            throw new SAXException("Current node is not an element "+
                                   getLocation());
        // Check if the current element has the same tag name of this event
        Element e=(Element)this.cur;
        String oldname=e.getTagName();
        if (!oldname.equals(name))
            throw new SAXException("Current element <"+oldname+
                                   "> differs from current event </"+name+"> "+
                                   getLocation());
        this.cur=this.cur.getParentNode();
        // If we have no parent element, throw an exception
        if (this.cur==null)
            throw new SAXException("No parent node "+getLocation());
    }

    /**
     * Receive notification of character data.
     *
     * @param chars The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     * @exception SAXException If this method was not called appropriately.
     */
    public void characters (char chars[], int start, int len)
    throws SAXException {
        if (this.cur==null)
            throw new SAXException("No current node "+getLocation());
        Text t=this.doc.createTextNode(new String(chars,start,len));
        this.cur.appendChild(t);
    }

    /**
     * Receive notification of ignorable whitespace data.
     *
     * @param chars The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     * @exception SAXException If this method was not called appropriately.
     */
    public void ignorableWhitespace (char chars[], int start, int len)
    throws SAXException {
        if (this.cur==null)
            throw new SAXException("No current node "+getLocation());
        Text t=this.doc.createTextNode(new String(chars,start,len));
        this.cur.appendChild(t);
    }

    /**
     * Receive notification of a processing instruction.
     *
     * @param tget The processing instruction target.
     * @param data The processing instruction data.
     * @exception SAXException If this method was not called appropriately.
     */
    public void processingInstruction (String tgt, String data)
    throws SAXException {
        if (this.cur==null)
            throw new SAXException("No current node "+getLocation());
        ProcessingInstruction p=this.doc.createProcessingInstruction(tgt,data);
        this.cur.appendChild(p);
    }

    /** Create a location string */
    private String getLocation() {
        if (this.loc==null) return("(Unknown Location)");
        else return new String("("+this.loc.getPublicId()+" line "+
                               this.loc.getLineNumber()+" column "+
                               this.loc.getColumnNumber()+")");
    }

    /**
     * The Listener interface must be implemented by those objects willing to
     * be notified of a successful DOM tree generation.
     */
    public static interface Listener {
        /**
         * Receive notification of a successfully completed DOM tree generation.
         */
        public void notify(Document doc)
        throws SAXException;
    }
}
