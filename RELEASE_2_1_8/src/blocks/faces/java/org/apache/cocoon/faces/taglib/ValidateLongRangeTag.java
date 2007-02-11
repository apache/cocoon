/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import org.apache.cocoon.faces.FacesUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.faces.validator.LongRangeValidator;
import javax.faces.validator.Validator;

/**
 * @version $Id$
 */
public class ValidateLongRangeTag extends ValidatorTag {

    private String minimum;
    private String maximum;

    public void setMaximum(String maximum) {
        this.maximum = maximum;
    }

    public void setMinimum(String minimum) {
        this.minimum = minimum;
    }

    public int doStartTag(String namespaceURI, String localName, String qName, Attributes atts)
    throws SAXException {
        super.setValidatorId("javax.faces.LongRange");
        return super.doStartTag(namespaceURI, localName, qName, atts);
    }

    protected Validator createValidator() {
        final UIComponentTag tag = FacesUtils.findParentUIComponentTag(this);
        LongRangeValidator validator = (LongRangeValidator) super.createValidator();

        if (maximum != null) {
            validator.setMaximum(tag.evaluateLong(maximum));
        }
        if (minimum != null) {
            validator.setMinimum(tag.evaluateLong(minimum));
        }

        return validator;
    }

    public void recycle() {
        super.recycle();
        this.minimum = null;
        this.maximum = null;
    }
}
