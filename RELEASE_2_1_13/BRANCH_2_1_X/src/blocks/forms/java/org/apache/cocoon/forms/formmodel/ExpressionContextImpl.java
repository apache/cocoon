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
package org.apache.cocoon.forms.formmodel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.forms.util.WidgetFinder;
import org.outerj.expression.ExpressionContext;

/**
 * Implementation of the ExpressionContext required for the evaluation of
 * expressions by xReporter expression interpreter.
 * 
 * @version $Id$
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
     * <p>You can access parent and root widgets (with ../widget and /widget paths
     * respectively), but mind that in xreporter expressions these variables names
     * must be placed in brakets to be correctly parsed. For example 
     * "{../widget} + otherWidget".
     *
     * <p>In case the value of a widget is null but the widget is required or calculated, then 
     * a special exception will be thrown, the {@link CannotYetResolveWarning}. This is 
     * because in that case, you'll probably want to re-evaluate the expression at a later time 
     * (since the widget is required, it will eventually get a value).
     *
     * <p>In case the value of the widget is null but the field is not required, then simply
     * null is returned. (TODO: a function IsNull() will provided in the expression library
     * so that expression writers can check for the likely condition where a non-required field
     * is null).
     *
     * <p>If the variable name does not refer to an existing widget, null is returned (TODO: this
     * behaviour will probably change in the future)
     * 
     * <p>If the variable name contains the "/./" notation, it will return a Collection of values,
     * using the {@link org.apache.cocoon.forms.util.WidgetFinder} utility.
     */
    public Object resolveVariable(String name) {
        if (name.indexOf("/./") != -1) {
            WidgetFinder finder = new WidgetFinder(widget, name, false);
            Collection widgets = finder.getWidgets();
            List result = new ArrayList();
            for (Iterator iter = widgets.iterator(); iter.hasNext();) {
                Widget widget = (Widget) iter.next();
                if (widget.getValue() != null) {
                    result.add(widget.getValue());
                } else if (widget.isRequired() || widget instanceof CalculatedField) {
                    // the widget currently has not yet a value, but since it is required or calculated, it will get a value sooner
                    // or later. Therefore, we throw an exception here indicating that this expression can currenlty
                    // not yet be evaluated, but will be at a later time.
                    throw new CannotYetResolveWarning();
                }
            }
            return result;
        } else {
            Widget widget;
            if (!referenceChildren)
                widget = ((ContainerWidget)this.widget.getParent()).lookupWidget(name);
            else
                widget = ((ContainerWidget)this.widget).lookupWidget(name);
            if (widget != null) {
                Object value = widget.getValue();

                if (value == null && (widget.isRequired() || widget instanceof CalculatedField)) {
                    // the widget currently has not yet a value, but since it is required or calculated, it will get a value sooner
                    // or later. Therefore, we throw an exception here indicating that this expression can currenlty
                    // not yet be evaluated, but will be at a later time.
                    throw new CannotYetResolveWarning();
                }

                // do some type conversions:
                //   * the expression library only knows about BigDecimals as being numbers, so convert Longs first to BigDecimals
                //   * same for Integer
                //   * all other Number instances will get converted to String first.
                if (value instanceof Long)
                    return new BigDecimal(((Long)value).longValue());
                else if (value instanceof Integer)
                    return new BigDecimal(((Integer)value).intValue());
                else if (value instanceof Number)
                    return new BigDecimal(value.toString());
                else
                    return value;
            }
            return null;            
        }
    }

    public Object get(String s) {
        return null;
    }

}
