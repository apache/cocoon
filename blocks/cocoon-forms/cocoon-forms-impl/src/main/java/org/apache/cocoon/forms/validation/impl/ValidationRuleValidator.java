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

import org.apache.cocoon.forms.datatype.ValidationRule;
import org.apache.cocoon.forms.formmodel.ExpressionContextImpl;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.forms.validation.ValidationErrorAware;
import org.apache.cocoon.forms.validation.WidgetValidator;

/**
 * An adapter to transform a {@link org.apache.cocoon.forms.datatype.ValidationRule} into a
 * {@link org.apache.cocoon.forms.validation.WidgetValidator}.
 * 
 * @version $Id$
 */
public class ValidationRuleValidator implements WidgetValidator {
    
    private ValidationRule rule;
    
    public ValidationRuleValidator(ValidationRule rule) {
        this.rule = rule;
    }

    public boolean validate(Widget widget)
    {
        if (! (widget instanceof ValidationErrorAware)) {
            // Invalid widget type
            throw new IllegalArgumentException("Widget '" + widget.getRequestParameterName() + "' is not ValidationErrorAware");
        }

        Object value = widget.getValue();
        if (value == null) {
            // No value. Consider it as correct (required="true" will set an error if present)
            return true;
            
        } else {
            // Non-null value: perform validation
            ValidationError error = this.rule.validate(value, new ExpressionContextImpl(widget));
            if (error != null) {
                // Validation failed
                ((ValidationErrorAware)widget).setValidationError(error);
                return false;
            } else {
                // Validation succeeded
                return true;
            }
        }
    }
}
