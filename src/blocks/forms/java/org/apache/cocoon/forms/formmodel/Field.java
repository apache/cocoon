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
package org.apache.cocoon.forms.formmodel;

import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.datatype.Datatype;
import org.apache.cocoon.forms.datatype.SelectionList;
import org.apache.cocoon.forms.datatype.convertor.ConversionResult;
import org.apache.cocoon.forms.event.*;
import org.apache.cocoon.forms.util.I18nMessage;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.forms.validation.ValidationErrorAware;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.Locale;

/**
 * A general-purpose Widget that can hold one value. A Field widget can be associated
 * with a {@link org.apache.cocoon.forms.datatype.Datatype Datatype}, and thus
 * a Field widget can be used to edit different kinds of data, such as strings,
 * numbers and dates. A Datatype can also have an associated SelectionList, so
 * that the value for the Field can be selected from a list, rather than being
 * entered in a textbox. The validation of the field is delegated to its associated
 * Datatype.
 *
 * @author Bruno Dumon
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id$
 */
public class Field extends AbstractWidget implements ValidationErrorAware, DataWidget, SelectableWidget,
        ValueChangedListenerEnabled {
    /** Overrides selection list defined in FieldDefinition, if any. */
    protected SelectionList selectionList;
    /** Additional listeners to those defined as part of the widget definition (if any). */
    private ValueChangedListener listener;

    private final FieldDefinition fieldDefinition;

    protected String enteredValue;
    protected Object value;

    /**
     * Value state indicating that a new value has been read from the request,
     * but has not yet been parsed.
     */
    protected final static int VALUE_UNPARSED = 0;

    /**
     * Value state indicating that a value has been parsed, but needs to be
     * validated (that must occur before the value is given to the application)
     */
    protected final static int VALUE_PARSED = 1;

    /**
     * Value state indicating that a parse error was encountered but should not
     * yet be displayed.
     */
    protected final static int VALUE_PARSE_ERROR = 2;

    /**
     * Value state indicating that validate() has been called when state was
     * VALUE_PARSE_ERROR. This makes the error visible on output.
     */
    protected final static int VALUE_DISPLAY_PARSE_ERROR = 3;

    /**
     * Transient value state indicating that validation is going on.
     *
     * @see #validate()
     */
    protected final static int VALUE_VALIDATING = 4;

    /**
     * Value state indicating that validation has occured, but that any error should not
     * yet be displayed.
     */
    protected final static int VALUE_VALIDATED = 5;

    /**
     * Value state indicating that value validation has occured, and the
     * validation error, if any, should be displayed.
     */
    protected final static int VALUE_DISPLAY_VALIDATION = 6;

    // At startup, we have no value to parse (both enteredValue and value are null),
    // but need to validate (e.g. error if field is required)
    protected int valueState = VALUE_PARSED;


    /**
     * Transient widget processing state indicating that the widget is currently validating
     * (used to avoid endless loops when a validator calls getValue)
     */
    protected ValidationError validationError;


    public Field(FieldDefinition fieldDefinition) {
        this.fieldDefinition = fieldDefinition;
    }

    public final FieldDefinition getFieldDefinition() {
        return this.fieldDefinition;
    }

    protected WidgetDefinition getDefinition() {
        return this.fieldDefinition;
    }

    public Object getValue() {
        // if getValue() is called on this field while we're validating, then it's because a validation
        // rule called getValue(), so then we just return the parsed (but not VALUE_VALIDATED) value to avoid an endless loop
        if (this.valueState == VALUE_VALIDATING) {
            return this.value;
        }

        // Parse the value
        if (this.valueState == VALUE_UNPARSED) {
            doParse();
        }

        // Validate the value if it was successfully parsed
        if (this.valueState == VALUE_PARSED) {
            doValidate();
        }

        return this.validationError == null ? this.value : null;
    }

    public void setValue(Object newValue) {
        if (newValue != null && !getDatatype().getTypeClass().isAssignableFrom(newValue.getClass())) {
            throw new RuntimeException("Incorrect value type for \"" + getRequestParameterName() +
                           "\" (expected " + getDatatype().getTypeClass() +
                           ", got " + newValue.getClass() + ").");
        }
        Object oldValue = this.value;
        boolean changed = ! (oldValue == null ? "" : oldValue).equals(newValue == null ? "" : newValue);
        // Do something only if value is different or null
        // (null allows to reset validation error)
        if (changed || newValue == null) {
            this.value = newValue;
            this.validationError = null;
            // Force validation, even if set by the application
            this.valueState = VALUE_PARSED;
            if (newValue != null) {
                this.enteredValue = getDatatype().convertToString(newValue, getForm().getLocale());
            } else {
                this.enteredValue = null;
            }
            if (changed && hasValueChangedListeners()) {
                getForm().addWidgetEvent(new ValueChangedEvent(this, oldValue, newValue));
            }
        }
    }

    public void readFromRequest(FormContext formContext) {
        String newEnteredValue = formContext.getRequest().getParameter(getRequestParameterName());
        // FIXME: Should we consider only non-null values, which allows to
        // split a form across several screens?
        //if (newEnteredValue != null) {
        readFromRequest(newEnteredValue);
        //}

    }

    protected void readFromRequest(String newEnteredValue) {
        // whitespace & empty field handling
        if (newEnteredValue != null) {
            // TODO make whitespace behaviour configurable !!
            newEnteredValue = newEnteredValue.trim();
            if (newEnteredValue.length() == 0) {
                newEnteredValue = null;
            }
        }

        // Only convert if the text value actually changed. Otherwise, keep the old value
        // and/or the old validation error (allows to keep errors when clicking on actions)
        if (!(newEnteredValue == null ? "" : newEnteredValue).equals((enteredValue == null ? "" : enteredValue))) {
            enteredValue = newEnteredValue;
            validationError = null;
            value = null;
            this.valueState = VALUE_UNPARSED;

            if (hasValueChangedListeners()) {
	        	    // Throw an event that will parse the value only if needed.
	    	        getForm().addWidgetEvent(new DeferredValueChangedEvent(this, value));
    	        }
        }
    }

    public boolean validate() {
        if (this.valueState == VALUE_UNPARSED) {
            doParse();
        }

        // Force validation on already validated values (but keep invalid parsings)
        if (this.valueState >= VALUE_VALIDATED) {
            this.valueState = VALUE_PARSED;
        }

        if (this.valueState == VALUE_PARSED) {
            doValidate();
            this.valueState = VALUE_DISPLAY_VALIDATION;
        } else if (this.valueState == VALUE_PARSE_ERROR) {
            this.valueState = VALUE_DISPLAY_PARSE_ERROR;
    }

        return this.validationError == null;
    }

    /**
     * Parse the value that has been read from the request.
     * Should be called when valueState is VALUE_UNPARSED.
     * On exit, valueState is set to either:
     * - VALUE_PARSED: successful parsing or null value. Value is set and ValidationError
     *   is cleared.
     * - VALUE_PARSE_ERROR: datatype parsing error. In that case, value is null and
     *   validationError is set.
     */
    private void doParse() {
        if (this.valueState != VALUE_UNPARSED) {
            throw new IllegalStateException("Field is not in UNPARSED state (" + this.valueState + ")");
        }
        // Clear value, it will be recomputed
        this.value = null;
        this.validationError = null;
        if (this.enteredValue != null) {
            // Parse the value
            ConversionResult conversionResult = getDatatype().convertFromString(this.enteredValue, getForm().getLocale());
            if (conversionResult.isSuccessful()) {
                this.value = conversionResult.getResult();
                this.valueState = VALUE_PARSED;
            } else {
                // Conversion failed
                this.validationError = conversionResult.getValidationError();
                // No need for further validation (and need to keep the above error)
                this.valueState = VALUE_PARSE_ERROR;
            }
        } else {
            // No value: needs to be validated
            this.valueState = VALUE_PARSED;
        }
    }

    /**
     * Validate the value once it has been parsed.
     * Should be called when valueState is VALUE_PARSED.
     * On exit, valueState is set to VALUE_VALIDATED, and validationError is set if
     * validation failed.
     */
    private void doValidate() {
        if (this.valueState != VALUE_PARSED) {
            throw new IllegalStateException("Field is not in PARSED state (" + this.valueState + ")");
        }

        // Go to transient validating state
        this.valueState = VALUE_VALIDATING;

        try {
            if (this.value == null && getFieldDefinition().isRequired()) {
                // Field is required
                this.validationError = new ValidationError(new I18nMessage("general.field-required", Constants.I18N_CATALOGUE));
            } else {
                if (super.validate() && value != null) {
                    // New-style validators were successful. Check the old-style ones.
                    this.validationError = getDatatype().validate(value, new ExpressionContextImpl(this));
                }
            }
        } finally {
            // Consider validation finished even in case of exception
            this.valueState = VALUE_VALIDATED;
        }
    }

    /**
     * Returns the validation error, if any. There will always be a validation error in case the
     * {@link #validate()} method returned false.
     */
    public ValidationError getValidationError() {
        // If needed, getValue() will do the validation
        getValue();
        return this.validationError;
    }

    /**
     * Set a validation error on this field. This allows fields to be externally marked as invalid by
     * application logic.
     *
     * @param error the validation error
     */
    public void setValidationError(ValidationError error) {
        this.validationError = error;
        this.valueState = VALUE_DISPLAY_VALIDATION;
    }

    public boolean isRequired() {
        return getFieldDefinition().isRequired();
    }


    private static final String FIELD_EL = "field";
    private static final String VALUE_EL = "value";
    private static final String VALIDATION_MSG_EL = "validation-message";


    /**
     * @return "field"
     */
    public String getXMLElementName() {
        return FIELD_EL;
    }

    /**
     * Adds the @required attribute
     */
    public AttributesImpl getXMLElementAttributes() {
        AttributesImpl attrs = super.getXMLElementAttributes();
        attrs.addCDATAAttribute("required", String.valueOf(isRequired()));
        return attrs;
    }

    public void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        if (enteredValue != null || value != null) {
            contentHandler.startElement(Constants.INSTANCE_NS, VALUE_EL, Constants.INSTANCE_PREFIX_COLON + VALUE_EL, XMLUtils.EMPTY_ATTRIBUTES);
            String stringValue;
            if (value != null) {
                stringValue = getDatatype().convertToString(value, locale);
            } else {
                stringValue = enteredValue;
            }
            contentHandler.characters(stringValue.toCharArray(), 0, stringValue.length());
            contentHandler.endElement(Constants.INSTANCE_NS, VALUE_EL, Constants.INSTANCE_PREFIX_COLON + VALUE_EL);
        }

        // validation message element: only present if the value is not valid
        if (validationError != null && (this.valueState == VALUE_DISPLAY_VALIDATION || this.valueState == VALUE_DISPLAY_PARSE_ERROR)) {
            contentHandler.startElement(Constants.INSTANCE_NS, VALIDATION_MSG_EL, Constants.INSTANCE_PREFIX_COLON + VALIDATION_MSG_EL, XMLUtils.EMPTY_ATTRIBUTES);
            validationError.generateSaxFragment(contentHandler);
            contentHandler.endElement(Constants.INSTANCE_NS, VALIDATION_MSG_EL, Constants.INSTANCE_PREFIX_COLON + VALIDATION_MSG_EL);
        }

        // generate selection list, if any
        if (selectionList != null) {
            selectionList.generateSaxFragment(contentHandler, locale);
        } else if (getFieldDefinition().getSelectionList() != null) {
            getFieldDefinition().getSelectionList().generateSaxFragment(contentHandler, locale);
        }

        // include some info about the datatype
        fieldDefinition.getDatatype().generateSaxFragment(contentHandler, locale);
    }


    /**
     * Set this field's selection list.
     * @param selectionList The new selection list.
     */
    public void setSelectionList(SelectionList selectionList) {
        if (selectionList != null &&
            selectionList.getDatatype() != null &&
            selectionList.getDatatype() != getDatatype()) {
            throw new RuntimeException("Tried to assign a SelectionList that is not associated with this widget's datatype.");
        }
        this.selectionList = selectionList;
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
        setSelectionList(getFieldDefinition().buildSelectionList(uri));
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
        setSelectionList(getFieldDefinition().buildSelectionListFromModel(model, valuePath, labelPath));
    }

    public Datatype getDatatype() {
        return getFieldDefinition().getDatatype();
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

    private boolean hasValueChangedListeners() {
        return this.listener != null || this.fieldDefinition.hasValueChangedListeners();
    }

    private void fireValueChangedEvent(ValueChangedEvent event) {
        if (this.listener != null) {
            this.listener.valueChanged(event);
        }
    }

    public void broadcastEvent(WidgetEvent event) {
        getFieldDefinition().fireValueChangedEvent((ValueChangedEvent)event);
        fireValueChangedEvent((ValueChangedEvent)event);
    }
}
