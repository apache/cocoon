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
package org.apache.cocoon.forms.transformation;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.formmodel.AggregateField;
import org.apache.cocoon.forms.formmodel.Repeater;
import org.apache.cocoon.forms.formmodel.Struct;
import org.apache.cocoon.forms.formmodel.Union;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.forms.validation.ValidationErrorAware;
import org.apache.cocoon.i18n.I18nUtils;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.xml.SaxBuffer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.jxpath.JXPathException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;

// TODO: Reduce the Element creation and deletion churn by using startElement
// and endElement methods which do not create or use Elements on the stack.
// The corresponding TODO in the EffectPipe needs to be completed first.

/**
 * The basic operation of this Pipe is that it replaces wt:widget (in the
 * {@link Constants#TEMPLATE_NS} namespace) tags (having an id attribute)
 * by the XML representation of the corresponding widget instance.
 *
 * <p>These XML fragments (normally all in the {@link Constants#INSTANCE_NS "CForms Instance"} namespace), can
 * then be translated to a HTML presentation by an XSL. This XSL will then only have to style
 * individual widget, and will not need to do the whole page layout.
 *
 * <p>For more information about the supported tags and their function, see the user documentation
 * for the forms template transformer.</p>
 *
 * @author Timothy Larson
 * @version CVS $Id: EffectWidgetReplacingPipe.java,v 1.5 2004/03/15 14:21:24 tim Exp $
 */
public class EffectWidgetReplacingPipe extends EffectPipe {

    /**
     * Form location attribute on <code>ft:form-template</code> element, containing
     * JXPath expression which should result in Form object.
     *
     * @see FormPipelineConfig#findForm
     */
    private static final String LOCATION = "location";

    private static final String CLASS = "class";
    private static final String CONTINUATION_ID = "continuation-id";
    private static final String FORM_TEMPLATE_EL = "form-template";
    private static final String NEW = "new";
    private static final String REPEATER_SIZE = "repeater-size";
    private static final String REPEATER_WIDGET = "repeater-widget";
    private static final String REPEATER_WIDGET_LABEL = "repeater-widget-label";
    private static final String AGGREGATE_WIDGET = "aggregate-widget";
    private static final String STRUCT = "struct";
    private static final String STYLING_EL = "styling";
    private static final String UNION = "union";
    private static final String VALIDATION_ERROR = "validation-error";
    private static final String WIDGET_LABEL = "widget-label";
    private static final String WIDGET = "widget";

    protected Widget contextWidget;
    protected LinkedList contextWidgets;
    protected String widgetId;
    protected Widget widget;
    protected Map classes;

    private final DocHandler                 docHandler             = new DocHandler();
    private final FormHandler                formHandler            = new FormHandler();
    private final NestedHandler              nestedHandler          = new NestedHandler();
    private final WidgetLabelHandler         widgetLabelHandler     = new WidgetLabelHandler();
    private final WidgetHandler              widgetHandler          = new WidgetHandler();
    private final RepeaterSizeHandler        repeaterSizeHandler    = new RepeaterSizeHandler();
    private final RepeaterWidgetLabelHandler repeaterWidgetLabelHandler = new RepeaterWidgetLabelHandler();
    private final RepeaterWidgetHandler      repeaterWidgetHandler  = new RepeaterWidgetHandler();
    private final AggregateWidgetHandler     aggregateWidgetHandler = new AggregateWidgetHandler();
    private final StructHandler              structHandler          = new StructHandler();
    private final UnionHandler               unionHandler           = new UnionHandler();
    private final UnionPassThruHandler       unionPassThruHandler   = new UnionPassThruHandler();
    private final NewHandler                 newHandler             = new NewHandler();
    private final ClassHandler               classHandler           = new ClassHandler();
    private final ContinuationIdHandler      continuationIdHandler  = new ContinuationIdHandler();
    private final StylingContentHandler      stylingHandler         = new StylingContentHandler();
    private final ValidationErrorHandler     validationErrorHandler = new ValidationErrorHandler();

    /**
     * Map containing all handlers
     */
    private final Map templates = new HashMap(12, 1);

    protected FormPipelineConfig pipeContext;

