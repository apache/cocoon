/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.woody.datatype.validationruleimpl;

import java.math.BigDecimal;

import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.datatype.ValidationError;
import org.apache.cocoon.woody.formmodel.CannotYetResolveWarning;
import org.apache.cocoon.woody.util.I18nMessage;
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
 * @version $Id: RangeValidationRule.java,v 1.7 2004/02/11 09:53:44 antonio Exp $
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
