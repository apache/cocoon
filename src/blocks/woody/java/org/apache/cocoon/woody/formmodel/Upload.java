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

import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.cocoon.servlet.multipart.Part;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.validation.ValidationError;
import org.apache.cocoon.woody.util.I18nMessage;
import org.apache.cocoon.woody.validation.ValidationErrorAware;
import org.apache.cocoon.xml.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A file-uploading Widget. This widget gives access via Woody, to Cocoon's 
 * file upload functionality.
 * 
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: Upload.java,v 1.5 2004/02/19 22:13:27 joerg Exp $
 */
public class Upload extends AbstractWidget implements ValidationErrorAware {
    private UploadDefinition uploadDefinition;
    private Part part;
    private ValidationError validationError;

    public Upload(UploadDefinition uploadDefinition) {
        this.uploadDefinition = uploadDefinition;
        this.setDefinition(uploadDefinition);
        setLocation(uploadDefinition.getLocation());
    }

    public UploadDefinition getUploadDefinition() {
        return this.uploadDefinition;
    }

    public String getId() {
        return definition.getId();
    }

    public Object getValue() {
        return this.part;
    }

    public void setValue(Object object) {
        throw new RuntimeException("Cannot manually set the value of an upload widget for field \"" + getFullyQualifiedId() + "\"");
    }

    public void readFromRequest(FormContext formContext) {
        Object obj = formContext.getRequest().get(getFullyQualifiedId());
        
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
            
        // If it's not a part and not null, clear any existing value
        // We also check if we're the submit widget, as a result of clicking the "..." button
        } else if (obj != null || getForm().getSubmitWidget() == this){
            // Clear the part, if any
            if (this.part != null) {
                this.part.dispose();
                this.part = null;
            }
            this.validationError = null;
        }
        
        // And keep the current state if the parameter doesn't exist or is null
    }

    public boolean validate(FormContext formContext) {
        if (this.part == null) {
            if (this.uploadDefinition.isRequired()) {
                this.validationError = new ValidationError(new I18nMessage("general.field-required", Constants.I18N_CATALOGUE));
            }
        } else {
            String mimeTypes = this.uploadDefinition.getMimeTypes();
            if (mimeTypes != null) {
                StringTokenizer tok = new StringTokenizer(this.uploadDefinition.getMimeTypes(), ", ");
                this.validationError = new ValidationError(new I18nMessage("upload.invalid-type", Constants.I18N_CATALOGUE));
                String contentType = this.part.getMimeType();
                while (tok.hasMoreTokens()) {
                    if (tok.nextToken().equals(contentType)) {
                        this.validationError = null;
                    }
                }
            } else {
                this.validationError = null;
            }
        }
        
        return validationError == null ? super.validate(formContext) : false;
    }

    /**
     * Returns the validation error, if any. There will always be a validation error in case the
     * {@link #validate(FormContext)} method returned false.
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

    private static final String FIELD_EL = "upload";
    private static final String VALUE_EL = "value";
    private static final String VALIDATION_MSG_EL = "validation-message";

    public void generateSaxFragment(ContentHandler contentHandler, Locale locale) throws SAXException {
        AttributesImpl fieldAttrs = new AttributesImpl();
        fieldAttrs.addCDATAAttribute("id", getFullyQualifiedId());
        fieldAttrs.addCDATAAttribute("required", String.valueOf(uploadDefinition.isRequired()));
        if (uploadDefinition.getMimeTypes() != null) {
            fieldAttrs.addCDATAAttribute("mime-types", uploadDefinition.getMimeTypes());
        }
        contentHandler.startElement(Constants.WI_NS, FIELD_EL, Constants.WI_PREFIX_COLON + FIELD_EL, fieldAttrs);

        if (this.part != null) {
            String name = (String)this.part.getHeaders().get("filename");
            contentHandler.startElement(Constants.WI_NS, VALUE_EL, Constants.WI_PREFIX_COLON + VALUE_EL, Constants.EMPTY_ATTRS);
            contentHandler.characters(name.toCharArray(), 0, name.length());
            contentHandler.endElement(Constants.WI_NS, VALUE_EL, Constants.WI_PREFIX_COLON + VALUE_EL);
        }

        // validation message element: only present if the value is not valid
        if (validationError != null) {
            contentHandler.startElement(Constants.WI_NS, VALIDATION_MSG_EL, Constants.WI_PREFIX_COLON + VALIDATION_MSG_EL, Constants.EMPTY_ATTRS);
            validationError.generateSaxFragment(contentHandler);
            contentHandler.endElement(Constants.WI_NS, VALIDATION_MSG_EL, Constants.WI_PREFIX_COLON + VALIDATION_MSG_EL);
        }

        // the display data
        this.definition.generateDisplayData(contentHandler);

        contentHandler.endElement(Constants.WI_NS, FIELD_EL, Constants.WI_PREFIX_COLON + FIELD_EL);
    }

    public void generateLabel(ContentHandler contentHandler) throws SAXException {
        definition.generateLabel(contentHandler);
    }
}
