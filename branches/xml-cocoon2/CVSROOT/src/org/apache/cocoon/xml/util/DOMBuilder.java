/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.xml.util;

import java.util.Vector;
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
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * The <code>DOMBuilder</code> is a utility class that will generate a W3C
 * DOM Document from SAX events.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-02-27 01:33:12 $
 */
public class DOMBuilder implements XMLConsumer {
    /** The document was not started */
    private static final int S_AVAIL=0;
    /** State between startDTD() and endDTD() */
    private static final int S_DTD=1;
    /** State between startDocument() and endDocument() */
    private static final int S_DOC=2;
    /** State between the first startElement() and the last endElement() */
    private static final int S_BODY=3;
    /** State between the first startElement() and the last endElement() */
    private static final int S_CDATA=4;
    /** The state names (used by Location to output better error messages */
    private static final String stateName[]={
        "Available", "DTD Processing", "Document", "Body", "CDATA Section"
    };
    
    /** The current state */
    private int state=S_AVAIL;
    /** The locator */
    private Locator locator=null;
    /** The listener */
    private Listener listener=null;
    /** The document factory */
    private DOMFactory factory=null;
    /** The namespaces table */
    private NamespacesTable namespaces=null;
    /** The current document */
    private Document document=null;
    /** The current node */
    private Node current=null;
    /** The document name (tag name of the root element) */
    private String name=null;
    /** The vector of namespaces declarations to include in the next element */
    private Vector undecl=new Vector();

    /**
     * Construct a new instance of this TreeGenerator.
     */
    public DOMBuilder(DOMFactory factory) {
        this(factory,null);
    }

    /**
     * Construct a new instance of this TreeGenerator.
     */
    public DOMBuilder(DOMFactory factory, Listener listener) {
        super();
        this.factory=factory;
        this.listener=listener;
    }

    /**
     * Return the newly built Document.
     */
    public Document getDocument() {
        return(this.document);
    }

    /**
     * Set the SAX Document Locator.
     *
     * @param loc The SAX Locator.
     */
    public void setDocumentLocator (Locator loc) {
        this.locator=loc;
    }

    /**
     * Receive notification of the beginning of a document.
     *
     * @exception SAXException If this method was not called appropriately.
     */
    public void startDocument()
    throws SAXException {
        if(state!=S_AVAIL) throw new SAXException("Invalid state"+location());
        // Create the namespaces table
        this.namespaces=new NamespacesTable();
        // Create a new Document empty document object
        this.document=this.factory.newDocument();
        // Set the current node
        this.current=this.document;
        // Do a state change
        state=S_DOC;
    }

    /**
     * Receive notification of the beginning of a document.
     *
     * @exception SAXException If this method was not called appropriately.
     */
    public void endDocument ()
    throws SAXException {
        if(state!=S_DOC) throw new SAXException("Invalid state"+location());
        // Check if the current element is the document
        if(this.document!=this.current)
            throw new SAXException("Invalid current node"+location());
        // Reset the current node and the document name
        this.current=null;
        this.name=null;
        // Do a state change and reset the DTD flag
        state=S_AVAIL;
        // Notify the listener
        if (this.listener!=null) this.listener.notify(this.document);
    }

    /**
     * Report the start of DTD declarations, if any. 
     *
     * @param name The document type name.
     * @param publicId The declared public identifier for the external DTD
     *                 subset, or null if none was declared.
     * @param systemId The declared system identifier for the external DTD
     *                 subset, or null if none was declared.
     * @exception SAXException If this method was not called appropriately.
     */
    public void startDTD(String name, String publicId, String systemId) 
    throws SAXException {
        // This method can be called only at DOCUMENT level
        if(state!=S_DOC) throw new SAXException("Invalid state"+location());
        // Check wether this method was already invoked
        if(this.name!=null)
            throw new SAXException("Duplicate DTD definition"+location());
        // Remember the specified document name
        this.name=name;
        // Recreate the document element
        Document doc=this.factory.newDocument(name,publicId,systemId);
        // Copy the old document root PIs
        NodeList list=this.document.getChildNodes();
        for (int x=0; x<list.getLength(); x++) {
            if (list.item(x).getNodeType()!=Node.DOCUMENT_TYPE_NODE)
                doc.appendChild(doc.importNode(list.item(x),true));
        }
        // Declare the new document as the new real document
        this.document=doc;
        this.current=this.document;
        // Do a state change
        state=S_DTD;
    }

