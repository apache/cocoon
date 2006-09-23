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
package org.apache.cocoon.forms.datatype.validationruleimpl;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.util.I18nMessage;
import org.apache.cocoon.forms.validation.ValidationError;
import org.outerj.expression.ExpressionContext;

/**
 * Performs the so called "mod 10" algorithm to check the validity of credit card numbers
 * such as VISA.
 *
 * <p>In addition to this, the credit card number can be further validated by its length
 * and prefix, but those properties are depended on the credit card type and such validation
 * is not performed by this validation rule.
 * 
 * @version $Id$
 */
public class Mod10ValidationRule extends AbstractValidationRule {
    public ValidationError validate(Object value, ExpressionContext expressionContext) {
        String numberToCheck = (String)value;
        int nulOffset = '0';
        int sum = 0;
        for (int i = 1; i <= numberToCheck.length(); i++) {
            int currentDigit = numberToCheck.charAt(numberToCheck.length() - i) - nulOffset;
            if ((i % 2) == 0) {
                currentDigit *= 2;
                currentDigit = currentDigit > 9 ? currentDigit - 9 : currentDigit;
                sum += currentDigit;
            } else {
                sum += currentDigit;
            }
        }
        if(!((sum % 10) == 0))
            return hasFailMessage() ? getFailMessage() : new ValidationError(new I18nMessage("validation.mod10", FormsConstants.I18N_CATALOGUE));
        else
            return null;
    }

    public boolean supportsType(Class clazz, boolean arrayType) {
        return String.class.isAssignableFrom(clazz) && !arrayType;
    }
}
