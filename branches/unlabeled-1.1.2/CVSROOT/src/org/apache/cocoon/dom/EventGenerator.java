/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.dom;

import org.apache.cocoon.XMLProducer;
import org.apache.cocoon.XMLConsumer;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributeListImpl;

/**
 * Generates SAX events from a DOM Document.
 * <br>
 * NOTE (PF) This class need to be FINISHED... It's INCOMPLETE.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-07 15:35:36 $
 */
public class EventGenerator implements XMLProducer {
    /** The document for wich SAX events are to be generated */
    private Document doc=null;

    /**
     * Create a new EventGenerator instance.
     */
    public EventGenerator() {
        this(null);
    }

    /**
     * Create a new EventGenerator instance.
     */
    public EventGenerator(Document doc) {
        super();
        this.setDocument(doc);
    }

    /**
     * Set the Document that will be used for building.
     */
    public void setDocument(Document doc) {
        this.doc=doc;
    }

    /**
     * Return the Document after the tree has been built.
     */
    public Document getDocument() {
        return(this.doc);
    }

    /**
     * Trigger SAX events generated the given Document in the specified
     * XMLConsumer.
     */
    public void produce(XMLConsumer cons)
    throws SAXException {
        if (this.doc==null)
            throw new SAXException("Root document not specified");
        if (cons==null)
            throw new SAXException("XML data consumer not specified");
        processNode(this.doc,cons);
    }
    
    /** Process a generic node */
    private void processNode(Node n, XMLConsumer h)
    throws SAXException {
        if (n==null) return;
        switch (n.getNodeType()) {
            case Node.CDATA_SECTION_NODE:
                processText((Text)n,h);
                break;
            case Node.DOCUMENT_NODE:
                processDocument((Document)n,h);
                break;
            case Node.ELEMENT_NODE:
                processElement((Element)n,h);
                break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                processProcessingInstruction((ProcessingInstruction)n,h);
                break;
            case Node.TEXT_NODE:
                processText((Text)n,h);
                break;
            case Node.ATTRIBUTE_NODE:
            case Node.COMMENT_NODE:
            case Node.DOCUMENT_FRAGMENT_NODE:
            case Node.DOCUMENT_TYPE_NODE:
            case Node.ENTITY_NODE:
            case Node.ENTITY_REFERENCE_NODE:
            case Node.NOTATION_NODE:
                processChildren(n,h);
                break;
        }
    }

    /** Process a Text node */
    private void processText(Text t, XMLConsumer h)
    throws SAXException {
        char c[]=t.getData().toCharArray();
        h.characters(c,0,c.length);
    }

    /** Pricess an Element node */
    private void processElement(Element e, XMLConsumer h)
    throws SAXException {
        h.startElement(e.getTagName(),null);
        processChildren(e,h);
        h.endElement(e.getTagName());
    }

    /** Process a Document node */
    private void processDocument(Document d, XMLConsumer h)
    throws SAXException {
        h.startDocument();
        processChildren(d,h);
        h.endDocument();
    }

    /** Pricess a ProcessingInstruction node */
    private void processProcessingInstruction(ProcessingInstruction p,
                                              XMLConsumer h)
    throws SAXException {
        h.processingInstruction(p.getTarget(),p.getData());
    }

    /** Process all children nodes of a Node */
    private void processChildren(Node n, XMLConsumer h)
    throws SAXException {
        NodeList l=n.getChildNodes();
        for(int x=0;x<l.getLength();x++) processNode(l.item(x),h);
    }
}