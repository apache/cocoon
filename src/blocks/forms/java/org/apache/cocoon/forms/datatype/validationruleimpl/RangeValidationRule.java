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

import java.math.BigDecimal;

import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.datatype.ValidationError;
import org.apache.cocoon.forms.formmodel.CannotYetResolveWarning;
import org.apache.cocoon.forms.util.I18nMessage;
import org.outerj.expression.Expression;
import org.outerj.expression.ExpressionContext;

/**
 * Checks numeric ranges.
 * Works for Integer, Long, BigDecimal, Float, Double, and Date values.
 * Numbers are converted to the BigDecimal before comparing.
 *
 * <p>This validation rule can perform 3 different checks:
 * <ul>
 *  <li>check minimum value
 *  <li>check maximum value
 *  <li>check min and max values (range check)
 * </ul>
 * 
 * @version $Id: RangeValidationRule.java,v 1.1 2004/03/09 10:34:10 reinhard Exp $
 */
public class RangeValidationRule extends AbstractValidationRule {
    private Expression minExpr;
	private Expression maxExpr;
    
    private static final String RANGE_ELEM = "range";
    private static final String MIN_ATTR = "min";
    private static final String MAX_ATTR = "max";
    

    public void setMinExpr(Expression minExpr) {
        this.minExpr = minExpr;
    }

    public void setMaxExpr(Expression maxExpr) {
        this.maxExpr = maxExpr;
    }

    public ValidationError validate(Object value, ExpressionContext expressionContext) {
        // HACK: JDK's Comparable can't even compare Decimal to Integer
        Comparable decimal;
        if (value instanceof Integer) {
            decimal = new BigDecimal(((Integer) value).intValue());
        } else if (value instanceof Long) {
            decimal = new BigDecimal(((Long) value).longValue()); 
        } else if (value instanceof Float) {
            decimal = new BigDecimal(((Float) value).floatValue()); 
        } else if (value instanceof Double) {
            decimal = new BigDecimal(((Double) value).doubleValue()); 
        } else {
            decimal = (Comparable) value;
        }

        Comparable min = null; 
        if (minExpr != null) {
            Object result = evaluateComparable(minExpr, expressionContext, MIN_ATTR, RANGE_ELEM);
            if (result instanceof ValidationError) {
                return (ValidationError) result;
            } else if (result instanceof CannotYetResolveWarning) {
                return null;
            }
            min = (Comparable) result;
        }
        
        Comparable max = null;
        if (maxExpr != null) {
            Object result = evaluateComparable(maxExpr, expressionContext, MAX_ATTR, RANGE_ELEM);
            if (result instanceof ValidationError) {
                return (ValidationError) result;
            } else if (result instanceof CannotYetResolveWarning) {
                return null;
            }
            max = (Comparable) result;
        }
        
        if (min != null && max != null) {
            if (decimal.compareTo(min) < 0 || decimal.compareTo(max) > 0) {
                return hasFailMessage() ? getFailMessage() : new ValidationError(new I18nMessage("validation.numeric.range",
                                                                                 new String[]{min.toString(), max.toString()},
                                                                                 Constants.I18N_CATALOGUE));
            }

            return null;
        } else if (min != null) {
            if (decimal.compareTo(min) < 0) {
                return hasFailMessage() ? getFailMessage() : new ValidationError(new I18nMessage("validation.numeric.min",
                                                                                 new String[]{min.toString()},
                                                                                 Constants.I18N_CATALOGUE));
            }

            return null;
        } else if (max != null) {
            if (decimal.compareTo(max) > 0) {
                return hasFailMessage() ? getFailMessage() : new ValidationError(new I18nMessage("validation.numeric.max",
                                                                                 new String[]{max.toString()},
                                                                                 Constants.I18N_CATALOGUE));
            }

            return null;
        }

        return null;
    }

    public boolean supportsType(Class clazz, boolean arrayType) {
        return Comparable.class.isAssignableFrom(clazz) && !arrayType;
    }
}