    /**
     * Report the end of DTD declarations. 
     *
     * @param chars The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     * @exception SAXException If this method was not called appropriately.
     */
    public void endDTD() 
    throws SAXException {
        // This method can be called only at DTD level
        if(state!=S_DTD) throw new SAXException("Invalid state"+location());
        // Do a state change
        state=S_DOC;
    }

    /**
     * Receive notification of the beginning of an element.
     *
     * @parameter uri The Namespace URI, or the empty string if the element
     *                has no Namespace URI or if Namespace processing is not
     *                being performed.
     * @parameter loc The local name (without prefix), or the empty
     *                string if Namespace processing is not being
     *                performed.
     * @parameter raw The raw XML 1.0 name (with prefix), or the empty
     *                string if raw names are not available.
     * @parameter a The attributes attached to the element. If there are no
     *              attributes, it shall be an empty Attributes object.
     * @exception SAXException If this method was not called appropriately.
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        NamespacesTable.Name n=this.namespaces.resolve(uri,raw,null,loc);
        // Check if this is we are defining the document root element
        if(state==S_DOC) {
            // Check if the DTD was specified
            if (this.name!=null) {
                // Check that this root element is equal to the one specified
                // in the DTD
                if (!this.name.equals(n.getRawName()))
                    throw new SAXException("The name specified in the DTD '"+
                                          this.name+"' differs from the root "+
                                          "element name '"+n.getRawName()+
                                          "'"+location());
            // Recreate the document since no DTD was specified
            } else {
                // Recreate the document element
                Document doc=this.factory.newDocument(n.getRawName());
                // Copy the old document root PIs
                NodeList list=this.document.getChildNodes();
                for (int x=0; x<list.getLength(); x++) {
                    if (list.item(x).getNodeType()!=Node.DOCUMENT_TYPE_NODE)
                        doc.appendChild(doc.importNode(list.item(x),true));
                }
                // Declare the new document as the new real document
                this.document=doc;
                this.current=this.document;
            }
            // Change the state before continuing
            state=S_BODY;
        }
        // Now that we initialized the root element we can perform the standard
        // element check
        if(state!=S_BODY) throw new SAXException("Invalid state"+location());
        // Create the Element node
        Element e=this.document.createElementNS(n.getUri(),n.getRawName());
        // Process all attributes
        for(int x=0;x<a.getLength();x++) {
            String auri=a.getURI(x);
            String aloc=a.getLocalName(x);
            String araw=a.getRawName(x);
            String aval=a.getValue(x);
            NamespacesTable.Name k=this.namespaces.resolve(auri,araw,null,aloc);
            // Set the attribute into the element
            auri=k.getPrefix().length()==0?"":k.getUri();
            e.setAttributeNS(auri,k.getRawName(),aval);
        }
        // Append the xmlns... attributes
        if (this.undecl.size()>0) {
            for (int x=0; x<this.undecl.size(); x++) {
                NamespacesTable.Declaration dec=null;
                dec=(NamespacesTable.Declaration)this.undecl.elementAt(x);
                String aname="xmlns";
                if (dec.getPrefix().length()>0) aname="xmlns:"+dec.getPrefix();
                e.setAttribute(aname,dec.getUri());
            }
            this.undecl.clear();
        }
        // Append this element to the parent and declare it current
        this.current.appendChild(e);
        this.current=e;
    }

    /**
     * Receive notification of the end of an element.
     *
     * @parameter uri The Namespace URI, or the empty string if the element
     *                has no Namespace URI or if Namespace processing is not
     *                being performed.
     * @parameter local The local name (without prefix), or the empty
     *                  string if Namespace processing is not being
     *                  performed.
     * @parameter raw The raw XML 1.0 name (with prefix), or the empty
     *                string if raw names are not available.
     * @exception SAXException If this method was not called appropriately.
     */
    public void endElement (String uri, String loc, String raw)
    throws SAXException {
        if(state!=S_BODY) throw new SAXException("Invalid state"+location());

        // Check if the current node is an element
        if (this.current.getNodeType()!=Node.ELEMENT_NODE)
            throw new SAXException("Current node is not an element"+location());

        // Check if the current element has the same tag name of this event
        NamespacesTable.Name n=this.namespaces.resolve(uri,raw,null,loc);
        String oldname=((Element)this.current).getTagName();
        if (!oldname.equals(n.getRawName()))
            throw new SAXException("Element end tag name '"+n.getRawName()+
                                   "' differs from start tag name '"+
                                   oldname+"'"+location());
        // Restore the old node as current
        this.current=this.current.getParentNode();
        if (this.current==null) throw new SAXException("No parent"+location());
        // Update the state if the current node is the document
        if (this.current==this.document) state=S_DOC;
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping. 
     *
     * @param pre The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     * @exception SAXException If this method was not called appropriately.
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        // This method can only called at DOCUMENT or BODY levels
        if((state<S_DOC)||(state>S_BODY))
            throw new SAXException("Invalid state"+location());
        // Insert this namespace in tables avoiding duplicates
        this.undecl.addElement(this.namespaces.addDeclaration(prefix,uri));
    }

