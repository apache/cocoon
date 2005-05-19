/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
import org.apache.cocoon.forms.formmodel.Group;
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

/**
 * The basic operation of this Pipe is that it replaces <code>ft:widget</code>
 * (in the {@link Constants#TEMPLATE_NS} namespace) tags (having an id attribute)
 * by the XML representation of the corresponding widget instance.
 *
 * <p>These XML fragments (normally all in the {@link Constants#INSTANCE_NS "CForms Instance"}
 * namespace), can then be translated to a HTML presentation by an XSLT.
 * This XSLT will then only have to style individual widget, and will not
 * need to do the whole page layout.
 *
 * <p>For more information about the supported tags and their function,
 * see the user documentation for the forms template transformer.</p>
 *
 * @version $Id$
 */
public class EffectWidgetReplacingPipe extends EffectPipe {

    /**
     * Form location attribute on <code>ft:form-template</code> element, containing
     * JXPath expression which should result in Form object.
     *
     * @see FormsPipelineConfig#findForm(String)
     */
    private static final String LOCATION = "location";

    private static final String AGGREGATE_WIDGET = "aggregate-widget";
    private static final String CLASS = "class";
    private static final String CONTINUATION_ID = "continuation-id";
    private static final String FORM_TEMPLATE_EL = "form-template";
    private static final String GROUP = "group";
    private static final String NEW = "new";
    private static final String REPEATER_SIZE = "repeater-size";
    private static final String REPEATER_WIDGET = "repeater-widget";
    private static final String REPEATER_WIDGET_LABEL = "repeater-widget-label";
    private static final String STRUCT = "struct";
    private static final String STYLING_EL = "styling";
    private static final String UNION = "union";
    private static final String VALIDATION_ERROR = "validation-error";
    private static final String WIDGET = "widget";
    private static final String WIDGET_LABEL = "widget-label";

    protected Widget contextWidget;
    protected LinkedList contextWidgets;
    protected Widget widget;
    protected Map classes;

    private final AggregateWidgetHandler     hAggregate       = new AggregateWidgetHandler();
    private final ClassHandler               hClass           = new ClassHandler();
    private final ContinuationIdHandler      hContinuationId  = new ContinuationIdHandler();
    private final DocHandler                 hDocument        = new DocHandler();
    private final FormHandler                hForm            = new FormHandler();
    private final GroupHandler               hGroup           = new GroupHandler();
    private final NestedHandler              hNested          = new NestedHandler();
    private final NewHandler                 hNew             = new NewHandler();
    private final RepeaterSizeHandler        hRepeaterSize    = new RepeaterSizeHandler();
    private final RepeaterWidgetHandler      hRepeaterWidget  = new RepeaterWidgetHandler();
    private final RepeaterWidgetLabelHandler hRepeaterWidgetLabel = new RepeaterWidgetLabelHandler();
    private final SkipHandler                hSkip            = new SkipHandler();
    private final StructHandler              hStruct          = new StructHandler();
    private final StylingContentHandler      hStyling         = new StylingContentHandler();
    private final UnionHandler               hUnion           = new UnionHandler();
    private final UnionPassThruHandler       hUnionPassThru   = new UnionPassThruHandler();
    private final ValidationErrorHandler     hValidationError = new ValidationErrorHandler();
    private final WidgetHandler              hWidget          = new WidgetHandler();
    private final WidgetLabelHandler         hWidgetLabel     = new WidgetLabelHandler();

    /**
     * Map containing all handlers
     */
    private final Map templates;

    protected FormsPipelineConfig pipeContext;

    /**
     * Namespace prefix used for the namespace <code>Constants.FT_NS</code>.
     */
    protected String namespacePrefix;


    public EffectWidgetReplacingPipe() {
        // Setup map of templates.
        templates = new HashMap();
        templates.put(AGGREGATE_WIDGET, hAggregate);
        templates.put(CLASS, hClass);
        templates.put(CONTINUATION_ID, hContinuationId);
        templates.put(GROUP, hGroup);
        templates.put(NEW, hNew);
        templates.put(REPEATER_SIZE, hRepeaterSize);
        templates.put(REPEATER_WIDGET, hRepeaterWidget);
        templates.put(REPEATER_WIDGET_LABEL, hRepeaterWidgetLabel);
        templates.put(STRUCT, hStruct);
        templates.put(UNION, hUnion);
        templates.put(VALIDATION_ERROR, hValidationError);
        templates.put(WIDGET, hWidget);
        templates.put(WIDGET_LABEL, hWidgetLabel);
    }

