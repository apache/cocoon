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
package org.apache.cocoon.woody.transformation;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.cocoon.components.sax.XMLByteStreamCompiler;
import org.apache.cocoon.components.sax.XMLByteStreamInterpreter;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.formmodel.Repeater;
import org.apache.cocoon.woody.formmodel.Widget;
import org.apache.cocoon.woody.formmodel.Form;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.i18n.I18nUtils;
import org.apache.commons.jxpath.JXPathException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The basic operation of this Pipe is that it replaces wi:widget tags (having an id attribute)
 * by the XML representation of the corresponding widget instance.
 *
 * <p>These XML fragments (normally all in the {@link Constants#WI_NS "Woody Instance"} namespace, can
 * then be translated to a HTML presentation by an XSL. This XSL will then only have to style
 * individual widget, and will not need to do the whole page layout.
 *
 * <p>For more information about the supported tags and their function, see the user documentation
 * for the woody template transformer.</p>
 */
public class WidgetReplacingPipe extends AbstractXMLPipe {
         
    protected static final String FORM_TEMPLATE_EL = "form-template";
    protected static final String STYLING_EL = "styling";
    /** Default key under which the woody form is stored in the JXPath context. */
    public static final String WOODY_FORM = "woody-form";

    protected Widget contextWidget;
    /** Indicates whether we're currently in a widget element. */
    protected boolean inWidgetElement;
    /** Compiler used to record the XML inside wi:widget elements. */
    protected XMLByteStreamCompiler xmlCompiler = new XMLByteStreamCompiler();
    /** Counts the element nesting. */
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
    /** Boolean indicating wether the current widget requires special repeater-treatement. */
    protected boolean repeaterWidget;

    protected WoodyTemplateTransformer.InsertStylingContentHandler stylingHandler = new WoodyTemplateTransformer.InsertStylingContentHandler();
    protected WoodyTemplateTransformer pipeContext;

    public void init(Widget contextWidget, WoodyTemplateTransformer pipeContext) {
        this.contextWidget = contextWidget;
        inWidgetElement = false;
        elementNestingCounter = 0;
        this.pipeContext = pipeContext;
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes attributes)
            throws SAXException {
        elementNestingCounter++;
        if (!inWidgetElement && namespaceURI.equals(Constants.WT_NS)
                && (localName.equals("widget") || localName.equals("repeater-widget"))) {
            checkContextWidgetAvailable(qName);
            inWidgetElement = true;
            widgetElementNesting = elementNestingCounter;
            xmlCompiler.recycle();

            // retrieve widget here, but its XML will only be streamed in the endElement call
            widget = getWidget(attributes);
            repeaterWidget = localName.equals("repeater-widget");
            if (repeaterWidget && !(widget instanceof Repeater))
                throw new SAXException("WoodyTemplateTransformer: the element \"repeater-widget\" can only be used for repeater widgets.");
        } else if (inWidgetElement) {
            xmlCompiler.startElement(namespaceURI, localName, qName, attributes);
        } else if (namespaceURI.equals(Constants.WT_NS)) {
            if (localName.equals("widget-label")) {
                checkContextWidgetAvailable(qName);
                Widget widget = getWidget(attributes);
                widget.generateLabel(contentHandler);
            } else if (localName.equals("repeater-widget-label")) {
                checkContextWidgetAvailable(qName);
                Widget widget = getWidget(attributes);
                if (!(widget instanceof Repeater))
                    throw new SAXException("WoodyTemplateTransformer: the element \"repeater-widget-label\" can only be used for repeater widgets.");
                String widgetId = attributes.getValue("widget-id");
                if (widgetId == null || widgetId.equals(""))
                    throw new SAXException("WoodyTemplateTransformer: the element \"repeater-widget-label\" requires a \"widget-id\" attribute.");
                ((Repeater)widget).generateWidgetLabel(widgetId, contentHandler);
            } else if (localName.equals("repeater-size")) {
                checkContextWidgetAvailable(qName);
                Widget widget = getWidget(attributes);
                if (!(widget instanceof Repeater))
                    throw new SAXException("WoodyTemplateTransformer: the element \"repeater-size\" can only be used for repeater widgets.");
                contentHandler.startPrefixMapping(Constants.WI_PREFIX, Constants.WI_NS);
                ((Repeater)widget).generateSize(contentHandler);
                contentHandler.endPrefixMapping(Constants.WI_PREFIX);
            } else if (localName.equals(FORM_TEMPLATE_EL)) {
                if (contextWidget != null)
                    throw new SAXException("Detected nested wt:form-template elements, this is not allowed.");
                contentHandler.startPrefixMapping(Constants.WI_PREFIX, Constants.WI_NS);

                // ====> Retrieve the form

                // first look for the form using the location attribute, if any
                String formJXPath = attributes.getValue("location");
                if (formJXPath != null) {
                    // remove the location attribute
                    AttributesImpl attrsCopy = new AttributesImpl(attributes);
                    attrsCopy.removeAttribute(attributes.getIndex("location"));
                    attributes = attrsCopy;

                    Object form = pipeContext.getJXPathContext().getValue(formJXPath);
                    if (form == null)
                        throw new SAXException("No form found at location \"" + formJXPath + "\".");
                    if (!(form instanceof Form))
                        throw new SAXException("Object returned by expression \"" + formJXPath + "\" is not a Woody Form.");
                    contextWidget = (Form)form;
                } else if (pipeContext.getAttributeName() != null) { // then see if an attribute-name was specified
                    contextWidget = (Form)pipeContext.getRequest().getAttribute(pipeContext.getAttributeName());
                    if (contextWidget == null)
                        throw new SAXException("No form found in request attribute with name \"" + pipeContext.getAttributeName() + "\"");
                } else { // and then see if we got a form from the flow
                    formJXPath = "/" + WoodyTemplateTransformer.WOODY_FORM;
                    Object form = null;
                    try {
                        form = pipeContext.getJXPathContext().getValue(formJXPath);
                    } catch (JXPathException e) {}
                    if (form != null)
                        contextWidget = (Form)form;
                    else
                        throw new SAXException("No Woody form found.");
                }

                // ====> Determine the Locale

                String localeAttr = attributes.getValue("locale");
                if (localeAttr != null) { // first use value of locale attribute if any
                    localeAttr = translateText(localeAttr);
                    pipeContext.setLocale(I18nUtils.parseLocale(localeAttr));
                } else if (pipeContext.getLocaleParameter() != null) { // then use locale specified as transformer parameter, if any
                    pipeContext.setLocale(pipeContext.getLocaleParameter());
                } else { // use locale specified in bizdata supplied for form
                    String localeJXPath = "/locale";
                    Object locale = null;
                    try {
                        locale = pipeContext.getJXPathContext().getValue(localeJXPath);
                    } catch (JXPathException e) {}
                    if (locale != null)
                        pipeContext.setLocale((Locale)locale);
                    else // final solution: use US locale
                        pipeContext.setLocale(Locale.getDefault());
                }

                String[] namesToTranslate = {"action"};
                Attributes transAtts = translateAttributes(attributes, namesToTranslate);
                contentHandler.startElement(Constants.WI_NS , FORM_TEMPLATE_EL, Constants.WI_PREFIX_COLON + FORM_TEMPLATE_EL, transAtts);
            } else {
                throw new SAXException("Unsupported WoodyTemplateTransformer element: " + localName);
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
                String newValue = translateText(newAtts.getValue(position));
                newAtts.setValue(position, newValue);                
            }
        }
        return newAtts;
    }

    /**
     * Replaces JXPath expressions embedded inside #{ and } by their value.
     */
    private String translateText(String original) {
        StringBuffer expression;
        StringBuffer translated = new StringBuffer();
        StringReader in = new StringReader(original);
        int chr;
        try {
            while ((chr = in.read()) != -1) {
                char c = (char) chr;
                if (c == '#') {
                    chr = in.read();
                    if (chr != -1) {
                        c = (char) chr;
                        if (c == '{') {
                            expression = new StringBuffer();
                            boolean more = true;
                            while ( more ) {
                                more = false;
                                if ((chr = in.read()) != -1) {
                                    c = (char)chr;
                                    if (c != '}') {
                                        expression.append(c);
                                        more = true;
                                    } else {
                                        translated.append(evaluateExpression(expression.toString()));
                                    }
                                } else {
                                    translated.append('#').append('{').append(expression);
                                }
                            } 
                        }
                    } else {
                        translated.append((char) chr);
                    }
                } else {
                    translated.append(c);
                }
            }
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }

        return translated.toString();
    }

    private String evaluateExpression(String expression) {
        return pipeContext.getJXPathContext().getValue(expression).toString();
    }

    protected Widget getWidget(Attributes attributes) throws SAXException {
        String widgetId = attributes.getValue("id");
        if (widgetId == null || widgetId.equals(""))
            throw new SAXException("WoodyTemplateTransformer: missing id attribute on a woody element.");
        Widget widget = contextWidget.getWidget(widgetId);
        if (widget == null)
            throw new SAXException("WoodyTemplateTransformer: widget with id \"" + widgetId + "\" does not exist in the container " + contextWidget.getFullyQualifiedId());
        return widget;
    }

    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        if (inWidgetElement && elementNestingCounter == widgetElementNesting &&
                namespaceURI.equals(Constants.WT_NS)
                && (localName.equals("widget") || localName.equals("repeater-widget"))) {

            if (repeaterWidget) {
                Repeater repeater = (Repeater)widget;
                WidgetReplacingPipe rowPipe = new WidgetReplacingPipe();
                XMLByteStreamInterpreter interpreter = new XMLByteStreamInterpreter();
                int rowCount = repeater.getSize();
                Object saxFragment = xmlCompiler.getSAXFragment();
                for (int i = 0; i < rowCount; i++) {
                    Repeater.RepeaterRow row = repeater.getRow(i);
                    rowPipe.init(row, pipeContext);
                    rowPipe.setContentHandler(contentHandler);
                    rowPipe.setLexicalHandler(lexicalHandler);
                    interpreter.setConsumer(rowPipe);
                    interpreter.deserialize(saxFragment);
                    interpreter.recycle();
                    rowPipe.recycle();
                }
            } else {
                stylingHandler.recycle();
                stylingHandler.setSaxFragment(xmlCompiler.getSAXFragment());
                stylingHandler.setContentHandler(contentHandler);
                stylingHandler.setLexicalHandler(lexicalHandler);
                contentHandler.startPrefixMapping(Constants.WI_PREFIX, Constants.WI_NS);
                widget.generateSaxFragment(stylingHandler, pipeContext.getLocale());
                contentHandler.endPrefixMapping(Constants.WI_PREFIX);
            }

            inWidgetElement = false;
            widget = null;
        } else if (inWidgetElement) {
            xmlCompiler.endElement(namespaceURI, localName, qName);
        } else if (namespaceURI.equals(Constants.WT_NS) &&
                (localName.equals("widget-label") || localName.equals("repeater-widget-label")
                || localName.equals("repeater-size"))) {
            // do nothing
        } else if (namespaceURI.equals(Constants.WT_NS) && localName.equals(FORM_TEMPLATE_EL)) {
            contextWidget = null;
            contentHandler.endElement(Constants.WI_NS, FORM_TEMPLATE_EL, Constants.WI_PREFIX_COLON + FORM_TEMPLATE_EL);
            contentHandler.endPrefixMapping(Constants.WI_PREFIX);
        } else {
            super.endElement(namespaceURI, localName, qName);
        }
        elementNestingCounter--;
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        if (inWidgetElement)
            xmlCompiler.startPrefixMapping(prefix, uri);
        else
            super.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix)
            throws SAXException {
        if (inWidgetElement)
            xmlCompiler.endPrefixMapping(prefix);
        else
            super.endPrefixMapping(prefix);
    }

    public void characters(char c[], int start, int len)
            throws SAXException {
        if (inWidgetElement)
            xmlCompiler.characters(c, start, len);
        else
            super.characters(c, start, len);
    }

    public void ignorableWhitespace(char c[], int start, int len)
            throws SAXException {
        if (inWidgetElement)
            xmlCompiler.ignorableWhitespace(c, start, len);
        else
            super.ignorableWhitespace(c, start, len);
    }

    public void processingInstruction(String target, String data)
            throws SAXException {
        if (inWidgetElement)
            xmlCompiler.processingInstruction(target, data);
        else
            super.processingInstruction(target, data);
    }

    public void skippedEntity(String name)
            throws SAXException {
        if (inWidgetElement)
            xmlCompiler.skippedEntity(name);
        else
            super.skippedEntity(name);
    }

    public void startEntity(String name)
            throws SAXException {
        if (inWidgetElement)
            xmlCompiler.startEntity(name);
        else
            super.startEntity(name);
    }

    public void endEntity(String name)
            throws SAXException {
        if (inWidgetElement)
            xmlCompiler.endEntity(name);
        else
            super.endEntity(name);
    }

    public void startCDATA()
            throws SAXException {
        if (inWidgetElement)
            xmlCompiler.startCDATA();
        else
            super.startCDATA();
    }

    public void endCDATA()
            throws SAXException {
        if (inWidgetElement)
            xmlCompiler.endCDATA();
        else
            super.endCDATA();
    }

    public void comment(char ch[], int start, int len)
            throws SAXException {
        if (inWidgetElement)
            xmlCompiler.comment(ch, start, len);
        else
            super.comment(ch, start, len);
    }

    /**
     * This ContentHandler helps in inserting SAX events before the closing tag of the root
     * element.
     */
    public class InsertStylingContentHandler extends AbstractXMLPipe implements Recyclable {
        private int elementNesting = 0;
        private XMLByteStreamInterpreter interpreter = new XMLByteStreamInterpreter();
        private Object saxFragment;

        public void setSaxFragment(Object saxFragment) {
            this.saxFragment = saxFragment;
        }

        public void recycle() {
            super.recycle();
            elementNesting = 0;
            interpreter.recycle();
            saxFragment = null;
        }

        public void startElement(String uri, String loc, String raw, Attributes a)
                throws SAXException {
            elementNesting++;
            super.startElement(uri, loc, raw, a);
        }

        public void endElement(String uri, String loc, String raw)
                throws SAXException {
            elementNesting--;
            if (elementNesting == 0 && saxFragment != null) {
                interpreter.setContentHandler(contentHandler);
                interpreter.setLexicalHandler(lexicalHandler);
                contentHandler.startElement(Constants.WI_NS, STYLING_EL, Constants.WI_PREFIX_COLON + STYLING_EL, Constants.EMPTY_ATTRS);
                interpreter.deserialize(saxFragment);
                contentHandler.endElement(Constants.WI_NS, STYLING_EL, Constants.WI_PREFIX_COLON + STYLING_EL);
            }
            super.endElement(uri, loc, raw);
        }
    }

    public void recycle() {
        super.recycle();
        this.contextWidget = null;
        this.widget = null;
    }

}
