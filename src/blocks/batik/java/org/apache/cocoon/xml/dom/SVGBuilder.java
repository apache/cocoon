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

import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.dom.svg.SVGDOMImplementation;

import org.apache.cocoon.xml.XMLConsumer;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * The <code>SVGBuilder</code> is a utility class that will generate a
 * SVG-DOM Document from SAX events using Batik's SVGDocumentFactory.
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Id: SVGBuilder.java,v 1.2 2003/04/27 14:48:32 cziegeler Exp $
 */
public class SVGBuilder extends SAXSVGDocumentFactory implements XMLConsumer, LogEnabled {
    protected Logger log;

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
            // add svg, and SVG_NAMESPACE to SAXDocumentFactory namespace handling
            // this is a fix only ties svg to svg namespace uri
            // it is not as general as tieing any prefix to svg namespace uri
            namespaces.put("svg", SVGDOMImplementation.SVG_NAMESPACE_URI);
        } catch (SAXException se) {
            log.error("SVGBuilder: startDocument", se);
            throw se;
        } catch (Exception ex){
            log.error("SVGBuilder: startDocument", ex);
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
        } catch (SAXException se) {
            log.error("SVGBuilder: endDocument", se);
            throw se;
        } catch (Exception ex){
            log.error("SVGBuilder: endDocument", ex);
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
