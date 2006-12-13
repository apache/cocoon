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

import org.apache.cocoon.forms.formmodel.CannotYetResolveWarning;
import org.apache.cocoon.forms.validation.ValidationError;
import org.outerj.expression.ExpressionContext;
import org.outerj.expression.ExpressionException;
import org.outerj.expression.Expression;

/**
 * Generic validation rule that evaluates an expression. If the outcome of the expression is true,
 * the validation is successful, otherwise not.
 * 
 * @version $Id$
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
