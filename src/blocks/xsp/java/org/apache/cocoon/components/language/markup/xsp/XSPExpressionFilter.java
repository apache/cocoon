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
package org.apache.cocoon.components.language.markup.xsp;

import org.apache.cocoon.components.language.markup.AbstractMarkupLanguage;
import org.apache.cocoon.components.language.markup.LogicsheetFilter;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLUtils;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import java.util.LinkedList;

/**
 * Filter attributes and text and expand {#expr} to xsp:attribute and xsp:expr
 * elements.
 *
 * @version $Id$
 */
public class XSPExpressionFilter extends LogicsheetFilter
                                 implements XSPExpressionParser.Handler {

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

        public void setDocumentLocator(Locator locator) {
            additionalFilter.setDocumentLocator(locator);
            expressionFilter.setDocumentLocator(locator);
        }
    }


    /** The markup language URI */
    private String markupURI;

    /** The markup language prefix */
    private String markupPrefix;

    /** Interpolation settings as nested properties */
    private LinkedList interpolationStack = new LinkedList();

    /** Default interpolation settings for given markup language */
    private InterpolationSettings defaultInterpolationSettings;

    /** The parser for XSP value templates */
    private XSPExpressionParser expressionParser = new XSPExpressionParser(this);



    public XSPExpressionFilter(XSPMarkupLanguage markup) {
        this.markupURI = markup.getURI();
        this.markupPrefix = markup.getPrefix();

        // Initialize default interpolation settings.
        defaultInterpolationSettings
            = new InterpolationSettings(markup.hasAttrInterpolation(),
                                        markup.hasTextInterpolation());
    }

    /**
     * Create a new <code>{@link XSPExpressionFilter}</code>.
     */
    public void startDocument() throws SAXException {
        interpolationStack.clear();
        interpolationStack.addLast(defaultInterpolationSettings);
        super.startDocument();
    }

    /**
     * Start a new element. If attribute value templates are enabled and the element has attributes
     * with templates, these are replaced by xsp:attribute tags.
     *
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String,
     *      java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String namespaceURI, String localName, String qName, Attributes attribs)
    throws SAXException {
        expressionParser.flush(locator, "...<"+qName+">");

        // Check template for interpolation flags
        attribs = pushInterpolationStack(attribs);

        if (getInterpolationSettings().attrInterpolation) {
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
            super.startElement(namespaceURI, localName, qName, staticAttribs);

            // Generate xsp:attribute elements for the attributes containing templates
            for (int i = 0; i < dynamicAttribs.getLength(); ++i) {
                AttributesImpl elemAttribs = new AttributesImpl();
                addAttribute(elemAttribs, "uri", dynamicAttribs.getURI(i));

                String qname = dynamicAttribs.getQName(i);

                if (qname != null) {
                    addAttribute(elemAttribs, "prefix", StringUtils.left(qname, qname.indexOf(':')));
                }

                String attrName = dynamicAttribs.getLocalName(i);
                addAttribute(elemAttribs, "name", attrName);

                super.startElement(markupURI, "attribute", markupPrefix + ":attribute", elemAttribs);
                expressionParser.consume(dynamicAttribs.getValue(i));
                expressionParser.flush(locator, "<"+qName+" "+attrName+"=\"...\">");
                super.endElement(markupURI, "attribute", markupPrefix + ":attribute");
            }
        } else {
            // Attribute value templates disabled => pass through element
            super.startElement(namespaceURI, localName, qName, attribs);
        }
    }

    /**
     * Check attributes for presence of interpolation flags.
     * Push current settings to stack.
     * Remove interpolation attributes and return cleaned attribute list.
     */
    private Attributes pushInterpolationStack(Attributes attribs) {
        String valueAttr = attribs.getValue(markupURI, AbstractMarkupLanguage.ATTR_INTERPOLATION);
        String valueText = attribs.getValue(markupURI, AbstractMarkupLanguage.TEXT_INTERPOLATION);

        // Neither interpolation flag in attribute list: push tail to stack.
        if (valueAttr == null && valueText == null ) {
            interpolationStack.addLast(interpolationStack.getLast());
            return attribs;
        }

        // Push new interpolation settings to stack and remove attributes.

        InterpolationSettings lastSettings = (InterpolationSettings)interpolationStack.getLast();
        boolean attrInterpolation = lastSettings.attrInterpolation;
        boolean textInterpolation = lastSettings.textInterpolation;

        AttributesImpl cleanedAttribs = new AttributesImpl(attribs);

        if (valueAttr != null) {
            attrInterpolation = Boolean.valueOf(valueAttr).booleanValue();
            cleanedAttribs.removeAttribute(cleanedAttribs.getIndex(markupURI, AbstractMarkupLanguage.ATTR_INTERPOLATION));
        }

        if (valueText != null) {
            textInterpolation = Boolean.valueOf(valueText).booleanValue();
            cleanedAttribs.removeAttribute(cleanedAttribs.getIndex(markupURI, AbstractMarkupLanguage.TEXT_INTERPOLATION));
        }

        interpolationStack.addLast(new InterpolationSettings(attrInterpolation,
                                                             textInterpolation));

        return cleanedAttribs;
    }

    /**
     * Flush the current expression.
     */
    public void endElement(String uri, String loc, String raw) throws SAXException {
        expressionParser.flush(locator, "...</"+raw+">");
        super.endElement(uri, loc, raw);

        // Pop stack of interpolation settings.
        interpolationStack.removeLast();
    }

    /**
     * Handle characters. If text templates are enabled, the text is parsed and expressions are
     * replaced.
     *
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (getInterpolationSettings().textInterpolation) {
            // Text templated enabled => Replace text expressions
            expressionParser.consume(ch, start, length);
        }
        else {
            // Text templates disabled => pass through text
            super.characters(ch, start, length);
        }
    }

    /**
     * Forward text to parent class.
     *
     * @see org.apache.cocoon.components.language.markup.xsp.XSPExpressionParser.Handler#handleText(char[],
     *      int, int)
     */
    public void handleText(char[] chars, int start, int length) throws SAXException {
        super.characters(chars, start, length);
    }

    /**
     * Wrap expressions in xsp:expr tags.
     *
     * @see org.apache.cocoon.components.language.markup.xsp.XSPExpressionParser.Handler#handleExpression(char[],
     *      int, int)
     */
    public void handleExpression(char[] chars, int start, int length) throws SAXException {
        super.startElement(markupURI, "expr", markupPrefix + ":expr", XMLUtils.EMPTY_ATTRIBUTES);
        super.characters(chars, start, length);
        super.endElement(markupURI, "expr", markupPrefix + ":expr");
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

    /**
     * Return current interpolation settings.
     */
    private InterpolationSettings getInterpolationSettings() {
        return (InterpolationSettings)interpolationStack.getLast();
    }

    /**
     * Structure to hold settings for attribute and text interpolation.
     */
    private static class InterpolationSettings {
        boolean attrInterpolation;
        boolean textInterpolation;

        InterpolationSettings(boolean attrInterpolation, boolean textInterpolation) {
            this.attrInterpolation = attrInterpolation;
            this.textInterpolation = textInterpolation;
        }
    }
}
