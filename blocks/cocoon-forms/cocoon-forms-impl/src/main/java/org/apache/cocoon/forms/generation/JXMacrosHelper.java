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

package org.apache.cocoon.forms.generation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.cocoon.ajax.BrowserUpdateTransformer;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.FormsRuntimeException;
import org.apache.cocoon.forms.event.ValueChangedListenerEnabled;
import org.apache.cocoon.forms.formmodel.Form;
import org.apache.cocoon.forms.formmodel.Repeater;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.formmodel.tree.Tree;
import org.apache.cocoon.forms.formmodel.tree.TreeWalker;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.i18n.I18nUtils;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.lang.BooleanUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Helper class for the implementation of the CForms template language with JXTemplate macros.
 *
 * @version $Id$
 */
public class JXMacrosHelper {

    private XMLConsumer cocoonConsumer;
    private Request request;
    private Locale locale;
    private ArrayStack widgetStack = new ArrayStack();
    private ArrayStack pipeStack = new ArrayStack();
    private Map classes; // lazily created
    private boolean ajaxRequest;
    private boolean ajaxTemplate;
    private Set updatedWidgets;
    private Set childUpdatedWidgets;

    /**
     * Builds and helper object, given the generator's consumer.
     *
     * @param consumer the generator's consumer
     * @return a helper object
     */
    public static JXMacrosHelper createHelper(XMLConsumer consumer, Request request, String locale) {
        return new JXMacrosHelper(consumer, request, locale);
    }

    public JXMacrosHelper(XMLConsumer consumer, Request request, String locale) {
        this.cocoonConsumer = consumer;
        this.request = request;
        this.locale = I18nUtils.parseLocale(locale);
        this.ajaxRequest = request.getParameter("cocoon-ajax") != null;
    }

    public Form getForm(Form form, String attributeName) {
        Form returnForm = form;
        // if there hasn't been passed a form object, try to find it in the request
        if(returnForm == null) {
            returnForm = (Form) this.request.getAttribute(attributeName);
        }
        if(returnForm != null) {
            return returnForm;
        }
        throw new FormsRuntimeException("The template cannot find a form object");
    }

    public void startForm(Form form, Map attributes) throws SAXException {
        
        this.updatedWidgets = form.getUpdatedWidgetIds();
        this.childUpdatedWidgets = form.getChildUpdatedWidgetIds();
        
        // build attributes
        AttributesImpl attrs = new AttributesImpl();
        // top-level widget-containers like forms might have their id set to ""
        // for those the @id should not be included.
        if (form.getId().length() != 0) {
            attrs.addCDATAAttribute("id", form.getRequestParameterName());
        }

        // Add the "state" attribute
        attrs.addCDATAAttribute("state", form.getCombinedState().getName());

        // Add locale attribute, useful for client-side code which needs to do stuff that
        // corresponds to the form locale (e.g. date pickers)
        attrs.addCDATAAttribute("locale", this.locale.toString().replaceAll("_", "-"));
        
        // Add the "listening" attribute is the value has change listeners
        if (form instanceof ValueChangedListenerEnabled &&
            ((ValueChangedListenerEnabled)form).hasValueChangedListeners()) {
            attrs.addCDATAAttribute("listening", "true");
        }
        Iterator iter = attributes.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            final String attrName = (String)entry.getKey();
            // check if the attribute has already been defined
            if ( attrs.getValue(attrName) != null ) {
                attrs.removeAttribute(attrName);
            }
            attrs.addCDATAAttribute(attrName, (String)entry.getValue());
        }
        
        this.ajaxTemplate = "true".equals(attributes.get("ajax"));

