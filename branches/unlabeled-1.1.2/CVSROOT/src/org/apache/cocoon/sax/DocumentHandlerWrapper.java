/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sax;

import java.util.Hashtable;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributeListImpl;
import org.xml.sax.DocumentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>,
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-12 00:33:32 $
 * @since Cocoon 2.0
 */
public class DocumentHandlerWrapper implements XMLConsumer {
    /** The SAX1 <code>DocumentHandler</code> */
    private DocumentHandler handler;
    /** The namespaces URI-PREFIX table */
    private Hashtable namespacesUriPrefix=null;
    /** The namespaces PREFIX-URI reversed table */
    private Hashtable namespacesPrefixUri=null;

    /**
     * Construct a new <code>XMLConsumerImpl</code> instance.
     */
    public DocumentHandlerWrapper(DocumentHandler h) {
        super();
        this.handler=h;
        this.namespacesUriPrefix=new Hashtable();
        this.namespacesPrefixUri=new Hashtable();
    }

    /**
     * Receive an object for locating the origin of SAX document events.
     *
     * @param locator An object that can return the location of any SAX
     *                document event.
     */
    public void setDocumentLocator(Locator locator) {
        this.handler.setDocumentLocator(locator);
    }

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument()
    throws SAXException {
        this.handler.startDocument();
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument()
    throws SAXException {
        this.handler.endDocument();
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     *
     * @param p The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     */
    public void startPrefixMapping(String p, String uri)
    throws SAXException {
        if (this.namespacesUriPrefix.put(uri,p)!=null)
            throw new SAXException("Namespace URI '"+uri+"' already declared");
        if (this.namespacesPrefixUri.put(p,uri)!=null)
            throw new SAXException("Namespace prefix '"+p+"' already declared");
    }

    /**
     * End the scope of a prefix-URI mapping.
     *
     * @param p The prefix that was being mapping.
     */
    public void endPrefixMapping(String p)
    throws SAXException {
        String uri=(String)this.namespacesPrefixUri.remove(p);
        if (uri==null)
            throw new SAXException("Namespace prefix '"+p+"' never declared");
        if (this.namespacesUriPrefix.remove(uri)==null)
            throw new SAXException("Namespace URI '"+uri+"' never declared");
    }

    /**
     * Receive notification of the beginning of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     * @param a The attributes attached to the element. If there are no
     *          attributes, it shall be an empty Attributes object.
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        String name=this.qualify(uri,loc,raw);
        AttributeListImpl list=new AttributeListImpl();
        for (int x=0; x<a.getLength(); x++) {
            String aname=a.getRawName(x);
            if(a.getURI(x).equals(uri)) {
                if(a.getLocalName(x).length()>0) aname=a.getLocalName(x);
                else aname=a.getRawName(x);
            } else aname=this.qualify(a.getURI(x),a.getLocalName(x),
                                      a.getRawName(x));
            list.addAttribute(aname,a.getType(x),a.getValue(x));
        }
        this.handler.startElement(name,list);
    }


    /**
     * Receive notification of the end of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     */
    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        this.handler.endElement(this.qualify(uri,loc,raw));
    }

    /**
     * Receive notification of character data.
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void characters(char ch[], int start, int len)
    throws SAXException {
        this.handler.characters(ch,start,len);
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void ignorableWhitespace(char ch[], int start, int len)
    throws SAXException {
        this.handler.ignorableWhitespace(ch,start,len);
    }

    /**
     * Receive notification of a processing instruction.
     *
     * @param target The processing instruction target.
     * @param data The processing instruction data, or null if none was
     *             supplied.
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        this.handler.processingInstruction(target,data);
    }

    /**
     * Receive notification of a skipped entity.
     *
     * @param name The name of the skipped entity.  If it is a  parameter
     *             entity, the name will begin with '%'.
     */
    public void skippedEntity(String name)
    throws SAXException {
    }

    /**
     * Report the start of DTD declarations, if any.
     *
     * @param name The document type name.
     * @param publicId The declared public identifier for the external DTD
     *                 subset, or null if none was declared.
     * @param systemId The declared system identifier for the external DTD
     *                 subset, or null if none was declared.
     */
    public void startDTD(String name, String publicId, String systemId)
    throws SAXException {
    }

    /**
     * Report the end of DTD declarations.
     */
    public void endDTD()
    throws SAXException {
    }

    /**
     * Report the beginning of an entity.
     *
     * @param name The name of the entity. If it is a parameter entity, the
     *             name will begin with '%'.
     */
    public void startEntity(String name)
    throws SAXException {
    }

    /**
     * Report the end of an entity.
     *
     * @param name The name of the entity that is ending.
     */
    public void endEntity(String name)
    throws SAXException {
    }

    /**
     * Report the start of a CDATA section.
     */
    public void startCDATA()
    throws SAXException {
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA()
    throws SAXException {
    }


    /**
     * Report an XML comment anywhere in the document.
     *
     * @param ch An array holding the characters in the comment.
     * @param start The starting position in the array.
     * @param len The number of characters to use from the array.
     */
    public void comment(char ch[], int start, int len)
    throws SAXException {
    }


    /** Return the fully namespace qualified name */
    private String qualify(String uri, String loc, String raw)
    throws SAXException {
        if(uri.length()>0) {
            String pre=(String)this.namespacesUriPrefix.get(uri);
            if (pre==null) throw new SAXException("No prefix declared for "+
                                                  "namespace uri '"+uri+"'");
            if (pre.length()>0) return(pre+":"+loc);
            else return(loc);
        } else if(raw.length()>0) {
            return(raw);
        } else if(loc.length()>0) {
            return(loc);
        }
        throw new SAXException("Cannot qualify namespaced name");
    }
}
