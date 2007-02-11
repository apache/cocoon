/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: SVGBuilder.java,v 1.6 2003/08/13 08:58:15 bruno Exp $
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
