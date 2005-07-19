/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.components.language.markup.xsp;

import org.apache.cocoon.components.language.markup.AbstractMarkupLanguage;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLFilterImpl;

public class XSPExpressionFilter implements ContentHandler, XSPExpressionParser.Handler {

    public static class XMLPipeAdapter extends AbstractXMLPipe {
        private XSPExpressionFilter expressionFilter;

        private AbstractXMLPipe additionalFilter;

        public XMLPipeAdapter(XSPExpressionFilter expressionFilter, AbstractXMLPipe additionalFilter) {
            this.additionalFilter = additionalFilter;
            this.expressionFilter = expressionFilter;
            super.setLexicalHandler(additionalFilter);
            super.setContentHandler(expressionFilter);
            expressionFilter.setContentHandler(additionalFilter);
        }

        public void setConsumer(XMLConsumer consumer) {
            additionalFilter.setConsumer(consumer);
        }

        public void setContentHandler(ContentHandler handler) {
            additionalFilter.setContentHandler(handler);
        }

        public void setLexicalHandler(LexicalHandler handler) {
            additionalFilter.setLexicalHandler(handler);
        }
    }

    public static class XMLFilterAdapter extends XMLFilterImpl {
        private XSPExpressionFilter expressionFilter;

        public XMLFilterAdapter(XSPExpressionFilter filter) {
            this.expressionFilter = filter;
            super.setContentHandler(filter);
        }

        public void setParent(XMLReader reader) {
            super.setParent(reader);
            reader.setContentHandler(this);
        }

        public void setContentHandler(ContentHandler contentHandler) {
            expressionFilter.setContentHandler(contentHandler);
        }
    }

    /** The markup language URI */
    private String markupURI;

    /** The markup language prefix */
    private String markupPrefix;

    /** Set default processing of attribute templates */
    private boolean defaultProcessAttribs;

    /** Set processing of attribute templates */
    private boolean processAttribs;

    /** Set default processing of text templates */
    private boolean defaultProcessText;

    /** Set processing of text templates */
    private boolean processText;

    /** The parser for XSP value templates */
    private XSPExpressionParser expressionParser = new XSPExpressionParser(this);

    /** First element was processed */
    private boolean firstElementProcessed;

    private ContentHandler contentHandler;

    public XSPExpressionFilter(XSPMarkupLanguage markup) {
        this.markupURI = markup.getURI();
        this.markupPrefix = markup.getPrefix();
        this.defaultProcessAttribs = markup.hasAttrInterpolation();
        this.defaultProcessText = markup.hasTextInterpolation();
    }

    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    /**
     * Create a new <code>{@link XSPExpressionFilter}</code>.
     * 
     * @param filter
     * @param filename
     * @param language
     */
    public void startDocument() throws SAXException {
        processAttribs = defaultProcessAttribs;
        processText = defaultProcessText;

        contentHandler.startDocument();
    }

