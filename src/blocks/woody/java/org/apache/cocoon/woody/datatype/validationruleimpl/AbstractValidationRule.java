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

import org.apache.cocoon.woody.datatype.ValidationRule;
import org.apache.cocoon.woody.datatype.ValidationError;
import org.apache.cocoon.woody.formmodel.CannotYetResolveWarning;
import org.apache.excalibur.xml.sax.XMLizable;
import org.outerj.expression.Expression;
import org.outerj.expression.ExpressionContext;
import org.outerj.expression.ExpressionException;

import java.math.BigDecimal;

/**
 * Abstract base class providing common functionality for many {@link ValidationRule}
 * implementations.
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
