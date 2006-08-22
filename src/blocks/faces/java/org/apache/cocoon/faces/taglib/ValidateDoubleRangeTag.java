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
package org.apache.cocoon.faces.taglib;

import org.apache.cocoon.faces.FacesUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.faces.validator.DoubleRangeValidator;
import javax.faces.validator.Validator;

/**
 * @version $Id$
 */
public class ValidateDoubleRangeTag extends ValidatorTag {

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
        super.setValidatorId("javax.faces.DoubleRange");
        return super.doStartTag(namespaceURI, localName, qName, atts);
    }

    protected Validator createValidator() {
        final UIComponentTag tag = FacesUtils.findParentUIComponentTag(this);
        DoubleRangeValidator validator = (DoubleRangeValidator) super.createValidator();

        if (maximum != null) {
            validator.setMaximum(tag.evaluateDouble(maximum));
        }
        if (minimum != null) {
            validator.setMinimum(tag.evaluateDouble(minimum));
        }

        return validator;
    }

    public void recycle() {
        super.recycle();
        this.minimum = null;
        this.maximum = null;
    }
}
