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

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;

import org.apache.cocoon.forms.datatype.Datatype;
import org.apache.cocoon.forms.formmodel.CannotYetResolveWarning;
import org.apache.cocoon.forms.formmodel.ExpressionContextImpl;
import org.apache.cocoon.forms.formmodel.Form;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.util.WidgetFinder;
import org.outerj.expression.Expression;
import org.outerj.expression.ExpressionException;

/**
 * An xreporter {@link org.outerj.expression.Expression} based algorithm that repeats the formula on a 
 * set of fields. 
 * 
 * <p>
 * The specified formula will be applied iterating on the specified widgets. The final result will be the result of the
 * last iteration. From inside the formula you can access this two extra variables :
 * <dl>
 *   <dt>formulaResult</dt>
 *   <dd>The result of the previous iteration, or the result of the initial result if this is the first iteration.</dd>
 *   <dt>formulaCurrent</dt>
 *   <dd>The value of the current trigger widget.</dd>
 * </dl>
 * </p>
 * <p>
 * The initial result is evaluated before starting the iteration, and its value is used as a formulaResult for the
 * first iteration.
 * </p>
 * <p>
 * It's possible to define nearly every cyclic arithmetic operation with this algorithm, for example :
 * <dl>
 *   <dt>Sum</dt>
 *   <dd>initial-result="0" formula="formulaResult + formulaCurrent"</dd>
 *   <dt>Multiplication</dt>
 *   <dd>initial-result="1" formula="formulaResult * formulaCurrent"</dd>
 * </dl>
 * </p>
 * <p>
 * More over, thru the use of advanced xreporter syntax it's possible to quickly implement also complex
 * algorithms:
 * <ul>
 *   <li>Count all items with a price higher than 100 : eval="formulaResult + If(price > 100, 1, 0)" 
 *           (read : the result is the previous result plus one if price is over 100, 0 if price is less than 100)</li>
 *   <li>Obtain a sum of all movements, wether they are positive or negative amount movements : 
 *           eval="formulaResult + Abs(amount)"</li>
 *   <li>Count how many slots are empty in the 10 items box you are using for packaging : 
 *           eval="formulaResult + Reminder(items, 10)"
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
public class RepeatedFormula extends SimpleFormula {

    private Expression initialResult = null;
    private String repeatOn = null;
    
    public Object calculate(Form form, Widget parent, Datatype datatype) {
        try {
            Object result = null;
            if (initialResult != null) {
                result = initialResult.evaluate(new ExpressionContextImpl(parent, true));
            }
            WidgetFinder finder = new WidgetFinder(parent, this.repeatOn, false);
            Collection widgets = finder.getWidgets();            
            for (Iterator iter = widgets.iterator(); iter.hasNext();) {
                Widget widget = (Widget) iter.next();
                ResultExpressionContext ctx = new ResultExpressionContext(widget, result);
                result = formula.evaluate(ctx);
            }
            return result;
        } catch (CannotYetResolveWarning w) {
            return null;
        } catch (ExpressionException e) {
            throw new Error("Error evaluating calculated field formula", e);
        }
    }
    
    static class ResultExpressionContext extends ExpressionContextImpl {
        Object result = null;
        Widget current = null;
        public ResultExpressionContext(Widget widget, Object result) {
            super(widget.getParent(), true);
            current = widget;
            this.result = result;
        }
        public Object resolveVariable(String name) {
            if (name.equals("formulaResult")) {
                return result;
            } 
            if (name.equals("formulaCurrent")) {
                Object value = current.getValue();
                if (value == null && current.isRequired()) {
                    throw new CannotYetResolveWarning();
                }
                if (value instanceof Long)
                    return new BigDecimal(((Long)value).longValue());
                else if (value instanceof Integer)
                    return new BigDecimal(((Integer)value).intValue());
                else
                    return value;
            }            
            return super.resolveVariable(name);
        }
    }
    
    
    /**
     * @return Returns the initialResult.
     */
    public Expression getInitialResult() {
        return initialResult;
    }
    /**
     * @param initialResult The initialResult to set.
     */
    public void setInitialResult(Expression initialResult) {
        this.initialResult = initialResult;
    }
    /**
     * @return Returns the iterateOn.
     */
    public String getRepeatOn() {
        return repeatOn;
    }
    /**
     * @param iterateOn The iterateOn to set.
     */
    public void setRepeatOn(String iterateOn) {
        this.repeatOn = iterateOn;
    }
}
