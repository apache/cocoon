/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import org.apache.arch.config.Configurable;
import org.apache.cocoon.Request;
import org.apache.cocoon.Response;
import org.apache.cocoon.xml.AbstractXMLProducer;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2000-02-27 17:49:41 $
 */
public class LinkTranslator extends AbstractXMLProducer implements XMLConsumer {

    private LinkResolver linkResolver=null;
    private String prepath=null;

    public LinkTranslator(LinkResolver resolver, String baseUri) {
        super();
        this.linkResolver=resolver;

        String relative="";
        for (int k=0; k<baseUri.length(); k++)
            if (baseUri.charAt(k)=='/') relative=relative+"../";
        if (relative.length()==0) this.prepath="./";
        else this.prepath=relative;
    }
    

    /**
     * Receive an object for locating the origin of SAX document events.
     *
     * @param locator An object that can return the location of any SAX
     *                document event.
     */
    public void setDocumentLocator(Locator locator) {
        if (super.contentHandler!=null)
            super.contentHandler.setDocumentLocator(locator);
    }

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument()
    throws SAXException {
        if (super.contentHandler!=null)
            super.contentHandler.startDocument();
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument()
    throws SAXException {
        if (super.contentHandler!=null)
            super.contentHandler.endDocument();
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     *
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        if (super.contentHandler!=null)
            super.contentHandler.startPrefixMapping(prefix,uri);
    }

    /**
     * End the scope of a prefix-URI mapping.
     *
     * @param prefix The prefix that was being mapping.
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        if (super.contentHandler!=null)
            super.contentHandler.endPrefixMapping(prefix);
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
        if (super.contentHandler==null) return;
        String tran=null;
        String part=null;
        AttributesImpl a2=new AttributesImpl();
        for (int x=0; x< a.getLength(); x++) {
            String auri=a.getURI(x);
            if (auri.equals("http://xml.apache.org/cocoon/links")) {
                if (a.getLocalName(x).equals("partition")) part=a.getValue(x);
                if (a.getLocalName(x).equals("translate")) tran=a.getValue(x);
            } else {
                a2.addAttribute(a.getURI(x),a.getLocalName(x),a.getRawName(x),
                               a.getType(x),a.getValue(x));
            }
        }
        if (tran!=null) {
            int x=a2.getIndex(tran);
            if (x>-1) {
                String resolved=this.linkResolver.resolve(a2.getValue(x),part);
                if (resolved!=null) a2.setValue(x,this.prepath+resolved);
            }
        }
        super.contentHandler.startElement(uri,loc,raw,a2);
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
        if (super.contentHandler!=null)
            super.contentHandler.endElement(uri,loc,raw);
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
        if (super.contentHandler!=null)
            super.contentHandler.characters(ch,start,len);
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
        if (super.contentHandler!=null)
            super.contentHandler.ignorableWhitespace(ch,start,len);
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
        if (super.contentHandler!=null)
            super.contentHandler.processingInstruction(target,data);
    }

    /**
     * Receive notification of a skipped entity.
     *
     * @param name The name of the skipped entity.  If it is a  parameter
     *             entity, the name will begin with '%'.
     */
    public void skippedEntity(String name)
    throws SAXException {
        if (super.contentHandler!=null)
            super.contentHandler.skippedEntity(name);
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
        if (super.lexicalHandler!=null)
            super.lexicalHandler.startDTD(name,publicId,systemId);
    }

    /**
     * Report the end of DTD declarations.
     */
    public void endDTD()
    throws SAXException {
        if (super.lexicalHandler!=null)
            super.lexicalHandler.endDTD();
    }

    /**
     * Report the beginning of an entity.
     *
     * @param name The name of the entity. If it is a parameter entity, the
     *             name will begin with '%'.
     */
    public void startEntity(String name)
    throws SAXException {
        if (super.lexicalHandler!=null)
            super.lexicalHandler.startEntity(name);
    }

    /**
     * Report the end of an entity.
     *
     * @param name The name of the entity that is ending.
     */
    public void endEntity(String name)
    throws SAXException {
        if (super.lexicalHandler!=null)
            super.lexicalHandler.endEntity(name);
    }

    /**
     * Report the start of a CDATA section.
     */
    public void startCDATA()
    throws SAXException {
        if (super.lexicalHandler!=null)
            super.lexicalHandler.startCDATA();
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA()
    throws SAXException {
        if (super.lexicalHandler!=null)
            super.lexicalHandler.endCDATA();
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
        if (super.lexicalHandler!=null)
            super.lexicalHandler.comment(ch,start,len);
    }
}
