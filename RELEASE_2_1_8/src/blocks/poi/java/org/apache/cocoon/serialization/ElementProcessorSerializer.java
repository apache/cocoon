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
package org.apache.cocoon.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Stack;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.cocoon.components.elementprocessor.CannotCreateElementProcessorException;
import org.apache.cocoon.components.elementprocessor.ElementProcessor;
import org.apache.cocoon.components.elementprocessor.ElementProcessorFactory;
import org.apache.cocoon.components.elementprocessor.types.Attribute;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * An implementation of nearly all of the methods included in the
 * org.apache.poi.serialization.Serializer interface
 *
 * This is an abstract class. Concrete extensions need to implement
 * the following methods:
 * <ul>
 *    <li>String getMimeType()</li>
 *    <li>void endDocument()</li>
 *    <li>ElementProcessorFactory getElementProcessorFactory()</li>
 *    <li>void doPreInitialization(ElementProcessor processor)</li>
 * </ul>
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @author Nicola Ken Barozzi (nicolaken@apache.org)
 * @version CVS $Id: ElementProcessorSerializer.java,v 1.5 2004/03/05 13:02:07 bdelacretaz Exp $
 */
public abstract class ElementProcessorSerializer
    extends AbstractLogEnabled implements Serializer, Serviceable
{
    private static final boolean _should_set_content_length = false;
    private OutputStream         _output_stream;
    private Stack                _open_elements;
    private Locator              _locator;
    /** Service Manager */
    protected ServiceManager   manager = null;

    /**
     * Constructor
     */

    public ElementProcessorSerializer() {
        _output_stream = null;
        _open_elements = new Stack();
        _locator       = null;
    }

    public void service(ServiceManager manager) {
        this.manager = manager;
    }
    
    /**
     * get the appropriate ElementProcessorFactory
     *
     * @return an ElementProcessorFactory suitable for the file type
     */

    protected abstract ElementProcessorFactory getElementProcessorFactory();

    /**
     * perform whatever pre-initialization seems good on the
     * ElementProcessor
     *
     * @param processor the processor to be initialized
     *
     * @exception SAXException on errors
     */

    protected abstract void doPreInitialization(ElementProcessor processor)
	    throws SAXException;

    /**
     * @return the output stream
     */

    protected OutputStream getOutputStream() {
        return _output_stream;
    }

    /**
     * Create a new SAXException
     *
     * @param message the exception message
     * @param e the underlying exception (may be null)
     *
     * @return new SAXException
     */

    protected SAXException SAXExceptionFactory(final String message,
                       final Exception e) {
        StringBuffer message_buffer = new StringBuffer();

        message_buffer.append((message == null) ? "" : message);
        if (_locator != null) {
            message_buffer.append("; System id: \"");
            message_buffer.append(_locator.getSystemId());
            message_buffer.append("\"; public id: \"");
            message_buffer.append(_locator.getPublicId());
            message_buffer.append("\"; line number: ");
            message_buffer.append(_locator.getLineNumber());
            message_buffer.append("; column number: ");
            message_buffer.append(_locator.getColumnNumber());
        }
        SAXException rval = null;

        if (e != null) {
            rval = new SAXException(message_buffer.toString(), e);
        } else {
            rval = new SAXException(message_buffer.toString());
        }
        return rval;
    }

    /**
     * Create a SAXException
     *
     * @param message the exception message
     *
     * @return new SAXException
     */

    protected SAXException SAXExceptionFactory(final String message) {
        return SAXExceptionFactory(message, null);
    }

    private ElementProcessor getCurrentElementProcessor() {
        return _open_elements.empty() ? null
                              : (ElementProcessor) _open_elements.peek();
    }

    private char [] cleanupArray(final char [] array, final int start,
                         final int length) {
        char[] output = new char[length];

        System.arraycopy(array, start, output, 0, length);
        return output;
    }

    /* ********** START implementation of SitemapOutputComponent ********** */

    /**
     * Set the OutputStream where the requested resource should be
     * serialized.
     *
     * @param out the OutputStream to which the serialized data will
     *            be written
     */

    public void setOutputStream(final OutputStream out) {
        _output_stream = out;
    }

    /**
     * Test if the component wants to set the content length.
     *
     * @return false
     */

    public boolean shouldSetContentLength() {
        return _should_set_content_length;
    }

    /* **********  END  implementation of SitemapOutputComponent ********** */
    /* ********** START implementation of LexicalHandler ********** */

    /**
     * Report an XML comment anywhere in the document. We don't really
     * care.
     *
     * @param ignored_ch
     * @param ignored_start
     * @param ignored_length
     */

    public void comment(final char [] ignored_ch, final int ignored_start,
                    final int ignored_length) {
    }

    /**
     * Report the end of a CDATA section. We don't really care.
     */

    public void endCDATA() {
    }

    /**
     * Report the end of DTD declarations. We don't really care.
     */

    public void endDTD() {
    }

    /**
     * Report the end of an entity. We don't really care.
     *
     * @param ignored_name
     */

    public void endEntity(final String ignored_name) {
    }

    /**
     * Report the start of a CDATA section. We don't really care.
     */

    public void startCDATA() {
    }

    /**
     * Report the start of DTD declarations, if any. We don't really
     * care.
     *
     * @param ignored_name
     * @param ignored_publicId
     * @param ignored_systemId
     */

    public void startDTD(final String ignored_name,
             final String ignored_publicId, final String ignored_systemId) {
    }

    /**
     * Report the beginning of some internal and external XML
     * entities. We don't really care.
     *
     * @param ignored_name
     */

    public void startEntity(final String ignored_name) {
    }

    /* **********  END  implementation of LexicalHandler ********** */
    /* ********** START implementation of ContentHandler ********** */

    /**
     * Receive notification of character data.
     *
     * @param ch the character array
     * @param start the start index in ch
     * @param length the length of the valid part of ch
     *
     * @exception SAXException if anything goes wrong in processing
     *            the character data
     */

    public void characters(final char [] ch, final int start,
           final int length) throws SAXException {
        try {
            getCurrentElementProcessor().acceptCharacters(cleanupArray(ch,
                    start, length));
        } catch (Exception e) {
            throw SAXExceptionFactory("could not process characters event", e);
        }
    }

    /**
     * Receive notification of the end of an element.
     *
     * @param ignored_namespaceURI
     * @param ignored_localName
     * @param ignored_qName
     *
     * @exception SAXException on any errors processing the event.
     */

    public void endElement(final String ignored_namespaceURI,
            final String ignored_localName, final String ignored_qName)
            throws SAXException {
        try {
            getCurrentElementProcessor().endProcessing();
            _open_elements.pop();
        } catch (Exception e) {
            throw SAXExceptionFactory("could not process endElement event",
                                      e);
        }
    }

    /**
     * End the scope of a prefix-URI mapping. We don't really care.
     *
     * @param ignored_prefix
     */

    public void endPrefixMapping(final String ignored_prefix) {
    }

    /**
     * Receive notification of ignorable whitespace in element
     * content.
     *
     * @param ch the character array
     * @param start the start index in ch
     * @param length the length of the valid part of ch
     *
     * @exception SAXException if anything goes wrong in processing
     *            the character data
     */

    public void ignorableWhitespace(final char [] ch, final int start,
                    final int length) throws SAXException {
        try {
            getCurrentElementProcessor()
                .acceptWhitespaceCharacters(cleanupArray(ch, start, length));
        } catch (Exception e) {
            throw SAXExceptionFactory(
                "could not process ignorableWhitespace event", e);
        }
    }

    /**
     * Receive notification of a processing instruction. We don't
     * really care.
     *
     * @param ignored_target
     * @param ignored_data
     */

    public void processingInstruction(final String ignored_target,
                                      final String ignored_data) {
    }

    /**
     * Receive an object for locating the origin of SAX document
     * events.
     *
     * @param locator the Locator object
     */

    public void setDocumentLocator(final Locator locator) {
        _locator = locator;
    }

    /**
     * Receive notification of a skipped entity. We don't really care.
     *
     * @param ignored_name
     */

    public void skippedEntity(final String ignored_name) {
    }

    /**
     * Receive notification of the beginning of a document.
     */

    public void startDocument() {
        // nothing to do; should be ready as soon as we were
        // constructed
    }

    /**
     * Receive notification of the beginning of an element.
     *
     * @param namespaceURI the namespace this element is in
     * @param localName the local name of the element
     * @param qName the qualified name of the element
     * @param atts the Attributes, if any, of the element
     *
     * @exception SAXException if we cannot create an ElementProcessor
     *            to handle the element
     */

    public void startElement(final String namespaceURI,
            final String localName, final String qName, final Attributes atts)
            throws SAXException {
        String name = "";

        if ((localName != null) && (localName.length() != 0)) {
            name = localName;
        } else if ((qName != null) && (qName.length() != 0)) {
            name = qName;
        }
        ElementProcessor processor;

        try {
            processor =
                getElementProcessorFactory().createElementProcessor(name);
        } catch (CannotCreateElementProcessorException e) {
            throw SAXExceptionFactory("could not process startElement event",
                                      e);
        }
        doPreInitialization(processor);
        Attribute[] attributes = (atts == null) ? new Attribute[0]
                                                : new Attribute[atts.getLength()];

        for (int j = 0; j < attributes.length; j++) {
            attributes[j] = new Attribute(atts.getQName(j), atts.getValue(j));
        }
        try {
            processor.initialize(attributes, getCurrentElementProcessor());
        } catch (IOException e) {
            throw SAXExceptionFactory("Exception processing startElement", e);
        }
        _open_elements.push(processor);
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping. We don't
     * really care.
     *
     * @param ignored_prefix
     * @param ignored_uri
     */

    public void startPrefixMapping(final String ignored_prefix,
                                   final String ignored_uri) {
    }
    /* **********  END  implementation of ContentHandler ********** */
}   // end public abstract class ElementProcessorSerializer
