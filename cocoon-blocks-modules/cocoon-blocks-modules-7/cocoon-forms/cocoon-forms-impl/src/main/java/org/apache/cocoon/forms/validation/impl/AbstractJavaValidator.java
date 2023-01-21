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
package org.apache.cocoon.forms.validation.impl;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.forms.validation.ConfigurableWidgetValidator;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.excalibur.xml.sax.XMLizable;
import org.w3c.dom.Element;

/**
 * Abstract base class for implementing an own Java validator which gets build by 
 * the {@link JavaClassValidatorBuilder}.
 *
 * @version $Id$
 */
public abstract class AbstractJavaValidator implements ConfigurableWidgetValidator {

    protected ValidationError validationError;

    public void setConfiguration(Element element) throws Exception {
        this.validationError = this.buildFailMessage(element);
    }

    /**
     * Checks if the validation rule configuration contains a custom failmessage, and if so,
     * sets it one the ValidationRule.
     */
    protected ValidationError buildFailMessage(Element validationRuleElement) {
        Element failMessageElement = DomHelper.getChildElement(validationRuleElement,
                                                               FormsConstants.DEFINITION_NS, "failmessage");
        if (failMessageElement != null) {
            XMLizable failMessage = DomHelper.compileElementContent(failMessageElement);
            return new ValidationError(failMessage);
        }
        return this.getDefaultFailMessage();
    }

    protected abstract ValidationError getDefaultFailMessage();
}
