/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.xml.util;

import java.util.Vector;
import org.apache.cocoon.xml.AbstractXMLProducer;
import org.apache.cocoon.xml.XMLConsumer;
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
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The <code>DOMStreamer</code> is a utility class that will generate SAX
 * events from a W3C DOM Document.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-27 01:25:43 $
 */
public class DOMStreamer extends AbstractXMLProducer {

    /**
     * Create a new <code>DOMStreamer</code> instance.
     */
    public DOMStreamer() {
        super();
    }

    /**
     * Create a new <code>DOMStreamer</code> instance.
     */
    public DOMStreamer(XMLConsumer consumer) {
        this(consumer,consumer);
    }

    /**
     * Create a new <code>DOMStreamer</code> instance.
     */
    public DOMStreamer(ContentHandler content) {
        this(content,null);
    }

    /**
     * Create a new <code>DOMStreamer</code> instance.
     */
    public DOMStreamer(LexicalHandler lexical) {
        this(null,lexical);
    }

    /**
     * Create a new <code>DOMStreamer</code> instance.
     */
    public DOMStreamer(ContentHandler content, LexicalHandler lexical) {
        this();
        super.setContentHandler(content);
        super.setLexicalHandler(lexical);
    }

    /**
     * Start the production of SAX events.
     */
    public void produce(Document document)
    throws SAXException {
        processNode(document);
    }
    
    /** Process a generic node */
    private void processNode(Node n)
    throws SAXException {
        if (n==null) return;
        try { 
            switch (n.getNodeType()) {
                case Node.DOCUMENT_NODE:
                    this.setDocument((Document)n);
                    break;
                case Node.DOCUMENT_TYPE_NODE:
                    this.setDocumentType((DocumentType)n);
                    break;
                case Node.ELEMENT_NODE:
                    this.setElement((Element)n);
                    break;
                case Node.TEXT_NODE:
                    this.setText((Text)n);
                    break;
                case Node.CDATA_SECTION_NODE:
                    this.setCDATASection((CDATASection)n);
                    break;
                case Node.PROCESSING_INSTRUCTION_NODE:
                    this.setProcessingInstruction((ProcessingInstruction)n);
                    break;
                case Node.COMMENT_NODE:
                    this.setComment((Comment)n);
                    break;
                case Node.ENTITY_REFERENCE_NODE:
                    this.setEntityReference((EntityReference)n);
                    break;
                case Node.ENTITY_NODE:
                case Node.NOTATION_NODE:
                    // Do nothing for ENTITY and NOTATION nodes
                    break;
                case Node.DOCUMENT_FRAGMENT_NODE:
                    throw new SAXException("Unexpected Document Fragment node");
                case Node.ATTRIBUTE_NODE:
                    throw new SAXException("Unexpected Attribute node");
                default:
                    throw new SAXException("Unknown node type "+n.getNodeType()+
                                           " class "+n.getClass().getName());
            }
        } catch (ClassCastException e) {
            throw new SAXException("Error casting node to appropriate type");
        }
    }

    /** Process all children nodes of a Node */
    private void processChildren(Node n)
    throws SAXException {
        NodeList l=n.getChildNodes();
        for(int x=0;x<l.getLength();x++) processNode(l.item(x));
    }
    
    /** Process a Document node */
    private void setDocument(Document n)
    throws SAXException {
        if (super.contentHandler!=null) super.contentHandler.startDocument();
        this.processChildren(n);
        if (super.contentHandler!=null) super.contentHandler.endDocument();
    }
    
    /** Process a DocumentType node */
    private void setDocumentType(DocumentType n)
    throws SAXException {
        if (super.lexicalHandler==null) return;
        super.lexicalHandler.startDTD(n.getName(),n.getPublicId(),n.getSystemId());
        super.lexicalHandler.endDTD();
    }

    /** Process a Element node */
    private void setElement(Element n)
    throws SAXException {
        if (super.contentHandler==null) {
            this.processChildren(n);
            return;
        }
        // Setup attributes
        AttributesImpl atts=new AttributesImpl();
        NamedNodeMap map=n.getAttributes();
        Vector nslist=new Vector();
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
            if (raw.equals("xmlns") || raw.startsWith("xmlns:")) {
                String prefix="";
                if (raw.length()>5) prefix=raw.substring(6);
                nslist.addElement(prefix);
                super.contentHandler.startPrefixMapping(prefix,val);
            } else atts.addAttribute(uri,loc,raw,"CDATA",val);
        }
        // Get and normalize values for the Element
        String uri=n.getNamespaceURI(); uri=(uri==null)?"":uri;
        String pre=n.getPrefix();       pre=(pre==null)?"":pre;
        String loc=n.getLocalName();    loc=(loc==null)?"":loc;
        String raw=n.getTagName();      raw=(raw==null)?"":raw;
        super.contentHandler.startElement(uri,loc,raw,atts);
        this.processChildren(n);
        super.contentHandler.endElement(uri,loc,raw);
        // Rerun through attributes to check for namespaces we declared.
        // Should we store those before in, maybe, a hashtable?
        for (int x=0; x<nslist.size(); x++) {
            String prefix=(String)nslist.elementAt(x);
            super.contentHandler.endPrefixMapping(prefix);
        }
    }
    
    /** Process a Text node */
    private void setText(Text n)
    throws SAXException {
        char data[]=n.getData().toCharArray();
        if (super.contentHandler!=null)
            super.contentHandler.characters(data,0,data.length);
    }
    
    /** Process a CDATASection node */
    private void setCDATASection(CDATASection n)
    throws SAXException {
        if (super.lexicalHandler!=null) super.lexicalHandler.startCDATA();
        char data[]=n.getData().toCharArray();
        if (super.contentHandler!=null)
            super.contentHandler.characters(data,0,data.length);
        if (super.lexicalHandler!=null) super.lexicalHandler.endCDATA();
    }
    
    /** Process a ProcessingInstruction node */
    private void setProcessingInstruction(ProcessingInstruction n)
    throws SAXException {
        if (super.contentHandler==null) return;
        super.contentHandler.processingInstruction(n.getTarget(),n.getData());
    }
    
    /** Process a Comment node */
    private void setComment(Comment n)
    throws SAXException {
        if (super.lexicalHandler==null) return;
        char data[]=n.getData().toCharArray();
        super.lexicalHandler.comment(data,0,data.length);
    }
    
    /** Process a EntityReference node */
    private void setEntityReference(EntityReference n)
    throws SAXException {
        if (n.hasChildNodes()) {
            if (super.lexicalHandler==null) this.processChildren(n);
            else {
                super.lexicalHandler.startEntity(n.getNodeName());
                this.processChildren(n);
                super.lexicalHandler.endEntity(n.getNodeName());
            }
            return;
        } else {
            if (n.getNodeValue()==null) {
                if (super.contentHandler==null) return;
                else super.contentHandler.skippedEntity(n.getNodeName());
            } else {
                char value[]=n.getNodeValue().toCharArray();
                if (super.lexicalHandler!=null)
                    super.lexicalHandler.startEntity(n.getNodeName());
                if (super.contentHandler!=null)
                    super.contentHandler.characters(value,0,value.length);
                if (super.lexicalHandler!=null)
                    super.lexicalHandler.endEntity(n.getNodeName());
            }
        }
    }
}