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
package org.apache.cocoon.woody.formmodel;

import org.outerj.expression.ExpressionContext;

import java.math.BigDecimal;

/**
 * Implementation of the ExpressionContext required for the evaluation of
 * expressions by xReporter expression interpreter.
 * 
 * @version $Id: ExpressionContextImpl.java,v 1.6 2004/03/05 13:02:31 bdelacretaz Exp $
 */
public class ExpressionContextImpl implements ExpressionContext {
    private Widget widget;
    private boolean referenceChildren;

    public ExpressionContextImpl(Widget widget) {
        this.widget = widget;
        this.referenceChildren = false;
    }

    /**
     * @param referenceChildren if true, variables will be resolved among the children of the given
     * container widget, rather than among the siblings of the widget.
     */
    public ExpressionContextImpl(Widget widget, boolean referenceChildren) {
        this.widget = widget;
        this.referenceChildren = referenceChildren;
    }

    /**
     * Variables refer to other widgets.
     *
     * <p>The current implementation only allows access to sibling widgets.
     *
     * <p>In case the value of a widget is null but the widget is required, then a special
     * exception will be thrown, the {@link CannotYetResolveWarning}. This is because in
     * that case, you'll probably want to re-evaluate the expression at a later time (since
     * the widget is required, it will eventually get a value).
     *
     * <p>In case the value of the widget is null but the field is not required, then simply
     * null is returned. (TODO: a function IsNull() will provided in the expression library
     * so that expression writers can check for the likely condition where a non-required field
     * is null).
     *
     * <p>If the variable name does not refer to an existing widget, null is returned (TODO: this
     * behaviour will probably change in the future)
     */
    public Object resolveVariable(String name) {
        // TODO allow to access other widgets instead of only siblings (allow going up with ../ notation or something)
        Widget widget;
        if (!referenceChildren)
            widget = this.widget.getParent().getWidget(name);
        else
            widget = this.widget.getWidget(name);
        if (widget != null) {
            Object value = widget.getValue();

            if (value == null && widget.isRequired()) {
                // the widget currently has not yet a value, but since it is required, it will get a value sooner
                // or later. Therefore, we throw an exception here indicating that this expression can currenlty
                // not yet be evaluated, but will be at a later time.
                throw new CannotYetResolveWarning();
            }

            // do some type conversions:
            //   * the expression library only knows about BigDecimals as being numbers, so convert Longs first to BigDecimals
            //   * ...
            if (value instanceof Long)
                return new BigDecimal(((Long)value).longValue());
            else if (value instanceof Integer)
                return new BigDecimal(((Integer)value).intValue());
            else
                return value;
        }
        return null;
    }

    public Object get(String s) {
        return null;
    }

}