    /**
     * Have we encountered a <wi:style> element in a widget ?
     */
    protected boolean gotStylingElement;

    /**
     * Namespace prefix used for the namespace <code>Constants.FT_NS</code>.
     */
    protected String namespacePrefix;


    public EffectWidgetReplacingPipe() {
        // Setup map of templates.
        templates.put(WIDGET, widgetHandler);
        templates.put(WIDGET_LABEL, widgetLabelHandler);
        templates.put(REPEATER_WIDGET, repeaterWidgetHandler);
        templates.put(AGGREGATE_WIDGET, aggregateWidgetHandler);
        templates.put(REPEATER_SIZE, repeaterSizeHandler);
        templates.put(REPEATER_WIDGET_LABEL, repeaterWidgetLabelHandler);
        templates.put(STRUCT, structHandler);
        templates.put(UNION, unionHandler);
        templates.put(NEW, newHandler);
        templates.put(CLASS, classHandler);
        templates.put(CONTINUATION_ID, continuationIdHandler);
        templates.put(VALIDATION_ERROR, validationErrorHandler);
    }

    private void throwSAXException(String message) throws SAXException{
        throw new SAXException("EffectFormTemplateTransformer: " + message);
    }

    public void init(Widget contextWidget, FormPipelineConfig pipeContext) {
        super.init();
        this.pipeContext = pipeContext;

        // Attach document handler
        handler = docHandler;

        // Initialize widget related variables
        contextWidgets = new LinkedList();
        classes = new HashMap();
    }

    protected String getWidgetId(Attributes attributes) throws SAXException {
        String widgetId = attributes.getValue("id");
        if (widgetId == null || widgetId.equals("")) {
            throwSAXException("Missing required widget \"id\" attribute.");
        }
        return widgetId;
    }

    protected Widget getWidget(String widgetId) throws SAXException {
        Widget widget = contextWidget.getWidget(widgetId);
        if (widget == null) {
            if (contextWidget.getFullyQualifiedId() == null) {
                throwSAXException("Widget with id \"" + widgetId + "\" does not exist in the form container.");
            } else {
                throwSAXException("Widget with id \"" + widgetId + "\" does not exist in the container \"" +
                                  contextWidget.getFullyQualifiedId() + "\"");
            }
        }
        return widget;
    }

    protected void getRepeaterWidget(String handler) throws SAXException {
        widgetId = getWidgetId(input.attrs);
        widget = getWidget(widgetId);
        if (!(widget instanceof Repeater)) {
            throwWrongWidgetType("RepeaterWidgetLabelHandler", input.loc, "repeater");
        }
    }

    public void throwWrongWidgetType(String pipeName, String element, String widget) throws SAXException {
        throwSAXException(pipeName + ": Element \"" + element + "\" can only be used for " + widget + " widgets.");
    }

    /**
     * Needed to get things working with JDK 1.3. Can be removed once we
     * don't support that platform any more.
     */
    private ContentHandler getContentHandler() {
        return this.contentHandler;
    }

    /**
     * Needed to get things working with JDK 1.3. Can be removed once we
     * don't support that platform any more.
     */
    private LexicalHandler getLexicalHandler() {
        return this.lexicalHandler;
    }

    public Handler nestedTemplate() throws SAXException {
        if (Constants.TEMPLATE_NS.equals(input.uri)) {
            // Element in forms template namespace.
            Handler handler = (Handler)templates.get(input.loc);
            if (handler != null) {
                return handler;
            } else if (FORM_TEMPLATE_EL.equals(input.loc)) {
                throwSAXException("Element \"form-template\" must not be nested.");
                return null; // Keep the compiler happy.
            } else {
                throwSAXException("Unrecognized template: " + input.loc);
                return null; // Keep the compiler happy.
            }
        } else {
            // Element not in forms namespace.
            return nestedHandler;
        }
    }

    //==============================================
    // Handler classes to transform CForms templates
    //==============================================

