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
package org.apache.cocoon.faces.taglib;

import org.apache.cocoon.taglib.TagSupport;

import org.apache.cocoon.faces.FacesUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.faces.component.EditableValueHolder;
import javax.faces.validator.Validator;

/**
 * @version CVS $Id$
 */
public class ValidatorTag extends TagSupport {

    private String validatorId;

    public String getValidatorId() {
        return this.validatorId;
    }

    public void setValidatorId(String validatorId) {
        this.validatorId = validatorId;
    }

    public int doStartTag(String namespaceURI, String localName, String qName, Attributes atts)
    throws SAXException {
        UIComponentTag tag = FacesUtils.findParentUIComponentTag(this);
        if (tag == null) {
            throw new SAXException("Tag <" + getClass().getName() + "> have to be nested within a UIComponentTag");
        }

        if (!tag.getCreated()) {
            return 0;
        }

        Validator validator = getValidator();
        ((EditableValueHolder)tag.getComponentInstance()).addValidator(validator);

        return SKIP_BODY;
    }

    protected Validator getValidator() {
        final UIComponentTag tag = FacesUtils.findParentUIComponentTag(this);
        String validatorIdVal = (String) tag.evaluate(validatorId);
        return tag.getApplication().createValidator(validatorIdVal);
    }

    public void recycle() {
        super.recycle();
        this.validatorId = null;
    }
}
