/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.dom;

import java.util.Hashtable;
import org.apache.cocoon.dom.DocumentFactory;
import org.apache.cocoon.sax.XMLConsumer;
import org.w3c.dom.*;
import org.xml.sax.Attributes;
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
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-02-10 05:04:53 $
 */
public class TreeGenerator implements XMLConsumer {
    /** The Namespaces table */
    private Hashtable namespaces=null;
    /** The Namespaces reversed table */
    private Hashtable namespacesReverse=null;
    /** The CDATA buffer */
    private String cdata=null;
    /** The document name (root tag element name) */
    private String name=null;
    /** The document public ID */
    private String publicId=null;
    /** The document system ID */
    private String systemId=null;
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
    /** The current DTD initialization status */
    private boolean dtd=false;

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
        this.namespaces=new Hashtable();
        this.namespacesReverse=new Hashtable();
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
     * Begin the scope of a prefix-URI Namespace mapping. 
     *
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     * @exception SAXException If this method was not called appropriately.
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        this.namespaces.put(uri,prefix);
        this.namespacesReverse.put(prefix,uri);
    }

    /**
     * End the scope of a prefix-URI mapping. 
     *
     * @param prefix The Namespace prefix that was being mapped.
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        String uri=(String)this.namespacesReverse.remove(prefix);
        if (uri==null) 
            throw new SAXException("Namespace \""+prefix+"\" never declared");
        else this.namespaces.remove(uri);
    }

    /**
     * Receive notification of the beginning of an element.
     * <br>
     * NOTE: (PF) 
     *
     * @parameter uri The Namespace URI, or the empty string if the element
     *                has no Namespace URI or if Namespace processing is not
     *                being performed.
     * @parameter local The local name (without prefix), or the empty
     *                  string if Namespace processing is not being
     *                  performed.
     * @parameter raw The raw XML 1.0 name (with prefix), or the empty
     *                string if raw names are not available.
     * @parameter atts The attributes attached to the element. If there are no
     *                 attributes, it shall be an empty Attributes object.
     * @exception SAXException If this method was not called appropriately.
     */
    public void startElement (String uri, String local, String raw,
                              Attributes atts)
    throws SAXException {
        // Check for CDATA
        if(this.cdata!=null)
            throw new SAXException("Invalid inside CDATA"+getLocation());

        if (this.doc==null) {
            this.doc=this.fac.newDocument(name);
            this.cur=this.doc;
        } else if (this.cur==null)
            throw new SAXException("No current node "+getLocation());

        String name=this.getQualifiedName(uri,local,raw);
        Element e=null;
        if (uri.length()>0) e=this.doc.createElement(name);
        else e=this.doc.createElementNS(uri,name);

        for(int x=0;x<atts.getLength();x++) {
            String auri=atts.getURI(x);
            String alocal=atts.getLocalName(x);
            String araw=atts.getRawName(x);
            String avalue=atts.getValue(x);
            String aname=this.getQualifiedName(auri,alocal,araw);
            if (auri.length()>0) e.setAttribute(aname,avalue);
            else e.setAttributeNS(auri,aname,avalue);
        }
        this.cur.appendChild(e);
        this.cur=e;
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
     * @parameter atts The attributes attached to the element. If there are no
     *                 attributes, it shall be an empty Attributes object.
     * @exception SAXException If this method was not called appropriately.
     */
    public void endElement (String uri, String local, String raw)
    throws SAXException {
        // Check for CDATA
        if(this.cdata!=null)
            throw new SAXException("Invalid inside CDATA"+getLocation());

        // Check if we have a current node.
        if (this.cur==null)
            throw new SAXException("No current node "+getLocation());
        // Check if the current node is an element
        if (this.cur.getNodeType()!=Node.ELEMENT_NODE)
            throw new SAXException("Current node is not an element "+
                                   getLocation());
        // Check if the current element has the same tag name of this event
        Element e=(Element)this.cur;
        String name=this.getQualifiedName(uri,local,raw);
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

        // Check for CDATA
        if(this.cdata!=null) {
            this.cdata=new String(this.cdata+new String(chars,start,len));
        } else {
            Text t=this.doc.createTextNode(new String(chars,start,len));
            this.cur.appendChild(t);
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
        // Check for CDATA
        if(this.cdata!=null)
            throw new SAXException("Invalid inside CDATA"+getLocation());

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
        // Check for CDATA
        if(this.cdata!=null)
            throw new SAXException("Invalid inside CDATA"+getLocation());

        if (this.cur==null)
            throw new SAXException("No current node "+getLocation());
        ProcessingInstruction p=this.doc.createProcessingInstruction(tgt,data);
        this.cur.appendChild(p);
    }

    /**
     * Receive notification of a skipped entity. 
     *
     * @param name The name of the skipped entity. If it is a parameter entity,
     *             the name will begin with '%'.
     */
    public void skippedEntity(java.lang.String name)
    throws SAXException {
        // Check for CDATA
        if(this.cdata!=null)
            throw new SAXException("Invalid inside CDATA"+getLocation());

        if (this.cur==null)
            throw new SAXException("No current node "+getLocation());
        EntityReference e=this.doc.createEntityReference(name);
        this.cur.appendChild(e);
    }

    /**
     * Report an XML comment anywhere in the document. 
     *
     * @param chars The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     * @exception SAXException If this method was not called appropriately.
     */
    public void comment(char[] ch, int start, int len) 
    throws SAXException {
        // Check for CDATA
        if(this.cdata!=null)
            throw new SAXException("Invalid inside CDATA"+getLocation());

        if (this.cur==null)
            throw new SAXException("No current node "+getLocation());
        Comment c=this.doc.createComment(new String(ch,start,len));
        this.cur.appendChild(c);
    }

    /**
     * Report the start of a CDATA section. 
     *
     * @exception SAXException If this method was not called appropriately.
     */
    public void startCDATA() 
    throws SAXException {
        if(this.cdata!=null)
            throw new SAXException("Nested CDATA in CDATA "+getLocation());
        this.cdata="";
    }

    /**
     * Report the end of a CDATA section. 
     *
     * @exception SAXException If this method was not called appropriately.
     */
    public void endCDATA() 
    throws SAXException {
        if(this.cdata==null)
            throw new SAXException("CDATA ends without start "+getLocation());
        CDATASection c=this.doc.createCDATASection(this.cdata);
        this.cur.appendChild(c);
        this.cdata=null;
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
        this.name=name;
        this.publicId=publicId;
        this.systemId=systemId;
        this.dtd=true;
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
        if (!this.dtd)
            throw new SAXException("DTD Declaration never started");
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
    }

    /** Return the Namespace-Qualified name */
    private String getQualifiedName(String uri, String local, String raw)
    throws SAXException {
        if (uri.length()>0) {
            if (raw.length()>0) return(raw);
            String prefix=(String)this.namespaces.get(uri);
            return(prefix+":"+local);
        } else if (raw.length()==0)
            throw new SAXException("No namespace URI and no RAW name "+
                                   this.getLocation());
        return(raw);
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
