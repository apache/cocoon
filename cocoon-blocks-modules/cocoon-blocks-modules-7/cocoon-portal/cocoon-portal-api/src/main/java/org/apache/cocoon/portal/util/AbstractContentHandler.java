/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.portal.util;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * This class wrapps a {@link ContentHandler} and a {@link LexicalHandler}.
 * <br>
 *
 * @version $Id$
 * @since 2.2
 */
public class AbstractContentHandler implements ContentHandler, LexicalHandler {

    /** The current <code>ContentHandler</code>. */
    protected final ContentHandler contentHandler;

    /** The optional <code>LexicalHandler</code> */
    protected final LexicalHandler lexicalHandler;

    /** The namespace handled by this handler (or null if this is a general purpose handler. */
    protected String namespaceUri;

    /**
     * The namespaces and their prefixes
     */
    private final List namespaces = new ArrayList(5);

    /**
     * The current prefix for our namespace
     */
    private String ourPrefix;

    /**
     * Remove namespace prefixes for our namespace?
     */
    protected boolean removeOurNamespacePrefixes = false;

    /**
     * Create a new <code>ContentHandlerWrapper</code> instance.
     */
    public AbstractContentHandler(ContentHandler contentHandler) {
        this(contentHandler, (contentHandler instanceof LexicalHandler ? (LexicalHandler)contentHandler : null));
    }

    /**
     * Create a new <code>ContentHandlerWrapper</code> instance.
     */
    public AbstractContentHandler(ContentHandler contentHandler,
                                 LexicalHandler lexicalHandler) {
        if ( contentHandler == null ) {
            throw new NullPointerException("ContentHandler must not be null.");
        }
        this.contentHandler = contentHandler;
        this.lexicalHandler = lexicalHandler;
    }

    /**
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator (Locator locator) {
        this.contentHandler.setDocumentLocator(locator);
    }

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument ()
    throws SAXException {
        this.contentHandler.startDocument();
    }

    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument ()
    throws SAXException {
        this.contentHandler.endDocument();
    }

    /**
     * Process the SAX event.
     * @see org.xml.sax.ContentHandler#startPrefixMapping
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        boolean isOurPrefix = false;
        if (prefix != null) {
            this.namespaces.add(new String[] {prefix, uri});
        }
        if (uri.equals(namespaceUri)) {
            this.ourPrefix = prefix;
            isOurPrefix = true;
        }
        if ( !removeOurNamespacePrefixes || !isOurPrefix) {
            this.contentHandler.startPrefixMapping(prefix, uri);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        boolean isOurPrefix = false;
        if (prefix != null) {
            // Find and remove the namespace prefix
            boolean found = false;
            for (int i = this.namespaces.size() - 1; i >= 0; i--) {
                final String[] prefixAndUri = (String[]) this.namespaces.get(i);
                if (prefixAndUri[0].equals(prefix)) {
                    this.namespaces.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new SAXException("Namespace for prefix '" + prefix + "' not found.");
            }

            if (prefix.equals(this.ourPrefix)) {
                isOurPrefix = true;
                // Reset our current prefix
                this.ourPrefix = null;

                // Now search if we have a different prefix for our namespace
                for (int i = this.namespaces.size() - 1; i >= 0; i--) {
                    final String[] prefixAndUri = (String[]) this.namespaces.get(i);
                    if (namespaceUri.equals(prefixAndUri[1])) {
                        this.ourPrefix = prefixAndUri[0];
                        break;
                    }
                }
            }
        }

        if ( !removeOurNamespacePrefixes || !isOurPrefix) {
            this.contentHandler.endPrefixMapping(prefix);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        this.contentHandler.startElement(uri, loc, raw, a);
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        this.contentHandler.endElement(uri, loc, raw);
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char ch[], int start, int len)
    throws SAXException {
        this.contentHandler.characters(ch,start,len);
    }

    /**
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char ch[], int start, int len)
    throws SAXException {
        this.contentHandler.ignorableWhitespace(ch,start,len);
    }

    /**
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        this.contentHandler.processingInstruction(target,data);
    }

    /**
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String name)
    throws SAXException {
        this.contentHandler.skippedEntity(name);
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
     */
    public void startDTD(String name, String publicId, String systemId)
    throws SAXException {
        if (this.lexicalHandler != null) {
            this.lexicalHandler.startDTD(name, publicId, systemId);
        }
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#endDTD()
     */
    public void endDTD()
    throws SAXException {
        if (this.lexicalHandler != null) {
            this.lexicalHandler.endDTD();
        }
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
     */
    public void startEntity(String name)
    throws SAXException {
        if (this.lexicalHandler != null) {
            this.lexicalHandler.startEntity(name);
        }
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
     */
    public void endEntity(String name)
    throws SAXException {
        if (this.lexicalHandler != null) {
            this.lexicalHandler.endEntity(name);
        }
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#startCDATA()
     */
    public void startCDATA()
    throws SAXException {
        if (this.lexicalHandler != null) {
            this.lexicalHandler.startCDATA();
        }
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#endCDATA()
     */
    public void endCDATA()
    throws SAXException {
        if (this.lexicalHandler != null) {
            this.lexicalHandler.endCDATA();
        }
    }

    /**
     * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
     */
    public void comment(char ch[], int start, int len)
    throws SAXException {
        if (this.lexicalHandler != null) {
            this.lexicalHandler.comment(ch, start, len);
        }
    }
}
