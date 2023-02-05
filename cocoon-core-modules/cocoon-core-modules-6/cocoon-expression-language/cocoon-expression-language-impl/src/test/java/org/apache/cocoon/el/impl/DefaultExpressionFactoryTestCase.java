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
package org.apache.cocoon.el.impl;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.cocoon.el.Expression;
import org.apache.cocoon.el.ExpressionException;
import org.apache.cocoon.el.impl.DefaultExpressionFactory;
import org.apache.cocoon.el.impl.javascript.JavaScriptCompiler;
import org.apache.cocoon.el.impl.jexl.JexlCompiler;
import org.apache.cocoon.el.impl.jxpath.JXPathCompiler;
import org.apache.cocoon.el.impl.objectmodel.ObjectModelImpl;

public class DefaultExpressionFactoryTestCase extends TestCase {
    
    private DefaultExpressionFactory expressionFactory;

    /*public void testContext() {
        ObjectModel parentContext = new ObjectModelImpl();
        parentContext.put("var1", "foo");
        parentContext.put("var2", "bar");

        ObjectModel objectModel = new ObjectModel(parentContext);
        objectModel.put("var1", "zonk");

        assertEquals("foo", parentContext.get("var1"));
        assertEquals("bar", parentContext.get("var2"));
        assertEquals("zonk", objectModel.get("var1"));
        assertEquals("bar", objectModel.get("var2"));
    }

    public void testContextBean() {
        ObjectModel parentContext = new ObjectModel();
        parentContext.setContextBean("foo");

        ObjectModel objectModel = new ObjectModel(parentContext);
        objectModel.setContextBean("bar");

        assertEquals("foo", parentContext.getContextBean());
        assertEquals("bar", objectModel.getContextBean());
    }*/
    
    protected void setUp() throws Exception {
        super.setUp();
        expressionFactory = new DefaultExpressionFactory();
        Map expressionCompilers = new HashMap();
        expressionCompilers.put("js", new JavaScriptCompiler());
        expressionCompilers.put("jexl", new JexlCompiler());
        JXPathCompiler jXPathCompiler = new JXPathCompiler();
        expressionCompilers.put("jxpath", jXPathCompiler);
        expressionCompilers.put("default", jXPathCompiler);
        expressionFactory.setExpressionCompilers(expressionCompilers);
    }

    public void testFactoryJexl() throws ExpressionException {
        assertNotNull("Test lookup of expression factory", expressionFactory);

        Expression expression = expressionFactory.getExpression("jexl", "1+2");
        assertNotNull("Test expression compilation", expression);

        assertEquals(new Long(3), expression.evaluate(new ObjectModelImpl()));
    }

    public void testFactoryJXPath() throws ExpressionException {
        assertNotNull("Test lookup of expression expressionFactory", expressionFactory);

        Expression expression = expressionFactory.getExpression("jxpath", "1+2");
        assertNotNull("Test expression compilation", expression);

        assertEquals(new Double(3), expression.evaluate(new ObjectModelImpl()));
    }

    public void testFactoryPluggable() throws ExpressionException {
        assertNotNull("Test lookup of expression expressionFactory", expressionFactory);

        Expression expression = expressionFactory.getExpression("1+2");
        assertNotNull("Test expression compilation", expression);
        assertEquals(new Double(3), expression.evaluate(new ObjectModelImpl()));

        expression = expressionFactory.getExpression("jexl:1+2");
        assertNotNull("Test expression compilation", expression);
        assertEquals(new Long(3), expression.evaluate(new ObjectModelImpl()));

        expression = expressionFactory.getExpression("jxpath:1+2");
        assertNotNull("Test expression compilation", expression);
        assertEquals(new Double(3), expression.evaluate(new ObjectModelImpl()));
    }
}

