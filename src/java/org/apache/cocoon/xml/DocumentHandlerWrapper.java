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
 * @version CVS $Id: DocumentHandlerWrapper.java,v 1.2 2004/03/08 14:04:00 cziegeler Exp $
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
