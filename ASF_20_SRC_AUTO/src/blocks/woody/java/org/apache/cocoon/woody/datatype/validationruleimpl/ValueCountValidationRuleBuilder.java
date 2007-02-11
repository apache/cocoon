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

import org.apache.cocoon.woody.datatype.ValidationRule;
import org.apache.cocoon.woody.util.DomHelper;
import org.w3c.dom.Element;
import org.outerj.expression.Expression;

/**
 * Builds {@link ValueCountValidationRule}s.
 * 
 * @version $Id: ValueCountValidationRuleBuilder.java,v 1.3 2004/03/05 13:02:30 bdelacretaz Exp $
 */
public class ValueCountValidationRuleBuilder extends AbstractValidationRuleBuilder {
    public ValidationRule build(Element validationRuleElement) throws Exception {
        ValueCountValidationRule rule = new ValueCountValidationRule();

        String exactExprString = validationRuleElement.getAttribute("exact");
        String minExprString = validationRuleElement.getAttribute("min");
        String maxExprString = validationRuleElement.getAttribute("max");

        if (exactExprString.length() > 0) {
            Expression expression = parseExpression(exactExprString, validationRuleElement, "exact");
            rule.setExactExpr(expression);
        } else if (minExprString.length() > 0 && maxExprString.length() > 0) {
            Expression expression = parseExpression(minExprString, validationRuleElement, "min");
            rule.setMinExpr(expression);
            expression = parseExpression(maxExprString, validationRuleElement, "max");
            rule.setMaxExpr(expression);
        } else if (minExprString.length() > 0) {
            Expression expression = parseExpression(minExprString, validationRuleElement, "min");
            rule.setMinExpr(expression);
        } else if (maxExprString.length() > 0) {
            Expression expression = parseExpression(maxExprString, validationRuleElement, "max");
            rule.setMaxExpr(expression);
        } else {
            throw new Exception("value-count validation rule requires a min and/or max, or exact attribute at " + DomHelper.getLocation(validationRuleElement));
        }

        buildFailMessage(validationRuleElement, rule);

        return rule;
    }
}
