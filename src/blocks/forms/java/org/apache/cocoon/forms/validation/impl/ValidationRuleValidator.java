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
package org.apache.cocoon.forms.validation.impl;

import org.apache.cocoon.forms.FormContext;
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
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: ValidationRuleValidator.java,v 1.2 2004/03/09 11:31:11 joerg Exp $
 */
public class ValidationRuleValidator implements WidgetValidator {
    
    private ValidationRule rule;
    
    public ValidationRuleValidator(ValidationRule rule) {
        this.rule = rule;
    }

    public boolean validate(Widget widget, FormContext context)
    {
        if (! (widget instanceof ValidationErrorAware)) {
            // Invalid widget type
            throw new IllegalArgumentException("Widget '" + widget.getFullyQualifiedId() + "' is not ValidationErrorAware");
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
