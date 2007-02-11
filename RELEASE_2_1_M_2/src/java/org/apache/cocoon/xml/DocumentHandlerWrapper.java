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
package org.apache.cocoon.xml;

import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.DocumentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributeListImpl;

import java.util.Vector;

/**
 * This class is an utility class &quot;wrapping&quot; around a SAX version 1.0
 * <code>DocumentHandler</code> and forwarding it those events received throug
 * its <code>XMLConsumers</code> interface.
 * <br>
 * This class fully supports XML namespaces, converting
 * <code>startPrefixMapping(...)</code> and <code>endPrefixMapping(...)</code>
 * calls into appropriate <code>xmlns</code> and <code>xmlns:...</code> element
 * attributes.
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @version CVS $Id: DocumentHandlerWrapper.java,v 1.1 2003/03/09 00:09:46 pier Exp $
 */
public class DocumentHandlerWrapper extends AbstractXMLConsumer implements LogEnabled /*, Recyclable*/ {

    protected Logger log;

    /** The current namespaces table. */
    private NamespacesTable namespaces=new NamespacesTable();
    /** The vector of namespaces declarations to include in the next element. */
    private Vector undecl=new Vector();

    /** The current <code>DocumentHandler</code>. */
    protected DocumentHandler documentHandler=null;

    /**
     * Create a new <code>DocumentHandlerWrapper</code> instance.
     */
    public DocumentHandlerWrapper() {
        super();
     }

    /**
     * Create a new <code>DocumentHandlerWrapper</code> instance.
     */
    public DocumentHandlerWrapper(DocumentHandler document) {
        this();
        this.setDocumentHandler(document);
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
     * Implementation of the recycle method
     */
    public void recycle() {
        this.documentHandler = null;
    }

    /**
     * Set the <code>DocumentHandler</code> that will receive XML data.
     *
     * @exception IllegalStateException If the <code>DocumentHandler</code>
     *                                  was already set.
     */
    public void setDocumentHandler(DocumentHandler document)
    throws IllegalStateException {
        if (this.documentHandler!=null) throw new IllegalStateException();
        this.documentHandler=document;
    }

    /**
     * Receive an object for locating the origin of SAX document events.
     */
    public void setDocumentLocator (Locator locator) {
        if (this.documentHandler==null) return;
        else this.documentHandler.setDocumentLocator(locator);
    }

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument ()
    throws SAXException {
        if (this.documentHandler==null)
            throw new SAXException("DocumentHandler not set");
        this.documentHandler.startDocument();
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument ()
    throws SAXException {
        if (this.documentHandler==null)
            throw new SAXException("DocumentHandler not set");
        this.documentHandler.endDocument();
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        this.undecl.addElement(this.namespaces.addDeclaration(prefix,uri));
    }

    /**
     * End the scope of a prefix-URI mapping.
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        if (namespaces.removeDeclaration(prefix)==null)
            throw new SAXException("Namespace prefix \""+prefix+
                                   "\" never declared");
    }

    /**
     * Receive notification of the beginning of an element.
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        if (this.documentHandler==null)
            throw new SAXException("DocumentHandler not set");
        NamespacesTable.Name name=this.namespaces.resolve(uri,raw,null,loc);
        // Create the AttributeList
        AttributeListImpl a2=new AttributeListImpl();
        // Set the xmlns:...="..." attributes
        if (this.undecl.size()>0) {
            for (int x=0; x<this.undecl.size(); x++) {
                NamespacesTable.Declaration dec=null;
                dec=(NamespacesTable.Declaration)this.undecl.elementAt(x);
                String aname="xmlns";
                if (dec.getPrefix().length()>0) aname="xmlns:"+dec.getPrefix();
                a2.addAttribute(aname,"CDATA",dec.getUri());
            }
            this.undecl.clear();
        }
        // Set the real attributes
        for (int x=0; x<a.getLength(); x++) {
            NamespacesTable.Name aname=namespaces.resolve(a.getURI(x),
                                                          a.getQName(x),
                                                          null,
                                                          a.getLocalName(x));
            a2.addAttribute(aname.getQName(),a.getType(x),a.getValue(x));
        }
        // Call the document handler startElement() method.
        this.documentHandler.startElement(name.getQName(),a2);
    }


    /**
     * Receive notification of the end of an element.
     */
    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        if (this.documentHandler==null)
            throw new SAXException("DocumentHandler not set");
        NamespacesTable.Name name=this.namespaces.resolve(uri,raw,null,loc);
        this.documentHandler.endElement(name.getQName());
    }

    /**
     * Receive notification of character data.
     */
    public void characters(char ch[], int start, int len)
    throws SAXException {
        if (this.documentHandler==null)
            throw new SAXException("DocumentHandler not set");
        this.documentHandler.characters(ch,start,len);
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     */
    public void ignorableWhitespace(char ch[], int start, int len)
    throws SAXException {
        if (this.documentHandler==null)
            throw new SAXException("DocumentHandler not set");
        this.documentHandler.ignorableWhitespace(ch,start,len);
    }

    /**
     * Receive notification of a processing instruction.
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        if (this.documentHandler==null)
            throw new SAXException("DocumentHandler not set");
        this.documentHandler.processingInstruction(target,data);
    }
}
