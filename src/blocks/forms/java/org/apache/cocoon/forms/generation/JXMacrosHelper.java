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

package org.apache.cocoon.forms.generation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.formmodel.Form;
import org.apache.cocoon.forms.formmodel.Repeater;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.commons.collections.ArrayStack;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Helper class for the implementation of the CForms template language with JXTemplate macros.
 *
 * @version CVS $Id$
 */
public class JXMacrosHelper {

    private XMLConsumer cocoonConsumer;
    private ArrayStack stack = new ArrayStack();
    private Map classes; // lazily created

    /**
     * Builds and helper object, given the generator's consumer.
     *
     * @param consumer the generator's consumer
     * @return a helper object
     */
    public static JXMacrosHelper createHelper(XMLConsumer consumer) {
        return new JXMacrosHelper(consumer);
    }

    public JXMacrosHelper(XMLConsumer consumer) {
        this.cocoonConsumer = consumer;
    }

    public void startForm(Form form, Map attributes) throws SAXException {
        // build attributes
        AttributesImpl attrs = new AttributesImpl();
        Iterator iter = attributes.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            attrs.addCDATAAttribute((String)entry.getKey(), (String)entry.getValue());
        }

        this.cocoonConsumer.startPrefixMapping(Constants.INSTANCE_PREFIX, Constants.INSTANCE_NS);
        this.cocoonConsumer.startElement(Constants.INSTANCE_NS,
                                         "form-template",
                                         Constants.INSTANCE_PREFIX_COLON + "form-template",
                                         attrs);
    }

    public void endForm() throws SAXException {
        this.cocoonConsumer.endElement(Constants.INSTANCE_NS,
                                       "form-template",
                                       Constants.INSTANCE_PREFIX_COLON + "form-template");
        this.cocoonConsumer.endPrefixMapping(Constants.INSTANCE_PREFIX);
    }

    /**
     * Flush the root element name that has been stored in
     * {@link #generateWidget(Widget, Locale)}.
     *
     * @param obj the object that is terminated (widget or validation error)
     * @throws SAXException
     */
    public void flushRoot(Object obj) throws SAXException {
        Object stackObj = stack.pop();
        if (stackObj != obj) {
            throw new IllegalStateException("Flushing on wrong widget (expected " + stackObj +
                                            ", got " + obj + ")");
        }
        ((RootBufferingPipe) stack.pop()).flushRoot();
    }

    /**
     * Get a child widget of a given widget, throwing an exception if no such child exists.
     *
     * @param currentWidget
     * @param id
     */
    public Widget getWidget(Widget currentWidget, String path) {
        Widget result = currentWidget.lookupWidget(path);

        if (result != null) {
            return result;
        } else {
            throw new IllegalArgumentException("Widget '" + currentWidget +
                                               "' has no child named '" + path + "'");
        }
    }

    public Repeater getRepeater(Widget currentWidget, String id) {
        Widget child = getWidget(currentWidget, id);
        if (child instanceof Repeater) {
            return (Repeater)child;
        } else {
            throw new IllegalArgumentException("Widget '" + child + "' is not a repeater");
        }
    }

    /**
     * Generate a widget's SAX fragment, buffering the root element's <code>endElement()</code>
     * event so that the template can insert styling information in it.
     *
     * @param widget
     * @param locale
     * @throws SAXException
     */
    public void generateWidget(Widget widget, Locale locale) throws SAXException {
        // Needs to be buffered
        RootBufferingPipe pipe = new RootBufferingPipe(this.cocoonConsumer);
        this.stack.push(pipe);
        this.stack.push(widget);
        widget.generateSaxFragment(pipe, locale);
    }

    public void generateWidgetLabel(Widget widget, String id) throws SAXException {
        getWidget(widget, id).generateLabel(this.cocoonConsumer);
    }

    public void generateRepeaterWidgetLabel(Widget widget, String id, String widgetId) throws SAXException {
        getRepeater(widget, id).generateWidgetLabel(widgetId, this.cocoonConsumer);
    }

    public void generateRepeaterSize(Widget widget, String id) throws SAXException {
        getRepeater(widget, id).generateSize(this.cocoonConsumer);
    }

    public void generateValidationError(ValidationError error) throws SAXException {
        // Needs to be buffered
        RootBufferingPipe pipe = new RootBufferingPipe(this.cocoonConsumer);
        this.stack.push(pipe);
        this.stack.push(error);
        error.generateSaxFragment(pipe);
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
            throw new IllegalArgumentException("No class '" + id + "' has been defined.");
        } else {
            return result;
        }
    }

    public boolean isSelectedCase(Widget unionWidget, String caseValue) {
        String value = (String)unionWidget.getValue();
        return caseValue.equals(value != null ? value : "");
    }

    public boolean isVisible(Widget widget) {
        return widget.getCombinedState().isDisplayingValues();
    }

    /**
     * A SAX pipe that buffers the <code>endElement()</code> event of the root element.
     * This is needed by the generator version of the Woody transformer (see woody-jxmacros.xml).
     *
     * @version CVS $Id$
     */
    private static class RootBufferingPipe extends AbstractXMLPipe {
        private int depth = 0;

        private String rootUri;
        private String rootLoc;
        private String rootRaw;

        public RootBufferingPipe(XMLConsumer next) {
            this.setConsumer(next);
        }

        public void startElement(String uri, String loc, String raw, Attributes a)
        throws SAXException {
            if (depth == 0) {
                // Root element: keep its description
                this.rootUri = uri;
                this.rootLoc = loc;
                this.rootRaw = raw;
            }
            depth++;
            super.startElement(uri, loc, raw, a);
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
