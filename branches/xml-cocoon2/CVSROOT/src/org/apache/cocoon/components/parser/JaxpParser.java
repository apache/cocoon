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
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

/**
 * An XMLParser that is only dependant on JAXP 1.1 compliant parsers.
 * If only we can get rid of the need for the Document...
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Revision: 1.1.2.6 $ $Date: 2001-04-23 02:06:16 $
 */
public class JaxpParser extends AbstractXMLProducer
implements Parser, ErrorHandler {

    final SAXParserFactory factory = SAXParserFactory.newInstance();
    final DocumentBuilderFactory docfactory = DocumentBuilderFactory.newInstance();

    public JaxpParser ()
    throws SAXException, ParserConfigurationException {
        this.factory.setNamespaceAware(true);
        this.factory.setValidating(false);
        this.docfactory.setNamespaceAware(true);
        this.docfactory.setValidating(false);
    }

    public void parse(InputSource in)
    throws SAXException, IOException {
        SAXParser parser = null;

        try {
            parser = this.factory.newSAXParser();
        } catch (Exception e) {
            getLogger().error("Cannot produce a valid parser", e);
            throw new SAXException("Could not get valid parser" + e.getMessage());
        }

        XMLReader reader = parser.getXMLReader();

        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
        reader.setProperty("http://xml.org/sax/properties/lexical-handler",
                           super.lexicalHandler);

        reader.setErrorHandler(this);
        reader.setContentHandler(super.contentHandler);
        reader.parse(in);
    }

    /**
     * Create a new Document object.
     */
    public Document newDocument() {
        DocumentBuilder builder = null;

        try {
            builder = this.docfactory.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            getLogger().error("Could not create DocumentBuilder", pce);
            return null;
        }
        return builder.newDocument();
    }

    /**
     * Create a new Document object with a specified DOCTYPE.
     */
    public Document newDocument(String name) {
        return this.newDocument(name, null, null);
    }

    /**
     * Create a new Document object with a specified DOCTYPE, public ID and
     * system ID.
     */
    public Document newDocument(String name, String publicId, String systemId) {
        DocumentBuilder builder = null;

        try {
            builder = this.docfactory.newDocumentBuilder();
        } catch (ParserConfigurationException pce) {
            getLogger().error("Could not build DocumentBuilder", pce);
            return null;
        }

        // Fixme: is there a better way to achieve this?
        DOMImplementation impl = builder.newDocument().getImplementation();
        return impl.createDocument(
            null,
            name,
            impl.createDocumentType(name, publicId, systemId)
        );

    }

    /**
     * Parses a new Document object from the given InputSource.
     */
    public Document parseDocument(InputSource input) throws SAXException, IOException {
      DocumentBuilder builder = null;
      try {
        builder = this.docfactory.newDocumentBuilder();
      } catch (ParserConfigurationException pce) {
        getLogger().error("Could not build DocumentBuilder", pce);
        return null;
      }
      return builder.parse(input);
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