    /**
     * Start a new element. If attribute value templates are enabled and the element has attributes
     * with templates, these are replaced by xsp:attribute tags.
     * 
     * @see org.xml.sax.contentHandler.#startElement(java.lang.String, java.lang.String,
     *      java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String localName, String qName, Attributes attribs)
            throws SAXException {
        expressionParser.flush();

        // Check template for processing flags in page
        if (!firstElementProcessed) {
            initFromAttribs(attribs);
            firstElementProcessed = true;
        }

        if (processAttribs) {
            // Attribute value templates enabled => process attributes
            AttributesImpl staticAttribs = new AttributesImpl();
            AttributesImpl dynamicAttribs = new AttributesImpl();

            // Gather attributes with and without templates separately
            for (int i = 0; i < attribs.getLength(); ++i) {
                String value = attribs.getValue(i);

                if (value.indexOf("{#") != -1) {
                    // The attribute contains templates
                    dynamicAttribs.addAttribute(attribs.getURI(i), attribs.getLocalName(i), attribs.getQName(i),
                            attribs.getType(i), value);
                }
                else {
                    // The attribute does not contain templates
                    staticAttribs.addAttribute(attribs.getURI(i), attribs.getLocalName(i), attribs.getQName(i),
                            attribs.getType(i), value);
                }
            }

            // Start the element with template-free attributes
            contentHandler.startElement(namespaceURI, localName, qName, staticAttribs);

            // Generate xsp:attribute elements for the attributes containing templates
            for (int i = 0; i < dynamicAttribs.getLength(); ++i) {
                AttributesImpl elemAttribs = new AttributesImpl();
                addAttribute(elemAttribs, "uri", dynamicAttribs.getURI(i));

                String qname = dynamicAttribs.getQName(i);

                if (qname != null) {
                    addAttribute(elemAttribs, "prefix", StringUtils.left(qname, qname.indexOf(':')));
                }

                addAttribute(elemAttribs, "name", dynamicAttribs.getLocalName(i));

                contentHandler.startElement(markupURI, "attribute", markupPrefix + ":attribute", elemAttribs);

                expressionParser.consume(dynamicAttribs.getValue(i));
                expressionParser.flush();

                contentHandler.endElement(markupURI, "attribute", markupPrefix + ":attribute");
            }
        }
        else {
            // Attribute value templates disabled => pass through element
            contentHandler.startElement(namespaceURI, localName, qName, attribs);
        }
    }

    protected void initFromAttribs(Attributes attribs) {
        String value = attribs.getValue(markupURI, XSPMarkupLanguage.ATTR_INTERPOLATION);

        if (value != null) {
            processAttribs = Boolean.valueOf(value).booleanValue();
        }

        value = attribs.getValue(markupURI, XSPMarkupLanguage.TEXT_INTERPOLATION);

        if (value != null) {
            processText = Boolean.valueOf(value).booleanValue();
        }
    }

    /**
     * Flush the current expression.
     */
    public void endElement(String uri, String loc, String raw) throws SAXException {
        expressionParser.flush();
        contentHandler.endElement(uri, loc, raw);
    }

    /**
     * Handle characters. If text templates are enabled, the text is parsed and expressions are
     * replaced.
     * 
     * @see org.xml.sax.contentHandler.#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (processText) {
            // Text templated enabled => Replace text expressions
            expressionParser.consume(ch, start, length);
        }
        else {
            // Text templates disabled => pass through text
            contentHandler.characters(ch, start, length);
        }
    }

    /**
     * Forward text to parent class.
     * 
     * @see org.apache.cocoon.components.language.markup.xsp.XSPExpressionParser.Handler#handleText(char[],
     *      int, int)
     */
    public void handleText(char[] chars, int start, int length) throws SAXException {
        contentHandler.characters(chars, start, length);
    }

    /**
     * Wrap expressions in xsp:expr tags.
     * 
     * @see org.apache.cocoon.components.language.markup.xsp.XSPExpressionParser.Handler#handleExpression(char[],
     *      int, int)
     */
    public void handleExpression(char[] chars, int start, int length) throws SAXException {
        contentHandler.startElement(markupURI, "expr", markupPrefix + ":expr", XMLUtils.EMPTY_ATTRIBUTES);
        contentHandler.characters(chars, start, length);
        contentHandler.endElement(markupURI, "expr", markupPrefix + ":expr");
    }

    /**
     * Add an attribute if it is neither <code>null</code> nor empty (length 0).
     * 
     * @param attribs The attributes
     * @param name The attribute name
     * @param value The attribute value
     */
    protected void addAttribute(AttributesImpl attribs, String name, String value) {
        if (value != null && value.length() > 0) {
            attribs.addCDATAAttribute(name, value);
        }
    }

    public void setDocumentLocator(Locator locator) {
        contentHandler.setDocumentLocator(locator);
    }

    public void endDocument() throws SAXException {
        contentHandler.endDocument();
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        contentHandler.endPrefixMapping(prefix);
    }

    public void ignorableWhitespace(char[] chars, int start, int length) throws SAXException {
        contentHandler.ignorableWhitespace(chars, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        contentHandler.processingInstruction(target, data);
    }

    public void skippedEntity(String name) throws SAXException {
        contentHandler.skippedEntity(name);
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        contentHandler.startPrefixMapping(prefix, uri);
    }
}
