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

import org.apache.cocoon.forms.datatype.ValidationRule;
import org.apache.cocoon.forms.formmodel.CannotYetResolveWarning;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.excalibur.xml.sax.XMLizable;
import org.outerj.expression.Expression;
import org.outerj.expression.ExpressionContext;
import org.outerj.expression.ExpressionException;

import java.math.BigDecimal;

/**
 * Abstract base class providing common functionality for many {@link ValidationRule}
 * implementations.
 * 
 * @version $Id: AbstractValidationRule.java,v 1.2 2004/03/09 14:58:45 cziegeler Exp $
 */
public abstract class AbstractValidationRule implements ValidationRule {
    private XMLizable failMessage;

    /**
     * Sets the failmessage to use for this validation rule, this will be used
     * instead of the validation rules' built-in message. The message itself should
     * be an object impementing XMLizable, such as a SaxBuffer instance. This
     * allows fail messages to contain mixed content (instead of just
     * being a string).
     */
    public void setFailMessage(XMLizable object) {
        this.failMessage = object;
    }

    /**
     * Returns the failMessage wrapped in a ValidationError object.
     */
    public ValidationError getFailMessage() {
        return new ValidationError(failMessage);
    }

    /**
     * Returns true if this validation rule has a user-defined fail message.
     */
    public boolean hasFailMessage() {
        return failMessage != null;
    }

    /**
     * Helper method for evaluating expressions whose result is numeric.
     *
     * @param exprName a name for the expression that's descriptive for the user, e.g. the name of the attribute in which it was defined
     * @param ruleName a descriptive name for the validation rule, usually the rule's element name
     * @return either a ValidationError (because expression evaluation failed) or a CannotYetResolveWarning
     * (because another, required field referenced in the expression has not yet a value), or a BigDecimal.
     */
    protected Object evaluateNumeric(Expression expression, ExpressionContext expressionContext, String exprName, String ruleName) {
        Object expressionResult;
        try {
            expressionResult = expression.evaluate(expressionContext);
        } catch (CannotYetResolveWarning w) {
            return w;
        } catch (ExpressionException e) {
            return new ValidationError("Error evaluating \"" + exprName + "\" expression on \"" +
                                       ruleName + "\" validation rule", false);
        }
        
        if (!(expressionResult instanceof BigDecimal)) {
            return new ValidationError("Got non-numeric result from \"" + exprName + "\" expression on \"" +
                                       ruleName + "\" validation rule", false);
        }

        return expressionResult;
    }

    /**
     * Helper method for evaluating expressions whose result is comparable.
     *
     * @param exprName a name for the expression that's descriptive for the user, e.g. the name of the attribute in which it was defined
     * @param ruleName a descriptive name for the validation rule, usually the rule's element name
     * @return either a ValidationError (because expression evaluation failed) or a CannotYetResolveWarning
     * (because another, required field referenced in the expression has not yet a value), or a BigDecimal.
     */
    protected Object evaluateComparable(Expression expression, ExpressionContext expressionContext, String exprName, String ruleName) {
        Object expressionResult;
        try {
            expressionResult = expression.evaluate(expressionContext);
        } catch (CannotYetResolveWarning w) {
            return w;
        } catch (ExpressionException e) {
            return new ValidationError("Error evaluating \"" + exprName + "\" expression on \"" +
                                       ruleName + "\" validation rule", false);
        }
        
        if (!(expressionResult instanceof Comparable)) {
            return new ValidationError("Got non-comparable result from \"" + exprName + "\" expression on \"" +
                                       ruleName + "\" validation rule", false);
        }

        return expressionResult;
    }
}
