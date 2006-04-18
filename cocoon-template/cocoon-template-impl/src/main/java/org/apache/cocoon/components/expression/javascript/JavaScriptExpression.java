/*
 * $Id$
 *
 * Created on 2005-10-17
 *
 * Copyright (c) 2005, MobileBox sp. z o.o.
 * All rights reserved.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.apache.cocoon.components.expression.javascript;

import java.util.Iterator;
import java.util.Map;
import java.io.StringReader;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.components.expression.AbstractExpression;
import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.components.expression.ExpressionException;
import org.apache.cocoon.components.expression.jexl.JSIntrospector;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.environment.TemplateObjectModelHelper;
import org.apache.commons.jexl.util.introspection.Info;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

public class JavaScriptExpression extends AbstractExpression {
    private Script script;
    private JSIntrospector introspector;

    public JavaScriptExpression(String language, String expression) {
        super(language, expression);
        compile();
    }

    private void compile() {
        Context ctx = Context.enter();
        try {
            // Note: used compileReader instead of compileString to work with the older Rhino in C2.1
            this.script = ctx.compileReader(TemplateObjectModelHelper.getScope(), new StringReader(getExpression()), "", 1, null);
        } catch (Exception e) {
            // Note: this catch block is only needed for the Rhino in C2.1 where the older
            //       Rhino does not throw RuntimeExceptions
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else{
                throw new CascadingRuntimeException("Runtime exception.", e);
            }
        } finally {
            Context.exit();
        }
    }

    public Object evaluate(ExpressionContext context) throws ExpressionException {
        Context ctx = Context.enter();
        try {
            Scriptable scope = ctx.newObject(TemplateObjectModelHelper.getScope());
            // Populate the scope
            Iterator iter = context.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String key = (String) entry.getKey();
                Object value = entry.getValue();
                scope.put(key, scope, Context.toObject(value, scope));
            }

            Object result = this.script.exec(ctx, scope);
            return FlowHelper.unwrap(result);
        } catch (Exception e) {
            // Note: this catch block is only needed for the Rhino in C2.1 where the older
            //       Rhino does not throw RuntimeExceptions
            if (e instanceof RuntimeException) {
                throw (RuntimeException)e;
            } else {
                throw new CascadingRuntimeException("Runtime exception", e);
            }
        } finally {
            Context.exit();
        }
    }

    public Iterator iterate(ExpressionContext context) throws ExpressionException {
        Object result = evaluate(context);
        if (result == null)
            return EMPTY_ITER;

        if (this.introspector == null)
            introspector = new JSIntrospector();

        Iterator iter = null;
        try {
            iter = introspector.getIterator(result, new Info("Unknown", 0, 0));
        } catch (Exception e) {
            throw new ExpressionException("Couldn't get an iterator from expression " + getExpression(), e);
        }

        if (iter == null)
            iter = EMPTY_ITER;
        return iter;
    }

    public void assign(ExpressionContext context, Object value) throws ExpressionException {
        throw new UnsupportedOperationException("assignment not implemented for javascript expressions");
    }

    public Object getNode(ExpressionContext context) throws ExpressionException {
        return evaluate(context);
    }
}
