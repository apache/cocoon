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
package org.apache.cocoon.el.impl.jexl;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.el.ExpressionException;
import org.apache.cocoon.el.impl.AbstractExpression;
import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.util.Introspector;
import org.apache.commons.jexl.util.introspection.Info;

/**
 * @version $Id$
 */
public class JexlExpression extends AbstractExpression {

    private final org.apache.commons.jexl.Expression compiledExpression;

    public JexlExpression(String language, String expression) throws ExpressionException {
        super(language, expression);
        try {
            this.compiledExpression = org.apache.commons.jexl.ExpressionFactory.createExpression(expression);
        } catch (Exception e) {
            throw new ExpressionException("Couldn't create expression " + expression, e);
        }
    }

    public Object evaluate(ObjectModel objectModel) throws ExpressionException {
        try {
            return this.compiledExpression.evaluate(new ContextAdapter(objectModel));
        } catch (Exception e) {
            throw new ExpressionException("Couldn't evaluate expression " + getExpression(), e);
        }
    }

    public Iterator iterate(ObjectModel objectModel) throws ExpressionException {
        Iterator iter = null;
        Object result = evaluate(objectModel);
        if (result != null) {
            /*
             * The Info object is supposed to contain the script location where
             * the expression is invoked and use that in a warning log message
             * if no iterator can be generated. This info is not available in
             * the expression object and might not be relevant either as it can
             * be used from a non script situation.
             */
            try {
                iter = Introspector.getUberspect().getIterator(result, new Info("Unknown", 0, 0));
            } catch (Exception e) {
                throw new ExpressionException("Couldn't get an iterator from expression " + getExpression(), e);
            }
        }
        if (iter == null) {
            iter = EMPTY_ITER;
        }
        return iter;
    }

    public void assign(ObjectModel objectModel, Object value) throws ExpressionException {
        throw new UnsupportedOperationException("Assign is not yet implemented for Jexl");
    }

    public Object getNode(ObjectModel objectModel) throws ExpressionException {
        return evaluate(objectModel);
    }

    private static class ContextAdapter implements JexlContext {
        private Map objectModel;

        public ContextAdapter(Map objectModel) {
            this.objectModel = objectModel;
        }

        public Map getVars() {
            return this.objectModel;
        }

        public void setVars(Map map) {
            this.objectModel = map;
        }
    }

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