        this.cocoonConsumer.startPrefixMapping(FormsConstants.INSTANCE_PREFIX, FormsConstants.INSTANCE_NS);
        this.cocoonConsumer.startElement(FormsConstants.INSTANCE_NS,
                                         "form-template",
                                         FormsConstants.INSTANCE_PREFIX_COLON + "form-template",
                                         attrs);
        // Push the form at the top of the stack
        this.widgetStack.push(Boolean.FALSE); // Not in an updated template
        this.widgetStack.push(form);
    }

    public void endForm() throws SAXException {
        this.widgetStack.pop();
        this.widgetStack.pop();
        this.cocoonConsumer.endElement(FormsConstants.INSTANCE_NS,
                                       "form-template",
                                       FormsConstants.INSTANCE_PREFIX_COLON + "form-template");
        this.cocoonConsumer.endPrefixMapping(FormsConstants.INSTANCE_PREFIX);
        
        this.ajaxTemplate = false;
        this.updatedWidgets = null;
    }
    
    private void startBuReplace(String id) throws SAXException {
        AttributesImpl attr = new AttributesImpl();
        attr.addCDATAAttribute("id", id);
        this.cocoonConsumer.startElement(BrowserUpdateTransformer.BU_NSURI, "replace", "bu:replace", attr);
    }
    
    private void endBuReplace(String id) throws SAXException {
        this.cocoonConsumer.endElement(BrowserUpdateTransformer.BU_NSURI, "replace", "bu:replace");
    }
    
    protected boolean pushWidget(String path, boolean unused) throws SAXException {
        Widget parent = peekWidget();
        if (path == null || path.length() == 0) {
            throw new FormsRuntimeException("Missing 'id' attribute on template instruction");
        }
        Widget widget = parent.lookupWidget(path);
        if (widget == null) {
            throw new FormsRuntimeException(parent + " has no child named '" + path + "'", parent.getLocation());
        }

        String id = widget.getFullName();
        // Is there an updated widget at a higher level in the template?
        boolean inUpdatedTemplate = ((Boolean)widgetStack.peek(1)).booleanValue();

        boolean display;

        if (ajaxRequest) {
            // An Ajax request. We will send partial updates
            if (inUpdatedTemplate) {
                // A parent widget has been updated: redisplay this one also
                display = true;
            } else if (this.updatedWidgets.contains(id)) {
                // Widget has been updated. We are now in an updated template section,
                // and widgets have to be surrounded with <bu:replace>
                inUpdatedTemplate = true;
                display = true;
            } else if (this.childUpdatedWidgets.contains(id)) {
                // A child need to be updated
                display = true;
            } else {
                // Doesn't need to be displayed
                display = false;
            }
        } else {
            // Not an ajax request
            if (ajaxTemplate) {
                // Surround all widgets with <bu:replace>, which the bu tranformer will use to check structure
                // consistency and add an id attribute to its child elements.
                inUpdatedTemplate = true;
            }
            // Display the widget
            display = true;
        }
        
        if (display) {
            // Widget needs to be displayed, but does it actually allows it?
            if (widget.getState().isDisplayingValues()) {
                if (inUpdatedTemplate) {
                    // Updated part of an Ajax template: surround with <bu:replace>
                    startBuReplace(id);
                }
            } else {
                if (ajaxTemplate) {
                    // Generate a placeholder, so that the page can be updated later
                    startBuReplace(id);
                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addCDATAAttribute("id", id);
                    this.cocoonConsumer.startElement(FormsConstants.INSTANCE_NS, "placeholder", FormsConstants.INSTANCE_PREFIX_COLON + "placeholder", attrs);
                    this.cocoonConsumer.endElement(FormsConstants.INSTANCE_NS, "placeholder", FormsConstants.INSTANCE_PREFIX_COLON + "placeholder");
                    endBuReplace(id);
                }
                // Production finished for this widget
                display = false;
            }
        }

        if (display) {
            this.widgetStack.push(BooleanUtils.toBooleanObject(inUpdatedTemplate));
            this.widgetStack.push(widget);
        }
        
        return display;
    }
    
    public Widget peekWidget() {
        return (Widget)this.widgetStack.peek();
    }
    
    public void popWidget() throws SAXException {
        Widget widget = (Widget)this.widgetStack.pop();
        boolean inUpdatedTemplate = ((Boolean)this.widgetStack.pop()).booleanValue();
        
        if (inUpdatedTemplate) {
            // Close the bu:replace
            endBuReplace(widget.getFullName());
        }
    }
    
    public boolean pushWidget(String path) throws SAXException {
        return pushWidget(path, false);
    }
    
    public boolean pushContainer(String path) throws SAXException {
        return pushWidget(path, true);
    }

    /**
     * Enter a repeater
     * 
     * @param path widget path
     * @param ajaxAware distinguishes between &lt;ft:repeater-widget&gt; and &lt;ft:repeater&gt;.
     * @return true if the repeater template is to be executed
     * @throws SAXException
     */
    public boolean pushRepeater(String path, boolean ajaxAware) throws SAXException {
        if (!ajaxAware && this.ajaxTemplate) {
            throw new IllegalStateException("Cannot use <ft:repeater-widget> in an Ajax form");
        }
        boolean result = pushWidget(path, true);
        if (result && !(peekWidget() instanceof Repeater)) {
            throw new IllegalArgumentException("Widget " + peekWidget() + " is not a repeater");
        }
        return result;
    }
    
    /**
     * Get a child widget of a given widget, throwing an exception if no such child exists.
     *
     * @param currentWidget
     * @param path
     */
    public Widget getWidget(Widget currentWidget, String path) {
        Widget result = currentWidget.lookupWidget(path);

        if (result != null) {
            return result;
        }
        throw new FormsRuntimeException(currentWidget + " has no child named '" + path + "'", currentWidget.getLocation());
    }

    private Repeater getRepeater(Widget currentWidget, String id) {
        Widget child = getWidget(currentWidget, id);
        if (child instanceof Repeater) {
            return (Repeater)child;
        }
        throw new FormsRuntimeException(child + " is not a repeater", child.getLocation());
    }

    /**
     * Generate a widget's SAX fragment, buffering the root element's <code>endElement()</code>
     * event so that the template can insert styling information in it.
     *
     * @param widget
     * @param arguments
     * @throws SAXException
     */
    public void generateWidget(Widget widget, Map arguments) throws SAXException {
        // Needs to be buffered
        RootBufferingPipe pipe = new RootBufferingPipe(this.cocoonConsumer, arguments);
        this.pipeStack.push(pipe);
        widget.generateSaxFragment(pipe, this.locale);
    }

    /**
     * Flush the root element name that has been stored in
     * {@link #generateWidget(Widget, Map)}.
     *
     * @throws SAXException
     */
    public void flushRootAndPop() throws SAXException {
        ((RootBufferingPipe) pipeStack.pop()).flushRoot();
        popWidget();
    }

    public void flushRoot() throws SAXException {
        ((RootBufferingPipe) pipeStack.pop()).flushRoot();
    }

    public void generateWidgetLabel(Widget widget, String id) throws SAXException {
        getWidget(widget, id).generateLabel(this.cocoonConsumer);
    }

    public void generateRepeaterWidgetLabel(Widget widget, String id, String widgetId) throws SAXException {
        // Widget labels are allowed either inside or outside of <ft:repeater>
        Repeater repeater = widget instanceof Repeater ? (Repeater)widget : getRepeater(widget, id);
        repeater.generateWidgetLabel(widgetId, this.cocoonConsumer);
    }

    public void generateRepeaterSize(Widget widget, String id) throws SAXException {
        getRepeater(widget, id).generateSize(this.cocoonConsumer);
    }

    private static final String VALIDATION_ERROR = "validation-error";

    public void generateValidationError(ValidationError error) throws SAXException {
        // Needs to be buffered
        RootBufferingPipe pipe = new RootBufferingPipe(this.cocoonConsumer);
        this.pipeStack.push(pipe);
        pipe.startElement(FormsConstants.INSTANCE_NS, VALIDATION_ERROR, FormsConstants.INSTANCE_PREFIX_COLON + VALIDATION_ERROR, XMLUtils.EMPTY_ATTRIBUTES);
        error.generateSaxFragment(pipe);
        pipe.endElement(FormsConstants.INSTANCE_NS, VALIDATION_ERROR, FormsConstants.INSTANCE_PREFIX_COLON + VALIDATION_ERROR);
    }

    public boolean isValidationError(Object object) {
        return object instanceof ValidationError;
    }

    public void defineClassBody(Form form, String id, Object body) {
        // TODO: check that class actually exists in the form
        if (this.classes == null) {
            this.classes = new HashMap();
        }

        // TODO: check if class doesn't already exist?
        this.classes.put(id, body);
    }

    public Object getClassBody(String id) {
        Object result = this.classes == null ? null : this.classes.get(id);

        if (result == null) {
            throw new FormsRuntimeException("No class '" + id + "' has been defined.");
        } 
        return result;
    }

    public boolean isSelectedCase(Widget unionWidget, String caseValue) {
        String value = (String)unionWidget.getValue();
        return caseValue.equals(value != null ? value : "");
    }

    public TreeWalker createWalker() {
        return new TreeWalker((Tree)peekWidget());
    }
    
    public boolean isVisible(Widget widget) throws SAXException {
        boolean visible = widget.getCombinedState().isDisplayingValues();
        
        if (!visible) {
            // Generate a placeholder it not visible
            String id = widget.getRequestParameterName();
            AttributesImpl attrs = new AttributesImpl();
            attrs.addCDATAAttribute("id", id);
            this.cocoonConsumer.startElement(BrowserUpdateTransformer.BU_NSURI, "replace", "bu:replace", attrs);
            this.cocoonConsumer.startElement(FormsConstants.INSTANCE_NS, "placeholder", FormsConstants.INSTANCE_PREFIX_COLON + "placeholder", attrs);
            this.cocoonConsumer.endElement(FormsConstants.INSTANCE_NS, "placeholder", FormsConstants.INSTANCE_PREFIX_COLON + "placeholder");
            this.cocoonConsumer.endElement(BrowserUpdateTransformer.BU_NSURI, "replace", "bu:replace");
        }

        return visible;
    }
    
    public boolean isModified(Widget widget) {
        return this.updatedWidgets.contains(widget.getRequestParameterName());
    }
    
    public boolean generateStyling(Map attributes) throws SAXException {
        return generateStyling(this.cocoonConsumer, attributes);
    }

    /**
     * Generate a <code>&lt;fi:styling&gt;</code> element holding the attributes of a <code>ft:*</code>
     * element that are in the "fi:" namespace.
     * 
     * @param attributes the template instruction attributes
     * @return true if a <code>&lt;fi:styling&gt;</code> was produced
     * @throws SAXException
     */
    public static boolean generateStyling(ContentHandler handler, Map attributes) throws SAXException {
        AttributesImpl attr = null;
        Iterator entries = attributes.entrySet().iterator();
        while(entries.hasNext()) {
            Map.Entry entry = (Map.Entry)entries.next();
            String key = (String)entry.getKey();
            
            // FIXME: JXTG only gives the local name of attributes, so we can't distinguish namespaces...            
            if (!"id".equals(key) && !"widget-id".equals(key)) {
                if (attr == null)
                    attr = new AttributesImpl();
                attr.addCDATAAttribute(key, (String)entry.getValue());
            }
        }
        
        if (attr != null) {
            // There were some styling attributes
            handler.startElement(FormsConstants.INSTANCE_NS, "styling", FormsConstants.INSTANCE_PREFIX_COLON + "styling", attr);
            handler.endElement(FormsConstants.INSTANCE_NS, "styling", FormsConstants.INSTANCE_PREFIX_COLON + "styling");
            return true;
        } else {
            return false;
        }
    }

    /**
     * A SAX pipe that buffers the <code>endElement()</code> event of the root element.
     * This is needed by the generator version of the FormsTransformer (see jx-macros.xml).
     *
     * @version $Id$
     */
    private static class RootBufferingPipe extends AbstractXMLPipe {
        private int depth = 0;

        private String rootUri;
        private String rootLoc;
        private String rootRaw;
        private Map arguments;
        private boolean forbidStyling = false;

        public RootBufferingPipe(XMLConsumer next) {
            this(next, Collections.EMPTY_MAP);
        }

        public RootBufferingPipe(XMLConsumer next, Map arguments) {
            this.setConsumer(next);
            this.arguments = arguments;
        }

        public void startElement(String uri, String loc, String raw, Attributes a)
        throws SAXException {
            super.startElement(uri, loc, raw, a);
            if (depth == 0) {
                // Root element: keep its description
                this.rootUri = uri;
                this.rootLoc = loc;
                this.rootRaw = raw;
                
                // And produce fi:styling from attributes
                this.forbidStyling = generateStyling(this.contentHandler, arguments);
            }
            
            if (depth == 1 && forbidStyling &&
                uri.equals(FormsConstants.INSTANCE_NS) && loc.equals("styling")) {
                throw new SAXException("Cannot use 'fi:*' attributes and <fi:styling> at the same time");
            }

            depth++;
        }

        public void endElement(String uri, String loc, String raw)
        throws SAXException {
            depth--;
            if (depth > 0) {
                // Propagate all but root element
                super.endElement(uri, loc, raw);
            }
        }

        public void flushRoot() throws SAXException {
            if (depth != 0) {
                throw new IllegalStateException("Depth is not zero");
            }
            super.endElement(this.rootUri, this.rootLoc, this.rootRaw);
        }
    }
}
