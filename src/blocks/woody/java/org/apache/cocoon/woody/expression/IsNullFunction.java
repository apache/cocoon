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
package org.apache.cocoon.woody.expression;

import org.outerj.expression.AbstractExpression;
import org.outerj.expression.Expression;
import org.outerj.expression.ExpressionContext;
import org.outerj.expression.ExpressionException;

/**
 * Returns true if argument is null.
 *  
 * @author <a href="mailto:vadim.gritsenko@dc.gov">Vadim Gritsenko</a>
 * @version CVS $Id: IsNullFunction.java,v 1.5 2004/03/09 13:54:24 reinhard Exp $
 */
public class IsNullFunction extends AbstractExpression {

    public Object evaluate(ExpressionContext context) throws ExpressionException {
        Object result = null;
        try {
            result = ((Expression)arguments.get(0)).evaluate(context);
        } catch (ExpressionException e) {
            // FIXME: Hack to handle null variables
            if (!e.getMessage().startsWith("Unknown variable")) {
                throw e;
            }
        }
        return result == null? Boolean.TRUE: Boolean.FALSE;
    }

    public void check() throws ExpressionException {
        if (arguments.size() != 1) {
            throw new ExpressionException(getDescription() + " requires one argument.", getLine(), getColumn());
        }
    }

    public Class getResultType() {
        return Boolean.class;
    }

    public String getDescription() {
        return "IsNull function";
    }
}
