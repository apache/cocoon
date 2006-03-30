/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import junit.framework.TestCase;
import org.apache.cocoon.components.expression.Expression;
import org.apache.cocoon.components.expression.ExpressionCompiler;
import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.components.expression.ExpressionException;

public class JexlTestCase extends TestCase {

    public void testExpression() throws ExpressionException {
        ExpressionCompiler compiler = new JexlCompiler();
        Expression expression = compiler.compile("jexl", "1+2");
        assertEquals(new Long(3), expression.evaluate(new ExpressionContext()));
    }

    public void testContextExpression() throws ExpressionException {
        ExpressionCompiler compiler = new JexlCompiler();
        ExpressionContext context = new ExpressionContext();
        context.put("a", new Long(1));
        context.put("b", new Long(2));
        Expression expression = compiler.compile("jexl", "a+b");
        assertEquals(new Long(3), expression.evaluate(context));
    }

    public void testIterator() throws ExpressionException {
        ExpressionCompiler compiler = new JexlCompiler();
        ExpressionContext context = new ExpressionContext();
        String[] arr = {"foo"};
        context.put("arr", arr);
        Expression expression = compiler.compile("jexl", "arr");
        Iterator iter = expression.iterate(context);
        assertTrue("hasNext", iter.hasNext());
        assertEquals("foo", iter.next());
        assertFalse("hasNext", iter.hasNext());
    }
}
