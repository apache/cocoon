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

import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.util.I18nMessage;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.forms.validation.ValidationErrorAware;
import org.apache.cocoon.servlet.multipart.Part;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.ObjectUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A file-uploading Widget. This widget gives access via Cocoon Forms, to Cocoon's 
 * file upload functionality.
 * 
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version $Id$
 */
public class Upload extends AbstractWidget implements ValidationErrorAware {

    private static final String UPLOAD_EL = "upload";
    private static final String VALUE_EL = "value";
    private static final String VALIDATION_MSG_EL = "validation-message";

    private final UploadDefinition uploadDefinition;
    private Part part;
    private ValidationError validationError;

    public Upload(UploadDefinition uploadDefinition) {
        super(uploadDefinition);
        this.uploadDefinition = uploadDefinition;
    }

    public UploadDefinition getUploadDefinition() {
        return this.uploadDefinition;
    }

    public WidgetDefinition getDefinition() {
        return uploadDefinition;
    }

    public Object getValue() {
        return this.part;
    }

    public void setValue(Object object) {
        if ((object == null) || (object instanceof Part)) {
            this.part = (Part)object;
        } else {
            throw new RuntimeException("The value of an upload widget must be of type " + Part.class + ".");
        }
        getForm().addWidgetUpdate(this);
    }

    public void readFromRequest(FormContext formContext) {
        if (!getCombinedState().isAcceptingInputs())
            return;

        Object obj = formContext.getRequest().get(getRequestParameterName());

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
            this.validationError = null;
            getForm().addWidgetUpdate(this);

        // If it's not a part and not null, clear any existing value
        // We also check if we're the submit widget, as a result of clicking the "..." button
        } else if (obj != null || getForm().getSubmitWidget() == this){
            // Clear the part, if any
            if (this.part != null) {
                this.part.dispose();
                this.part = null;
            }
            this.validationError = null;
            getForm().addWidgetUpdate(this);
        }

        // And keep the current state if the parameter doesn't exist or is null
    }

    public boolean validate() {
        if (!getCombinedState().isValidatingValues()) {
            this.wasValid = true;
            return true;
        }
        ValidationError newError = null;

        if (this.part == null) {
            if (this.uploadDefinition.isRequired()) {
                newError = new ValidationError(new I18nMessage("general.field-required", Constants.I18N_CATALOGUE));
                getForm().addWidgetUpdate(this);
            }
        } else {
            String mimeTypes = this.uploadDefinition.getMimeTypes();
            if (mimeTypes != null) {
                StringTokenizer tok = new StringTokenizer(this.uploadDefinition.getMimeTypes(), ", ");
                String contentType = this.part.getMimeType();
                boolean found = false;
                while(tok.hasMoreTokens()) {
                    if (tok.nextToken().equals(contentType)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    newError = new ValidationError(new I18nMessage("upload.invalid-type", Constants.I18N_CATALOGUE));
                }
            } else {
                newError = null;
            }
        }
        
        if (!ObjectUtils.equals(this.validationError, newError)) {
            setValidationError(newError);
        }
        this.wasValid = validationError == null ? super.validate() : false;
        return this.wasValid;
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
        attrs.addCDATAAttribute("required", String.valueOf(uploadDefinition.isRequired()));
        if (uploadDefinition.getMimeTypes() != null) {
            attrs.addCDATAAttribute("mime-types", uploadDefinition.getMimeTypes());
        }
        return attrs;
    }

    public void generateItemSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        if (this.part != null) {
            String name = (String)this.part.getHeaders().get("filename");
            contentHandler.startElement(Constants.INSTANCE_NS, VALUE_EL, Constants.INSTANCE_PREFIX_COLON + VALUE_EL, XMLUtils.EMPTY_ATTRIBUTES);
            contentHandler.characters(name.toCharArray(), 0, name.length());
            contentHandler.endElement(Constants.INSTANCE_NS, VALUE_EL, Constants.INSTANCE_PREFIX_COLON + VALUE_EL);
        }

        // validation message element: only present if the value is not valid
        if (validationError != null) {
            contentHandler.startElement(Constants.INSTANCE_NS, VALIDATION_MSG_EL, Constants.INSTANCE_PREFIX_COLON + VALIDATION_MSG_EL, XMLUtils.EMPTY_ATTRIBUTES);
            validationError.generateSaxFragment(contentHandler);
            contentHandler.endElement(Constants.INSTANCE_NS, VALIDATION_MSG_EL, Constants.INSTANCE_PREFIX_COLON + VALIDATION_MSG_EL);
        }
    }

}
