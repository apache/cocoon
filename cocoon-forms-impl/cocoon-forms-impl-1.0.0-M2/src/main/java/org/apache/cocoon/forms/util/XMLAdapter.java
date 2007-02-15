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
package org.apache.cocoon.forms.util;

import java.util.Iterator;
import java.util.Locale;

import org.apache.cocoon.xml.AbstractXMLConsumer;
import org.apache.cocoon.forms.datatype.Datatype;
import org.apache.cocoon.forms.datatype.convertor.ConversionResult;
import org.apache.cocoon.forms.formmodel.Action;
import org.apache.cocoon.forms.formmodel.AggregateField;
import org.apache.cocoon.forms.formmodel.BooleanField;
import org.apache.cocoon.forms.formmodel.ContainerWidget;
import org.apache.cocoon.forms.formmodel.DataWidget;
import org.apache.cocoon.forms.formmodel.Form;
import org.apache.cocoon.forms.formmodel.MultiValueField;
import org.apache.cocoon.forms.formmodel.Repeater;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Adapter class that wraps a <code>Form</code> object and makes it
 * possible to populate a widget hierarchy from XML in form of SAX
 * events and serialize the content of the widget hierarchy as XML.
 *
 * <p>The XML format is such that there is one XML element for each
 * widget and the element get the widgets id as name. Exceptions from
 * this is that the elements in a repeater gets the name
 * <code>item</code> and a attribute <code>position</code> with the
 * position of the repeater child, instead of just a number (which is
 * not allowed as element name). Childs of a
 * <code>MultiValueField</code> are also embeded within a
 * <code>item</code> element. If the <code>Form</code> widget does
 * not have an id it get the name <code>uknown</code>.</p>
 *
 * <p>An <code>AggregateField</code> can both be interpreted as one value
 * and as several widgets. This ambiguity is resolved by chosing to emit
 * the single value rather than the fields as XML. For population of the
 * form both forms are however allowed.</p>
 *
 * @version $Id$
 */
public class XMLAdapter extends AbstractXMLConsumer implements XMLizable {

    /** Name of element in list. */
    private final static String ITEM = "item";
    /** Name of unkown element. */
    private final static String UNKNOWN = "unknown";
    /** Name of position attribute in list. */
    private final static String POSITION = "position";
    /** The namespace prefix of this component. */
    private final static String PREFIX = "";
    /** The namespace URI of this component. */
    private final static String URI = "";

    /** The <code>ContentHandler</code> receiving SAX events. */
    private ContentHandler contentHandler;
    /** The <code>Widget</code> to read and write XML to. */
    private Widget widget;
    /** The <code>Widget</code> that we are currently writing to. */
    private Widget currentWidget;
    /** The <code>Locale</code> that decides how to convert widget values to strings */
    private Locale locale;
    /** Is a <code>MultiValueField</code> handled? */
    private boolean isMultiValueItem;
    /** The buffer used to receive character events */
    private StringBuffer textBuffer;


    /**
     * Wrap a <code>Form</code> with an <code>XMLAdapter</code>
     */
    public XMLAdapter(Widget widget) {
        this.widget = widget;
        this.locale = Locale.US;
    }

    /**
     * Set the locale used for conversion between XML data and Java objects
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * Get the locale used for conversion between XML data and Java objects
     */
    public Locale getLocale() {
        return this.locale;
    }

    /* ================ SAX -> Widget ================ */

    /*
     * The current state during handling of input events is described
     * by <code>currentWidget</code> that points to the widget that is
     * beeing populated. The state that the population has not began
     * yet or that it is finished is encoded by setting
     * <code>currentWidget</code> to <code>null</code> and the state of
     * being within a <code>item</code> within a
     * <code>MultiValueField</code> is encoded by setting the variable
     * <code>isMultiValueItem</code> to true.
    */

    /**
     * Receive notification of the beginning of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     * @param a The attributes attached to the element. If there are no
     *          attributes, it shall be an empty Attributes object.
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        handleText();

        if (this.currentWidget == null) {
            // The name of the root element is ignored
            this.currentWidget = this.widget;

        } else if (this.currentWidget instanceof ContainerWidget) {
            Widget child = ((ContainerWidget)this.currentWidget).getChild(loc);
            if (child == null) {
                throw new SAXException("There is no widget with id: " + loc +
                                       " as child to: " + this.currentWidget.getId());
            }
            this.currentWidget = child;

        } else if (this.currentWidget instanceof Repeater) {
            // In a repeater the XML elements are added in the order
            // they are recieved, the position attribute is not used
            if (!ITEM.equals(loc)) {
                throw new SAXException("The element: " + loc +
                                       " is not allowed as a direct child of a Repeater");
            }
            Repeater repeater = (Repeater) currentWidget;
            this.currentWidget = repeater.addRow();

        } else if (this.currentWidget instanceof MultiValueField) {
            this.isMultiValueItem = true;
            if (!ITEM.equals(loc)) {
                throw new SAXException("The element: " + loc +
                                       " is not allowed as a direct child of a MultiValueField");
            }
        }
    }

    /**
     * Receive notification of the end of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     */
    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        handleText();
        if (this.currentWidget == null)
            throw new SAXException("Wrong state");

        String id = this.currentWidget.getId();

        if (this.currentWidget instanceof Form) {
            this.currentWidget = null;
            return;
        } else if (this.currentWidget instanceof AggregateField) {
            ((AggregateField)this.currentWidget).combineFields();
        } else if (this.currentWidget instanceof Repeater.RepeaterRow) {
            id = ITEM;
        } else if (this.currentWidget instanceof MultiValueField && loc.equals(ITEM)) {
            this.isMultiValueItem = false;
            return;
        }

