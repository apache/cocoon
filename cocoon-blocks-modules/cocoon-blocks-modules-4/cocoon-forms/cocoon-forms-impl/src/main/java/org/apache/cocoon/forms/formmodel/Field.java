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

import org.apache.cocoon.environment.Request;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.FormsRuntimeException;
import org.apache.cocoon.forms.datatype.Datatype;
import org.apache.cocoon.forms.datatype.SelectionList;
import org.apache.cocoon.forms.datatype.convertor.ConversionResult;
import org.apache.cocoon.forms.event.DeferredValueChangedEvent;
import org.apache.cocoon.forms.event.ValueChangedEvent;
import org.apache.cocoon.forms.event.ValueChangedListener;
import org.apache.cocoon.forms.event.ValueChangedListenerEnabled;
import org.apache.cocoon.forms.event.WidgetEvent;
import org.apache.cocoon.forms.event.WidgetEventMulticaster;
import org.apache.cocoon.forms.util.I18nMessage;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.forms.validation.ValidationErrorAware;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A general-purpose Widget that can hold one value. A Field widget can be associated
 * with a {@link org.apache.cocoon.forms.datatype.Datatype Datatype}, and thus
 * a Field widget can be used to edit different kinds of data, such as strings,
 * numbers and dates. A Datatype can also have an associated SelectionList, so
 * that the value for the Field can be selected from a list, rather than being
 * entered in a textbox. The validation of the field is delegated to its associated
 * Datatype.
 *
 * @version $Id$
 */
