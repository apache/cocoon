/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.components.parser;

import java.io.IOException;
import org.apache.cocoon.xml.AbstractXMLProducer;
import org.apache.cocoon.xml.dom.DOMFactory;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DocumentTypeImpl;
import org.apache.xerces.parsers.SAXParser;
import org.apache.avalon.Poolable;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.w3c.dom.Document;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.12 $ $Date: 2001-01-08 14:31:02 $
 */
public class XercesParser extends AbstractXMLProducer
implements Parser, ErrorHandler, DOMFactory, Poolable {

    final SAXParser parser;

    public XercesParser ()
    throws SAXException {
        this.parser = new SAXParser();

    this.parser.setFeature("http://xml.org/sax/features/validation",false);
    this.parser.setFeature("http://xml.org/sax/features/namespaces",true);
	this.parser.setFeature("http://xml.org/sax/features/namespace-prefixes",
                          true);
    }

    public void parse(InputSource in)
    throws SAXException, IOException {
      this.parser.setProperty("http://xml.org/sax/properties/lexical-handler",
                              super.lexicalHandler);
        this.parser.setErrorHandler(this);
        this.parser.setContentHandler(super.contentHandler);
        this.parser.parse(in);
    }

    /**
     * Create a new Document object.
     */
    public Document newDocument() {
        return(newDocument(null,null,null));
    }

    /**
     * Create a new Document object with a specified DOCTYPE.
     */
    public Document newDocument(String name) {
        return(newDocument(name,null,null));
    }

    /**
     * Create a new Document object with a specified DOCTYPE, public ID and
     * system ID.
     */
    public Document newDocument(String name, String pub, String sys) {
        DocumentImpl doc=new DocumentImpl();
        if ((pub!=null)||(sys!=null)) {
            DocumentTypeImpl dtd=new DocumentTypeImpl(doc,name,pub,sys);
            doc.appendChild(dtd);
        } else if (name!=null) {
            DocumentTypeImpl dtd=new DocumentTypeImpl(doc,name);
            doc.appendChild(dtd);
        }
        return(doc);
    }

    /**
     * Receive notification of a recoverable error.
     */
    public void error(SAXParseException e)
    throws SAXException {
        throw new SAXException("Error parsing "+e.getSystemId()+" (line "+
                               e.getLineNumber()+" col. "+e.getColumnNumber()+
                               "): "+e.getMessage(),e);
    }

    /**
     * Receive notification of a fatal error.
     */
    public void fatalError(SAXParseException e)
    throws SAXException {
        throw new SAXException("Fatal error parsing "+e.getSystemId()+" (line "+
                               e.getLineNumber()+" col. "+e.getColumnNumber()+
                               "): "+e.getMessage(),e);
    }

    /**
     * Receive notification of a warning.
     */
    public void warning(SAXParseException e)
    throws SAXException {
        throw new SAXException("Warning parsing "+e.getSystemId()+" (line "+
                               e.getLineNumber()+" col. "+e.getColumnNumber()+
                               "): "+e.getMessage(),e);
    }
}
