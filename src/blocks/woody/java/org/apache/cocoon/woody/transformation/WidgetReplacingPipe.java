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
package org.apache.cocoon.woody.transformation;

import java.util.Locale;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.cocoon.i18n.I18nUtils;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.formmodel.Repeater;
import org.apache.cocoon.woody.formmodel.Widget;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.xml.SaxBuffer;
import org.apache.commons.jxpath.JXPathException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The basic operation of this Pipe is that it replaces wt:widget (in the
 * {@link Constants#WT_NS} namespace) tags (having an id attribute)
 * by the XML representation of the corresponding widget instance.
 *
 * <p>These XML fragments (normally all in the {@link Constants#WI_NS "Woody Instance"} namespace), can
 * then be translated to a HTML presentation by an XSL. This XSL will then only have to style
 * individual widget, and will not need to do the whole page layout.
 *
 * <p>For more information about the supported tags and their function, see the user documentation
 * for the woody template transformer.</p>
 * 
 * @version CVS $Id: WidgetReplacingPipe.java,v 1.25 2004/03/09 13:54:12 reinhard Exp $
 */
public class WidgetReplacingPipe extends AbstractXMLPipe {

    private static final String REPEATER_SIZE = "repeater-size";
    private static final String REPEATER_WIDGET_LABEL = "repeater-widget-label";
    private static final String WIDGET_LABEL = "widget-label";
    private static final String WIDGET = "widget";
    private static final String LOCATION = "location";
    private static final String REPEATER_WIDGET = "repeater-widget";
    private static final String CONTINUATION_ID = "continuation-id";
    private static final String FORM_TEMPLATE_EL = "form-template";
    private static final String STYLING_EL = "styling";

    protected Widget contextWidget;

    /**
     * Indicates whether we're currently in a widget element.
     */
    protected boolean inWidgetElement;

    /**
     * Buffer used to temporarily record SAX events.
     */
    protected SaxBuffer saxBuffer;

    /**
     * Counts the element nesting.
     */
    protected int elementNestingCounter;

    /**
     * Contains the value of the {@link #elementNestingCounter} on the moment the transformer
     * encountered a wi:widget element. Used to detect the corresponding endElement call
     * for the wi:widget element.
     */
    protected int widgetElementNesting;

    /**
     * If {@link #inWidgetElement} = true, then this contains the widget currenlty being handled.
     */
    protected Widget widget;

    /**
     * Boolean indicating wether the current widget requires special repeater-treatement.
     */
    protected boolean repeaterWidget;

    protected WidgetReplacingPipe.InsertStylingContentHandler stylingHandler = new WidgetReplacingPipe.InsertStylingContentHandler();
    protected WoodyPipelineConfig pipeContext;

    /**
     * Have we encountered a <wi:style> element in a widget ?
     */
    protected boolean gotStylingElement;

    /** 
     * Namespace prefix used for the namespace <code>Constants.WT_NS</code>.
     */
    protected String namespacePrefix;
    

    public void init(Widget newContextWidget, WoodyPipelineConfig newPipeContext) {
        contextWidget = newContextWidget;
        inWidgetElement = false;
        elementNestingCounter = 0;
        pipeContext = newPipeContext;
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes attributes)
            throws SAXException {
        elementNestingCounter++;

        if (inWidgetElement) {
            if (elementNestingCounter == widgetElementNesting + 1 &&
                Constants.WI_NS.equals(namespaceURI) && STYLING_EL.equals(localName)) {
                gotStylingElement = true;
            }
            saxBuffer.startElement(namespaceURI, localName, qName, attributes);
        } else if (Constants.WT_NS.equals(namespaceURI)) {
            if (localName.equals(WIDGET) || localName.equals(REPEATER_WIDGET)) {
                checkContextWidgetAvailable(qName);
                inWidgetElement = true;
                widgetElementNesting = elementNestingCounter;
                gotStylingElement = false;
                saxBuffer = new SaxBuffer();
                // retrieve widget here, but its XML will only be streamed in the endElement call
                widget = getWidget(attributes);
                repeaterWidget = localName.equals(REPEATER_WIDGET);
                if (repeaterWidget && !(widget instanceof Repeater)) {
                    throw new SAXException("WoodyTemplateTransformer: the element \"repeater-widget\" can only be used for repeater widgets.");
                }
            } else if (localName.equals(WIDGET_LABEL)) {
                checkContextWidgetAvailable(qName);
                Widget widget = getWidget(attributes);
                widget.generateLabel(contentHandler);
            } else if (localName.equals(REPEATER_WIDGET_LABEL)) {
                checkContextWidgetAvailable(qName);
                Widget widget = getWidget(attributes);
                if (!(widget instanceof Repeater)) {
                    throw new SAXException("WoodyTemplateTransformer: the element \"repeater-widget-label\" can only be used for repeater widgets.");
                }
                String widgetId = attributes.getValue("widget-id");
                if (widgetId == null || widgetId.equals("")) {
                    throw new SAXException("WoodyTemplateTransformer: the element \"repeater-widget-label\" requires a \"widget-id\" attribute.");
                }
                ((Repeater)widget).generateWidgetLabel(widgetId, contentHandler);
            } else if (localName.equals(REPEATER_SIZE)) {
                checkContextWidgetAvailable(qName);
                Widget widget = getWidget(attributes);
                if (!(widget instanceof Repeater))
                    throw new SAXException("WoodyTemplateTransformer: the element \"repeater-size\" can only be used for repeater widgets.");
                contentHandler.startPrefixMapping(Constants.WI_PREFIX, Constants.WI_NS);
                ((Repeater)widget).generateSize(contentHandler);
                contentHandler.endPrefixMapping(Constants.WI_PREFIX);
            } else if (localName.equals(FORM_TEMPLATE_EL)) {
                if (contextWidget != null) {
                    throw new SAXException("Detected nested wt:form-template elements, this is not allowed.");
                }
                contentHandler.startPrefixMapping(Constants.WI_PREFIX, Constants.WI_NS);

                // ====> Retrieve the form

                // first look for the form using the location attribute, if any
                String formJXPath = attributes.getValue(LOCATION);
                if (formJXPath != null) {
                    // remove the location attribute
                    AttributesImpl attrsCopy = new AttributesImpl(attributes);
                    attrsCopy.removeAttribute(attributes.getIndex(LOCATION));
                    attributes = attrsCopy;
                }
                contextWidget = pipeContext.findForm(formJXPath);

                // ====> Determine the Locale
                //TODO pull this locale stuff also up in the Config object?

                String localeAttr = attributes.getValue("locale");
                if (localeAttr != null) { // first use value of locale attribute if any
                    localeAttr = pipeContext.translateText(localeAttr);
                    pipeContext.setLocale(I18nUtils.parseLocale(localeAttr));
                } else if (pipeContext.getLocaleParameter() != null) { // then use locale specified as transformer parameter, if any
                    pipeContext.setLocale(pipeContext.getLocaleParameter());
                } else { // use locale specified in bizdata supplied for form
                    Object locale = null;
                    try {
                        locale = pipeContext.evaluateExpression("/locale");
                    } catch (JXPathException e) {}
                    if (locale != null) {
                        pipeContext.setLocale((Locale)locale);
                    }
                    else {
                        // final solution: use locale defined in the server machine
                        pipeContext.setLocale(Locale.getDefault());
                    }
                }

                String[] namesToTranslate = {"action"};
                Attributes transAtts = translateAttributes(attributes, namesToTranslate);
                contentHandler.startElement(Constants.WI_NS , FORM_TEMPLATE_EL, Constants.WI_PREFIX_COLON + FORM_TEMPLATE_EL, transAtts);

            } else if (localName.equals(CONTINUATION_ID)){
                // Insert the continuation id
                // FIXME(SW) we could avoid costly JXPath evaluation if we had the objectmodel here.
                Object idObj = pipeContext.evaluateExpression("$continuation/id");
                if (idObj == null) {
                    throw new SAXException("No continuation found");
                }

                String id = idObj.toString();
                contentHandler.startPrefixMapping(Constants.WI_PREFIX, Constants.WI_NS);
                contentHandler.startElement(Constants.WI_NS, CONTINUATION_ID, Constants.WI_PREFIX_COLON + CONTINUATION_ID, attributes);
                contentHandler.characters(id.toCharArray(), 0, id.length());
                contentHandler.endElement(Constants.WI_NS, CONTINUATION_ID, Constants.WI_PREFIX_COLON + CONTINUATION_ID);
                contentHandler.endPrefixMapping(Constants.WI_PREFIX);
            } else {
                throw new SAXException("WoodyTemplateTransformer: Unsupported element: " + localName);
            }
        } else {
            super.startElement(namespaceURI, localName, qName, attributes);
        }
    }

    private void checkContextWidgetAvailable(String widgetElementName) throws SAXException {
        if (contextWidget == null)
            throw new SAXException(widgetElementName + " cannot be used outside a wt:form-template element");
    }

    private Attributes translateAttributes(Attributes attributes, String[] names) {
        AttributesImpl newAtts = new AttributesImpl(attributes);
        if (names!= null) {
            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                int position = newAtts.getIndex(name);
                String newValue = pipeContext.translateText(newAtts.getValue(position));
                newAtts.setValue(position, newValue);                
            }
        }
        return newAtts;
    }


    protected Widget getWidget(Attributes attributes) throws SAXException {
        String widgetId = attributes.getValue("id");
        if (widgetId == null || widgetId.equals("")) {
            throw new SAXException("WoodyTemplateTransformer: missing id attribute on a woody element.");
        }
        Widget widget = contextWidget.getWidget(widgetId);
        if (widget == null) {
            throw new SAXException("WoodyTemplateTransformer: widget with id \"" + widgetId + "\" does not exist in the container " + contextWidget.getFullyQualifiedId());
        }
        return widget;
    }

    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {

        if (inWidgetElement) {
            if (elementNestingCounter == widgetElementNesting && Constants.WT_NS.equals(namespaceURI)
                && (localName.equals(WIDGET) || localName.equals(REPEATER_WIDGET))) {
                    if (repeaterWidget) {
                        Repeater repeater = (Repeater)widget;
                        WidgetReplacingPipe rowPipe = new WidgetReplacingPipe();
                        int rowCount = repeater.getSize();
                        for (int i = 0; i < rowCount; i++) {
                            Repeater.RepeaterRow row = repeater.getRow(i);
                            rowPipe.init(row, pipeContext);
                            rowPipe.setContentHandler(contentHandler);
                            rowPipe.setLexicalHandler(lexicalHandler);
                            saxBuffer.toSAX(rowPipe);
                            rowPipe.recycle();
                        }
                    } else {
                        stylingHandler.recycle();
                        stylingHandler.setSaxFragment(saxBuffer);
                        stylingHandler.setContentHandler(contentHandler);
                        stylingHandler.setLexicalHandler(lexicalHandler);
                        contentHandler.startPrefixMapping(Constants.WI_PREFIX, Constants.WI_NS);
                        widget.generateSaxFragment(stylingHandler, pipeContext.getLocale());
                        contentHandler.endPrefixMapping(Constants.WI_PREFIX);
                    }
                    inWidgetElement = false;
                    widget = null;
                } else {
                    saxBuffer.endElement(namespaceURI, localName, qName);
                }
        } else if (Constants.WT_NS.equals(namespaceURI)) {
            if (localName.equals(WIDGET_LABEL) || localName.equals(REPEATER_WIDGET_LABEL)
                || localName.equals(REPEATER_SIZE) || localName.equals(CONTINUATION_ID)) {
                // Do nothing
            } else if (localName.equals(FORM_TEMPLATE_EL)) {
                contextWidget = null;
                contentHandler.endElement(Constants.WI_NS, FORM_TEMPLATE_EL,
                                          Constants.WI_PREFIX_COLON + FORM_TEMPLATE_EL);
                contentHandler.endPrefixMapping(Constants.WI_PREFIX);
            } else {
                super.endElement(namespaceURI, localName, qName);
            }
        } else {
            super.endElement(namespaceURI, localName, qName);
        }
        elementNestingCounter--;
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        if (inWidgetElement) {
            saxBuffer.startPrefixMapping(prefix, uri);
        } else {
            super.startPrefixMapping(prefix, uri);
        }
    }

    public void endPrefixMapping(String prefix)
            throws SAXException {
        if (inWidgetElement) {
            saxBuffer.endPrefixMapping(prefix);
        } else {
            super.endPrefixMapping(prefix);
        }
    }

    public void characters(char c[], int start, int len)
            throws SAXException {
        if (inWidgetElement) {
            saxBuffer.characters(c, start, len);
        } else {
            super.characters(c, start, len);
        }
    }

    public void ignorableWhitespace(char c[], int start, int len)
            throws SAXException {
        if (inWidgetElement) {
            saxBuffer.ignorableWhitespace(c, start, len);
        } else {
            super.ignorableWhitespace(c, start, len);
        }
    }

    public void processingInstruction(String target, String data)
            throws SAXException {
        if (inWidgetElement) {
            saxBuffer.processingInstruction(target, data);
        } else {
            super.processingInstruction(target, data);
        }
    }

    public void skippedEntity(String name)
            throws SAXException {
        if (inWidgetElement) {
            saxBuffer.skippedEntity(name);
        } else {
            super.skippedEntity(name);
        }
    }

    public void startEntity(String name)
            throws SAXException {
        if (inWidgetElement)
            saxBuffer.startEntity(name);
        else
            super.startEntity(name);
    }

    public void endEntity(String name)
            throws SAXException {
        if (inWidgetElement) {
            saxBuffer.endEntity(name);
        } else {
            super.endEntity(name);
        }
    }

    public void startCDATA()
            throws SAXException {
        if (inWidgetElement) {
            saxBuffer.startCDATA();
        } else {
            super.startCDATA();
        }
    }

    public void endCDATA()
            throws SAXException {
        if (inWidgetElement) {
            saxBuffer.endCDATA();
        } else {
            super.endCDATA();
        }
    }

    public void comment(char ch[], int start, int len)
            throws SAXException {
        if (inWidgetElement) {
            saxBuffer.comment(ch, start, len);
        } else {
            super.comment(ch, start, len);
        }
    }

    public void recycle() {
        super.recycle();
        this.contextWidget = null;
        this.widget = null;
        this.namespacePrefix = null;
    }

    /**
     * This ContentHandler helps in inserting SAX events before the closing tag of the root
     * element.
     */
    public class InsertStylingContentHandler extends AbstractXMLPipe implements Recyclable {
        private int elementNesting;
        private SaxBuffer saxBuffer;

        public void setSaxFragment(SaxBuffer saxFragment) {
            saxBuffer = saxFragment;
        }

        public void recycle() {
            super.recycle();
            elementNesting = 0;
            saxBuffer = null;
        }

        public void startElement(String uri, String loc, String raw, Attributes a)
                throws SAXException {
            elementNesting++;
            super.startElement(uri, loc, raw, a);
        }

        public void endElement(String uri, String loc, String raw)
                throws SAXException {
            elementNesting--;
            if (elementNesting == 0 && saxBuffer != null) {
                if (gotStylingElement) {
                    // Just deserialize
                    saxBuffer.toSAX(contentHandler);
                } else {
                    // Insert an enclosing <wi:styling>
                    contentHandler.startElement(Constants.WI_NS, STYLING_EL, Constants.WI_PREFIX_COLON + STYLING_EL, Constants.EMPTY_ATTRS);
                    saxBuffer.toSAX(contentHandler);
                    contentHandler.endElement(Constants.WI_NS, STYLING_EL, Constants.WI_PREFIX_COLON + STYLING_EL);
                } 
            }
            super.endElement(uri, loc, raw);
        }
    }
}