    protected class DocHandler extends Handler {
        public Handler process() throws SAXException {
            switch (event) {
            case EVENT_SET_DOCUMENT_LOCATOR:
                return this;
            case EVENT_START_PREFIX_MAPPING:
                if(Constants.TEMPLATE_NS.equals(input.uri)) {
                    // We consume this namespace completely.
                    EffectWidgetReplacingPipe.this.namespacePrefix = input.prefix;
                    return this;
                } else {
                    // Pass through all others.
                    out.copy();
                    return this;
                }
            case EVENT_ELEMENT:
                if (Constants.TEMPLATE_NS.equals(input.uri)) {
                    if (FORM_TEMPLATE_EL.equals(input.loc)) {
                        return formHandler;
                    } else {
                        throwSAXException("CForms template \"" + input.loc +
                                "\" not permitted outside \"form-template\"");
                    }
                } else {
                    // Pass through all others.
                    out.copy();
                    return this;
                }
            case EVENT_END_PREFIX_MAPPING:
                // We consume this namespace completely
                return this;
            default:
                out.copy();
                return this;
            }
        }
    }

    protected class FormHandler extends Handler {
        public Handler process() throws SAXException {
            switch(event) {
            case EVENT_START_ELEMENT:
                if (contextWidget != null) {
                    throwSAXException("Detected nested wt:form-template elements, this is not allowed.");
                }
                out.startPrefixMapping(Constants.INSTANCE_PREFIX, Constants.INSTANCE_NS);

                // ====> Retrieve the form
                // First look for the form using the location attribute, if any
                String formJXPath = input.attrs.getValue(LOCATION);
                if (formJXPath != null) {
                    // remove the location attribute
                    AttributesImpl attrsCopy = new AttributesImpl(input.attrs);
                    attrsCopy.removeAttribute(input.attrs.getIndex(LOCATION));
                    input.attrs = attrsCopy;
                    input.mine = true;
                }
                contextWidget = pipeContext.findForm(formJXPath);

                // ====> Determine the Locale
                //TODO pull this locale stuff also up in the Config object?
                String localeAttr = input.attrs.getValue("locale");
                if (localeAttr != null) { // first use value of locale attribute if any
                    localeAttr = pipeContext.translateText(localeAttr);
                    pipeContext.setLocale(I18nUtils.parseLocale(localeAttr));
                } else if (pipeContext.getLocaleParameter() != null) { // then use locale specified as transformer parameter, if any
                    pipeContext.setLocale(pipeContext.getLocaleParameter());
                } else {
                    //TODO pull this locale stuff also up in the Config object?
                    // use locale specified in bizdata supplied for form
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
                Attributes transAttrs = translateAttributes(input.attrs, namesToTranslate);
                out.element(Constants.INSTANCE_PREFIX, Constants.INSTANCE_NS, "form-template", transAttrs);
                out.startElement();
                return this;
            case EVENT_ELEMENT:
                return nestedTemplate();
            case EVENT_END_ELEMENT:
                out.copy();
                out.endPrefixMapping(Constants.INSTANCE_PREFIX);
                contextWidget = null;
                return this;
            default:
                out.copy();
                return this;
            }
        }
    }

    protected class NestedHandler extends Handler {
        public Handler process() throws SAXException {
            switch(event) {
            case EVENT_ELEMENT:
                return nestedTemplate();
            default:
                out.copy();
                return this;
            }
        }
    }

    protected class WidgetLabelHandler extends Handler {
        public Handler process() throws SAXException {
            switch (event) {
            case EVENT_START_ELEMENT:
                widgetId = getWidgetId(input.attrs);
                Widget widget = getWidget(widgetId);
                widget.generateLabel(getContentHandler());
                widget = null;
                return this;
            case EVENT_ELEMENT:
                return nullHandler;
            case EVENT_END_ELEMENT:
                return this;
            default:
                out.copy();
                return this;
            }
        }
    }

