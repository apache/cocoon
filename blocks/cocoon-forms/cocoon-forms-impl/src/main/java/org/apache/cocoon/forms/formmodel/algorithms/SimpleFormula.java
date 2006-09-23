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

import org.apache.avalon.framework.CascadingError;
import org.apache.cocoon.forms.datatype.Datatype;
import org.apache.cocoon.forms.formmodel.CannotYetResolveWarning;
import org.apache.cocoon.forms.formmodel.ExpressionContextImpl;
import org.apache.cocoon.forms.formmodel.Form;
import org.apache.cocoon.forms.formmodel.Widget;
import org.outerj.expression.Expression;
import org.outerj.expression.ExpressionContext;
import org.outerj.expression.ExpressionException;

/**
 * An xreported expression based algorithm.
 * <p>
 * This algorithm can be used to write simple formulas, examples are :
 * <ul>
 *   <li>20% VAT calculation : eval="(amount / 100) * 20"</li>
 *   <li>100$ volume discount : eval="If(amount > 1000,100,0)" 
 *           (read: if amount is greater than 1000 then the result will be 100, otherwise 0)</li>
 *   <li>Number of boxes needed to carry that number of items :  eval="Ceiling(items / boxsize)"</li>
 *   <li>Number of items you can add before another box is needed : eval="Reminder(items,boxside)"</li>
 * </ul>
 * </p>
 * <p>
 * Note: please take care that xreporter expressions are not that accurate when it comes to decimals. The default
 * divide operator rounds the result, see http://issues.cocoondev.org/browse/XRP-115. Also consider that the 
 * available set of functions can be expanded implementing and using new ones. Please see 
 * <a href="http://outerthought.net/wqm/xreporter/en/expressions.html">
 * http://outerthought.net/wqm/xreporter/en/expressions.html</a> for an overview of xreportes expressions and 
 * {@link org.apache.cocoon.forms.expression.IsNullFunction} or 
 * {@link org.apache.cocoon.forms.expression.StringFunction}
 * for examples of custom xreporter functions. 
 * </p>
 * @version $Id$
 */
public class SimpleFormula extends AbstractBaseAlgorithm {

    protected Expression formula;
    
    public boolean isSuitableFor(Datatype dataType) {
        return dataType.getTypeClass().isAssignableFrom(formula.getResultType());
    }

    public Object calculate(Form form, Widget parent, Datatype datatype) {
        ExpressionContext ctx = new ExpressionContextImpl(parent, true);
        try {
            return formula.evaluate(ctx);
        } catch (CannotYetResolveWarning w) {
            return null;
        } catch (ExpressionException e) {
            throw new CascadingError("Error evaluating calculated field formula", e);
        }
    }

    /**
     * @return Returns the formula.
     */
    public Expression getFormula() {
        return formula;
    }
    /**
     * @param formula The formula to set.
     */
    public void setFormula(Expression formula) {
        this.formula = formula;
    }
}
