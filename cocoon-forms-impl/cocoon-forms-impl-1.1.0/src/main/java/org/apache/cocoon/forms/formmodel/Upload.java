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
import java.util.StringTokenizer;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.event.ValueChangedEvent;
import org.apache.cocoon.forms.event.ValueChangedListener;
import org.apache.cocoon.forms.event.ValueChangedListenerEnabled;
import org.apache.cocoon.forms.event.WidgetEvent;
import org.apache.cocoon.forms.event.WidgetEventMulticaster;
import org.apache.cocoon.forms.util.I18nMessage;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.forms.validation.ValidationErrorAware;

import org.apache.cocoon.servlet.multipart.Part;
import org.apache.cocoon.servlet.multipart.RejectedPart;

import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.environment.Request;

import org.apache.commons.lang.ObjectUtils;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A file-uploading Widget. This widget gives access via Cocoon Forms, to Cocoon's
 * file upload functionality.
 * <p>
 * This widget accepts value-changed listeners, but the event raised does not hold
 * the previous value, as uploads are heavyweight resources that must be released
 * as soon as possible.
 *
 * @version $Id$
 */
public class Upload extends AbstractWidget
                    implements ValidationErrorAware, ValueChangedListenerEnabled {

    private static final String UPLOAD_EL = "upload";
    private static final String VALUE_EL = "value";
    private static final String VALIDATION_MSG_EL = "validation-message";

    private final UploadDefinition definition;
    private Part part;
    private ValidationError validationError;
    private ValueChangedListener listener;


    public Upload(UploadDefinition uploadDefinition) {
        super(uploadDefinition);
        this.definition = uploadDefinition;
        this.listener = uploadDefinition.getValueChangedListener();
    }

    public WidgetDefinition getDefinition() {
        return this.definition;
    }

    public UploadDefinition getUploadDefinition() {
        return this.definition;
    }

    public Object getValue() {
        return this.isValid() ? this.part : null;
    }

    public void setValue(Object object) {
        if (object == this.part) {
            return;
        }

        if ((object == null) || (object instanceof Part)) {
            this.part = (Part) object;
        } else {
            throw new RuntimeException("The value of an upload widget must be of type " + Part.class + ".");
        }

        changed();
    }

    public void readFromRequest(FormContext formContext) {
        if (!getCombinedState().isAcceptingInputs()) {
            return;
        }

        Form form = getForm();
        Request request = formContext.getRequest();
        String fullId = getRequestParameterName();

        Object obj = request.get(fullId);

        if (fullId.equals(request.getParameter(Form.SUBMIT_ID_PARAMETER))) {
           form.setSubmitWidget(this);
        }


        // If the request object is a Part, keep it
        if (obj instanceof Part) {
            Part requestPart = (Part)obj;
            if (this.part != null) {
                // Replace the current part
                this.part.dispose();
            }

            // Keep the request part
            requestPart.setDisposeWithRequest(false);
            this.part = requestPart;
            if (validateOversize()) {
                // Clear any validation error
                setValidationError(null);
            }
            changed();

        // If it's not a part and not null, clear any existing value
        // We also check if we're the submit widget, as a result of clicking the "..." button
        } else if (obj != null || form.getSubmitWidget() == this){
            // Clear the part, if any
            if (this.part != null) {
                this.part.dispose();
                this.part = null;
            }
            setValidationError(null);
            // Ensure we redisplay it
            changed();
        }

        // And keep the current state if the parameter doesn't exist or is null
    }

    private void changed() {
        if (this.hasValueChangedListeners() || this.getForm().hasFormHandler()) {
            this.getForm().addWidgetEvent(new ValueChangedEvent(this, null, this.part));
        }
        getForm().addWidgetUpdate(this);
    }

    private boolean validateMimeType() {
        String mimeTypes = this.definition.getMimeTypes();
        if (mimeTypes != null) {
            StringTokenizer tok = new StringTokenizer(mimeTypes, ", ");
            String contentType = this.part.getMimeType();
            while(tok.hasMoreTokens()) {
                if (tok.nextToken().equals(contentType)) {
                    return true;
                }
            }
            I18nMessage message = new I18nMessage("upload.invalid-type",
                                                  new String[] {contentType},
                                                  FormsConstants.I18N_CATALOGUE);
            setValidationError(new ValidationError(message));
            return false;
        }

        // No mime type restriction
        return true;
    }

    /**
     * Check if the part is oversized, and if yes sets the validation error accordingly
     */
    private boolean validateOversize() {
        if (!this.part.isRejected()) {
            return true;
        }

        // Set a validation error indicating the sizes in kbytes (rounded)
        RejectedPart rjp = (RejectedPart)this.part;
        int size = (rjp.getContentLength() + 512) / 1024;
        int maxSize = (rjp.getMaxContentLength() + 512) / 1024;
        String[] i18nParams = new String[] { String.valueOf(size), String.valueOf(maxSize) };
        I18nMessage i18nMessage = new I18nMessage("upload.rejected", i18nParams, FormsConstants.I18N_CATALOGUE);
        setValidationError(new ValidationError(i18nMessage));
        return false;
    }

    public boolean validate() {
        if (!getCombinedState().isValidatingValues()) {
            this.wasValid = true;
            return true;
        }

        if (this.part == null) {
            if (this.definition.isRequired()) {
                I18nMessage i18nMessage = new I18nMessage("general.field-required", FormsConstants.I18N_CATALOGUE);
                setValidationError(new ValidationError(i18nMessage));
            }
        } else if (validateOversize() && validateMimeType()) {
            super.validate();
        }

        this.wasValid = this.validationError == null;
        return this.wasValid;
    }

    /**
     * Returns the validation error, if any. There will always be a validation error in case the
     * {@link #validate()} method returned false.
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
        if(!ObjectUtils.equals(this.validationError, error)) {
            this.validationError = error;
            getForm().addWidgetUpdate(this);
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

    /**
     * @return "upload"
     */
    public String getXMLElementName() {
        return UPLOAD_EL;
    }

    /**
     * Adds attributes @required, @mime-types
     */
    public AttributesImpl getXMLElementAttributes() {
        AttributesImpl attrs = super.getXMLElementAttributes();
        attrs.addCDATAAttribute("id", getRequestParameterName());
        attrs.addCDATAAttribute("required", String.valueOf(this.definition.isRequired()));
        if (this.definition.getMimeTypes() != null) {
            attrs.addCDATAAttribute("mime-types", this.definition.getMimeTypes());
        }
        return attrs;
    }

    public void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        if (this.part != null) {
            String name = (String)this.part.getHeaders().get("filename");
            contentHandler.startElement(FormsConstants.INSTANCE_NS, VALUE_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALUE_EL, XMLUtils.EMPTY_ATTRIBUTES);
            contentHandler.characters(name.toCharArray(), 0, name.length());
            contentHandler.endElement(FormsConstants.INSTANCE_NS, VALUE_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALUE_EL);
        }

        // validation message element: only present if the value is not valid
        if (this.validationError != null) {
            contentHandler.startElement(FormsConstants.INSTANCE_NS, VALIDATION_MSG_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALIDATION_MSG_EL, XMLUtils.EMPTY_ATTRIBUTES);
            this.validationError.generateSaxFragment(contentHandler);
            contentHandler.endElement(FormsConstants.INSTANCE_NS, VALIDATION_MSG_EL, FormsConstants.INSTANCE_PREFIX_COLON + VALIDATION_MSG_EL);
        }
    }
}
