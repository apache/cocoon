/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.xml.dom;

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

import org.apache.log.LogKit;
import org.apache.log.Logger;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.apache.batik.css.CSSDocumentHandler;

/**
 * The <code>SVGBuilder</code> is a utility class that will generate a 
 * SVG-DOM Document from SAX events using Batik's SVGDocumentFactory.
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Revision: 1.1.2.3 $ $Date: 2001-01-08 16:32:56 $
 */
public class SVGBuilder extends SAXSVGDocumentFactory implements XMLConsumer {
    protected Logger log = LogKit.getLoggerFor("cocoon");

    private static final String SAX_PARSER
        = "org.apache.xerces.parsers.SAXParser";

    private final static String CSS_PARSER_CLASS_NAME =
        "org.w3c.flute.parser.Parser";

	static {
        CSSDocumentHandler.setParserClassName(CSS_PARSER_CLASS_NAME);
	}

    /**
     * Construct a new instance of this TreeGenerator.
     */
    protected SVGBuilder() {
        super(SAX_PARSER);
    }

    /**
     * Return the newly built Document.
     */
    public Document getDocument() {
        return(this.document);
    }

    /**
     * Receive notification of the beginning of a document.
     *
     * @exception SAXException If this method was not called appropriately.
     */
    public void startDocument()
    throws SAXException {
		try {
			// Create SVG Document
			String namespaceURI = SVGDOMImplementation.SVG_NAMESPACE_URI;
			this.document = implementation.createDocument(namespaceURI, "svg", null);
			super.startDocument();	
		} catch (Exception ex){
            log.error("SVGBuilder: startDocument", ex);
			ex.printStackTrace();
            throw new SAXException("SVGBuilder: startDocument", ex);
		}
    }

    /**
     * Receive notification of the beginning of a document.
     *
     * @exception SAXException If this method was not called appropriately.
     */
    public void endDocument ()
    throws SAXException {
		try {
			super.endDocument();

			// FIXME: Hack.
			((org.apache.batik.dom.svg.SVGOMDocument)this.document).setURLObject(new java.net.URL("http://xml.apache.org"));

			this.notify(this.document);
		} catch (Exception ex){
            log.error("SVGBuilder: endDocument", ex);
			ex.printStackTrace();
            throw new SAXException("SVGBuilder: endDocument", ex);
		}
    }

    /**
     * Receive notification of a successfully completed DOM tree generation.
     */
    protected void notify(Document doc)
    throws SAXException {

    }
}