public class Field extends AbstractWidget
                   implements ValidationErrorAware, DataWidget, SelectableWidget,
                              ValueChangedListenerEnabled {

    /**
     * If the field was rendered as a suggestion-list and the user chose one of the suggestions,
     * the field's value is the chosen item's value and the <code>SUGGESTED_LABEL_ATTR</code> field
     * attribute contains the chosen item's label.
     *
     * @see #isSuggested()
     * @since 2.1.9
     */
    public static final String SUGGESTED_LABEL_ATTR = "suggested-label";

    /**
     * Value state indicating that a new value has been read from the request,
     * but has not yet been parsed.
     */
    protected static final int VALUE_UNPARSED = 0;

    /**
     * Value state indicating that a value has been parsed, but needs to be
     * validated (that must occur before the value is given to the application)
     */
    protected static final int VALUE_PARSED = 1;

    /**
     * Value state indicating that a parse error was encountered but should not
     * yet be displayed.
     */
    protected static final int VALUE_PARSE_ERROR = 2;

    /**
     * Value state indicating that validate() has been called when state was
     * VALUE_PARSE_ERROR. This makes the error visible on output.
     */
    protected static final int VALUE_DISPLAY_PARSE_ERROR = 3;

    /**
     * Transient value state indicating that validation is going on.
     *
     * @see #validate()
     */
    protected static final int VALUE_VALIDATING = 4;

    /**
     * Value state indicating that validation has occured, but that any error should not
     * yet be displayed.
     */
    protected static final int VALUE_VALIDATED = 5;

    /**
     * Value state indicating that value validation has occured, and the
     * validation error, if any, should be displayed.
     */
    protected static final int VALUE_DISPLAY_VALIDATION = 6;

    private static final String FIELD_EL = "field";
    private static final String VALUE_EL = "value";
    private static final String VALIDATION_MSG_EL = "validation-message";


    /**
     * Definition of the field.
     */
    private final FieldDefinition definition;

    /**
     * Overrides selection list defined in FieldDefinition, if any.
     */
    protected SelectionList selectionList;

    /**
     * Additional listeners to those defined as part of the widget definition (if any).
     */
    private ValueChangedListener listener;

    protected String enteredValue;
    protected Object value;

    protected boolean required;

    /**
     * Transient widget processing state indicating that the widget is currently validating
     * (used to avoid endless loops when a validator calls getValue).
     */
    protected int valueState = VALUE_PARSED;

    protected ValidationError validationError;


    public Field(FieldDefinition fieldDefinition) {
        super(fieldDefinition);

        this.definition = fieldDefinition;
        this.listener = fieldDefinition.getValueChangedListener();
        /*
         * At startup, we have no value to parse (both enteredValue and value are null),
         * but still need to validate (e.g. error if field is required), so initial value
         * is set to {@link #VALUE_PARSED}.
         */
        this.valueState = VALUE_PARSED;
    }

    public WidgetDefinition getDefinition() {
        return this.definition;
    }

    public final FieldDefinition getFieldDefinition() {
        return this.definition;
    }

    public void initialize() {
        Object value = this.definition.getInitialValue();
        if (value != null) {
            setValue(value);
        }
        this.selectionList = this.definition.getSelectionList();
        this.required = this.definition.isRequired();
        super.initialize();
    }

    /**
     * If this field has a selection-list, indicates if the value comes from that list
     * or if a new value was input by the user.
     *
     * @since 2.1.9
     * @return true if the user has chosen a suggested value
     */
    public boolean isSuggested() {
        return this.getAttribute(SUGGESTED_LABEL_ATTR) != null;
    }

    /**
     * Set the suggestion label associated to the widget's current value. This is used to initialize
     * a combobox's rendering. If not such label exists, the widget's value is used.
     *
     * @since 2.1.9
     */
    public void setSuggestionLabel(String label) {
        if (this.definition.getSuggestionList() == null) {
            throw new FormsRuntimeException("Field '" + getRequestParameterName() + "' has no suggestion list.",
                                            getLocation());
        }
        setAttribute(SUGGESTED_LABEL_ATTR, label);
    }

    /**
     * If the user has chosen an item in a suggestion list, returns that item's label.
     *
     * @since 2.1.9
     * @return the item's label, or <code>null</code> if the user entered a new value or
     *         if there's not suggestion list.
     */
    public String getSuggestionLabel() {
        return (String) getAttribute(SUGGESTED_LABEL_ATTR);
    }

    public Object getValue() {
        // if getValue() is called on this field while we're validating, then it's because a validation
        // rule called getValue(), so then we just return the parsed (but not VALUE_VALIDATED) value to avoid an endless loop
        if (this.valueState == VALUE_VALIDATING) {
            return this.value;
        }

        ValidationError oldError = this.validationError;

        // Parse the value
        if (this.valueState == VALUE_UNPARSED) {
            doParse();
        }

        // Validate the value if it was successfully parsed
        if (this.valueState == VALUE_PARSED) {
            doValidate();
        }

        if (oldError != null && this.validationError == null) {
            // The parsing process removed an existing validation error. This happens
            // mainly when a required field is given a value.
            getForm().addWidgetUpdate(this);
        }

        return this.validationError == null ? this.value : null;
    }

    public void setValue(Object newValue) {
        if (newValue != null && !getDatatype().getTypeClass().isAssignableFrom(newValue.getClass())) {
            throw new FormsRuntimeException("Incorrect value type for '" + getRequestParameterName() +
                                            "'. Expected " + getDatatype().getTypeClass() + ", got " + newValue.getClass() + ").",
                                            getLocation());
        }

        // Is it a new value?
        boolean changed;
        if (this.valueState == VALUE_UNPARSED) {
            // Current value was not parsed
            changed = true;
        } else if (this.value == null) {
            // Is current value not null?
            changed = (newValue != null);
        } else {
            // Is current value different?
            changed = !this.value.equals(newValue);
        }

        // Do something only if value is different or null
        // (null allows to reset validation error)
        if (changed || newValue == null) {
            // Do we need to call listeners? If yes, keep (and parse if needed) old value.
            boolean callListeners = changed && (hasValueChangedListeners() || this.getForm().hasFormHandler());
            Object oldValue = callListeners ? getValue() : null;

            this.value = newValue;
            this.validationError = null;
            // Force validation, even if set by the application
            this.valueState = VALUE_PARSED;
            if (newValue != null) {
                this.enteredValue = getDatatype().convertToString(newValue, getForm().getLocale());
            } else {
                this.enteredValue = null;
            }

            if (callListeners) {
                getForm().addWidgetEvent(new ValueChangedEvent(this, oldValue, newValue));
            }
            getForm().addWidgetUpdate(this);
        }
    }

    public void readFromRequest(FormContext formContext) {
        if (!getCombinedState().isAcceptingInputs()) {
            return;
        }

        String paramName = getRequestParameterName();
        Request request = formContext.getRequest();

        String newEnteredValue = request.getParameter(paramName);

        if (this.definition.getSuggestionList() != null) {
            // The Dojo ComboBox sends the typed value or the chosen item's label in the
            // request parameter and sends an additional "*_selected" parameter containing
            // the value of the chosen item (if any).
            // So if *_selected exists, use
            String selectedValue = request.getParameter(paramName + "_selected");
            if (StringUtils.isNotEmpty(selectedValue)) {
                setSuggestionLabel(newEnteredValue);
                newEnteredValue = selectedValue;
            } else {
                this.removeAttribute(SUGGESTED_LABEL_ATTR);
            }
        }

        // FIXME: Should we consider only non-null values?
        // Although distinguishing an empty value (input present but blank) from a null value
        // (input not present in the form) is possible, this distinction is not possible for
        // several other kinds of widgets such as BooleanField or MultiValueField. So we keep
        // it consistent with other widgets.
        //if (newEnteredValue != null) {
        readFromRequest(newEnteredValue);
        //}
    }

    protected void readFromRequest(String newEnteredValue) {
        // whitespace & empty field handling
        newEnteredValue = applyWhitespaceTrim(newEnteredValue);

        // Only convert if the text value actually changed. Otherwise, keep the old value
        // and/or the old validation error (allows to keep errors when clicking on actions)
        boolean changed;
        if (enteredValue == null) {
            changed = (newEnteredValue != null);
        } else {
            changed = !enteredValue.equals(newEnteredValue);
        }

        if (changed) {
            ValidationError oldError = this.validationError;

            // If we have some value-changed listeners, we must make sure the current value has been
            // parsed, to fill the event. Otherwise, we don't need to spend that extra CPU time.
            boolean hasListeners = hasValueChangedListeners() || this.getForm().hasFormHandler();
            Object oldValue = hasListeners ? getValue() : null;

            enteredValue = newEnteredValue;
            validationError = null;
            value = null;
            this.valueState = VALUE_UNPARSED;

            if (hasListeners) {
                // Throw an event that will hold the old value and
                // will lazily compute the new value only if needed.
                getForm().addWidgetEvent(new DeferredValueChangedEvent(this, oldValue));
            }

            if (oldError != null) {
                // There was a validation error, and the user entered a new value: refresh
                // the widget, because the previous error was cleared
                getForm().addWidgetUpdate(this);
            }
        }
    }

    protected String applyWhitespaceTrim(String value) {
        if (value != null) {
            Whitespace trim = this.definition.getWhitespaceTrim();
            if(trim == null || trim == Whitespace.TRIM) {
            	value = value.trim();
            } else if(trim == Whitespace.PRESERVE) {
                // do nothing.
            } else if(trim == Whitespace.TRIM_START) {
                value = StringUtils.stripStart(value, null);
            } else if(trim == Whitespace.TRIM_END) {
                value = StringUtils.stripEnd(value, null);
            }

            // treat empty strings as null
            if (value.length() == 0) {
                value = null;
            }
        }
        return value;
    }

    /**
     * @see org.apache.cocoon.forms.formmodel.Widget#validate()
     */
    public boolean validate() {
        if (!getCombinedState().isValidatingValues()) {
            this.wasValid = true;
            return true;
        }

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
            if (this.validationError != null) {
                getForm().addWidgetUpdate(this);
            }
        } else if (this.valueState == VALUE_PARSE_ERROR) {
            this.valueState = VALUE_DISPLAY_PARSE_ERROR;
            getForm().addWidgetUpdate(this);
        }

        this.wasValid = this.validationError == null;
        return this.wasValid;
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

        // reset validation errot
        this.validationError = null;

        try {
            if (this.value == null && this.required) {
                // Field is required
                this.validationError = new ValidationError(new I18nMessage("general.field-required", FormsConstants.I18N_CATALOGUE));
            } else if (!super.validate()) {
                // New-style validators failed.
            } else if (this.value != null) {
                // Check the old-style ones.
                this.validationError = getDatatype().validate(this.value, new ExpressionContextImpl(this));
            }
        } finally {
            // Consider validation finished even in case of exception
            this.valueState = VALUE_VALIDATED;
        }
    }

    /**
     * Returns the validation error, if any. There will always be a validation error in case the
     * {@link #validate} method returned false.
     *
     * <br>This method does not cause parsing to take effect, use {@link #getValue} if value
     * is not parsed yet.
     */
    public ValidationError getValidationError() {
        return this.validationError;
    }

    /**
     * Set a validation error on this field. This allows fields to be externally marked as invalid by
     * application logic.
     *
     * @param error the validation error
     */
    public void setValidationError(ValidationError error) {
        if (this.valueState >= VALUE_VALIDATED) {
            this.valueState = VALUE_DISPLAY_VALIDATION;
        }

        if (!ObjectUtils.equals(this.validationError, error)) {
            this.validationError = error;
            getForm().addWidgetUpdate(this);
        }
    }

    public boolean isRequired() {
        return this.required;
    }

    public void setRequired(boolean required) {
        this.required = required;
        getForm().addWidgetUpdate(this);
    }

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
        if (locale == null) {
            locale = getForm().getLocale();
        }

        if (enteredValue != null || value != null) {
            contentHandler.startElement(FormsConstants.INSTANCE_NS, VALUE_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALUE_EL, XMLUtils.EMPTY_ATTRIBUTES);
            String stringValue;
            if (value != null) {
                stringValue = getDatatype().convertToString(value, locale);
            } else {
                stringValue = enteredValue;
            }
            contentHandler.characters(stringValue.toCharArray(), 0, stringValue.length());
            contentHandler.endElement(FormsConstants.INSTANCE_NS, VALUE_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALUE_EL);
        }

        // Suggested label, if any
        String suggestedLabel = getSuggestionLabel();
        if (suggestedLabel != null) {
            contentHandler.startElement(FormsConstants.INSTANCE_NS, "suggestion", FormsConstants.INSTANCE_PREFIX_COLON + "suggestion", XMLUtils.EMPTY_ATTRIBUTES);
            contentHandler.characters(suggestedLabel.toCharArray(), 0, suggestedLabel.length());
            contentHandler.endElement(FormsConstants.INSTANCE_NS, "suggestion", FormsConstants.INSTANCE_PREFIX_COLON + "suggestion");
        }

        // validation message element: only present if the value is not valid
        if (validationError != null && (this.valueState == VALUE_DISPLAY_VALIDATION || this.valueState == VALUE_DISPLAY_PARSE_ERROR)) {
            contentHandler.startElement(FormsConstants.INSTANCE_NS, VALIDATION_MSG_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALIDATION_MSG_EL, XMLUtils.EMPTY_ATTRIBUTES);
            validationError.generateSaxFragment(contentHandler);
            contentHandler.endElement(FormsConstants.INSTANCE_NS, VALIDATION_MSG_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALIDATION_MSG_EL);
        }

        // generate selection list, if any
        if (selectionList != null) {
            selectionList.generateSaxFragment(contentHandler, locale);
        }

        // include some info about the datatype
        definition.getDatatype().generateSaxFragment(contentHandler, locale);
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

    public SelectionList getSuggestionList() {
        return getFieldDefinition().getSuggestionList();
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
