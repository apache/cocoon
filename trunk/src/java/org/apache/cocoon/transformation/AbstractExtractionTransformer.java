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
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.dom.DOMBuilder;

import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;


/**
 * This transformer sieves an incoming stream of xml
 * and feeds a DOMBuilder with it.
 *
 * @author <a href="mailto:paul@luminas.co.uk">Paul Russell</a>
 * @author <a href="mailto:haul@apache.org">Christian Haul</a>
 * @version CVS $Id: AbstractExtractionTransformer.java,v 1.4 2003/12/06 21:22:07 cziegeler Exp $
 */
abstract public class AbstractExtractionTransformer extends AbstractTransformer {

    protected DOMBuilder currentBuilder;

    private Map prefixMap;

    protected int extractLevel;


    /** Setup the transformer. */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters parameters)
            throws ProcessingException, SAXException, IOException {
        extractLevel = 0;
        prefixMap = new HashMap();
    }

    public void recycle() {
        super.recycle();
        this.extractLevel = 0;
        this.currentBuilder = null;
        this.prefixMap = null;
    }


    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     *
     * @param prefix The Namespace prefix being declared.
     * @param uri The Namespace URI the prefix is mapped to.
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        if (extractLevel == 0) {
            super.startPrefixMapping(prefix,uri);
            prefixMap.put(prefix,uri);
        } else {
            this.currentBuilder.startPrefixMapping(prefix,uri);
        }
    }

    /**
     * End the scope of a prefix-URI mapping.
     *
     * @param prefix The prefix that was being mapping.
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        if (extractLevel == 0) {
            super.endPrefixMapping(prefix);
            prefixMap.remove(prefix);
        } else {
            this.currentBuilder.endPrefixMapping(prefix);
        }
    }


    /**
     * Receive notification of the beginning of an element. Uses
     * startExtraction to determine whether to start
     * extracting. Nested triggering tags result in only one document.
     * * startExtractedDocument with the first node of the extracted
     * Document.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     * @param a The attributes attached to the element. If there are no
     *          attributes, it shall be an empty Attributes object.
     */
    public void startElement(String uri, String loc, String raw, Attributes a) throws SAXException {
        if (!startExtracting(uri, loc, raw, a)) {

            if (extractLevel == 0) {
                super.startElement(uri,loc,raw,a);
            } else {
                this.currentBuilder.startElement(uri,loc,raw,a);
            }

        } else {

            extractLevel++;
            if (this.getLogger().isDebugEnabled()) {
                getLogger().debug("extractLevel now " + extractLevel + ".");
            }

            if (extractLevel != 1) {
                this.currentBuilder.startElement(uri,loc,raw,a);
            } else {

                // setup new document
                this.currentBuilder = new DOMBuilder();
                this.currentBuilder.startDocument();
                // setup namespaces
                Iterator itt = prefixMap.entrySet().iterator();
                while (itt.hasNext()) {
                    Map.Entry entry = (Map.Entry)itt.next();
                    this.currentBuilder.startPrefixMapping(
                        (String)entry.getKey(),
                        (String)entry.getValue()
                    );
                }
                // start root node
                startExtractingDocument(uri, loc, raw, a);

            }

        }
    }


    /**
     * Receive notification of the end of an element. Uses
     * endExtraction to determine whether to stop extracting or
     * not. Calls endExtractedDocument with the extracted document.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     */
    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        if (extractLevel == 0) {
            super.endElement(uri,loc,raw);
        } else {
            if (endExtracting(uri, loc, raw)) {
                extractLevel--;
                if (this.getLogger().isDebugEnabled()) {
                    getLogger().debug("extractLevel now " + extractLevel + ".");
                }

                if (extractLevel != 0) {
                    this.currentBuilder.endElement(uri,loc,raw);
                } else {

                    // end root element
                    endExtractingDocument(uri, loc, raw);
                    // finish building the document. remove existing prefix mappings.
                    Iterator itt = prefixMap.entrySet().iterator();
                    while (itt.hasNext()) {
                        Map.Entry entry = (Map.Entry) itt.next();
                        this.currentBuilder.endPrefixMapping(
                            (String)entry.getKey()
                        );
                    }
                    this.currentBuilder.endDocument();

                    handleExtractedDocument(this.currentBuilder.getDocument());

                    if (this.getLogger().isDebugEnabled()) {
                        getLogger().debug("Stored document.");
                    }

                }
            } else {
                this.currentBuilder.endElement(uri, loc, raw);
            }
        }
    }

    /**
     * Receive notification of character data.
     *
     * @param c The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void characters(char c[], int start, int len)
    throws SAXException {
        if (extractLevel == 0) {
            super.characters(c,start,len);
        } else {
            this.currentBuilder.characters(c,start,len);
        }
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     *
     * @param c The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void ignorableWhitespace(char c[], int start, int len)
    throws SAXException {
        if (extractLevel == 0) {
            super.ignorableWhitespace(c,start,len);
        } else {
            this.currentBuilder.ignorableWhitespace(c,start,len);
        }
    }

    /**
     * Receive notification of a processing instruction.
     *
     * @param target The processing instruction target.
     * @param data The processing instruction data, or null if none was
     *             supplied.
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        if (extractLevel == 0) {
            super.processingInstruction(target,data);
        } else {
            this.currentBuilder.processingInstruction(target,data);
        }
    }

    /**
     * Receive notification of a skipped entity.
     *
     * @param name The name of the skipped entity.  If it is a  parameter
     *             entity, the name will begin with '%'.
     */
    public void skippedEntity(String name)
    throws SAXException {
        if (extractLevel == 0) {
            super.skippedEntity(name);
        } else {
            this.currentBuilder.skippedEntity(name);
        }
    }

    /**
     * Report the start of DTD declarations, if any.
     *
     * @param name The document type name.
     * @param publicId The declared public identifier for the external DTD
     *                 subset, or null if none was declared.
     * @param systemId The declared system identifier for the external DTD
     *                 subset, or null if none was declared.
     */
    public void startDTD(String name, String publicId, String systemId)
    throws SAXException {
        if (extractLevel == 0) {
            super.startDTD(name,publicId,systemId);
        } else {
            throw new SAXException(
                "Recieved startDTD after beginning fragment extraction process."
            );
        }
    }

    /**
     * Report the end of DTD declarations.
     */
    public void endDTD()
    throws SAXException {
        if (extractLevel == 0) {
            super.endDTD();
        } else {
            throw new SAXException(
                "Recieved endDTD after beginning fragment extraction process."
            );
        }
    }

    /**
     * Report the beginning of an entity.
     *
     * @param name The name of the entity. If it is a parameter entity, the
     *             name will begin with '%'.
     */
    public void startEntity(String name)
    throws SAXException {
        if (extractLevel == 0) {
            super.startEntity(name);
        } else {
            this.currentBuilder.startEntity(name);
        }
    }

    /**
     * Report the end of an entity.
     *
     * @param name The name of the entity that is ending.
     */
    public void endEntity(String name)
    throws SAXException {
        if (extractLevel == 0) {
            super.endEntity(name);
        } else {
            this.currentBuilder.endEntity(name);
        }
    }

    /**
     * Report the start of a CDATA section.
     */
    public void startCDATA()
    throws SAXException {
        if (extractLevel == 0) {
            super.startCDATA();
        } else {
            this.currentBuilder.startCDATA();
        }
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA()
    throws SAXException {
        if (extractLevel == 0) {
            super.endCDATA();
        } else {
            this.currentBuilder.endCDATA();
        }
    }

    /**
     * Report an XML comment anywhere in the document.
     *
     * @param ch An array holding the characters in the comment.
     * @param start The starting position in the array.
     * @param len The number of characters to use from the array.
     */
    public void comment(char ch[], int start, int len)
    throws SAXException {
        if (extractLevel == 0) {
            super.comment(ch,start,len);
        } else {
            this.currentBuilder.comment(ch,start,len);
        }
    }



    /**
     * Receive notification of the beginning of an element and signal extraction start.
     * 
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     * @param a The attributes attached to the element. If there are no
     *          attributes, it shall be an empty Attributes object.
     * @return a <code>boolean</code> value to signal to start extracting
     */
    abstract boolean startExtracting(String uri, String loc, String raw, Attributes a);

    /**
     * Receive notification of the beginning of the extracted Document. Per default send
     * startElement message to document builder. Override if necessary. Must override 
     * {@link #endExtractingDocument} as well. 
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     * @param a The attributes attached to the element. If there are no
     *          attributes, it shall be an empty Attributes object.
     */
    public void startExtractingDocument(String uri, String loc, String raw, Attributes a) throws SAXException{
        this.currentBuilder.startElement(uri,loc,raw,a);
    }

    /**
     * Receive notification of the end of an element and signal extraction end.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     * @return a <code>boolean</code> value to signal to stop extracting
     */
    abstract boolean endExtracting(String uri, String loc, String raw);

    /**
     * Receive notification of the end of the extracted Document. Per default, 
     * send endElement message to document builder. Override if necessary.
     * Must override {@link #startExtractingDocument} as well.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     */
    public void endExtractingDocument(String uri, String loc, String raw) throws SAXException{
        this.currentBuilder.endElement(uri,loc,raw);
    }

    /**
     * Receive notification of the end of the extracted Document.
     *
     * @param doc a <code>Document</code> value
     */
    abstract void handleExtractedDocument(Document doc);
    

}
