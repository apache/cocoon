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
package org.apache.cocoon.woody.formmodel;

import org.outerj.expression.ExpressionContext;

import java.math.BigDecimal;

/**
 * Implementation of the ExpressionContext required for the evaluation of
 * expressions by xReporter expression interpreter.
 * 
 * @version $Id: ExpressionContextImpl.java,v 1.5 2004/02/11 10:43:30 antonio Exp $
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
