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
package org.apache.cocoon.components.expression.jxpath;

import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.components.expression.Expression;
import org.apache.cocoon.components.expression.ExpressionCompiler;
import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.components.expression.ExpressionException;
import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Variables;

public class JXPathExpression implements Expression {

    private final String language;
    private final String expression;
    private final CompiledExpression compiledExpression;

    public JXPathExpression(String language, String expression)
        throws ExpressionException {
        this.language = language;
        this.expression = expression;
        this.compiledExpression = JXPathContext.compile(expression);
    }

    public Object evaluate(ExpressionContext context)
        throws ExpressionException{
        return this.compiledExpression.getValue(getContext(context));
    }

    public Iterator iterate(ExpressionContext context)
        throws ExpressionException {
        return this.compiledExpression.iterate(getContext(context));
    }

    public void assign(ExpressionContext context, Object value)
        throws ExpressionException {
        this.compiledExpression.setValue(getContext(context), value);
    }

    public String getExpression() {
        return this.expression;
    }

    public String getLanguage() {
        return this.language;
    }

    private JXPathContext getContext(ExpressionContext context) {
        // This could be made more efficient by caching the
        // JXPathContext within the Context object.
        JXPathContext jxcontext = JXPathContext.newContext(null, context.getContextBean());
        jxcontext.setVariables(new VariableAdapter(context));
        return jxcontext;
    }

    private static class VariableAdapter implements Variables {
        private ExpressionContext context;

        public VariableAdapter(ExpressionContext context) {
            this.context = context;
        }

        public void declareVariable(String name, Object value) {
            this.context.put(name, value);
        }

        public Object getVariable(String name) {
            return this.context.get(name);
        }

        public boolean isDeclaredVariable(String name) {
            return this.context.containsKey(name);
        }

        public void undeclareVariable(String name) {
            throw new UnsupportedOperationException("Operation undeclareVariable is not supported");
        }
    }
}
