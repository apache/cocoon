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

import java.lang.reflect.Array;
import java.util.Locale;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.datatype.Datatype;
import org.apache.cocoon.forms.datatype.SelectionList;
import org.apache.cocoon.forms.datatype.convertor.ConversionResult;
import org.apache.cocoon.forms.datatype.convertor.Convertor;
import org.apache.cocoon.forms.event.*;
import org.apache.cocoon.forms.util.I18nMessage;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.forms.validation.ValidationErrorAware;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A MultiValueField is mostly the same as a normal {@link Field}, but can
 * hold multiple values. A MultiValueField should have a Datatype which
 * has a SelectionList, because the user will always select the values
 * from a list. A MultiValueField has no concept of "required", you should
 * instead use the ValueCountValidationRule to check how many items the user
 * has selected.
 *
 * <p>A MultiValueField also has a {@link Datatype} associated with it. In
 * case of MultiValueFields, this Datatype will always be an array
 * type, thus {@link Datatype#isArrayType()} will always return true, and
 * this in return has an influence on the kind of validation rules that
 * can be used with the Datatype (see {@link Datatype} description for more
 * information).</p>
 *
 * @version $Id$
 */
public class MultiValueField extends AbstractWidget
                             implements ValidationErrorAware, SelectableWidget, DataWidget,
                                        ValueChangedListenerEnabled {

    private static final String MULTIVALUEFIELD_EL = "multivaluefield";
    private static final String VALUES_EL = "values";
    private static final String VALUE_EL = "value";
    private static final String VALIDATION_MSG_EL = "validation-message";

    private final MultiValueFieldDefinition definition;

    private SelectionList selectionList;
    private String[] enteredValues;
    private String invalidEnteredValue;
    private Object[] values;
    private ValidationError validationError;
    private ValueChangedListener listener;


    public MultiValueField(MultiValueFieldDefinition definition) {
        super(definition);
        this.definition = definition;
        this.listener = definition.getValueChangedListener();
    }

    public void initialize() {
        this.selectionList = this.definition.getSelectionList();
        super.initialize();
    }

    public WidgetDefinition getDefinition() {
        return definition;
    }

    public void readFromRequest(FormContext formContext) {
        if (!getCombinedState().isAcceptingInputs()) {
            return;
        }

        enteredValues = formContext.getRequest().getParameterValues(getRequestParameterName());
        invalidEnteredValue = null;
        validationError = null;
        Object[] oldValues = values;
        values = null;

        boolean conversionFailed = false;
        if (enteredValues != null) {
            Object[] tempValues = (Object[])Array.newInstance(getDatatype().getTypeClass(), enteredValues.length);
            for (int i = 0; i < enteredValues.length; i++) {
                String param = enteredValues[i];
                ConversionResult conversionResult = definition.getDatatype().convertFromString(param, formContext.getLocale());
                if (conversionResult.isSuccessful()) {
                    tempValues[i] = conversionResult.getResult();
                } else {
                    conversionFailed = true;
                    invalidEnteredValue = param;
                    break;
                }
            }

            if (!conversionFailed)
                values = tempValues;
            else
                values = null;
        } else {
            values = new Object[0];
        }

        engenderChangeEvent(oldValues);
    }

    private void engenderChangeEvent(Object[] oldValues) {
        boolean hasListeners = hasValueChangedListeners() || this.getForm().hasFormHandler();
        if (hasListeners) {
            if (values != null) {
                boolean changed = false;
                if (oldValues == null) {
                    changed = true;
                } else if (oldValues.length != values.length) {
                    changed = true;
                } else {
                    for (int i = 0; i < values.length; i++) {
                        if (!values[i].equals(oldValues[i])) {
                            changed = true;
                            break;
                        }
                    }
                }
                if (changed)
                    getForm().addWidgetEvent(new ValueChangedEvent(this, oldValues, values));
            }
        }
    }

    public boolean validate() {
        if (!getCombinedState().isValidatingValues()) {
            this.wasValid = true;
            return true;
        }

        if (values != null) {
            validationError = definition.getDatatype().validate(values, new ExpressionContextImpl(this));
        } else if (invalidEnteredValue != null) {
            validationError = new ValidationError(new I18nMessage("multivaluefield.conversionfailed", new String[] {invalidEnteredValue}, FormsConstants.I18N_CATALOGUE));
        }

        this.wasValid = validationError == null ? super.validate() : false;
        return this.wasValid;
    }

    /**
     * @return "multivaluefield"
     */
    public String getXMLElementName() {
        return MULTIVALUEFIELD_EL;
    }

    public void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        contentHandler.startElement(FormsConstants.INSTANCE_NS, VALUES_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALUES_EL, XMLUtils.EMPTY_ATTRIBUTES);
        Convertor convertor = definition.getDatatype().getConvertor();
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                contentHandler.startElement(FormsConstants.INSTANCE_NS, VALUE_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALUE_EL, XMLUtils.EMPTY_ATTRIBUTES);
                String value = convertor.convertToString(values[i], locale, null);
                contentHandler.characters(value.toCharArray(), 0, value.length());
                contentHandler.endElement(FormsConstants.INSTANCE_NS, VALUE_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALUE_EL);
            }
        } else if (enteredValues != null) {
            for (int i = 0; i < enteredValues.length; i++) {
                contentHandler.startElement(FormsConstants.INSTANCE_NS, VALUE_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALUE_EL, XMLUtils.EMPTY_ATTRIBUTES);
                String value = enteredValues[i];
                contentHandler.characters(value.toCharArray(), 0, value.length());
                contentHandler.endElement(FormsConstants.INSTANCE_NS, VALUE_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALUE_EL);
            }
        }
        contentHandler.endElement(FormsConstants.INSTANCE_NS, VALUES_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALUES_EL);

        // validation message element
        if (validationError != null) {
            contentHandler.startElement(FormsConstants.INSTANCE_NS, VALIDATION_MSG_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALIDATION_MSG_EL, XMLUtils.EMPTY_ATTRIBUTES);
            validationError.generateSaxFragment(contentHandler);
            contentHandler.endElement(FormsConstants.INSTANCE_NS, VALIDATION_MSG_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALIDATION_MSG_EL);
        }

        // the selection list
        if (this.selectionList != null) {
            this.selectionList.generateSaxFragment(contentHandler, locale);
        }

        // include some info about the datatype
        definition.getDatatype().generateSaxFragment(contentHandler, locale);
    }


    public Object getValue() {
        return values;
    }

    public void setValue(Object value) {
        if (value == null) {
            setValues(new Object[0]);
        } else if (value.getClass().isArray()) {
            setValues((Object[])value);
        } else {
            throw new RuntimeException("Cannot set value of field '" + getRequestParameterName() + "' with an object of type " +
                                       value.getClass().getName());
        }
        getForm().addWidgetUpdate(this);
    }

    public void setValues(Object[] values) {
        // check that all the objects in the array correspond to the datatype
        for (int i = 0; i < values.length; i++) {
            if (!definition.getDatatype().getTypeClass().isAssignableFrom(values[i].getClass())) {
                throw new RuntimeException("Cannot set value of field '" + getRequestParameterName() + "' with an object of type " +
                                           values[i].getClass().getName());
            }
        }
        Object[] oldValues = this.values;
        this.values = values;
        engenderChangeEvent(oldValues);
        getForm().addWidgetUpdate(this);
    }

    /**
     * Set this field's selection list.
     * @param selectionList The new selection list.
     */
    public void setSelectionList(SelectionList selectionList) {
        if (selectionList == null) {
            throw new IllegalArgumentException("An MultiValueField's selection list cannot be null.");
        }

        if (selectionList.getDatatype() != null &&
                selectionList.getDatatype() != definition.getDatatype()) {
            throw new RuntimeException("Tried to assign a SelectionList that is not associated with this widget's datatype.");
        }
        this.selectionList = selectionList;
        getForm().addWidgetUpdate(this);
    }

    /**
     * Read this field's selection list from an external source.
     * All Cocoon-supported protocols can be used.
     * The format of the XML produced by the source should be the
     * same as in case of inline specification of the selection list,
     * thus the root element should be a <code>fd:selection-list</code>
     * element.
     * @param uri The URI of the source.
     */
    public void setSelectionList(String uri) {
        setSelectionList(this.definition.buildSelectionList(uri));
    }

    /**
     * Set this field's selection list using values from an in-memory
     * object. The <code>object</code> parameter should point to a collection
     * (Java collection or array, or Javascript array) of objects. Each object
     * belonging to the collection should have a <em>value</em> property and a
     * <em>label</em> property, whose values are used to specify the <code>value</code>
     * attribute and the contents of the <code>fd:label</code> child element
     * of every <code>fd:item</code> in the list.
     * <p>Access to the values of the above mentioned properties is done
     * via <a href="http://jakarta.apache.org/commons/jxpath/users-guide.html">XPath</a> expressions.
     * @param model The collection used as a model for the selection list.
     * @param valuePath An XPath expression referring to the attribute used
     * to populate the values of the list's items.
     * @param labelPath An XPath expression referring to the attribute used
     * to populate the labels of the list's items.
     */
    public void setSelectionList(Object model, String valuePath, String labelPath) {
        setSelectionList(this.definition.buildSelectionListFromModel(model, valuePath, labelPath));
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

    public ValidationError getValidationError() {
        return this.validationError;
    }

    public void setValidationError(ValidationError error) {
        this.validationError = error;
        getForm().addWidgetUpdate(this);
    }

    public Datatype getDatatype() {
        return definition.getDatatype();
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
}