    protected class WidgetHandler extends Handler {
        public Handler process() throws SAXException {
            switch (event) {
            case EVENT_START_ELEMENT:
                widgetId = getWidgetId(input.attrs);
                widget = getWidget(widgetId);
                gotStylingElement = false;
                out.bufferInit();
                return this;

            case EVENT_ELEMENT:
                if (Constants.INSTANCE_NS.equals(input.uri) && STYLING_EL.equals(input.loc)) {
                    gotStylingElement = true;
                }
                return bufferHandler;

            case EVENT_END_ELEMENT:
                stylingHandler.recycle();
                stylingHandler.setSaxFragment(out.getBuffer());
                stylingHandler.setContentHandler(getContentHandler());
                stylingHandler.setLexicalHandler(getLexicalHandler());
                widget.generateSaxFragment(stylingHandler, pipeContext.getLocale());
                widget = null;
                out.bufferFini();
                return this;

            default:
                out.copy();
                return this;
            }
        }
    }

    protected class RepeaterSizeHandler extends Handler {
        public Handler process() throws SAXException {
            switch(event) {
            case EVENT_START_ELEMENT:
                getRepeaterWidget("RepeaterSizeHandler");
                ((Repeater)widget).generateSize(getContentHandler());
                widget = null;
                return this;
            case EVENT_ELEMENT:
                return nullHandler;
            case EVENT_END_ELEMENT:
                return this;
            default:
                out.copy();
                return this;
            }
        }
    }

    protected class RepeaterWidgetLabelHandler extends Handler {
        public Handler process() throws SAXException {
            switch(event) {
            case EVENT_START_ELEMENT:
                getRepeaterWidget("RepeaterWidgetLabelHandler");
                String widgetId = input.attrs.getValue("widget-id");
                if (widgetId == null || widgetId.equals(""))
                    throwSAXException("Element repeater-widget-label missing required widget-id attribute.");
                ((Repeater)widget).generateWidgetLabel(widgetId, getContentHandler());
                widget = null;
                return this;
            case EVENT_ELEMENT:
                return nullHandler;
            case EVENT_END_ELEMENT:
                return this;
            default:
                out.copy();
                return this;
            }
        }
    }

    protected class RepeaterWidgetHandler extends Handler {
        public Handler process() throws SAXException {
            switch(event) {
            case EVENT_START_ELEMENT:
                getRepeaterWidget("RepeaterWidgetHandler");
                out.bufferInit();
                return this;
            case EVENT_ELEMENT:
                return bufferHandler;
            case EVENT_END_ELEMENT:
                Repeater repeater = (Repeater)widget;
                int rowCount = repeater.getSize();
                handlers.addFirst(handler);
                handler = nestedHandler;
                contextWidgets.addFirst(contextWidget);
                for (int i = 0; i < rowCount; i++) {
                    Repeater.RepeaterRow row = repeater.getRow(i);
                    contextWidget = row;
                    out.getBuffer().toSAX(EffectWidgetReplacingPipe.this);
                }
                contextWidget = (Widget)contextWidgets.removeFirst();
                handler = (Handler)handlers.removeFirst();
                widget = null;
                out.bufferFini();
                return this;
            default:
                out.buffer();
                return this;
            }
        }
    }

    protected class AggregateWidgetHandler extends Handler {
        public Handler process() throws SAXException {
            switch(event) {
            case EVENT_START_ELEMENT:
                widgetId = getWidgetId(input.attrs);
                widget = getWidget(widgetId);
                if (!(widget instanceof AggregateField)) {
                    throwWrongWidgetType("AggregateWidgetHandler", input.loc, "aggregate");
                }
                contextWidgets.addFirst(contextWidget);
                contextWidget = widget;
                return this;
            case EVENT_ELEMENT:
                return nestedTemplate();
            case EVENT_END_ELEMENT:
                contextWidget = (Widget)contextWidgets.removeFirst();
                return this;
            default:
                out.copy();
                return this;
            }
        }
    }

    protected class StructHandler extends Handler {
        public Handler process() throws SAXException {
            switch(event) {
            case EVENT_START_ELEMENT:
                widgetId = getWidgetId(input.attrs);
                widget = getWidget(widgetId);
                if (!(widget instanceof Struct)) {
                    throwWrongWidgetType("StructHandler", input.loc, "struct");
                }
                contextWidgets.addFirst(contextWidget);
                contextWidget = widget;
                out.element(Constants.INSTANCE_PREFIX, Constants.INSTANCE_NS, "struct");
                out.attributes();
                out.startElement();
                return this;
            case EVENT_ELEMENT:
                return nestedTemplate();
            case EVENT_END_ELEMENT:
                out.copy();
                contextWidget = (Widget)contextWidgets.removeFirst();
                return this;
            default:
                out.copy();
                return this;
            }
        }
    }