    /**
     * End the scope of a prefix-URI mapping. 
     *
     * @param prefix The Namespace prefix that was being mapped.
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        // This method can only called at DOCUMENT or BODY levels
        if((state<S_DOC)||(state>S_BODY))
            throw new SAXException("Invalid state"+location());
        // Check if the namespace we're asked to remove was declared
        if (this.namespaces.removeDeclaration(prefix)==null)
            throw new SAXException("Prefix \""+prefix+"\" never declared"+
                                   location());
    }

    /**
     * Report the start of a CDATA section. 
     *
     * @exception SAXException If this method was not called appropriately.
     */
    public void startCDATA() 
    throws SAXException {
        // This method can only called at BODY level
        if(state!=S_BODY) throw new SAXException("Invalid state"+location());
        CDATASection cdata=this.document.createCDATASection("");
        // Set the CDATASection as the current element
        this.current.appendChild(cdata);
        this.current=cdata;
        // Do a state change        
        state=S_CDATA;
    }

    /**
     * Report the end of a CDATA section. 
     *
     * @exception SAXException If this method was not called appropriately.
     */
    public void endCDATA() 
    throws SAXException {
        // This method can only called at BODY level
        if(state!=S_CDATA) throw new SAXException("Invalid state"+location());
        // Set the parent of the CDATASection as the current element
        // We don't need to check the node type because in CDATA state the 
        // current element can be ONLY a CDATASection node
        this.current=this.current.getParentNode();
        if (this.current==null) throw new SAXException("No parent"+location());
        // Do a state change, and revert to the BODY state
        state=S_BODY;
    }

    /**
     * Report the beginning of an entity. 
     *
     * @param chars The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     * @exception SAXException If this method was not called appropriately.
     */
    public void startEntity(java.lang.String name) 
    throws SAXException {
        // This method can only called at BODY level
        if(state!=S_BODY) throw new SAXException("Invalid state"+location());
        // Update the current element with the entity reference node
        EntityReference eref=this.document.createEntityReference(name);
        this.current.appendChild(eref);
        this.current=eref;
    }

