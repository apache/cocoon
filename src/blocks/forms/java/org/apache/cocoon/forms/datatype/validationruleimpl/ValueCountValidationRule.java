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
import org.apache.cocoon.forms.datatype.ValidationError;
import org.apache.cocoon.forms.formmodel.CannotYetResolveWarning;
import org.apache.cocoon.forms.util.I18nMessage;
import org.outerj.expression.ExpressionContext;
import org.outerj.expression.Expression;

import java.math.BigDecimal;

/**
 * Checks the number of values (i.e. the size of the array). This only works for Datatypes
 * for which {@link org.apache.cocoon.forms.datatype.Datatype#isArrayType()} returns
 * true.
 *
 * <p>This validation rule can perform 4 different checks:
 * <ul>
 *  <li>check exact array size
 *  <li>check minimum array size
 *  <li>check maximum array size
 *  <li>check min and max array size
 * </ul>
 * 
 * @version $Id: ValueCountValidationRule.java,v 1.2 2004/03/09 11:31:12 joerg Exp $
 */
public class ValueCountValidationRule extends AbstractValidationRule {
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
        Object[] array = (Object[])value;

        if (exactExpr != null) {
            Object result = evaluateNumeric(exactExpr, expressionContext, "exact", "value-count");
            if (result instanceof ValidationError)
                return (ValidationError)result;
            else if (result instanceof CannotYetResolveWarning)
                return null;
            int length = ((BigDecimal)result).intValue();
            if (array.length != length)
                return hasFailMessage() ? getFailMessage() : new ValidationError(new I18nMessage("validation.array.exact-valuecount", new String[] {String.valueOf(length)}, Constants.I18N_CATALOGUE));
            return null;
        } else if (minExpr != null && maxExpr != null) {
            Object result = evaluateNumeric(minExpr, expressionContext, "min", "value-count");
            if (result instanceof ValidationError)
                return (ValidationError)result;
            else if (result instanceof CannotYetResolveWarning)
                return null;
            int minLength = ((BigDecimal)result).intValue();

            result = evaluateNumeric(maxExpr, expressionContext, "max", "value-count");
            if (result instanceof ValidationError)
                return (ValidationError)result;
            else if (result instanceof CannotYetResolveWarning)
                return null;
            int maxLength = ((BigDecimal)result).intValue();

            if (array.length < minLength || array.length > maxLength)
                return hasFailMessage() ? getFailMessage() : new ValidationError(new I18nMessage("validation.array.range-valuecount", new String[] {String.valueOf(minLength), String.valueOf(maxLength)}, Constants.I18N_CATALOGUE));
            return null;
        } else if (minExpr != null) {
            Object result = evaluateNumeric(minExpr, expressionContext, "min", "value-count");
            if (result instanceof ValidationError)
                return (ValidationError)result;
            else if (result instanceof CannotYetResolveWarning)
                return null;
            int length = ((BigDecimal)result).intValue();
            if (array.length < length)
                return hasFailMessage() ? getFailMessage() : new ValidationError(new I18nMessage("validation.array.min-valuecount", new String[] {String.valueOf(length)}, Constants.I18N_CATALOGUE));
            return null;
        } else if (maxExpr != null) {
            Object result = evaluateNumeric(maxExpr, expressionContext, "max", "value-count");
            if (result instanceof ValidationError)
                return (ValidationError)result;
            else if (result instanceof CannotYetResolveWarning)
                return null;
            int length = ((BigDecimal)result).intValue();
            if (array.length > length)
                return hasFailMessage() ? getFailMessage() : new ValidationError(new I18nMessage("validation.array.max-valuecount", new String[] {String.valueOf(length)}, Constants.I18N_CATALOGUE));
            return null;
        }
        return null;
    }

    public boolean supportsType(Class clazz, boolean arrayType) {
        return arrayType;
    }
}
