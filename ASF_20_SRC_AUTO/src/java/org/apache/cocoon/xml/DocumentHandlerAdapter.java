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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.xml.sax.AttributeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.DocumentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class is an utility class &quot;adapting&quot; a SAX version 1.0
 * <code>DocumentHandler</code>, to SAX version 2 <code>ContentHandler</code>.
 * <br>
 * This class fully supports XML namespaces, converting <code>xmlns</code> and
 * <code>xmlns:...</code> element attributes into appropriate
 * <code>startPrefixMapping(...)</code> and <code>endPrefixMapping(...)</code>
 * calls.
 *
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @version CVS $Id: DocumentHandlerAdapter.java,v 1.3 2004/03/05 13:03:01 bdelacretaz Exp $
 */
public class DocumentHandlerAdapter extends AbstractXMLProducer
implements DocumentHandler {

    /** The element-oriented namespace-uri stacked mapping table. */
    private Hashtable stackedNS=new Hashtable();
    /** The current namespaces table. */
    private NamespacesTable namespaces=new NamespacesTable();
    /** The current stack depth.*/
    private int stack=0;

    /**
     * Create a new <code>DocumentHandlerAdapter</code> instance.
     */
    public DocumentHandlerAdapter() {
        super();
    }

    /**
     * Create a new <code>DocumentHandlerAdapter</code> instance.
     */
    public DocumentHandlerAdapter(XMLConsumer consumer) {
        this();
        super.setConsumer(consumer);
    }

    /**
     * Create a new <code>DocumentHandlerAdapter</code> instance.
     */
    public DocumentHandlerAdapter(ContentHandler content) {
        this();
        super.setContentHandler(content);
    }

    /**
     * Receive an object for locating the origin of SAX document events.
     */
    public void setDocumentLocator (Locator locator) {
        if (super.contentHandler==null) return;
        else super.contentHandler.setDocumentLocator(locator);
    }

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument ()
        throws SAXException {
        if (super.contentHandler==null)
            throw new SAXException("ContentHandler not set");
        super.contentHandler.startDocument();
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument ()
        throws SAXException {
        if (super.contentHandler==null)
            throw new SAXException("ContentHandler not set");
        super.contentHandler.endDocument();
    }

    /**
     * Receive notification of the beginning of an element.
     */
    public void startElement (String name, AttributeList a)
        throws SAXException {
        if (super.contentHandler==null)
            throw new SAXException("ContentHandler not set");
        // Check for namespace declarations (two loops because we're not sure
        // about attribute ordering.
        AttributesImpl a2=new AttributesImpl();
        Vector nslist=new Vector();
        for (int x=0; x<a.getLength(); x++) {
            String att=a.getName(x);
            String uri=a.getValue(x);
            if (att.equals("xmlns") || att.startsWith("xmlns:")) {
                String pre="";
                if (att.length()>5) pre=att.substring(6);
                this.namespaces.addDeclaration(pre,uri);
                nslist.addElement(pre);
                super.contentHandler.startPrefixMapping(pre,uri);
            }
        }
        if (nslist.size()>0) this.stackedNS.put(new Integer(this.stack),nslist);
        // Resolve the element namespaced name
        NamespacesTable.Name w=this.namespaces.resolve(null,name,null,null);
        // Second loop through attributes to fill AttributesImpl
        for (int x=0; x<a.getLength(); x++) {
            String att=a.getName(x);
            if (att.equals("xmlns") || att.startsWith("xmlns:")) continue;
            // We have something different from a namespace declaration
            NamespacesTable.Name k=this.namespaces.resolve(null,att,null,null);
            String val=a.getValue(x);
            String typ=a.getType(x);
            String uri=k.getPrefix().length()==0?"":k.getUri();
            a2.addAttribute(uri,k.getLocalName(),k.getQName(),typ,val);
        }
        // Notify the contentHandler
        super.contentHandler.startElement(w.getUri(),w.getLocalName(),
                                          w.getQName(),a2);
        // Forward on the stack
        this.stack++;
    }

    /**
     * Receive notification of the end of an element.
     */
    public void endElement (String name)
        throws SAXException {
        if (super.contentHandler==null)
            throw new SAXException("ContentHandler not set");
            // Get back on the stack
            this.stack--;
        // Notify the contentHandler
        NamespacesTable.Name w=this.namespaces.resolve(null,name,null,null);
        super.contentHandler.endElement(w.getUri(),w.getLocalName(),
                                        w.getQName());
        // Undeclare namespaces
        Vector nslist=(Vector)this.stackedNS.remove(new Integer(this.stack));
        if (nslist==null) return;
        if (nslist.size()==0) return;
        Enumeration e=nslist.elements();
        while (e.hasMoreElements()) {
            String prefix=(String)e.nextElement();
            NamespacesTable.Declaration d=namespaces.removeDeclaration(prefix);
            super.contentHandler.endPrefixMapping(d.getPrefix());
        }
    }


    /**
     * Receive notification of character data.
     */
    public void characters (char ch[], int start, int len)
        throws SAXException {
        if (super.contentHandler==null)
            throw new SAXException("ContentHandler not set");
        super.contentHandler.characters(ch,start,len);
    }


    /**
     * Receive notification of ignorable whitespace in element content.
     */
    public void ignorableWhitespace (char ch[], int start, int len)
        throws SAXException {
        if (super.contentHandler==null)
            throw new SAXException("ContentHandler not set");
        super.contentHandler.ignorableWhitespace(ch,start,len);
    }


    /**
     * Receive notification of a processing instruction.
     */
    public void processingInstruction (String target, String data)
        throws SAXException {
        if (super.contentHandler==null)
            throw new SAXException("ContentHandler not set");
        super.contentHandler.processingInstruction(target,data);
    }
}
