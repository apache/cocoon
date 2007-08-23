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

import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.cocoon.el.Expression;
import org.apache.cocoon.el.ExpressionCompiler;
import org.apache.cocoon.el.ExpressionException;
import org.apache.cocoon.el.impl.jexl.JexlCompiler;
import org.apache.cocoon.el.impl.objectmodel.ObjectModelImpl;
import org.apache.cocoon.el.objectmodel.ObjectModel;

public class JexlTestCase extends TestCase {

    public void testExpression() throws ExpressionException {
        ExpressionCompiler compiler = new JexlCompiler();
        Expression expression = compiler.compile("jexl", "1+2");
        assertEquals(new Long(3), expression.evaluate(new ObjectModelImpl()));
    }

    public void testContextExpression() throws ExpressionException {
        ExpressionCompiler compiler = new JexlCompiler();
        ObjectModel objectModel = new ObjectModelImpl();
        objectModel.put("a", new Long(1));
        objectModel.put("b", new Long(2));
        Expression expression = compiler.compile("jexl", "a+b");
        assertEquals(new Long(3), expression.evaluate(objectModel));
    }

    public void testIterator() throws ExpressionException {
        ExpressionCompiler compiler = new JexlCompiler();
        ObjectModel objectModel = new ObjectModelImpl();
        String[] arr = {"foo"};
        objectModel.put("arr", arr);
        Expression expression = compiler.compile("jexl", "arr");
        Iterator iter = expression.iterate(objectModel);
        assertTrue("hasNext", iter.hasNext());
        assertEquals("foo", iter.next());
        assertFalse("hasNext", iter.hasNext());
    }
}
