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

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.w3c.dom.Node;

/**
 * A special purpose <code>XMLConsumer</code> used for including files.
 * It basically ignores the <code>startDocument</code>,
 * </code>endDocument</code>, <code>startDTD</code> and <code>endDTD</code>
 * messages.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: IncludeXMLConsumer.java,v 1.2 2003/09/25 12:55:02 vgritsenko Exp $
 */
public class IncludeXMLConsumer implements XMLConsumer {

    /** The TrAX factory for serializing xml */
    final private static TransformerFactory transformerFactory = TransformerFactory.newInstance();

    final private ContentHandler contentHandler;
    final private LexicalHandler lexicalHandler;

    private boolean ignoreEmptyCharacters = false;
    private boolean ignoreRootElement = false;
    private int     ignoreRootElementCount;
    private boolean inDTD = false;

    /**
     * Constructor
     */
    public IncludeXMLConsumer (XMLConsumer consumer) {
        this.contentHandler = consumer;
        this.lexicalHandler = consumer;
    }

    /**
     * Constructor
     */
    public IncludeXMLConsumer (ContentHandler contentHandler, LexicalHandler lexicalHandler) {
        this.contentHandler = contentHandler;
        this.lexicalHandler = lexicalHandler;
    }

    /**
     * Constructor
     */
    public IncludeXMLConsumer (ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
        this.lexicalHandler = (contentHandler instanceof LexicalHandler ? (LexicalHandler)contentHandler : null);
    }

    /**
     * Include a node into the current chain.
     * @param node The DOM Node to be included
     * @param contentHandler The SAX ContentHandler receiving the information
     * @param lexicalHandler The SAX LexicalHandler receiving the information (optional)
     */
    public static void includeNode(Node           node,
                                   ContentHandler contentHandler,
                                   LexicalHandler lexicalHandler)
    throws SAXException {
        if (node != null) {
            try {
                IncludeXMLConsumer filter = new IncludeXMLConsumer(contentHandler, lexicalHandler);
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(node);
                SAXResult result = new SAXResult(filter);
                result.setLexicalHandler(filter);
                transformer.transform(source, result);
            } catch (TransformerConfigurationException e) {
                throw new SAXException("TransformerConfigurationException", e);
            } catch (TransformerException e) {
                throw new SAXException("TransformerException", e);
            }
        }
    }

    /**
     * Controll SAX EventHandling
     * If set to <CODE>true</CODE> all empty characters events are ignored.
     * The default is <CODE>false</CODE>.
     */
    public void setIgnoreEmptyCharacters(boolean value) {
        this.ignoreEmptyCharacters = value;
    }

    /**
     * Controll SAX EventHandling
     * If set to <CODE>true</CODE> the root element is ignored.
     * The default is <CODE>false</CODE>.
     */
    public void setIgnoreRootElement(boolean value) {
        this.ignoreRootElement = value;
        this.ignoreRootElementCount = 0;
    }

    public void setDocumentLocator(Locator loc) {
        this.contentHandler.setDocumentLocator(loc);
    }

    public void startDocument() throws SAXException {
        // Ignored
    }

    public void endDocument() throws SAXException {
        // Ignored
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        this.contentHandler.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        this.contentHandler.endPrefixMapping(prefix);
    }

    public void startElement(String uri, String local, String qName, Attributes attr) throws SAXException {
        if (this.ignoreRootElement == false ||
            this.ignoreRootElementCount > 0) {
            this.contentHandler.startElement(uri,local,qName,attr);
        }
        this.ignoreRootElementCount++;
    }

    public void endElement(String uri, String local, String qName) throws SAXException {
        this.ignoreRootElementCount--;
        if (!this.ignoreRootElement  || this.ignoreRootElementCount > 0) {
            this.contentHandler.endElement(uri, local, qName);
        }
    }

    public void characters(char[] ch, int start, int end) throws SAXException {
        if (this.ignoreEmptyCharacters) {
            String text = new String(ch, start, end).trim();
            if (text.length() > 0) {
                this.contentHandler.characters(text.toCharArray(),0,text.length());
            }
        } else {
            this.contentHandler.characters(ch, start, end);
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int end) throws SAXException {
        if (!this.ignoreEmptyCharacters) {
            this.contentHandler.ignorableWhitespace(ch, start, end);
        }
    }

    public void processingInstruction(String name, String value) throws SAXException {
        this.contentHandler.processingInstruction(name, value);
    }

    public void skippedEntity(String ent) throws SAXException {
        this.contentHandler.skippedEntity(ent);
    }

    public void startDTD(String name, String public_id, String system_id)
    throws SAXException {
        // Ignored
        inDTD = true;
    }

    public void endDTD() throws SAXException {
        // Ignored
        inDTD = false;
    }

    public void startEntity(String name) throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.startEntity(name);
        }
    }

    public void endEntity(String name) throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.endEntity(name);
        }
    }

    public void startCDATA() throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.startCDATA();
        }
    }

    public void endCDATA() throws SAXException {
        if (lexicalHandler != null) {
            lexicalHandler.endCDATA();
        }
    }

    public void comment(char ary[], int start, int length)
        throws SAXException {
        if (!inDTD && lexicalHandler != null) {
            lexicalHandler.comment(ary,start,length);
        }
    }
}
