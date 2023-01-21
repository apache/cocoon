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
package org.apache.cocoon.el.impl.javascript;

import java.util.Iterator;
import java.util.Map;

import org.apache.cocoon.el.ExpressionException;
import org.apache.cocoon.el.impl.AbstractExpression;
import org.apache.cocoon.el.impl.jexl.JSIntrospector;
import org.apache.cocoon.el.objectmodel.ObjectModel;
import org.apache.commons.jexl.util.introspection.Info;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

/**
 *
 * @version $Id$
 */
public class JavaScriptExpression extends AbstractExpression {

    private Script script;
    private JSIntrospector introspector;
    private Scriptable rootScope;

    public JavaScriptExpression(String language, String expression, Scriptable rootScope) {
        super(language, expression);
        this.rootScope = rootScope;
        compile();
    }

    private void compile() {
        Context ctx = Context.enter();
        try {
            this.script = ctx.compileString(getExpression(), "", 1, null);
        } finally {
            Context.exit();
        }
    }

    public Object evaluate(ObjectModel objectModel) throws ExpressionException {
        Context ctx = Context.enter();
        try {
            Scriptable scope = ctx.newObject(getScope(rootScope));

            // Populate the scope
            Iterator iter = objectModel.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                Object value = entry.getValue();
                scope.put(key, scope, Context.toObject(value, scope));
            }

            Object result = this.script.exec(ctx, scope);
            return unwrap(result);
        } finally {
            Context.exit();
        }
    }

    public Iterator iterate(ObjectModel objectModel) throws ExpressionException {
        Object result = evaluate(objectModel);
        if (result == null) {
            return EMPTY_ITER;
        }

        if (this.introspector == null) {
            introspector = new JSIntrospector();
        }

        Iterator iter;
        try {
            iter = introspector.getIterator(result, new Info("Unknown", 0, 0));
        } catch (Exception e) {
            throw new ExpressionException("Couldn't get an iterator from expression " + getExpression(), e);
        }

        if (iter == null) {
            iter = EMPTY_ITER;
        }

        return iter;
    }

    public void assign(ObjectModel objectModel, Object value) throws ExpressionException {
        throw new UnsupportedOperationException("assignment not implemented for javascript expressions");
    }

    public Object getNode(ObjectModel objectModel) throws ExpressionException {
        return evaluate(objectModel);
    }

    private Scriptable getScope(Scriptable rootScope) {
        Scriptable scope;
        Context ctx = Context.enter();
        try {
            scope = ctx.newObject(rootScope);
            scope.setPrototype(rootScope);
            scope.setParentScope(null);
        } catch (Exception e) {
            throw new RuntimeException("Exception", e);
        } finally {
            Context.exit();
        }
        return scope;
    }
    
    /**
     * Unwrap a Rhino object (getting the raw java object) and convert undefined to null
     */
    private Object unwrap(Object obj) {
        if (obj instanceof Wrapper) {
            obj = ((Wrapper)obj).unwrap();
        } else if (obj == Undefined.instance) {
            obj = null;
        }
        return obj;
    }
}
