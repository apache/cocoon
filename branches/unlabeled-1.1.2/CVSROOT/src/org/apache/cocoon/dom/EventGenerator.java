/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.dom;

import org.apache.cocoon.sax.XMLProducer;
import org.apache.cocoon.sax.XMLConsumer;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The <code>EventGenerator</code> is a utility class that will generate SAX
 * events from a DOM Document.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2000-02-10 05:04:53 $
 * @since Cocoon 2.0
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
        try { 
            switch (n.getNodeType()) {
                case Node.DOCUMENT_NODE:
                    this.setDocument((Document)n,h);
                    break;
                case Node.DOCUMENT_TYPE_NODE:
                    this.setDocumentType((DocumentType)n,h);
                    break;
                case Node.ELEMENT_NODE:
                    this.setElement((Element)n,h);
                    break;
                case Node.TEXT_NODE:
                    this.setText((Text)n,h);
                    break;
                case Node.CDATA_SECTION_NODE:
                    this.setCDATASection((CDATASection)n,h);
                    break;
                case Node.PROCESSING_INSTRUCTION_NODE:
                    this.setProcessingInstruction((ProcessingInstruction)n,h);
                    break;
                case Node.COMMENT_NODE:
                    this.setComment((Comment)n,h);
                    break;
                case Node.ENTITY_REFERENCE_NODE:
                    this.setEntityReference((EntityReference)n,h);
                    break;
                case Node.ATTRIBUTE_NODE:
                    throw new SAXException("Unexpected Attribute node");
                case Node.DOCUMENT_FRAGMENT_NODE:
                    throw new SAXException("Unexpected Document Fragment node");
                case Node.ENTITY_NODE:
                    throw new SAXException("Unexpected Entity node");
                case Node.NOTATION_NODE:
                    throw new SAXException("Unexpected Notation node");
            }
        } catch (ClassCastException e) {
            throw new SAXException("Error casting node to appropriate type");
        }
    }

    /** Process all children nodes of a Node */
    private void processChildren(Node n, XMLConsumer h)
    throws SAXException {
        NodeList l=n.getChildNodes();
        for(int x=0;x<l.getLength();x++) processNode(l.item(x),h);
    }
    
    /** Process a Document node */
    private void setDocument(Document n, XMLConsumer h)
    throws SAXException {
        h.startDocument();
        this.processChildren(n,h);
        h.endDocument();
    }
    
    /** Process a DocumentType node */
    private void setDocumentType(DocumentType n, XMLConsumer h)
    throws SAXException {
        h.startDTD(n.getName(),n.getPublicId(),n.getSystemId());
        h.endDTD();
    }

    /** Process a Element node */
    private void setElement(Element n, XMLConsumer h)
    throws SAXException {
        // Setup attributes
        AttributesImpl atts=new AttributesImpl();
        NamedNodeMap map=n.getAttributes();
        for (int x=0; x<map.getLength(); x++) {
            if (map.item(x).getNodeType()!=Node.ATTRIBUTE_NODE) continue;
            Attr a=(Attr)map.item(x);
            // Start getting and normalizing the values from the attribute
            String uri=a.getNamespaceURI(); uri=(uri==null)?"":uri;
            String pre=a.getPrefix();       pre=(pre==null)?"":pre;
            String loc=a.getLocalName();    loc=(loc==null)?"":loc;
            String raw=a.getName();         raw=(raw==null)?"":raw;
            String val=a.getValue();        val=(val==null)?"":val;
            // Check if we need to declare the start of a namespace prefix
            // Should we rely on URI instead of prefixes???
            if(pre.equals("xmlns")) {
                if ((loc.length()==0)||(val.length()==0))
                    throw new SAXException("Invalid xmlns: attribute");
                h.startPrefixMapping(loc,val);
            }
            atts.addAttribute(uri,loc,raw,"CDATA",val);
        }
        // Get and normalize values for the Element
        String uri=n.getNamespaceURI(); uri=(uri==null)?"":uri;
        String pre=n.getPrefix();       pre=(pre==null)?"":pre;
        String loc=n.getLocalName();    loc=(loc==null)?"":loc;
        String raw=n.getTagName();      raw=(raw==null)?"":raw;
        h.startElement(uri,loc,raw,atts);
        this.processChildren(n,h);
        h.endElement(uri,loc,raw);
        // Rerun through attributes to check for namespaces we declared.
        // Should we store those before in, maybe, a hashtable?
        for (int x=0; x<map.getLength(); x++) {
            if (map.item(x).getNodeType()!=Node.ATTRIBUTE_NODE) continue;
            Attr a=(Attr)map.item(x);
            String apre=a.getPrefix();       apre=(apre==null)?"":apre;
            String aloc=a.getLocalName();    aloc=(aloc==null)?"":aloc;
            if(apre.equals("xmlns")) h.endPrefixMapping(aloc);
        }
    }
    
    /** Process a Text node */
    private void setText(Text n, XMLConsumer h)
    throws SAXException {
        char data[]=n.getData().toCharArray();
        h.characters(data,0,data.length);
    }
    
    /** Process a CDATASection node */
    private void setCDATASection(CDATASection n, XMLConsumer h)
    throws SAXException {
        h.startCDATA();
        char data[]=n.getData().toCharArray();
        h.characters(data,0,data.length);
        h.endCDATA();
    }
    
    /** Process a ProcessingInstruction node */
    private void setProcessingInstruction(ProcessingInstruction n, XMLConsumer h)
    throws SAXException {
        h.processingInstruction(n.getTarget(),n.getData());
    }
    
    /** Process a Comment node */
    private void setComment(Comment n, XMLConsumer h)
    throws SAXException {
        char data[]=n.getData().toCharArray();
        h.characters(data,0,data.length);
    }
    
    /** Process a EntityReference node */
    private void setEntityReference(EntityReference n, XMLConsumer h)
    throws SAXException {
        h.startEntity(n.getNodeName());
        this.processChildren(n,h);
        h.endEntity(n.getNodeName());
    }
}