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

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.util.Introspector;
import org.apache.commons.jexl.util.introspection.Info;
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
        Iterator iter = null;
        Object result = evaluate(context);
        if (result != null) {
            /* The Info object is supposed to contain the script
               location where the expression is invoked and use that
               in a warning log message if no iterator can be
               generated. This info is not available in the expression
               object and might not be relevant either as it can be
               used from a non script situation.
            */
            try {
                iter = Introspector.getUberspect().getIterator(result, new Info("Unknown", 0, 0));
            } catch (Exception e) {
                throw new ExpressionException("Couldn't get an iterator from expression " +
                                              getExpression(), e);
            }
        }
        if (iter == null) {
            iter = EMPTY_ITER;
        }
        return iter;
    }

    public void assign(ExpressionContext context, Object value)
        throws ExpressionException {
        throw new UnsupportedOperationException("Assign is not yet implemented for Jexl");
    }

    public String getExpression() {
        return this.compiledExpression.getExpression();
    }

    public String getLanguage() {
        return this.language;
    }

    private static class ContextAdapter implements JexlContext {
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

    private static final Iterator EMPTY_ITER = new Iterator() {
        public boolean hasNext() {
            return false;
        }

        public Object next() {
            return null;
        }

        public void remove() {
            // EMPTY
        }
    };

    static {
        // Hack: there's no _nice_ way to add my introspector to Jexl right now
        try {
            Field field = Introspector.class.getDeclaredField("uberSpect");
            field.setAccessible(true);
            field.set(null, new JSIntrospector());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
