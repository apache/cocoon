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
import org.apache.cocoon.forms.formmodel.CannotYetResolveWarning;
import org.apache.cocoon.forms.util.I18nMessage;
import org.apache.cocoon.forms.validation.ValidationError;
import org.outerj.expression.ExpressionContext;
import org.outerj.expression.Expression;

import java.math.BigDecimal;

/**
 * Checks the length of a String.
 *
 * <p>This validation rule can perform 4 different checks:
 * <ul>
 *  <li>check exact string length
 *  <li>check minimum string length
 *  <li>check maximum string length
 *  <li>check min and max string length
 * </ul>
 * 
 * @version $Id$
 */
public class LengthValidationRule extends AbstractValidationRule {
    private Expression exactExpr;
    private Expression minExpr;
    private Expression maxExpr;

    public void setExactExpr(Expression exactExpr) {
        this.exactExpr = exactExpr;
    }

    public void setMinExpr(Expression minExpr) {
        this.minExpr = minExpr;
    }

    public void setMaxExpr(Expression maxExpr) {
        this.maxExpr = maxExpr;
    }

    public ValidationError validate(Object value, ExpressionContext expressionContext) {
        String string = (String)value;

        if (exactExpr != null) {
            Object result = evaluateNumeric(exactExpr, expressionContext, "exact", "length");
            if (result instanceof ValidationError) {
                return (ValidationError)result;
            } else if (result instanceof CannotYetResolveWarning) {
                return null;
            }
            int length = ((BigDecimal)result).intValue();
            if (string.length() != length) {
                return hasFailMessage() ? getFailMessage() : new ValidationError(new I18nMessage("validation.string.exact-length", new String[] {String.valueOf(length)}, FormsConstants.I18N_CATALOGUE));
            }
            return null;
        } else if (minExpr != null && maxExpr != null) {
            Object result = evaluateNumeric(minExpr, expressionContext, "min", "length");
            if (result instanceof ValidationError) {
                return (ValidationError)result;
            } else if (result instanceof CannotYetResolveWarning) {
                return null;
            }
            int minLength = ((BigDecimal)result).intValue();

            result = evaluateNumeric(maxExpr, expressionContext, "max", "length");
            if (result instanceof ValidationError) {
                return (ValidationError)result;
            } else if (result instanceof CannotYetResolveWarning) {
                return null;
            }
            int maxLength = ((BigDecimal)result).intValue();

            if (string.length() < minLength || string.length() > maxLength) {
                return hasFailMessage() ? getFailMessage() : new ValidationError(new I18nMessage("validation.string.range-length", new String[] {String.valueOf(minLength), String.valueOf(maxLength)}, FormsConstants.I18N_CATALOGUE));
            }
            return null;
        } else if (minExpr != null) {
            Object result = evaluateNumeric(minExpr, expressionContext, "min", "length");
            if (result instanceof ValidationError) {
                return (ValidationError)result;
            } else if (result instanceof CannotYetResolveWarning) {
                return null;
            }
            int length = ((BigDecimal)result).intValue();
            if (string.length() < length) {
                return hasFailMessage() ? getFailMessage() : new ValidationError(new I18nMessage("validation.string.min-length", new String[] {String.valueOf(length)}, FormsConstants.I18N_CATALOGUE));
            }
            return null;
        } else if (maxExpr != null) {
            Object result = evaluateNumeric(maxExpr, expressionContext, "max", "length");
            if (result instanceof ValidationError) {
                return (ValidationError)result;
            } else if (result instanceof CannotYetResolveWarning) {
                return null;
            }
            int length = ((BigDecimal)result).intValue();
            if (string.length() > length) {
                return hasFailMessage() ? getFailMessage() : new ValidationError(new I18nMessage("validation.string.max-length", new String[] {String.valueOf(length)}, FormsConstants.I18N_CATALOGUE));
            }
            return null;
        }
        return null;
    }

    public boolean supportsType(Class clazz, boolean arrayType) {
        return clazz.isAssignableFrom(String.class) && !arrayType;
    }
}
