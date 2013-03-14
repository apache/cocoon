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
package org.apache.cocoon.forms.transformation;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.event.ValueChangedListenerEnabled;
import org.apache.cocoon.forms.formmodel.AggregateField;
import org.apache.cocoon.forms.formmodel.DataWidget;
import org.apache.cocoon.forms.formmodel.Group;
import org.apache.cocoon.forms.formmodel.Repeater;
import org.apache.cocoon.forms.formmodel.Struct;
import org.apache.cocoon.forms.formmodel.Union;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.forms.validation.ValidationErrorAware;
import org.apache.cocoon.i18n.I18nUtils;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.SaxBuffer;
import org.apache.cocoon.xml.XMLUtils;

import org.apache.commons.jxpath.JXPathException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The basic operation of this Pipe is that it replaces <code>ft:widget</code>
 * (in the {@link FormsConstants#TEMPLATE_NS} namespace) tags (having an id attribute)
 * by the XML representation of the corresponding widget instance.
 *
 * <p>These XML fragments (normally all in the {@link FormsConstants#INSTANCE_NS "CForms Instance"}
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
    private static final String CHOOSE = "choose";
    private static final String CLASS = "class";
    private static final String CONTINUATION_ID = "continuation-id";
    private static final String FORM_TEMPLATE_EL = "form-template";
    private static final String GROUP = "group";
    private static final String NEW = "new";
    private static final String REPEATER = "repeater";
    private static final String REPEATER_ROWS = "repeater-rows";
    private static final String REPEATER_SIZE = "repeater-size";
    private static final String REPEATER_WIDGET = "repeater-widget";
    private static final String REPEATER_WIDGET_LABEL = "repeater-widget-label";
    private static final String STRUCT = "struct";
    private static final String STYLING_EL = "styling";
    private static final String UNION = "union";
    private static final String VALIDATION_ERROR = "validation-error";
    private static final String WIDGET = "widget";
    private static final String WIDGET_LABEL = "widget-label";

    private final AggregateWidgetHandler           hAggregate       = new AggregateWidgetHandler();
    private final ChooseHandler                    hChoose          = new ChooseHandler();
    protected final ChoosePassThruHandler          hChoosePassThru  = new ChoosePassThruHandler();
    private final ClassHandler                     hClass           = new ClassHandler();
    private final ContinuationIdHandler            hContinuationId  = new ContinuationIdHandler();
    private final DocHandler                       hDocument        = new DocHandler();
    protected final FormHandler                    hForm            = new FormHandler();
    private final GroupHandler                     hGroup           = new GroupHandler();
    protected final NestedHandler                  hNested          = new NestedHandler();
    private final NewHandler                       hNew             = new NewHandler();
    private final RepeaterSizeHandler              hRepeaterSize    = new RepeaterSizeHandler();
    private final RepeaterHandler                  hRepeater        = new RepeaterHandler();
    private final RepeaterRowsHandler              hRepeaterRows    = new RepeaterRowsHandler();
    private final RepeaterWidgetHandler            hRepeaterWidget  = new RepeaterWidgetHandler();
    private final RepeaterWidgetLabelHandler       hRepeaterWidgetLabel = new RepeaterWidgetLabelHandler();
    protected final SkipHandler                    hSkip            = new SkipHandler();
    private final StructHandler                    hStruct          = new StructHandler();
    protected final StylingContentHandler          hStyling         = new StylingContentHandler();
    private final UnionHandler                     hUnion           = new UnionHandler();
    protected final UnionPassThruHandler           hUnionPassThru   = new UnionPassThruHandler();
    private final ValidationErrorHandler           hValidationError = new ValidationErrorHandler();
    private final WidgetHandler                    hWidget          = new WidgetHandler();
    private final WidgetLabelHandler               hWidgetLabel     = new WidgetLabelHandler();

    /**
     * Map containing all handlers
     */
    protected final Map templates;

    protected FormsPipelineConfig pipeContext;

    /**
     * The namespaces and their prefixes
     */
    private final List namespaces;

    /**
     * True if instance namespace has been mapped to the
     * 'fi' prefix.
     */
    protected boolean hasInstanceNamespace;

    protected Widget contextWidget;
    protected LinkedList contextWidgets;
    protected LinkedList chooseWidgets;
    protected Widget widget;
    protected Map classes;


    public EffectWidgetReplacingPipe() {
        namespaces = new ArrayList(5);
        // Setup map of templates.
        templates = new HashMap();
        templates.put(AGGREGATE_WIDGET, hAggregate);
        templates.put(CHOOSE, hChoose);
        templates.put(CLASS, hClass);
        templates.put(CONTINUATION_ID, hContinuationId);
        templates.put(GROUP, hGroup);
        templates.put(NEW, hNew);
        templates.put(REPEATER, hRepeater);
        templates.put(REPEATER_ROWS, hRepeaterRows);
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
        this.contextWidgets = new LinkedList();
        this.chooseWidgets = new LinkedList();
        this.classes = new HashMap();
    }

    public void recycle() {
        super.recycle();
        this.contextWidget = null;
        this.widget = null;
        this.pipeContext = null;
        this.namespaces.clear();
        this.hasInstanceNamespace = false;
    }

    /**
     * Get value of the required attribute
     */
    protected String getAttributeValue(String loc, Attributes attrs, String name) throws SAXException {
        String value = attrs.getValue(name);
        if (value == null) {
            throw new SAXException("Element '" + loc + "' missing required '" + name + "' attribute, " +
                                   "at " + getLocation());
        }
        return value;
    }

    /**
     * Get non-empty value of the required attribute
     */
    protected String getRequiredAttributeValue(String loc, Attributes attrs, String name) throws SAXException {
        String value = attrs.getValue(name);
        if (value == null || value.length() == 0) {
            throw new SAXException("Element '" + loc + "' missing required '" + name + "' attribute, " +
                                   "at " + getLocation());
        }
        return value;
    }

    /**
     * Set the widget by the id attribute
     */
    protected void setWidget(String loc, Attributes attrs) throws SAXException {
        setWidget(loc, getRequiredAttributeValue(loc, attrs, "id"));
    }

    /**
     * Set the widget by its path
     */
    protected void setWidget(String loc, String path) throws SAXException {
        widget = contextWidget.lookupWidget(path);
        if (widget == null) {
            if (contextWidget.getRequestParameterName().length() == 0) {
                throw new SAXException("Element '" + loc + "' refers to unexistent widget path '" + path + "', " +
                                       "relative to the form container, at " + getLocation());
            } else {
                throw new SAXException("Element '" + loc + "' refers to unexistent widget path '" + path + "', " +
                                       "relative to the '" + contextWidget.getRequestParameterName() + "', " +
                                       "at " + getLocation());
            }
        }
    }

    /**
     * Set typed widget by the id attribute
     */
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
    protected ContentHandler getContentHandler() {
        return this.contentHandler;
    }

    /**
     * Needed to get things working with JDK 1.3. Can be removed once we
     * don't support that platform any more.
     */
    protected LexicalHandler getLexicalHandler() {
        return this.lexicalHandler;
    }

    /**
     * Process the SAX event.
     * @see org.xml.sax.ContentHandler#startPrefixMapping
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        if (prefix != null) {
            this.namespaces.add(new String[] {prefix, uri});
        }

        // Consume template namespace mapping
        if (!FormsConstants.TEMPLATE_NS.equals(uri)) {
            super.startPrefixMapping(prefix, uri);
        }
    }

    /**
     * Process the SAX event.
     * @see org.xml.sax.ContentHandler#endPrefixMapping
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        String uri = null;

        if (prefix != null) {
            // Find and remove the namespace prefix
            boolean found = false;
            for (int i = this.namespaces.size() - 1; i >= 0; i--) {
                final String[] prefixAndUri = (String[]) this.namespaces.get(i);
                if (prefixAndUri[0].equals(prefix)) {
                    uri = prefixAndUri[1];
                    this.namespaces.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new SAXException("Namespace for prefix '" + prefix + "' not found.");
            }
        }

        // Consume template namespace mapping
        if (!FormsConstants.TEMPLATE_NS.equals(uri)) {
            super.endPrefixMapping(prefix);
        }
    }

    /**
     * @return True if prefix is already mapped into the namespace
     */
    protected boolean hasPrefixMapping(String uri, String prefix) {
        final int l = this.namespaces.size();
        for (int i = 0; i < l; i++) {
            String[] prefixAndUri = (String[]) this.namespaces.get(i);
            if (prefixAndUri[0].equals(prefix) && prefixAndUri[1].equals(uri)) {
                return true;
            }
        }

        return false;
    }

    //
    // Handler classes to transform CForms template elements
    //

    protected class NestedHandler extends CopyHandler {
        public Handler nestedElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            // Is it forms namespace?
            if (!FormsConstants.TEMPLATE_NS.equals(uri)) {
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
        public Handler nestedElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            if (FormsConstants.TEMPLATE_NS.equals(uri)) {
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

            // set some general attributes
            // top-level widget-containers like forms might have their id set to ""
            // for those the @id should not be included.
            if (contextWidget.getId().length() != 0 && newAttrs.getValue("id") == null ) {
                newAttrs.addCDATAAttribute("id", contextWidget.getRequestParameterName());
            }

            // Add the "state" attribute
            if ( newAttrs.getValue("state") == null ) {
                newAttrs.addCDATAAttribute("state", contextWidget.getCombinedState().getName());
            }
            
            // Add the "listening" attribute is the value has change listeners
            if (contextWidget instanceof ValueChangedListenerEnabled &&
                ((ValueChangedListenerEnabled)contextWidget).hasValueChangedListeners() &&
                newAttrs.getValue("listening") == null ) {
                newAttrs.addCDATAAttribute("listening", "true");
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
            Attributes transAttrs = null;
            try {
                transAttrs = translateAttributes(newAttrs, namesToTranslate);
            } catch (RuntimeException e) {
                throw new SAXException( e.getMessage() + " " +getLocation());
            }
            
            hasInstanceNamespace = hasPrefixMapping(FormsConstants.INSTANCE_NS, FormsConstants.INSTANCE_PREFIX);
            if (!hasInstanceNamespace) {
                getContentHandler().startPrefixMapping(FormsConstants.INSTANCE_PREFIX, FormsConstants.INSTANCE_NS);
            }
            getContentHandler().startElement(FormsConstants.INSTANCE_NS, "form-template", FormsConstants.INSTANCE_PREFIX_COLON + "form-template", transAttrs);
            return this;
        }

        public void endElement(String uri, String loc, String raw)
        throws SAXException {
            getContentHandler().endElement(FormsConstants.INSTANCE_NS, "form-template", FormsConstants.INSTANCE_PREFIX_COLON + "form-template");
            if (!hasInstanceNamespace) {
                getContentHandler().endPrefixMapping(FormsConstants.INSTANCE_PREFIX);
            }
            contextWidget = null;
        }
    }

    /**
     * <code>ft:choose</code>, <code>ft:union</code> use this.
     */
    protected class SkipHandler extends NestedHandler {
        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            return this;
        }

        public void endElement(String uri, String loc, String raw)
        throws SAXException {
        }
    }

    //
    // Widget Handlers
    //

    /**
     * Handles <code>ft:widget-label</code> element.
     */
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

    /**
     * Handles <code>ft:widget</code> element.
     */
    protected class WidgetHandler extends NullHandler {
        // Widgets can't be nested, so this variable is Ok
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
            if (FormsConstants.INSTANCE_NS.equals(uri)) {
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
                // Pipe widget XML through the special handler to insert styling element
                // before fi:widget end element.
                hasStyling = false;
                hStyling.recycle();
                hStyling.setSaxFragment(endBuffer());
                hStyling.setContentHandler(getContentHandler());
                hStyling.setLexicalHandler(getLexicalHandler());
                widget.generateSaxFragment(hStyling, pipeContext.getLocale());
            } else {
                // Pipe widget XML directly into the output handler
                widget.generateSaxFragment(getContentHandler(), pipeContext.getLocale());
            }
            widget = null;
        }
    }

    //
    // Repeater Handlers
    //

    /**
     * Handles <code>ft:repeater-size</code> element.
     */
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

    /**
     * Handles <code>ft:repeater-widget-label</code> element.
     */
    protected class RepeaterWidgetLabelHandler extends ErrorHandler {
        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            Repeater repeater;
            if (contextWidget instanceof Repeater) {
                repeater = (Repeater)contextWidget;
            } else {
                setTypedWidget(loc, attrs, Repeater.class, "repeater");
                repeater = (Repeater)widget;
                widget = null;
            }
            String path = getRequiredAttributeValue(loc, attrs, "widget-id");
            repeater.generateWidgetLabel(path, getContentHandler());
            return this;
        }

        public void endElement(String uri, String loc, String raw)
        throws SAXException {
        }
    }

    /**
     * Handles <code>ft:repeater</code> element. Should contain repeater-rows
     */
    protected class RepeaterHandler extends NestedHandler {
        protected Class getWidgetClass() {
            return Repeater.class;
        }

        protected String getWidgetName() {
            return "repeater";
        }

        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            setTypedWidget(loc, attrs, getWidgetClass(), getWidgetName());
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

    /**
     * Handles <code>ft:repeater-rows</code> element.
     */
    protected class RepeaterRowsHandler extends BufferHandler {
        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            if (!(contextWidget instanceof Repeater)) {
                throw new SAXException("<repeater-rows> cannot be used with " + contextWidget + ", at " + getLocation());
            }
            beginBuffer();
            return this;
        }

        public Handler nestedElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            return hBuffer;
        }

        public void endElement(String uri, String loc, String raw)
        throws SAXException {
            SaxBuffer buffer = endBuffer();
            final Repeater repeater = (Repeater) contextWidget;
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

    /**
     * Handles <code>ft:repeater-widget</code> element: a single element for both the repeater and its rows
     */
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

    //
    // Grouping widgets Handlers
    //

    /**
     * Handles <code>ft:group</code> element.
     */
    protected class GroupHandler extends NestedHandler {
        protected Class getWidgetClass() {
            return Group.class;
        }

        protected String getWidgetName() {
            return "group";
        }

        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            setTypedWidget(loc, attrs, getWidgetClass(), getWidgetName());
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

    /**
     * Handles <code>ft:aggregate</code> element.
     */
    protected class AggregateWidgetHandler extends GroupHandler {
        protected Class getWidgetClass() {
            return AggregateField.class;
        }

        protected String getWidgetName() {
            return "aggregate";
        }
    }

    /**
     * Handles <code>ft:choose</code> element.
     */
    protected class ChooseHandler extends CopyHandler {
        public Handler startElement(String uri, String loc, String raw, Attributes attrs) throws SAXException {
            setWidget(loc, getRequiredAttributeValue(loc, attrs, "path"));
            // TODO: Should instead check for datatype convertable to String.
            if (!(widget instanceof DataWidget)) {
                throw new SAXException("Element '" + loc + "' can only be used with DataWidget widgets, " +
                                       "at " + getLocation());
            }
            // Choose does not change the context widget like Union does.
            chooseWidgets.addFirst(widget);
            return this;
        }

        public Handler nestedElement(String uri, String loc, String raw, Attributes attrs) throws SAXException {
            if (FormsConstants.TEMPLATE_NS.equals(uri)) {
                if ("when".equals(loc)) {
                    String testValue = getAttributeValue(loc, attrs, "value");
                    String value = (String) ((Widget) chooseWidgets.get(0)).getValue();
                    return testValue.equals(value) ? hSkip : hNull;
                }
                throw new SAXException("Element '" + loc + "' is not permitted within 'choose', " +
                                       "at " + getLocation());
            }
            return hChoosePassThru;
        }

        public void endElement(String uri, String loc, String raw) throws SAXException {
            chooseWidgets.removeFirst();
        }
    }

    /**
     * Handles <code>ft:choose/ft:when</code> element.
     */
    protected class ChoosePassThruHandler extends CopyHandler {
        public Handler nestedElement(String uri, String loc, String raw, Attributes attrs) throws SAXException {
            if (FormsConstants.TEMPLATE_NS.equals(uri)) {
                if ("when".equals(loc)) {
                    String testValue = getAttributeValue(loc, attrs, "value");
                    String value = (String) ((Widget) chooseWidgets.get(0)).getValue();
                    return testValue.equals(value)?  hSkip: hNull;
                }
                throw new SAXException("Element '" + loc + "' is not permitted within 'choose', " +
                                       "at " + getLocation());
            }
            return this;
        }
    }

    /**
     * Handles <code>ft:struct</code> element.
     */
    protected class StructHandler extends GroupHandler {
        protected Class getWidgetClass() {
            return Struct.class;
        }

        protected String getWidgetName() {
            return "struct";
        }
    }

    /**
     * Handles <code>ft:union</code> element.
     */
    protected class UnionHandler extends GroupHandler {
        protected Class getWidgetClass() {
            return Union.class;
        }

        protected String getWidgetName() {
            return "union";
        }

        public Handler nestedElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            if (FormsConstants.TEMPLATE_NS.equals(uri)) {
                if ("case".equals(loc)) {
                    String id = getAttributeValue(loc, attrs, "id");
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
    }

    /**
     * Handles <code>ft:union/ft:case</code> element.
     */
    protected class UnionPassThruHandler extends CopyHandler {
        public Handler nestedElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            if (FormsConstants.TEMPLATE_NS.equals(uri)) {
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

    /**
     * Handles <code>ft:new</code> element.
     */
    protected class NewHandler extends CopyHandler {
        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            String id = getRequiredAttributeValue(loc, attrs, "id");
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

    /**
     * Handles <code>ft:class</code> element.
     * <pre>
     * &lt;ft:class id="..."&gt;
     *   ...
     * &lt;/ft:class&gt;
     * </pre>
     */
    protected class ClassHandler extends BufferHandler {
        // FIXME What if <class> is nested within <class>?
        private String widgetPath;

        public Handler startElement(String uri, String loc, String raw, Attributes attrs)
        throws SAXException {
            widgetPath = getRequiredAttributeValue(loc, attrs, "id");
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

    /**
     * Handles <code>ft:continuation-id</code> element.
     * <pre>
     * &lt;ft:continuation-id/&gt;
     * </pre>
     */
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
            getContentHandler().startElement(FormsConstants.INSTANCE_NS, "continuation-id", FormsConstants.INSTANCE_PREFIX_COLON + "continuation-id", attrs);
            getContentHandler().characters(id.toCharArray(), 0, id.length());
            getContentHandler().endElement(FormsConstants.INSTANCE_NS, "continuation-id", FormsConstants.INSTANCE_PREFIX_COLON + "continuation-id");
            return this;
        }

        public void endElement(String uri, String loc, String raw) throws SAXException {
        }
    }

    /**
     * This ContentHandler helps in inserting SAX events before the closing tag of the root
     * element.
     */
    protected class StylingContentHandler extends AbstractXMLPipe {

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
                    getContentHandler().startElement(FormsConstants.INSTANCE_NS, VALIDATION_ERROR, FormsConstants.INSTANCE_PREFIX_COLON + VALIDATION_ERROR, XMLUtils.EMPTY_ATTRIBUTES);
                    error.generateSaxFragment(getContentHandler());
                    getContentHandler().endElement(FormsConstants.INSTANCE_NS, VALIDATION_ERROR, FormsConstants.INSTANCE_PREFIX_COLON + VALIDATION_ERROR);
                }
            }
            widget = null;
        }
    }



    private Attributes translateAttributes(Attributes attributes, String[] names) {
        AttributesImpl newAtts = new AttributesImpl(attributes);
        if (names!= null) {
            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                int position = newAtts.getIndex(name);
                String newValue = pipeContext.translateText(newAtts.getValue(position));
                if(position>-1)
                    newAtts.setValue(position, newValue);
                else
                    throw new RuntimeException("Attribute \""+name+"\" not present!");
            }
        }
        return newAtts;
    }
}
