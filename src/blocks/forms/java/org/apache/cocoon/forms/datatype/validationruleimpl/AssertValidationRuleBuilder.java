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
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;
import org.outerj.expression.Expression;

/**
 * Builds {@link AssertValidationRule}s.
 * 
 * @version $Id: AssertValidationRuleBuilder.java,v 1.1 2004/03/09 10:34:10 reinhard Exp $
 */
public class AssertValidationRuleBuilder extends AbstractValidationRuleBuilder {
    public ValidationRule build(Element validationRuleElement) throws Exception {
        String exprStr = validationRuleElement.getAttribute("test");
        if (exprStr.length() == 0)
            throw new Exception("assert validation rule requires a \"test\" attribute at " + DomHelper.getLocation(validationRuleElement));
        Expression testExpression = parseExpression(exprStr, validationRuleElement, "test");
        if (testExpression.getResultType() != null && !Boolean.class.isAssignableFrom(testExpression.getResultType()))
            throw new Exception("Expression should evaluate to a boolean on assert validation rule at " + DomHelper.getLocation(validationRuleElement));
        AssertValidationRule rule = new AssertValidationRule(testExpression);
        buildFailMessage(validationRuleElement, rule);
        return rule;
    }
}
