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
package org.apache.cocoon.forms.formmodel.algorithms;

import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.forms.expression.ExpressionManager;
import org.apache.cocoon.forms.formmodel.CalculatedFieldAlgorithm;
import org.apache.cocoon.forms.util.DomHelper;
import org.outerj.expression.Expression;
import org.outerj.expression.VariableFunction;
import org.w3c.dom.Element;

/**
 * Builds a {@link org.apache.cocoon.forms.formmodel.algorithms.SimpleFormula}
 * algorithm.
 * <p>
 * The syntax is as follows :
 * <code>
 *   &lt;fd:value type="formula" eval="..."&gt; [triggers="..."]/&gt;
 * </code>
 * </p>
 * @version $Id$
 */
public class SimpleFormulaBuilder extends AbstractBaseAlgorithmBuilder {

    private ExpressionManager expressionManager;
    
    public CalculatedFieldAlgorithm build(Element algorithmElement) throws Exception {
        String formula = DomHelper.getAttribute(algorithmElement, "eval");
        SimpleFormula ret = new SimpleFormula();
        setupExpression(formula, ret);
        super.setup(algorithmElement, ret);
        return ret;
    }

    protected Expression setupExpression(String formula) throws Exception {
        return expressionManager.parse(formula);
    }
    
    protected void setupExpression(String formula, SimpleFormula algo) throws Exception {
        Expression expression = expressionManager.parse(formula);
        algo.setFormula(expression);
        List vars = expressionManager.parseVariables(formula);
        for (Iterator iter = vars.iterator(); iter.hasNext();) {
            VariableFunction var = (VariableFunction) iter.next();
            algo.addTrigger(var.getVariableName());
        }
    }

    public void setExpressionManager( ExpressionManager expressionManager )
    {
        this.expressionManager = expressionManager;
    }    
}
