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
package org.apache.cocoon.components.source;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.XMLConsumer;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * This is an <code>XMLConsumer</code> which rewrites the stream according
 * to the configuration. The configuration can have the following
 * parameters:
 *
 * <ul>
 * <li><code>rewriteURLMode</code>: The mode to rewrite the urls. Currently,
 *     values <code>none</code> and <code>cocoon</code> are supported.
 * <li><code>baseURL</code>: The current URL to rewrite.
 * <li><code>cocoonURL</code>: The url all links are resolved to.
 * <li><code>urlParameterName</code>: The parameter name to use for
 *     links (all links are then "cocoonURL?urlParameterName=LINK").
 * </ul>
 *
 * <p>URLRewriter rewrites only href, src, background, and action attributes.
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: URLRewriter.java,v 1.3 2004/06/11 17:41:11 vgritsenko Exp $
 */
public final class URLRewriter implements XMLConsumer {

    public static final String PARAMETER_MODE = "rewriteURLMode";
    public static final String MODE_NONE   = "none";
    public static final String MODE_COCOON = "cocoon";
    public static final String PARAMETER_PARAMETER_NAME = "urlParameterName";
    public static final String PARAMETER_URL  = "baseURL";
    public static final String PARAMETER_COCOON_URL = "cocoonURL";

    /** The <code>ContentHandler</code> */
    private ContentHandler contentHandler;
    /** The <code>LexicalHandler</code> */
    private LexicalHandler lexicalHandler;

    /**
     * The mode:
     *  0: no rewriting
     *  1: cocoon
     */
    private int mode;

    /** The base url */
    private String baseUrl;
    /** The cocoon url */
    private String cocoonUrl;

    /**
     * Create a new rewriter
     */
    public URLRewriter(Parameters configuration,
                       ContentHandler contentHandler,
                       LexicalHandler lexicalHandler)
    throws ProcessingException {
        try {
            this.contentHandler = contentHandler;
            this.lexicalHandler = lexicalHandler;
            this.mode = 0;
            if (configuration != null &&
                    MODE_COCOON.equalsIgnoreCase(configuration.getParameter(PARAMETER_MODE, null))) {
                this.mode = 1;
                this.baseUrl = configuration.getParameter(PARAMETER_URL);
                this.cocoonUrl = configuration.getParameter(PARAMETER_COCOON_URL) +
                        '?' + configuration.getParameter(PARAMETER_PARAMETER_NAME) + '=';
            }
        } catch (org.apache.avalon.framework.parameters.ParameterException local) {
            throw new ProcessingException("URLRewriter: configuration exception.", local);
        }
    }

    /**
     * Create a new rewriter
     */
    public URLRewriter(Parameters configuration,
                       ContentHandler contentHandler)
    throws ProcessingException {
        this(configuration, contentHandler,
             (contentHandler instanceof LexicalHandler ? (LexicalHandler)contentHandler : null));
    }

    /**
     * SAX Event Handling
     */
    public void setDocumentLocator(Locator locator) {
        contentHandler.setDocumentLocator(locator);
    }

    /**
     * SAX Event Handling
     */
    public void startDocument()
    throws SAXException {
        contentHandler.startDocument();
    }

    /**
     * SAX Event Handling
     */
    public void endDocument()
    throws SAXException {
        contentHandler.endDocument();
    }

    /**
     * SAX Event Handling
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        contentHandler.startPrefixMapping(prefix,uri);
    }

    /**
     * SAX Event Handling
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        contentHandler.endPrefixMapping(prefix);
    }

    /**
     * SAX Event Handling
     */
    public void startElement(String namespace, String name, String raw,
                             Attributes attr)
    throws SAXException {
        if (this.mode == 1) {
            String attrname;
            AttributesImpl newattr = null;
            String value;

            for(int i = 0; i < attr.getLength(); i++) {
                attrname = attr.getLocalName(i);
                if (attrname.equals("href") == true ||
                        attrname.equals("action") == true) {
                    if (newattr == null) {
                        newattr = new AttributesImpl(attr);
                    }
                    value = attr.getValue(i);
                    if (value.indexOf(':') == -1) {
                        try {
                            URL baseURL = new URL(new URL(this.baseUrl), value);
                            value = baseURL.toExternalForm();
                        } catch (MalformedURLException local) {
                            value = attr.getValue(i);
                        }
                    }
                    newattr.setValue(i, this.cocoonUrl + value);
                } else if (attrname.equals("src") == true ||
                        attrname.equals("background") == true) {
                    if (newattr == null) {
                        newattr = new AttributesImpl(attr);
                    }
                    value = attr.getValue(i);
                    if (value.indexOf(':') == -1) {
                        try {
                            URL baseURL = new URL(new URL(this.baseUrl), value);
                            value = baseURL.toExternalForm();
                        } catch (MalformedURLException local) {
                            value = attr.getValue(i);
                        }
                    }
                    newattr.setValue(i, value);
                }
            }
            if (newattr != null) {
                contentHandler.startElement(namespace, name, raw, newattr);
                return;
            }
        }
        contentHandler.startElement(namespace, name, raw, attr);
    }

    /**
     * SAX Event Handling
     */
    public void endElement(String namespace, String name, String raw)
    throws SAXException {
        contentHandler.endElement(namespace, name, raw);
    }

    /**
     * SAX Event Handling
     */
    public void characters(char ary[], int start, int length)
    throws SAXException {
        contentHandler.characters(ary, start, length);
    }

    /**
     * SAX Event Handling
     */
    public void ignorableWhitespace(char ary[], int start, int length)
    throws SAXException {
        contentHandler.ignorableWhitespace(ary, start, length);
    }

    /**
     * SAX Event Handling
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        contentHandler.processingInstruction(target, data);
    }

    /**
     * SAX Event Handling
     */
    public void skippedEntity(String name)
    throws SAXException {
        contentHandler.skippedEntity(name);
    }

    /**
     * SAX Event Handling
     */
    public void startDTD(String name, String public_id, String system_id)
            throws SAXException {
        if (lexicalHandler != null) lexicalHandler.startDTD(name, public_id, system_id);
    }

    /**
     * SAX Event Handling
     */
    public void endDTD() throws SAXException {
        if (lexicalHandler != null) lexicalHandler.endDTD();
    }

    /**
     * SAX Event Handling
     */
    public void startEntity(String name) throws SAXException {
        if (lexicalHandler != null) lexicalHandler.startEntity(name);
    }

    /**
     * SAX Event Handling
     */
    public void endEntity(String name) throws SAXException {
        if (lexicalHandler != null) lexicalHandler.endEntity(name);
    }

    /**
     * SAX Event Handling
     */
    public void startCDATA() throws SAXException {
        if (lexicalHandler != null) lexicalHandler.startCDATA();
    }

    /**
     * SAX Event Handling
     */
    public void endCDATA() throws SAXException {
        if (lexicalHandler != null) lexicalHandler.endCDATA();
    }


    /**
     * SAX Event Handling
     */
    public void comment(char ary[], int start, int length)
    throws SAXException {
        if (this.lexicalHandler != null) {
            lexicalHandler.comment(ary, start, length);
        }
    }
}
