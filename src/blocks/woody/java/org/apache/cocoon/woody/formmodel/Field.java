/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.woody.formmodel;

import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.datatype.ValidationError;
import org.apache.cocoon.woody.datatype.SelectionList;
import org.apache.cocoon.woody.datatype.Datatype;
import org.apache.cocoon.woody.event.DeferredValueChangedEvent;
import org.apache.cocoon.woody.event.WidgetEvent;
import org.apache.cocoon.woody.event.ValueChangedEvent;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.util.Locale;

/**
 * A general-purpose Widget that can hold one value. A Field widget can be associated
 * with a {@link org.apache.cocoon.woody.datatype.Datatype Datatype}, and thus
 * a Field widget can be used to edit different kinds of data, such as strings,
 * numbers and dates. A Datatype can also have an associated SelectionList, so
 * that the value for the Field can be selected from a list, rather than being
 * entered in a textbox. The validation of the field is delegated to its associated
 * Datatype.
 * 
 * @author Bruno Dumon
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: Field.java,v 1.11 2003/09/25 17:37:30 sylvain Exp $
 */
public class Field extends AbstractWidget {
    private SelectionList selectionList;
    private FieldDefinition definition;
    
    private String enteredValue = null;
    private Object value = null;

    // At startup, we don't need to parse (both enteredValue and value are null),
    // but need to validate (error if field is required)
    private boolean needsParse = true;
    private boolean needsValidate = true;
    
    private ValidationError validationError;

    public Field(FieldDefinition fieldDefinition) {
        this.definition = fieldDefinition;
    }

    public FieldDefinition getFieldDefinition() {
        return this.definition;
    }

    public String getId() {
        return definition.getId();
    }

    public Object getValue() {
        // Parse the value
        if (this.needsParse) {
            
            // Clear value, it will be recomputed
            this.value = null;
            if (this.enteredValue == null) {
                this.value = null;
                this.needsParse = false;
                this.needsValidate = true;
                
            } else {
                // Parse the value
                this.value = definition.getDatatype().convertFromString(this.enteredValue, getForm().getLocale());
            
                if (this.value == null) {
                    // Conversion failed
                    this.validationError = new ValidationError(
                        "datatype.conversion-failed",
                        new String[] {"datatype." + definition.getDatatype().getDescriptiveName()},
                        new boolean[] { true }
                    );
                    
                    // No need for further validation (and need to keep the above error)
                    this.needsValidate = false;
                } else {
                    // Conversion successfull
                    this.needsParse = false;
                    this.needsValidate = true;
                }
            }
        }
        
        // Validate the value
        if (this.needsValidate) {
            
            // Clear error, it will be recomputed
            this.validationError = null;
            
            if (this.value == null) {
                // No value : is it required ?
                if (this.definition.isRequired()) {                    
                    this.validationError = new ValidationError("general.field-required");
                }
                
            } else {
                this.validationError = definition.getDatatype().validate(value, new ExpressionContextImpl(this));
            }
            
            this.needsValidate = false;
        }
        
        return this.validationError == null ? this.value : null;
    }

    public void setValue(Object newValue) {
        if (newValue != null && !definition.getDatatype().getTypeClass().isAssignableFrom(newValue.getClass()))
            throw new RuntimeException("Incorrect value type for \"" + getFullyQualifiedId() +
               "\" (expected " + definition.getDatatype().getTypeClass() + ", got " + newValue.getClass() + ".");

        Object oldValue = this.value;
        
        boolean changed = ! (oldValue == null ? "" : oldValue).equals(newValue == null ? "" : newValue);
        
        // Do something only if value is different or null
        // (null allows to reset validation error)
        if (changed || newValue == null) {
        
            this.value = newValue;
    
            this.needsParse = false;
            this.validationError = null;
            // Force validation, even if set by the application
            this.needsValidate = true;

            this.enteredValue = definition.getDatatype().convertToString(newValue, getForm().getLocale());
    
            if (changed) {
                getForm().addWidgetEvent(new ValueChangedEvent(this, oldValue, newValue));
            }
        }
    }