    protected class UnionHandler extends Handler {
        public Handler process() throws SAXException {
            switch(event) {
            case EVENT_START_ELEMENT:
                widgetId = getWidgetId(input.attrs);
                widget = getWidget(widgetId);
                if (!(widget instanceof Union)) {
                    throwWrongWidgetType("UnionHandler", input.loc, "union");
                }
                contextWidgets.addFirst(contextWidget);
                contextWidget = widget;
                out.element(Constants.INSTANCE_PREFIX, Constants.INSTANCE_NS, "union");
                out.startElement();
                return this;
            case EVENT_ELEMENT:
                if (Constants.TEMPLATE_NS.equals(input.uri)) {
                    if ("case".equals(input.loc)) {
                        String id = input.attrs.getValue("id");
                        if (id == null) throwSAXException("Element \"case\" missing required \"id\" attribute.");
                        String value = (String)contextWidget.getValue();
                        if (id.equals(value != null ? value : "")) {
                            return nestedHandler;
                        } else {
                            return nullHandler;
                        }
                    } else if (FORM_TEMPLATE_EL.equals(input.loc)) {
                        throwSAXException("Element \"form-template\" must not be nested.");
                    } else {
                        throwSAXException("Unrecognized template: " + input.loc);
                    }
                } else {
                    return unionPassThruHandler;
                }
            case EVENT_END_ELEMENT:
                out.endElement();
                contextWidget = (Widget)contextWidgets.removeFirst();
                return this;
            default:
                out.copy();
                return this;
            }
        }
    }

    protected class UnionPassThruHandler extends Handler {
        public Handler process() throws SAXException {
            switch(event) {
            case EVENT_ELEMENT:
                if (Constants.TEMPLATE_NS.equals(input.uri)) {
                    if ("case".equals(input.loc)) {
                        if (contextWidget.getValue().equals(input.attrs.getValue("id"))) {
                            return nestedHandler;
                        } else {
                            return nullHandler;
                        }
                    } else if (FORM_TEMPLATE_EL.equals(input.loc)) {
                        throwSAXException("Element \"form-template\" must not be nested.");
                    } else {
                        throwSAXException("Unrecognized template: " + input.loc);
                    }
                } else {
                    return this;
                }
            default:
                out.copy();
                return this;
            }
        }
    }

    protected class NewHandler extends Handler {
        public Handler process() throws SAXException {
            switch (event) {
            case EVENT_START_ELEMENT:
                widgetId = getWidgetId(input.attrs);
                SaxBuffer classBuffer = (SaxBuffer)classes.get(widgetId);
                if (classBuffer == null) {
                    throwSAXException("New: Class \"" + widgetId + "\" does not exist.");
                }
                handlers.addFirst(handler);
                handler = nestedHandler;
                classBuffer.toSAX(EffectWidgetReplacingPipe.this);
                handler = (Handler)handlers.removeFirst();
                return this;
            case EVENT_ELEMENT:
                return nullHandler;
            case EVENT_END_ELEMENT:
                return this;
            default:
                out.copy();
                return this;
            }
        }
    }

    protected class ClassHandler extends Handler {
        public Handler process() throws SAXException {
            switch (event) {
            case EVENT_START_ELEMENT:
                widgetId = getWidgetId(input.attrs);
                out.bufferInit();
                return this;
            case EVENT_ELEMENT:
                return bufferHandler;
            case EVENT_END_ELEMENT:
                classes.put(widgetId, out.getBuffer());
                out.bufferFini();
                return this;
            default:
                out.buffer();
                return this;
            }
        }
    }