        if (loc.equals(id))
            this.currentWidget = this.currentWidget.getParent();
        else
            throw new SAXException("Unexpected element, was: " + loc +
                                   " expected: " + id);
    }

    /**
     * Receive notification of character data.
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void characters(char ch[], int start, int len)
    throws SAXException {
        // Buffer text, as a single text node can be sent in several chunks.
        if (this.textBuffer == null) {
            this.textBuffer = new StringBuffer();
        }
        this.textBuffer.append(ch, start, len);
    }

    /**
     * Handle text nodes, if any. Called on every potential text node boundary,
     * i.e. start and end element events.
     *
     * @throws SAXException
     */
    private void handleText() throws SAXException {
        if (this.textBuffer == null)
            return;

        String input = this.textBuffer.toString().trim();
        this.textBuffer = null; // clear buffer
        if (input.length() == 0)
            return;

        if (this.currentWidget instanceof MultiValueField && isMultiValueItem) {
            MultiValueField field = (MultiValueField) this.currentWidget;
            Datatype type = field.getDatatype();
            ConversionResult conv = type.convertFromString(input, this.locale);
            if (!conv.isSuccessful()) {
                throw new SAXException("Could not convert: " + input +
                                       " to " + type.getTypeClass());
            }
            Object[] values = (Object[]) field.getValue();
            int valLen = values == null ? 0 : values.length;
            Object[] newValues = new Object[valLen + 1];
            for (int i = 0; i < valLen; i++) {
                newValues[i] = values[i];
            }
            newValues[valLen] = conv.getResult();
            field.setValues(newValues);
        } else if (this.currentWidget instanceof DataWidget) {
            DataWidget data = (DataWidget) this.currentWidget;
            Datatype type = data.getDatatype();
            ConversionResult conv = type.convertFromString(input, this.locale);
            if (!conv.isSuccessful()) {
                throw new SAXException("Could not convert: " + input +
                                       " to " + type.getTypeClass());
            }
            data.setValue(conv.getResult());
        } else if (this.currentWidget instanceof BooleanField) {
            // FIXME: BooleanField should implement DataWidget, which
            // would make this case unnecessary
            if ("true".equals(input))
                this.currentWidget.setValue(Boolean.TRUE);
            else if ("false".equals(input))
                this.currentWidget.setValue(Boolean.FALSE);
            else
                throw new SAXException("Unkown boolean: " + input);
        } else {
            throw new SAXException("Unknown widget type: " + this.currentWidget);
        }
    }

    /* ================ Widget -> SAX ================ */

    /*
     * Just recurses in deep first order over the widget hierarchy and
     * emits XML
     */

    /**
     * Generates SAX events representing the object's state.
     */
    public void toSAX( ContentHandler handler ) throws SAXException {
        this.contentHandler = handler;

        this.contentHandler.startDocument();
        this.contentHandler.startPrefixMapping(PREFIX, URI);

        generateSAX(this.widget);

        this.contentHandler.endPrefixMapping(PREFIX);
        this.contentHandler.endDocument();
    }

    /**
     * Generate XML data.
     */
    private void generateSAX(Widget widget)
        throws SAXException {
        generateSAX(widget, null);
    }

    private void generateSAX(Widget widget, String id)
        throws SAXException {

        // no XML output for actions
        if (widget instanceof Action)
            return;

        if (id == null)
            id = widget.getId().length() == 0 ? UNKNOWN : widget.getId();

        final AttributesImpl attr = new AttributesImpl();
        if (widget instanceof Repeater.RepeaterRow)
            attribute(attr, POSITION, widget.getId());

        start(id, attr);
        // Placing the handling DataWidget before ContainerWidget
        // means that an AggregateField is handled like a DataWidget
        if (widget instanceof MultiValueField) {
            Datatype datatype = ((MultiValueField)widget).getDatatype();
            Object[] values = (Object[])widget.getValue();
            if (values != null)
                for (int i = 0; i < values.length; i++) {
                    start(ITEM, attr);
                    data(datatype.convertToString(values[i], this.locale));
                    end(ITEM);
                }
        } else if (widget instanceof DataWidget) {
            Datatype datatype = ((DataWidget)widget).getDatatype();
            if (widget.getValue() != null)
                data(datatype.convertToString(widget.getValue(), this.locale));
        } else if (widget instanceof BooleanField) {
            // FIXME: BooleanField should implement DataWidget, which
            // would make this case unnecessary
            if (widget.getValue() != null) {
                data(widget.getValue().toString());
            }
        } else if (widget instanceof ContainerWidget) {
            Iterator children = ((ContainerWidget)widget).getChildren();
            while (children.hasNext())
                generateSAX((Widget)children.next());
        } else if (widget instanceof Repeater) {
            Repeater repeater = (Repeater)widget;
            for (int i = 0; i < repeater.getSize(); i++)
                generateSAX(repeater.getRow(i), ITEM);
        }
        end(id);
    }

    private void attribute(AttributesImpl attr, String name, String value) {
        attr.addAttribute("", name, name, "CDATA", value);
    }

    private void start(String name, AttributesImpl attr)
    throws SAXException {
        String qName = PREFIX == "" ? name : PREFIX + ":" + name;
        this.contentHandler.startElement(URI, name, qName, attr);
        attr.clear();
    }

    private void end(String name)
    throws SAXException {
        String qName = PREFIX == "" ? name : PREFIX + ":" + name;
        this.contentHandler.endElement(URI, name, qName);
    }

    private void data(String data)
    throws SAXException {
        this.contentHandler.characters(data.toCharArray(), 0, data.length());
    }
}
