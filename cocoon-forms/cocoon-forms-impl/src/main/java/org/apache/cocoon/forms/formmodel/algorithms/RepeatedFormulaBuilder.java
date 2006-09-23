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

import org.apache.cocoon.forms.formmodel.CalculatedFieldAlgorithm;
import org.apache.cocoon.forms.util.DomHelper;
import org.outerj.expression.Expression;
import org.w3c.dom.Element;

/**
 * Builds a {@link org.apache.cocoon.forms.formmodel.algorithms.RepeatedFormula}
 * algorithm.
 * <p>
 * The syntax is as follows :
 * <code>
 *   &lt;fd:value type="repeatedformula" [inital-result="..."] eval="..." [triggers="..."]/&gt;
 * </code>
 * </p>
 * @version $Id$
 */
public class RepeatedFormulaBuilder extends SimpleFormulaBuilder {

    public CalculatedFieldAlgorithm build(Element algorithmElement) throws Exception {
        RepeatedFormula ret = new RepeatedFormula();
        String formula = DomHelper.getAttribute(algorithmElement, "eval");
        setupExpression(formula,ret);
        
        // Remove formulaResult and formulaCurrent fom the list of triggers
        Iterator iter = ret.getTriggerWidgets();
        while (iter.hasNext()) {
            String wdg = (String) iter.next();
            if (wdg.equals("formulaResult") || wdg.equals("formulaCurrent")) {
                iter.remove();
            }
        }
        
        // Set the repeat-on attribute and add it to the list of triggers
        String repeatOn = DomHelper.getAttribute(algorithmElement, "repeat-on");
        ret.setRepeatOn(repeatOn);
        ret.addTrigger(repeatOn);
        
        String initialResult = DomHelper.getAttribute(algorithmElement, "initial-result");
        Expression expression = setupExpression(initialResult);
        ret.setInitialResult(expression);
        super.setup(algorithmElement, ret);
        return ret;
    }
}
