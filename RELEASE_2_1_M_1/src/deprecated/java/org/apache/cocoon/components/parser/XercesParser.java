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
package org.apache.cocoon.components.parser;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.cocoon.components.resolver.Resolver;
import org.apache.cocoon.xml.AbstractXMLProducer;
import org.apache.xerces.dom.DocumentImpl;
import org.apache.xerces.dom.DocumentTypeImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.parsers.SAXParser;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;

/**
 *
 * @deprecated The Avalon XML Parser is now used inside Cocoon. This role
 *             will be removed in future releases.

 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @version CVS $Id: XercesParser.java,v 1.1 2003/03/09 00:06:57 pier Exp $
 */
public class XercesParser extends AbstractXMLProducer
implements Parser, ErrorHandler, Composable, Disposable {

    /** the SAX Parser */
    final SAXParser parser;

    /** the component manager */
    protected ComponentManager manager;

    /** the Entity Resolver */
    protected Resolver resolver = null;

    public XercesParser ()
    throws SAXException {
        this.parser = new SAXParser();

        this.parser.setFeature("http://xml.org/sax/features/validation", false);
        this.parser.setFeature("http://xml.org/sax/features/namespaces", true);
    }

    /**
     * Get the Entity Resolver from the component manager
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Looking up " + Resolver.ROLE);
        }
        if ( manager.hasComponent( Resolver.ROLE ) ) {
            this.resolver = (Resolver)manager.lookup(Resolver.ROLE);
        }
    }

    /**
     * Dispose
     */
    public void dispose() {
        if (this.manager != null) {
            this.manager.release( this.resolver );
        }
    }

    public void parse(InputSource in)
    throws SAXException, IOException {
        this.parser.setProperty("http://xml.org/sax/properties/lexical-handler",
                                super.lexicalHandler);
        this.parser.setErrorHandler(this);
        this.parser.setContentHandler(super.contentHandler);
        if(this.resolver != null)
            this.parser.setEntityResolver(this.resolver);
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
     * Parses a new Document object from the given InputSource.
     */
    public Document parseDocument(InputSource input) throws SAXException, IOException {
        DOMParser parser = null;

        try {
            parser = new DOMParser();

            parser.setFeature("http://xml.org/sax/features/validation",false);
            parser.setFeature("http://xml.org/sax/features/namespaces",true);

            parser.parse(input);
        } catch (Exception pce) {
            getLogger().error("Could not build DocumentBuilder", pce);
            return null;
        }

        return parser.getDocument();
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
