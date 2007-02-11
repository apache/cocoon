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
 * @version CVS $Id: DocumentHandlerAdapter.java,v 1.2 2004/03/01 03:50:59 antonio Exp $
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