    public void readFromRequest(FormContext formContext) {
        String newEnteredValue = formContext.getRequest().getParameter(getFullyQualifiedId());
        
        // whitespace & empty field handling
        if (newEnteredValue != null) {
            // TODO make whitespace behaviour configurable !!
            newEnteredValue.trim();

            if (newEnteredValue.length() == 0) {
                newEnteredValue = null;
            }
        }
        
        // Only convert if the text value actually changed. Otherwise, keep the old value
        // and/or the old validation error (allows to keep errors when clicking on actions)
        if (!(newEnteredValue == null ? "" : newEnteredValue).equals((enteredValue == null ? "" : enteredValue))) {
            
            getForm().addWidgetEvent(new DeferredValueChangedEvent(this, value));

            enteredValue = newEnteredValue;
            validationError = null;
            value = null;
            needsParse = true;

        }
        
        // Always revalidate, as validation may depend on the value of other fields
        this.needsValidate = true;
    }

    public boolean validate(FormContext formContext) {
        // If needed, getValue() will do the validation
        getValue();
        return this.validationError == null;
    }

    /**
     * Returns the validation error, if any. There will always be a validation error in case the
     * {@link #validate} method returned false.
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
    }

    public boolean isRequired() {
        return definition.isRequired();
    }

    private static final String FIELD_EL = "field";
    private static final String VALUE_EL = "value";
    private static final String VALIDATION_MSG_EL = "validation-message";
    private static final String LABEL_EL = "label";

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        AttributesImpl fieldAttrs = new AttributesImpl();
        fieldAttrs.addCDATAAttribute("id", getFullyQualifiedId());
        fieldAttrs.addCDATAAttribute("required", String.valueOf(definition.isRequired()));
        contentHandler.startElement(Constants.WI_NS, FIELD_EL, Constants.WI_PREFIX_COLON + FIELD_EL, fieldAttrs);

        if (enteredValue != null || value != null) {
            contentHandler.startElement(Constants.WI_NS, VALUE_EL, Constants.WI_PREFIX_COLON + VALUE_EL, Constants.EMPTY_ATTRS);
            String stringValue;
            if (value != null)
                stringValue = definition.getDatatype().convertToString(value, locale);
            else
                stringValue = enteredValue;
            contentHandler.characters(stringValue.toCharArray(), 0, stringValue.length());
            contentHandler.endElement(Constants.WI_NS, VALUE_EL, Constants.WI_PREFIX_COLON + VALUE_EL);
        }

        // validation message element: only present if the value is not valid
        if (validationError != null) {
            contentHandler.startElement(Constants.WI_NS, VALIDATION_MSG_EL, Constants.WI_PREFIX_COLON + VALIDATION_MSG_EL, Constants.EMPTY_ATTRS);
            validationError.generateSaxFragment(contentHandler);
            contentHandler.endElement(Constants.WI_NS, VALIDATION_MSG_EL, Constants.WI_PREFIX_COLON + VALIDATION_MSG_EL);
        }

        // the label
        contentHandler.startElement(Constants.WI_NS, LABEL_EL, Constants.WI_PREFIX_COLON + LABEL_EL, Constants.EMPTY_ATTRS);
        definition.generateLabel(contentHandler);
        contentHandler.endElement(Constants.WI_NS, LABEL_EL, Constants.WI_PREFIX_COLON + LABEL_EL);

        // the selection list, if any
        if (selectionList != null) {
            selectionList.generateSaxFragment(contentHandler, locale);
        } else if (definition.getSelectionList() != null) {
            definition.getSelectionList().generateSaxFragment(contentHandler, locale);
        }

        contentHandler.endElement(Constants.WI_NS, FIELD_EL, Constants.WI_PREFIX_COLON + FIELD_EL);
    }

    public void generateLabel(ContentHandler contentHandler) throws SAXException {
        definition.generateLabel(contentHandler);
    }

    public void setSelectionList(SelectionList selectionList) {
        if (selectionList != null &&
            selectionList.getDatatype() != null &&
            selectionList.getDatatype() != definition.getDatatype()) {

            throw new RuntimeException("Tried to assign a SelectionList that is not associated with this widget's datatype.");
        }
        this.selectionList = selectionList;
    }
    
    public void setSelectionList(String uri) {
        setSelectionList(this.definition.buildSelectionList(uri));
    }

    public Datatype getDatatype() {
        return definition.getDatatype();
    }
    
    public void broadcastEvent(WidgetEvent event) {
        this.definition.fireValueChangedEvent((ValueChangedEvent)event);
    }
}
