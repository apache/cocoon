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
package org.apache.cocoon.components.expression.jexl;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.jexl.JexlContext;
import org.apache.cocoon.components.expression.Expression;
import org.apache.cocoon.components.expression.ExpressionCompiler;
import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.components.expression.ExpressionException;

public class JexlExpression implements Expression {

    private final String language;
    private final org.apache.commons.jexl.Expression compiledExpression;

    public JexlExpression(String language, String expression)
        throws ExpressionException {
        this.language = language;
        try {
            this.compiledExpression =
                org.apache.commons.jexl.ExpressionFactory.createExpression(expression);
        } catch (Exception e) {
            throw new ExpressionException("Couldn't create expression " + expression, e);
        }
    }

    public Object evaluate(ExpressionContext context)
        throws ExpressionException{
        try {
            return this.compiledExpression.evaluate(new ContextAdapter(context));
        } catch (Exception e) {
            throw new ExpressionException("Couldn't evaluate expression " +
                                          getExpression(), e);
        }
    }

    public Iterator iterate(ExpressionContext context)
        throws ExpressionException {
        return null;
    }

    public void assign(ExpressionContext context, Object value)
        throws ExpressionException {
    }

    public String getExpression() {
        return this.compiledExpression.getExpression();
    }

    public String getLanguage() {
        return this.language;
    }

    static class ContextAdapter implements JexlContext {
        private final ExpressionContext context;
        public ContextAdapter(ExpressionContext context) {
            this.context = context;
        }

        public Map getVars() {
            return this.context.getVars();
        }

        public void setVars(Map map) {
            this.context.setVars(map);
        }
    }
}
