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
package org.apache.cocoon.forms.datatype.validationruleimpl;

import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.util.I18nMessage;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.commons.validator.EmailValidator;
import org.outerj.expression.ExpressionContext;

/**
 * ValidationRule that checks that a string is an email address.
 * 
 * @version $Id$
 */
public class EmailValidationRule extends AbstractValidationRule {

    public ValidationError validate(Object value, ExpressionContext expressionContext) {
        String email = (String)value;

        if (isEmail(email))
            return null;
        else
            return hasFailMessage() ? getFailMessage() : new ValidationError(new I18nMessage("validation.string.invalidemail", Constants.I18N_CATALOGUE));
    }

    public boolean supportsType(Class clazz, boolean arrayType) {
        return clazz.isAssignableFrom(String.class) && !arrayType;
    }

    boolean isEmail(String email) {
        EmailValidator ev = EmailValidator.getInstance();
        return ev.isValid(email);
    }
}