    public void init(Widget contextWidget, FormsPipelineConfig pipeContext) {
        // Document handler is top level handler
        super.init(hDocument);
        this.pipeContext = pipeContext;

        // Initialize widget related variables
        contextWidgets = new LinkedList();
        classes = new HashMap();
    }

    protected String getWidgetId(String loc, Attributes attributes) throws SAXException {
        String widgetId = attributes.getValue("id");
        if (widgetId == null || widgetId.equals("")) {
            throw new SAXException("Element '" + loc + "' missing required 'id' attribute, " +
                                   "at " + getLocation());
        }
        return widgetId;
    }

    /**
     * Get widget ID in the attributes, and find the widget
     */
    protected void setWidget(String loc, Attributes attrs) throws SAXException {
        String id = getWidgetId(loc, attrs);
        widget = contextWidget.lookupWidget(id);
        if (widget == null) {
            if (contextWidget.getRequestParameterName().equals("")) {
                throw new SAXException("Element '" + loc + "' refers to unexistent widget path '" + id + "', " +
                                       "relative to the form container, at " + getLocation());
            } else {
                throw new SAXException("Element '" + loc + "' refers to unexistent widget path '" + id + "', " +
                                       "relative to the '" + contextWidget.getRequestParameterName() + "', " +
                                       "at " + getLocation());
            }
        }
    }

    protected void setTypedWidget(String loc, Attributes attrs, Class wclass, String wname) throws SAXException {
        setWidget(loc, attrs);
        if (!wclass.isInstance(widget)) {
            throw new SAXException("Element '" + loc + "' can only be used with " + wname + " widgets, " +
                                   "at " + getLocation());
        }
    }

    protected boolean isVisible(Widget widget) {
        return widget.getCombinedState().isDisplayingValues();
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

    //
    // Handler classes to transform CForms template elements
    //

    protected class NestedHandler extends CopyHandler {
        public Handler nestedElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            // Is it forms namespace?
            if (!Constants.TEMPLATE_NS.equals(uri)) {
                return hNested;
            }

            Handler handler = (Handler) templates.get(loc);
            if (handler == null) {
                throw new SAXException("Element '" + loc + "' was not recognized, " +
                                       "at " + getLocation());
            }

            return handler;
        }
    }

    /**
     * Top level handler for the forms template
     */
    protected class DocHandler extends CopyHandler {
        public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
            if (Constants.TEMPLATE_NS.equals(uri)) {
                // We consume this namespace completely.
                EffectWidgetReplacingPipe.this.namespacePrefix = prefix;
                return;
            }

            // Pass through all others.
            super.startPrefixMapping(prefix, uri);
        }

        public void endPrefixMapping(String prefix)
        throws SAXException {
            if (prefix.equals(EffectWidgetReplacingPipe.this.namespacePrefix)) {
                // We consume this namespace completely.
                EffectWidgetReplacingPipe.this.namespacePrefix = null;
                return;
            }

            // Pass through all others.
            super.endPrefixMapping(prefix);
        }

        public Handler nestedElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            if (Constants.TEMPLATE_NS.equals(uri)) {
                if (!FORM_TEMPLATE_EL.equals(loc)) {
                    throw new SAXException("Element '" + loc + "' is not permitted outside of " +
                                           "'form-template', at " + getLocation());
                }

                return hForm;
            }

