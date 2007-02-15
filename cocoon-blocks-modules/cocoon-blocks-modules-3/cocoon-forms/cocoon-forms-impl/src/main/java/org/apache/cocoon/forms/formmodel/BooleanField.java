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

import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.event.ValueChangedEvent;
import org.apache.cocoon.forms.event.ValueChangedListener;
import org.apache.cocoon.forms.event.ValueChangedListenerEnabled;
import org.apache.cocoon.forms.event.WidgetEvent;
import org.apache.cocoon.forms.event.WidgetEventMulticaster;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.forms.validation.ValidationErrorAware;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;

import org.apache.commons.lang.BooleanUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A widget to select a boolean value. Usually rendered as a checkbox.
 *
 * <p>You may wonder why we don't use a {@link Field} widget with an associated
 * Boolean Datatype instead. The reason is that many of the features of the Field
 * widget are overkill for a Boolean: validation is unnecessary (if the field is
 * not true it is false), the selectionlist associated with a Datatype also
 * has no purpose here (there would always be only 2 choices: true or false),
 * and the manner in which the request parameter of this widget is interpreted
 * is different (missing or empty request parameter means 'false', rather than null value).
 *
 * @version $Id$
 */
public class BooleanField extends AbstractWidget
                          implements ValidationErrorAware, ValueChangedListenerEnabled {

    private static final String BOOLEAN_FIELD_EL = "booleanfield";
    private static final String VALUE_EL = "value";
    private static final String VALIDATION_MSG_EL = "validation-message";

    // FIXME(SW) : should the initial value be false or null ? This would allow
    // event listeners to be triggered at bind time.
    private Boolean value = Boolean.FALSE;
    private final BooleanFieldDefinition definition;
    /** Additional listeners to those defined as part of the widget definition (if any). */
    private ValueChangedListener listener;
    protected ValidationError validationError;


    public BooleanField(BooleanFieldDefinition definition) {
        super(definition);
        this.definition = definition;
        this.listener = definition.getValueChangedListener();
    }

    public WidgetDefinition getDefinition() {
        return this.definition;
    }

    public void initialize() {
        Boolean value = this.definition.getInitialValue();
        if (value != null) {
            setValue(value);
        }
        super.initialize();
    }

    public void readFromRequest(FormContext formContext) {
        if (!getCombinedState().isAcceptingInputs()) {
            return;
        }

        validationError = null;
        Object oldValue = value;
        String param = formContext.getRequest().getParameter(getRequestParameterName());

        value = BooleanUtils.toBooleanObject(definition.getTrueParamValue().equals(param));

        if (!value.equals(oldValue)) {
            getForm().addWidgetEvent(new ValueChangedEvent(this, oldValue, value));
        }
    }

    /**
     * Returns the validation error, if any. There will always be a validation error in case the
     * {@link #validate()} method returned false.
     */
    public ValidationError getValidationError() {
        return validationError;
    }

    /**
     * Set a validation error on this field. This allows fields to be externally marked as invalid by
     * application logic.
     *
     * @param error the validation error
     */
    public void setValidationError(ValidationError error) {
        this.validationError = error;
        getForm().addWidgetUpdate(this);
    }

    /**
     * @return "booleanfield"
     */
    public String getXMLElementName() {
        return BOOLEAN_FIELD_EL;
    }

    protected AttributesImpl getXMLElementAttributes() {
        AttributesImpl attrs = super.getXMLElementAttributes();
        // Add the parameter value for true
        attrs.addCDATAAttribute("true-value", definition.getTrueParamValue());
        return attrs;
    }

    public void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        // value element
        contentHandler.startElement(FormsConstants.INSTANCE_NS, VALUE_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALUE_EL, XMLUtils.EMPTY_ATTRIBUTES);

        String stringValue = BooleanUtils.toBoolean(value) ? definition.getTrueParamValue() : "false";

        contentHandler.characters(stringValue.toCharArray(), 0, stringValue.length());
        contentHandler.endElement(FormsConstants.INSTANCE_NS, VALUE_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALUE_EL);

        // validation message element: only present if the value is not valid
        if (validationError != null) {
            contentHandler.startElement(FormsConstants.INSTANCE_NS, VALIDATION_MSG_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALIDATION_MSG_EL, XMLUtils.EMPTY_ATTRIBUTES);
            validationError.generateSaxFragment(contentHandler);
            contentHandler.endElement(FormsConstants.INSTANCE_NS, VALIDATION_MSG_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALIDATION_MSG_EL);
        }
    }

    public Object getValue() {
        return value;
    }

    /**
     * Sets value of the field. If value is null, it is considered to be false
     * (see class comment).
     */
    public void setValue(Object object) {
        if (object == null) {
            object = Boolean.FALSE;
        }

        if (!(object instanceof Boolean)) {
            throw new RuntimeException("Cannot set value of boolean field '" + getRequestParameterName() + "' to a non-Boolean value.");
        }

        Object oldValue = value;
        value = (Boolean)object;
        if (!value.equals(oldValue)) {
            Form form = getForm();
            if (hasValueChangedListeners() || this.getForm().hasFormHandler()) {
                form.addWidgetEvent(new ValueChangedEvent(this, oldValue, value));
            }
            form.addWidgetUpdate(this);
        }
    }

    /**
     * Adds a ValueChangedListener to this widget instance. Listeners defined
     * on the widget instance will be executed in addtion to any listeners
     * that might have been defined in the widget definition.
     */
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
