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
package org.apache.cocoon.components.source;


import java.net.MalformedURLException;
import java.net.URL;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 *  This is an <code>XMLConsumer</code> which rewrites the stream
 *  according to the configuration.
 *  The configuration can have the following parameters:
 *  "rewriteURLMode" : The mode to rewrite the urls. Currently none and cocoon
 *                     are supported.
 *  "cocoonURL"    : The url all links are resolved to
 *  "urlParameterName" : The parameter name to use for links (all links are
 *                       then "cocoonURL?urlParameterName=LINK"
 *  "baseURL"        : The current URL to rewrite
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @version CVS $Id: URLRewriter.java,v 1.1 2003/03/09 00:09:12 pier Exp $
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
    /** The mode:
     *  0 : no rewriting
     *  1 : cocoon */
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
            if (configuration != null
                && configuration.getParameter(PARAMETER_MODE, null) != null) {
                if (configuration.getParameter(PARAMETER_MODE, null).equalsIgnoreCase(MODE_COCOON) == true) {
                    this.mode = 1;
                    this.baseUrl = configuration.getParameter(PARAMETER_URL);
                    this.cocoonUrl = configuration.getParameter(PARAMETER_COCOON_URL) +
                                       '?' + configuration.getParameter(PARAMETER_PARAMETER_NAME) + '=';
                }
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
                if (attrname.equals("href") == true
                    || attrname.equals("action") == true) {
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
                } else if (attrname.equals("src") == true
                           || attrname.equals("background") == true) {
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
        contentHandler.startElement(namespace,name,raw,attr);
    }

    /**
     * SAX Event Handling
     */
    public void endElement(String namespace, String name, String raw)
    throws SAXException {
        contentHandler.endElement(namespace,name,raw);
    }

    /**
     * SAX Event Handling
     */
    public void characters(char ary[], int start, int length)
    throws SAXException {
        contentHandler.characters(ary,start,length);
    }

    /**
     * SAX Event Handling
     */
    public void ignorableWhitespace(char ary[], int start, int length)
    throws SAXException {
        contentHandler.ignorableWhitespace(ary,start,length);
    }

    /**
     * SAX Event Handling
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        contentHandler.processingInstruction(target,data);
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
        if (lexicalHandler != null) lexicalHandler.startDTD(name,public_id,system_id);
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
            lexicalHandler.comment(ary,start,length);
        }
    }

}
