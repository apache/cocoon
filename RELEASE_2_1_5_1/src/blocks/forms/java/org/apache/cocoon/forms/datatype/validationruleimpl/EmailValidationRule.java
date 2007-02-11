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
import org.outerj.expression.ExpressionContext;

/**
 * ValidationRule that checks that a string is an email address.
 * 
 * @version $Id: EmailValidationRule.java,v 1.2 2004/03/09 14:58:45 cziegeler Exp $
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

    private boolean isEmail(String email) {
        // TODO there's room for improvement here

        // check that the email address does not contain spaces
        int space = email.indexOf(' ');
        if (space != -1)
            return false;

        // check that there is an @, and that there's at least one character before the @
        int atpos = email.indexOf('@');
        if (atpos < 1)
            return false;

        atpos++;

        // check there's not second at
        int anotheratpos = email.indexOf('@', atpos);
        if (anotheratpos != -1)
            return false;

        // check there's at least one dot after the at
        int dotAfterAt = email.indexOf('.', atpos);
        if (dotAfterAt == -1)
            return false;

        return true;
    }
}
