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
package org.apache.cocoon.woody.datatype.validationruleimpl;

import org.apache.cocoon.woody.datatype.ValidationError;
import org.apache.cocoon.woody.formmodel.CannotYetResolveWarning;
import org.outerj.expression.ExpressionContext;
import org.outerj.expression.ExpressionException;
import org.outerj.expression.Expression;

/**
 * Generic validation rule that evaluates an expression. If the outcome of the expression is true,
 * the validation is successful, otherwise not.
 * 
 * @version $Id: AssertValidationRule.java,v 1.5 2004/03/05 13:02:30 bdelacretaz Exp $
 */
public class AssertValidationRule extends AbstractValidationRule {
    private Expression testExpression;

    public AssertValidationRule(Expression testExpression) {
        this.testExpression = testExpression;
    }

    public ValidationError validate(Object value, ExpressionContext expressionContext) {
        Object expressionResult;
        try {
            expressionResult = testExpression.evaluate(expressionContext);
        } catch (CannotYetResolveWarning w) {
            return null;
        } catch (ExpressionException e) {
            return new ValidationError("Error evaluating expression on assert validation rule.", false);
        }

        if (!(expressionResult instanceof Boolean))
            return new ValidationError("Got non-boolean result from expression on assert validation rule.", false);

        if (((Boolean)expressionResult).booleanValue())
            return null;
        else
            return hasFailMessage() ? getFailMessage() : new ValidationError("Assertion validation rule failed.", false);
    }

    public boolean supportsType(Class clazz, boolean arrayType) {
        return true;
    }
}
