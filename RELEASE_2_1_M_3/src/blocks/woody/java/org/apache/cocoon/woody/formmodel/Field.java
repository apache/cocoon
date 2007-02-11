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
 */
public class Field extends AbstractWidget {
    private FieldDefinition definition;
    private String enteredValue;
    private Object value;
    private boolean conversionFailed;
    private ValidationError validationError;

    public Field(FieldDefinition fieldDefinition) {
        this.definition = fieldDefinition;
    }

    public String getId() {
        return definition.getId();
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object object) {
        if (object != null && !definition.getDatatype().getTypeClass().isAssignableFrom(object.getClass()))
            throw new RuntimeException("Tried to set value of field \"" + getFullyQualifiedId() + "\" with an object of an incorrect type.");

        value = object;
        validationError = null;
        conversionFailed = false;
    }

    public void readFromRequest(FormContext formContext) {
        enteredValue = formContext.getRequest().getParameter(getFullyQualifiedId());
        validationError = null;
        conversionFailed = false;

        // whitespace & empty field handling
        if (enteredValue != null) {
            // TODO make whitespace behaviour configurable !!
            enteredValue.trim();

            if (enteredValue.equals(""))
                enteredValue = null;
        }

        // try to convert entered string to the field's native datatype
        if (enteredValue != null) {
            value = definition.getDatatype().convertFromStringLocalized(enteredValue, formContext.getLocale());
            if (value == null)
                conversionFailed = true;
        } else
            value = null;
    }

    public boolean validate(FormContext formContext) {
        if (value != null)
            validationError = definition.getDatatype().validate(value, new ExpressionContextImpl(this));
        else if (conversionFailed)
            validationError = new ValidationError("datatype.conversion-failed", new String[] {"datatype." + definition.getDatatype().getDescriptiveName()}, new boolean[] { true });
        else if (definition.isRequired())
            validationError = new ValidationError("general.field-required");

        return validationError == null;
    }

    /**
     * Returns the validation error, if any. There will always be a validation error in case the
     * {@link #validate} method returned false.
     */
    public ValidationError getValidationError() {
        return validationError;
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
                stringValue = definition.getDatatype().convertToStringLocalized(value, locale);
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
        if (definition.getDatatype().getSelectionList() != null) {
            definition.getDatatype().getSelectionList().generateSaxFragment(contentHandler, locale);
        }

        contentHandler.endElement(Constants.WI_NS, FIELD_EL, Constants.WI_PREFIX_COLON + FIELD_EL);
    }

    public void generateLabel(ContentHandler contentHandler) throws SAXException {
        definition.generateLabel(contentHandler);
    }
}