            return super.nestedElement(uri, loc, raw, attrs);
        }
    }

    /**
     * <code>ft:form-template</code> element handler.
     * <pre>
     * &lt;ft:form-template locale="..." location="..."&gt;
     *   ...
     * &lt;/ft:form-template&gt;
     * </pre>
     */
    protected class FormHandler extends NestedHandler {
        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            if (contextWidget != null) {
                throw new SAXException("Element 'form-template' can not be nested, " +
                                       "at " + getLocation());
            }

            AttributesImpl newAttrs = attrs == null || attrs.getLength() == 0?
                    new AttributesImpl():
                    new AttributesImpl(attrs);

            // ====> Retrieve the form
            String formLocation = attrs.getValue(LOCATION);
            if (formLocation != null) {
                // Remove the location attribute
                newAttrs.removeAttribute(newAttrs.getIndex(LOCATION));
            }
            contextWidget = pipeContext.findForm(formLocation);

            // ====> Check if form visible (and skip it if it's not)
            if (!isVisible(contextWidget)) {
                return hNull;
            }

            // ====> Determine the Locale
            // TODO pull this locale stuff also up in the Config object?
            String localeAttr = attrs.getValue("locale");
            if (localeAttr != null) { // first use value of locale attribute if any
                localeAttr = pipeContext.translateText(localeAttr);
                pipeContext.setLocale(I18nUtils.parseLocale(localeAttr));
            } else if (pipeContext.getLocaleParameter() != null) { // then use locale specified as transformer parameter, if any
                pipeContext.setLocale(pipeContext.getLocaleParameter());
            } else {
                // use locale specified in bizdata supplied for form
                Object locale = null;
                try {
                    locale = pipeContext.evaluateExpression("/locale");
                } catch (JXPathException e) {}
                if (locale != null) {
                    pipeContext.setLocale((Locale)locale);
                } else {
                    // final solution: use locale defined in the server machine
                    pipeContext.setLocale(Locale.getDefault());
                }
            }

            // We need to merge input.attrs with possible overruling attributes
            // from the pipeContext
            pipeContext.addFormAttributes(newAttrs);
            String[] namesToTranslate = {"action"};
            Attributes transAttrs = translateAttributes(newAttrs, namesToTranslate);

            getContentHandler().startPrefixMapping(Constants.INSTANCE_PREFIX, Constants.INSTANCE_NS);
            getContentHandler().startElement(Constants.INSTANCE_NS, "form-template", Constants.INSTANCE_PREFIX_COLON + "form-template", transAttrs);
            return this;
        }

        public void endElement(String uri, String loc, String raw)
        throws SAXException {
            getContentHandler().endElement(Constants.INSTANCE_NS, "form-template", Constants.INSTANCE_PREFIX_COLON + "form-template");
            getContentHandler().endPrefixMapping(Constants.INSTANCE_PREFIX);
            contextWidget = null;
        }
    }

    protected class SkipHandler extends NestedHandler {
        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            return this;
        }

        public void endElement(String uri, String loc, String raw)
        throws SAXException {
        }
    }

    protected class WidgetLabelHandler extends ErrorHandler {
        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            setWidget(loc, attrs);
            widget.generateLabel(getContentHandler());
            return this;
        }

        public void endElement(String uri, String loc, String raw)
        throws SAXException {
        }
    }

    protected class WidgetHandler extends NullHandler {
        private boolean hasStyling;

        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            setWidget(loc, attrs);
            if (!isVisible(widget)) {
                return hNull;
            }

            hasStyling = false;
            return this;
        }

        public Handler nestedElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            if (Constants.INSTANCE_NS.equals(uri)) {
                if (!STYLING_EL.equals(loc)) {
                    throw new SAXException("Element '" + loc + "' is not permitted within 'widget', " +
                                           "at " + getLocation());
                }
                hasStyling = true;
                beginBuffer();
                // Buffer styling elements
                return hBuffer;
            }
            return hNull;
        }

        public void endElement(String uri, String loc, String raw)
        throws SAXException {
            if (hasStyling) {
                hasStyling = false;
                hStyling.recycle();
                hStyling.setSaxFragment(endBuffer());
                hStyling.setContentHandler(getContentHandler());
                hStyling.setLexicalHandler(getLexicalHandler());
                widget.generateSaxFragment(hStyling, pipeContext.getLocale());
            } else {
                widget.generateSaxFragment(getContentHandler(), pipeContext.getLocale());
            }
            widget = null;
        }
    }

    protected class RepeaterSizeHandler extends ErrorHandler {
        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            setTypedWidget(loc, attrs, Repeater.class, "repeater");
            ((Repeater) widget).generateSize(getContentHandler());
            widget = null;
            return this;
        }

        public void endElement(String uri, String loc, String raw)
        throws SAXException {
        }
    }

    protected class RepeaterWidgetLabelHandler extends ErrorHandler {
        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            setTypedWidget(loc, attrs, Repeater.class, "repeater");
            String widgetPath = attrs.getValue("widget-id");
            if (widgetPath == null || widgetPath.equals("")) {
                throw new SAXException("Element '" + loc + "' missing required 'widget-id' attribute, " +
                                       "at " + getLocation());
            }
            ((Repeater)widget).generateWidgetLabel(widgetPath, getContentHandler());
            widget = null;
            return this;
        }

        public void endElement(String uri, String loc, String raw)
        throws SAXException {
        }
    }

    protected class RepeaterWidgetHandler extends BufferHandler {
        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            setTypedWidget(loc, attrs, Repeater.class, "repeater");
            if (isVisible(widget)) {
                beginBuffer();
                return this;
            }
            return hNull;
        }

        public Handler nestedElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            return hBuffer;
        }

        public void endElement(String uri, String loc, String raw)
        throws SAXException {
            SaxBuffer buffer = endBuffer();
            final Repeater repeater = (Repeater) widget;
            final int rowCount = repeater.getSize();
            pushHandler(hNested);
            contextWidgets.addFirst(contextWidget);
            for (int i = 0; i < rowCount; i++) {
                contextWidget = repeater.getRow(i);
                if (isVisible(contextWidget)) {
                    buffer.toSAX(EffectWidgetReplacingPipe.this);
                }
            }
            contextWidget = (Widget) contextWidgets.removeFirst();
            popHandler();
            widget = null;
        }
    }

    protected class AggregateWidgetHandler extends NestedHandler {
        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            setTypedWidget(loc, attrs, AggregateField.class, "aggregate");
            if (!isVisible(widget)) {
                return hNull;
            }

            contextWidgets.addFirst(contextWidget);
            contextWidget = widget;
            return this;
        }

        public void endElement(String uri, String loc, String raw)
        throws SAXException {
            contextWidget = (Widget) contextWidgets.removeFirst();
        }
    }

    protected class GroupHandler extends AggregateWidgetHandler {
        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            setTypedWidget(loc, attrs, Group.class, "group");
            if (!isVisible(widget)) {
                return hNull;
            }

            contextWidgets.addFirst(contextWidget);
            contextWidget = widget;
            return this;
        }
    }

    protected class StructHandler extends AggregateWidgetHandler {
        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            setTypedWidget(loc, attrs, Struct.class, "struct");
            if (!isVisible(widget)) {
                return hNull;
            }

            contextWidgets.addFirst(contextWidget);
            contextWidget = widget;
            return this;
        }
    }

    protected class UnionHandler extends CopyHandler {
        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            setTypedWidget(loc, attrs, Union.class, "union");
            if (!isVisible(widget)) {
                return hNull;
            }

            contextWidgets.addFirst(contextWidget);
            contextWidget = widget;
            return this;
        }

        public Handler nestedElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            if (Constants.TEMPLATE_NS.equals(uri)) {
                if ("case".equals(loc)) {
                    String id = attrs.getValue("id");
                    if (id == null) {
                        throw new SAXException("Element 'case' missing required 'id' attribute, " +
                                               "at " + getLocation());
                    }
                    String value = (String) contextWidget.getValue();
                    if (id.equals(value != null ? value : "")) {
                        return hSkip;
                    }
                    return hNull;
                }
                throw new SAXException("Element '" + loc + "' is not permitted within 'union', " +
                                       "at " + getLocation());
            }
            return hUnionPassThru;
        }

        public void endElement(String uri, String loc, String raw) throws SAXException {
            contextWidget = (Widget)contextWidgets.removeFirst();
        }
    }

    protected class UnionPassThruHandler extends CopyHandler {
        public Handler nestedElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            if (Constants.TEMPLATE_NS.equals(uri)) {
                if ("case".equals(loc)) {
                    if (contextWidget.getValue().equals(attrs.getValue("id"))) {
                        return hSkip;
                    }
                    return hNull;
                }
                throw new SAXException("Element '" + loc + "' is not permitted within 'union', " +
                                       "at " + getLocation());
            }
            return this;
        }
    }

    protected class NewHandler extends CopyHandler {
        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            String id = getWidgetId(loc, attrs);
            SaxBuffer buffer = (SaxBuffer) classes.get(id);
            if (buffer == null) {
                throw new SAXException("New: Class '" + id + "' does not exist, " +
                                       "at " + getLocation());
            }
            pushHandler(hNested);
            buffer.toSAX(EffectWidgetReplacingPipe.this);
            popHandler();
            return this;
        }

        public Handler nestedElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            return hNull;
        }

        public void endElement(String uri, String loc, String raw)
        throws SAXException {
        }
    }

    protected class ClassHandler extends BufferHandler {
        // FIXME What if <class> is nested within <class>?
        private String widgetPath;

        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            widgetPath = getWidgetId(loc, attrs);
            beginBuffer();
            return this;
        }

        public Handler nestedElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            return hBuffer;
        }

        public void endElement(String uri, String loc, String raw) throws SAXException {
            classes.put(widgetPath, endBuffer());
        }
    }

    protected class ContinuationIdHandler extends ErrorHandler {
        protected String getName() {
            return "continuation-id";
        }

        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            // Insert the continuation id
            // FIXME(SW) we could avoid costly JXPath evaluation if we had the objectmodel here.
            Object idObj = pipeContext.evaluateExpression("$cocoon/continuation/id");
            if (idObj == null) {
                throw new SAXException("No continuation found");
            }

            String id = idObj.toString();
            getContentHandler().startElement(Constants.INSTANCE_NS, "continuation-id", Constants.INSTANCE_PREFIX_COLON + "continuation-id", attrs);
            getContentHandler().characters(id.toCharArray(), 0, id.length());
            getContentHandler().endElement(Constants.INSTANCE_NS, "continuation-id", Constants.INSTANCE_PREFIX_COLON + "continuation-id");
            return this;
        }

        public void endElement(String uri, String loc, String raw) throws SAXException {
        }
    }

    /**
     * This ContentHandler helps in inserting SAX events before the closing tag of the root
     * element.
     */
    protected class StylingContentHandler extends AbstractXMLPipe
                                          implements Recyclable {

        private int elementNesting;
        private SaxBuffer styling;

        public void setSaxFragment(SaxBuffer saxFragment) {
            styling = saxFragment;
        }

        public void recycle() {
            super.recycle();
            elementNesting = 0;
            styling = null;
        }

        public void startElement(String uri, String loc, String raw, Attributes a)
        throws SAXException {
            elementNesting++;
            super.startElement(uri, loc, raw, a);
        }

        public void endElement(String uri, String loc, String raw)
        throws SAXException {
            elementNesting--;
            if (elementNesting == 0) {
                styling.toSAX(getContentHandler());
            }
            super.endElement(uri, loc, raw);
        }
    }

    /**
     * Inserts validation errors (if any) for the Field widgets
     */
    protected class ValidationErrorHandler extends NullHandler {
        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            setWidget(loc, attrs);
            return this;
        }

        public Handler nestedElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            return hNull;
        }

        public void endElement(String uri, String loc, String raw)
        throws SAXException {
            if (widget instanceof ValidationErrorAware) {
                ValidationError error = ((ValidationErrorAware)widget).getValidationError();
                if (error != null) {
                    getContentHandler().startElement(Constants.INSTANCE_NS, VALIDATION_ERROR, Constants.INSTANCE_PREFIX_COLON + VALIDATION_ERROR, XMLUtils.EMPTY_ATTRIBUTES);
                    error.generateSaxFragment(hStyling);
                    getContentHandler().endElement(Constants.INSTANCE_NS, VALIDATION_ERROR, Constants.INSTANCE_PREFIX_COLON + VALIDATION_ERROR);
                }
            }
            widget = null;
        }
    }


    public void recycle() {
        super.recycle();
        this.contextWidget = null;
        this.widget = null;
        this.pipeContext = null;
        this.namespacePrefix = null;
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
}