    /**
     * Report the end of an entity. 
     *
     * @param chars The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     * @exception SAXException If this method was not called appropriately.
     */
    public void endEntity(java.lang.String name) 
    throws SAXException {
        // This method can only called at BODY level
        if(state!=S_BODY) throw new SAXException("Invalid state"+location());

        // Check if the current node is an entity reference
        if (this.current.getNodeType()!=Node.ENTITY_REFERENCE_NODE)
            throw new SAXException("Current node is not an entity reference"+
                                   location());
        // Check if the current element has the same tag name of this event
        String oldname=((EntityReference)this.current).getNodeName();
        if (!oldname.equals(name))
            throw new SAXException("Entity reference closing name '"+name+"' "+
                                   "differs from start name '"+oldname+"'"+
                                   location());
        // Restore the old node as current
        this.current=this.current.getParentNode();
        if (this.current==null) throw new SAXException("No parent"+location());
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
        // This method can only called at BODY or CDATA levels
        if(state<S_BODY) throw new SAXException("Invalid state "+location());
        // Check if we are in the CDATA state
        String data=new String(chars,start,len);        
        if(state==S_CDATA) {
            ((CDATASection)this.current).appendData(data);
        } else {
            Text text=this.document.createTextNode(data);
            this.current.appendChild(text);
        }
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
        // This is because some parsers may report ignorable whitespace outside
        // the scope of the root element
        if(state==S_DOC) return;
        // This method can only called at BODY or CDATA levels
        if(state<S_BODY) throw new SAXException("Invalid state"+location());
        // Check if we are in the CDATA state
        if(state==S_CDATA)
            throw new SAXException("CDATA sections cannot contain ignorable "+
                                   "whitespace"+location());
        // Create and append a text node
        Text text=this.document.createTextNode(new String(chars,start,len));
        this.current.appendChild(text);
    }

    /**
     * Receive notification of a processing instruction.
     *
     * @param target The processing instruction target.
     * @param data The processing instruction data.
     * @exception SAXException If this method was not called appropriately.
     */
    public void processingInstruction (String target, String data)
    throws SAXException {
        // This is because Xerces reports processing instructions inside DTDs
        if(state==S_DTD) return;
        // This method can only called at DOCUMENT or BODY levels
        if((state<S_DOC)||(state>S_BODY))
            throw new SAXException("Invalid state"+location());
        // Create and append a processing instruction node
        ProcessingInstruction pi;
        pi=this.document.createProcessingInstruction(target,data);
        this.current.appendChild(pi);
    }

    /**
     * Report an XML comment anywhere in the document. 
     *
     * @param chars The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     * @exception SAXException If this method was not called appropriately.
     */
    public void comment(char chars[], int start, int len) 
    throws SAXException {
        // This is because Xerces reports comments inside DTDs
        if(state==S_DTD) return;
        // This method can only called at DOCUMENT or BODY levels
        if((state<S_DOC)||(state>S_BODY))
            throw new SAXException("Invalid state"+location());
        // Create and append a comment node
        Comment com=this.document.createComment(new String(chars,start,len));
        this.current.appendChild(com);
    }

    /**
     * Receive notification of a skipped entity. 
     *
     * @param name The name of the skipped entity. If it is a parameter entity,
     *             the name will begin with '%'.
     */
    public void skippedEntity(java.lang.String name)
    throws SAXException {
        // This method can only called at BODY level
        if(state!=S_BODY) throw new SAXException("Invalid state"+location());
        // Create and append a comment node
        EntityReference eref=this.document.createEntityReference(name);
        this.current.appendChild(eref);
    }

    /** Create a location string */
    private String location() {
        if (this.locator==null) return("");
        String pub=this.locator.getPublicId();
        String sys=this.locator.getSystemId();
        pub=((pub==null) ? "" : ("Public ID=\""+pub+"\" "));
        sys=((sys==null) ? "" : ("System ID=\""+sys+"\" "));
        int l=this.locator.getLineNumber();
        int c=this.locator.getColumnNumber();
        String lin=((l<0) ? "" : ("line="+l+""));
        String col=((c<0) ? "" : (" col="+c+""));
        return new String(" ("+sys+pub+lin+col+" State: "+
                          stateName[this.state]+")");
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
