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
package org.apache.cocoon.forms.datatype;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.source.Source;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.apache.cocoon.forms.datatype.convertor.DefaultFormatCache;
import org.apache.cocoon.forms.datatype.convertor.ConversionResult;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.Locale;

/**
 * SelectionList implementation that always reads its content from the source
 * each time it is requested.
 *
 * <p>Note: the class {@link SelectionListBuilder} also interprets the same wd:selection-list XML, so if
 * anything changes here to how that XML is interpreted, it also needs to change over there and vice versa.
 * 
 * @version $Id: DynamicSelectionList.java,v 1.3 2004/05/06 14:59:44 bruno Exp $
 */
public class DynamicSelectionList implements SelectionList {
    private String src;
    private Datatype datatype;
    private ServiceManager serviceManager;

    public DynamicSelectionList(Datatype datatype, String src, ServiceManager serviceManager) {
        this.datatype = datatype;
        this.src = src;
        this.serviceManager = serviceManager;
    }

    public Datatype getDatatype() {
        return datatype;
    }

    /*
     * This method is only used by a test case and by the public version
     * of generateSaxFragment. 
     */
    void generateSaxFragment(ContentHandler contentHandler, Locale locale, Source source) throws ProcessingException, SAXException, IOException {
        SelectionListHandler handler = new SelectionListHandler(locale);
        handler.setContentHandler(contentHandler);
        SourceUtil.toSAX(serviceManager, source, null, handler);
    }

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        SourceResolver sourceResolver = null;
        Source source = null;
        try {
            sourceResolver = (SourceResolver)serviceManager.lookup(SourceResolver.ROLE);
            source = sourceResolver.resolveURI(src);
            generateSaxFragment(contentHandler, locale, source);
        } catch (Exception e) {
            throw new SAXException("Error while generating selection list: " + e.getMessage(), e);
        } finally {
            if (sourceResolver != null) {
                if (source != null)
                    try { sourceResolver.release(source); } catch (Exception e) {}
                serviceManager.release(sourceResolver);
            }
        }
    }

    /**
     * XMLConsumer used to handle selection lists generated on the fly.
     */
    public class SelectionListHandler extends AbstractXMLPipe {
        private Object currentValue;
        private String currentValueAsString;
        private boolean hasLabel;
        private Locale locale;
        /** The convertor used to parse the values in the selection list. */
        private Convertor convertor;
        private DOMBuilder convertorConfigDOMBuilder;
        private int convertorConfigNestingLevel = 0;
        private Convertor.FormatCache fromFormatCache = new DefaultFormatCache();
        private Convertor.FormatCache toFormatCache = new DefaultFormatCache();

        public SelectionListHandler(Locale locale) {
            this.locale = locale;
        }

        public void startDocument()
                throws SAXException {
        }

        public void endDocument()
                throws SAXException {
        }

        public void endDTD()
                throws SAXException {
        }

        public void startDTD(String name, String publicId, String systemId)
                throws SAXException {
        }

        public void startElement(String namespaceURI, String localName, String qName, Attributes attributes)
                throws SAXException {
            if (convertorConfigNestingLevel > 0) {
                convertorConfigNestingLevel++;
                convertorConfigDOMBuilder.startElement(namespaceURI, localName,  qName, attributes);
            } else if (namespaceURI.equals(Constants.DEFINITION_NS)) {
                if (localName.equals("item")) {
                    if (convertor == null) {
                        // if no convertor was explicitely configured, use the default one of the datatype
                        convertor = datatype.getConvertor();
                    }
                    hasLabel = false;

                    String unparsedValue = attributes.getValue("value");
                    if (unparsedValue == null || "".equals(unparsedValue)) {
                        // Empty (or null) value translates into the empty string
                        currentValueAsString = "";
                    } else {
                        ConversionResult conversionResult = convertor.convertFromString(unparsedValue, locale, fromFormatCache);
                        if (!conversionResult.isSuccessful()) {
                            throw new SAXException("Could not interpret the following value: \"" + unparsedValue + "\".");
                        }
                        currentValue = conversionResult.getResult();
                        currentValueAsString = datatype.getConvertor().convertToString(currentValue, locale, toFormatCache);
                    }
                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addCDATAAttribute("value", currentValueAsString);
                    super.startElement(Constants.INSTANCE_NS, localName, Constants.INSTANCE_PREFIX_COLON + localName, attrs);
                } else if (localName.equals("label")) {
                    hasLabel = true;
                    super.startElement(Constants.INSTANCE_NS, localName, Constants.INSTANCE_PREFIX_COLON + localName, attributes);
                } else if (localName.equals("selection-list")) {
                    super.startElement(Constants.INSTANCE_NS, localName, Constants.INSTANCE_PREFIX_COLON + localName, attributes);
                } else if (convertor == null && localName.equals("convertor")) {
                    // record the content of this element in a dom-tree
                    convertorConfigDOMBuilder = new DOMBuilder();
                    convertorConfigDOMBuilder.startElement(namespaceURI, localName, qName, attributes);
                    convertorConfigNestingLevel++;
                } else {
                    super.startElement(namespaceURI, localName, qName, attributes);
                }
            } else {
                super.startElement(namespaceURI, localName, qName, attributes);
            }
        }

        private static final String LABEL_EL = "label";

        public void endElement(String namespaceURI, String localName, String qName)
                throws SAXException {
            if (convertorConfigNestingLevel > 0) {
                convertorConfigNestingLevel--;
                convertorConfigDOMBuilder.endElement(namespaceURI, localName, qName);
                if (convertorConfigNestingLevel == 0) {
                    Element convertorElement = convertorConfigDOMBuilder.getDocument().getDocumentElement();
                    try {
                        convertor = datatype.getBuilder().buildConvertor(convertorElement);
                    } catch (Exception e) {
                        throw new SAXException("Error building convertor from convertor configuration embedded in selection list XML.", e);
                    }
                }
            } else if (namespaceURI.equals(Constants.DEFINITION_NS)) {
                if (localName.equals("item")) {
                    if (!hasLabel) {
                        // make the label now
                        super.startElement(Constants.INSTANCE_NS, LABEL_EL, Constants.INSTANCE_PREFIX_COLON + LABEL_EL, new AttributesImpl());
                        super.characters(currentValueAsString.toCharArray(), 0, currentValueAsString.length());
                        super.endElement(Constants.INSTANCE_NS, LABEL_EL, Constants.INSTANCE_PREFIX_COLON + LABEL_EL);
                    }
                    super.endElement(Constants.INSTANCE_NS, localName, Constants.INSTANCE_PREFIX_COLON + localName);
                } else if (localName.equals("label")) {
                    super.endElement(Constants.INSTANCE_NS, localName, Constants.INSTANCE_PREFIX_COLON + localName);
                } else if (localName.equals("selection-list")) {
                    super.endElement(Constants.INSTANCE_NS, localName, Constants.INSTANCE_PREFIX_COLON + localName);
                } else {
                    super.endElement(namespaceURI, localName, qName);
                }
            } else {
                super.endElement(namespaceURI, localName, qName);
            }
        }

        public void comment(char ch[], int start, int len)
                throws SAXException {
            if (convertorConfigNestingLevel > 0) {
                convertorConfigDOMBuilder.comment(ch, start, len);
            } else
                super.comment(ch, start, len);
        }

        public void startPrefixMapping(String prefix, String uri)
                throws SAXException {
            if (convertorConfigNestingLevel > 0) {
                convertorConfigDOMBuilder.startPrefixMapping(prefix, uri);
            } else
                super.startPrefixMapping(prefix, uri);
        }

        public void endPrefixMapping(String prefix)
                throws SAXException {
            if (convertorConfigNestingLevel > 0) {
                convertorConfigDOMBuilder.endPrefixMapping(prefix);
            } else
                super.endPrefixMapping(prefix);
        }

        public void characters(char c[], int start, int len)
                throws SAXException {
            if (convertorConfigNestingLevel > 0) {
                convertorConfigDOMBuilder.characters(c, start, len);
            } else
                super.characters(c, start, len);
        }

        public void ignorableWhitespace(char c[], int start, int len)
                throws SAXException {
            if (convertorConfigNestingLevel > 0) {
                convertorConfigDOMBuilder.ignorableWhitespace(c, start, len);
            } else
                super.ignorableWhitespace(c, start, len);
        }

        public void processingInstruction(String target, String data)
                throws SAXException {
            if (convertorConfigNestingLevel > 0) {
                convertorConfigDOMBuilder.processingInstruction(target, data);
            } else
                super.processingInstruction(target, data);
        }

        public void skippedEntity(String name)
                throws SAXException {
            if (convertorConfigNestingLevel > 0) {
                convertorConfigDOMBuilder.skippedEntity(name);
            } else
                super.skippedEntity(name);
        }

        public void startEntity(String name)
                throws SAXException {
            if (convertorConfigNestingLevel > 0) {
                convertorConfigDOMBuilder.startEntity(name);
            } else
                super.startEntity(name);
        }

        public void endEntity(String name)
                throws SAXException {
            if (convertorConfigNestingLevel > 0) {
                convertorConfigDOMBuilder.endEntity(name);
            } else
                super.endEntity(name);
        }

        public void startCDATA()
                throws SAXException {
            if (convertorConfigNestingLevel > 0) {
                convertorConfigDOMBuilder.startCDATA();
            } else
                super.startCDATA();
        }

        public void endCDATA()
                throws SAXException {
            if (convertorConfigNestingLevel > 0) {
                convertorConfigDOMBuilder.endCDATA();
            } else
                super.endCDATA();
        }
    }
}
