/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.xml.dom;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.excalibur.pool.Recyclable;

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;

import org.apache.cocoon.xml.XMLConsumer;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * The <code>SVGBuilder</code> is a utility class that will generate a
 * SVG-DOM Document from SAX events using Batik's SVGDocumentFactory.
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Id: SVGBuilder.java,v 1.7 2004/03/05 13:01:47 bdelacretaz Exp $
 */
public class SVGBuilder extends SAXSVGDocumentFactory implements XMLConsumer, LogEnabled, Recyclable {
    protected Logger log;

    protected Locator locator;

    private static final String SAX_PARSER
        = "org.apache.xerces.parsers.SAXParser";

    /**
     * Construct a new instance of this TreeGenerator.
     */
    protected SVGBuilder() {
        super(SAX_PARSER);
    }

    /**
     * Provide component with a logger.
     *
     * @param logger the logger
     */
    public void enableLogging(Logger logger) {
        if (this.log == null) {
            this.log = logger;
        }
    }

    protected Logger getLogger() {
        return this.log;
    }

    /**
     * Return the newly built Document.
     */
    public Document getDocument() {
        return super.document;
    }

    /**
     * Receive notification of the beginning of a document.
     *
     * @exception SAXException If this method was not called appropriately.
     */
    public void startDocument() throws SAXException {
        try {
            // Create SVG Document
            String namespaceURI = SVGDOMImplementation.SVG_NAMESPACE_URI;
            super.document = implementation.createDocument(namespaceURI, "svg", null);
            super.startDocument();
            // Add svg, and SVG_NAMESPACE to SAXDocumentFactory namespace handling.
            // This ties 'svg' prefix used above to the svg namespace uri.
            namespaces.put("svg", SVGDOMImplementation.SVG_NAMESPACE_URI);
        } catch (SAXException se) {
            throw se;
        } catch (Exception ex){
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Got exception in startDocument, rethrowing", ex);
            }
            throw new SAXException("Exception in startDocument", ex);
        }
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
        super.setDocumentLocator(locator);
    }

    /**
     * Receive notification of the beginning of a document.
     *
     * @exception SAXException If this method was not called appropriately.
     */
    public void endDocument() throws SAXException {
        try {
            super.endDocument();

            // FIXME: Hack.
            URL baseURL = null;
            try {
                if (this.locator != null) {
                    baseURL = new URL(this.locator.getSystemId());
                } else {
                    baseURL = new URL("http://localhost/");
                    getLogger().warn("setDocumentLocator was not called, will use http://localhost/ as base URI");
                }
                ((org.apache.batik.dom.svg.SVGOMDocument)super.document).setURLObject(baseURL);
            } catch (MalformedURLException e) {
                getLogger().warn("Unable to set document base URI to " + baseURL + ", will default to http://localhost/", e);
                ((org.apache.batik.dom.svg.SVGOMDocument)super.document).setURLObject(new URL("http://localhost/"));
            }

            notify(super.document);
        } catch (SAXException se) {
            throw se;
        } catch (Exception ex){
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Got exception in endDocument, rethrowing", ex);
            }
            throw new SAXException("Exception in endDocument", ex);
        }
    }

    /**
     * Receive notification of a successfully completed DOM tree generation.
     */
    protected void notify(Document doc) throws SAXException {
    }

    public void recycle() {
        locator = null;
    }

}