    protected class ContinuationIdHandler extends Handler {
        public Handler process() throws SAXException {
            switch(event) {
            case EVENT_START_ELEMENT:
                // Insert the continuation id
                // FIXME(SW) we could avoid costly JXPath evaluation if we had the objectmodel here.
                Object idObj = pipeContext.evaluateExpression("$continuation/id");
                if (idObj == null) {
                    throwSAXException("No continuation found");
                }

                String id = idObj.toString();
                out.element(Constants.INSTANCE_PREFIX, Constants.INSTANCE_NS, "continuation-id", input.attrs);
                out.startElement();
                out.characters(id.toCharArray(), 0, id.length());
                out.endElement();
                return this;
            case EVENT_END_ELEMENT:
                return this;
            case EVENT_IGNORABLE_WHITESPACE:
                return this;
            default:
                throwSAXException("ContinuationIdHandler: No content allowed in \"continuation-id\" element");
                return null; // Keep the compiler happy.
            }
        }
    }

    /**
     * This ContentHandler helps in inserting SAX events before the closing tag of the root
     * element.
     */
    protected class StylingContentHandler extends AbstractXMLPipe implements Recyclable {
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
                    saxBuffer.toSAX(getContentHandler());
                } else {
                    // Insert an enclosing <wi:styling>
                    out.startElement(Constants.INSTANCE_NS, STYLING_EL, Constants.INSTANCE_PREFIX_COLON + STYLING_EL, XMLUtils.EMPTY_ATTRIBUTES);
                    saxBuffer.toSAX(getContentHandler());
                    out.endElement(Constants.INSTANCE_NS, STYLING_EL, Constants.INSTANCE_PREFIX_COLON + STYLING_EL);
                }
            }
            super.endElement(uri, loc, raw);
        }
    }

    /**
     * Inserts validation errors (if any) for the Field widgets
     */
    protected class ValidationErrorHandler extends Handler {
        public Handler process() throws SAXException {
            switch (event) {
            case EVENT_START_ELEMENT:
                widgetId = getWidgetId(input.attrs);
                widget = getWidget(widgetId);
                out.bufferInit();
                return this;

            case EVENT_ELEMENT:
                return bufferHandler;

            case EVENT_END_ELEMENT:
                if (widget instanceof ValidationErrorAware) {
                    ValidationError error = ((ValidationErrorAware)widget).getValidationError();
                    if (error != null) {
                        out.startElement(Constants.INSTANCE_NS, VALIDATION_ERROR, Constants.INSTANCE_PREFIX_COLON + VALIDATION_ERROR, XMLUtils.EMPTY_ATTRIBUTES);
                        error.generateSaxFragment(stylingHandler);
                        out.endElement(Constants.INSTANCE_NS, VALIDATION_ERROR, Constants.INSTANCE_PREFIX_COLON + VALIDATION_ERROR);
                    }
                }
                widget = null;
                out.bufferFini();
                return this;

            default:
                out.copy();
                return this;
            }
        }
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

//    /**
//     * Replaces JXPath expressions embedded inside #{ and } by their value.
//     */
//    private String translateText(String original) {
//        StringBuffer expression;
//        StringBuffer translated = new StringBuffer();
//        StringReader in = new StringReader(original);
//        int chr;
//        try {
//            while ((chr = in.read()) != -1) {
//                char c = (char) chr;
//                if (c == '#') {
//                    chr = in.read();
//                    if (chr != -1) {
//                        c = (char) chr;
//                        if (c == '{') {
//                            expression = new StringBuffer();
//                            boolean more = true;
//                            while ( more ) {
//                                more = false;
//                                if ((chr = in.read()) != -1) {
//                                    c = (char)chr;
//                                    if (c != '}') {
//                                        expression.append(c);
//                                        more = true;
//                                    } else {
//                                        translated.append(evaluateExpression(expression.toString()));
//                                    }
//                                } else {
//                                    translated.append('#').append('{').append(expression);
//                                }
//                            }
//                        }
//                    } else {
//                        translated.append((char) chr);
//                    }
//                } else {
//                    translated.append(c);
//                }
//            }
//        } catch (IOException ignored) {
//            ignored.printStackTrace();
//        }
//        return translated.toString();
//    }

//    private String evaluateExpression(String expression) {
//        return pipeContext.evaluateExpression(expression).toString();
//    }

    public void recycle() {
        super.recycle();
        this.contextWidget = null;
        this.widget = null;
        widgetId = null;
        this.namespacePrefix = null;
    }
}
