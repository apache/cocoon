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
package org.apache.cocoon.forms.formmodel;

import java.util.Locale;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.datatype.Datatype;
import org.apache.cocoon.forms.datatype.SelectionList;
import org.apache.cocoon.forms.event.ValueChangedEvent;
import org.apache.cocoon.forms.event.ValueChangedListener;
import org.apache.cocoon.forms.event.ValueChangedListenerEnabled;
import org.apache.cocoon.forms.event.WidgetEvent;
import org.apache.cocoon.forms.event.WidgetEventMulticaster;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.ObjectUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * An Output widget can be used to show a non-editable value to the user.
 * An Output widget is associated with a certain
 * {@link org.apache.cocoon.forms.datatype.Datatype Datatype}.
 *
 * <p>An Output widget is always valid and never required.
 *
 * @version $Id$
 */
public class Output extends AbstractWidget
                    implements DataWidget, ValueChangedListenerEnabled {

    private final OutputDefinition definition;
    private Object value;
    private ValueChangedListener listener;


    public Output(OutputDefinition definition) {
        super(definition);
        this.definition = definition;
        this.listener = definition.getValueChangedListener();
    }

    public Datatype getDatatype() {
        return definition.getDatatype();
    }

    public WidgetDefinition getDefinition() {
        return definition;
    }

    public OutputDefinition getOutputDefinition() {
        return definition;
    }

    public void initialize() {
        Object value = this.definition.getInitialValue();
        if (value != null) {
            setValue(value);
        }
        super.initialize();
    }

    public void readFromRequest(FormContext formContext) {
        // do nothing
    }

    /**
     * @see org.apache.cocoon.forms.formmodel.Widget#validate()
     */
    public boolean validate() {
        return true;
    }

    /**
     * @see org.apache.cocoon.forms.formmodel.Widget#isValid()
     */
    public boolean isValid() {
        return true;
    }

    private static final String OUTPUT_EL = "output";
    private static final String VALUE_EL = "value";


    /**
     * @return "output"
     */
    public String getXMLElementName() {
        return OUTPUT_EL;
    }

    protected void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        // the value
        if (value != null) {
            contentHandler.startElement(FormsConstants.INSTANCE_NS, VALUE_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALUE_EL, XMLUtils.EMPTY_ATTRIBUTES);
            String stringValue;
            stringValue = definition.getDatatype().convertToString(value, locale);
            contentHandler.characters(stringValue.toCharArray(), 0, stringValue.length());
            contentHandler.endElement(FormsConstants.INSTANCE_NS, VALUE_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALUE_EL);
        }

        // generate selection list, if any
        SelectionList selectionList = definition.getSelectionList();
        if (selectionList != null) {
            selectionList.generateSaxFragment(contentHandler, locale);
        }

        // include some info about the datatype
        definition.getDatatype().generateSaxFragment(contentHandler, locale);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object object) {
        if (object != null && !definition.getDatatype().getTypeClass().isAssignableFrom(object.getClass())) {
            throw new RuntimeException("Tried to set value of output widget \""
                                       + getRequestParameterName()
                                       + "\" with an object of an incorrect type: "
                                       + "expected " + definition.getDatatype().getTypeClass()
                                       + ", received " + object.getClass() + ".");
        }
        if (!ObjectUtils.equals(value, object)) {
            Object oldValue = value;
            value = object;
            if (hasValueChangedListeners() || getForm().hasFormHandler()) {
                getForm().addWidgetEvent(new ValueChangedEvent(this, oldValue, value));
            }
            getForm().addWidgetUpdate(this);
        }
    }

    public void addValueChangedListener(ValueChangedListener listener) {
        this.listener = WidgetEventMulticaster.add(this.listener, listener);
    }

    public void removeValueChangedListener(ValueChangedListener listener) {
        this.listener = WidgetEventMulticaster.remove(this.listener, listener);
    }

    public boolean hasValueChangedListeners() {
        return this.listener != null;
    }

    public void broadcastEvent(WidgetEvent event) {
        if (event instanceof ValueChangedEvent) {
            if (this.listener != null) {
                this.listener.valueChanged((ValueChangedEvent)event);
            }
        } else {
            // Other kinds of events
            super.broadcastEvent(event);
        }
    }
}
