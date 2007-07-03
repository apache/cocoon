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
package org.apache.cocoon.components.expression;

import org.apache.cocoon.CocoonTestCase;

public class ExpressionTestCase extends CocoonTestCase {

    public void testContext() {
        ExpressionContext parentContext = new ExpressionContext();
        parentContext.put("var1", "foo");
        parentContext.put("var2", "bar");

        ExpressionContext context = new ExpressionContext(parentContext);
        context.put("var1", "zonk");

        assertEquals("foo", parentContext.get("var1"));
        assertEquals("bar", parentContext.get("var2"));
        assertEquals("zonk", context.get("var1"));
        assertEquals("bar", context.get("var2"));
    }

    public void testContextBean() {
        ExpressionContext parentContext = new ExpressionContext();
        parentContext.setContextBean("foo");

        ExpressionContext context = new ExpressionContext(parentContext);
        context.setContextBean("bar");

        assertEquals("foo", parentContext.getContextBean());
        assertEquals("bar", context.getContextBean());
    }

    public void testFactoryJexl() throws ExpressionException {
        ExpressionFactory factory = (ExpressionFactory) this.getBeanFactory().getBean(ExpressionFactory.ROLE);
        assertNotNull("Test lookup of expression factory", factory);

        Expression expression = factory.getExpression("jexl", "1+2");
        assertNotNull("Test expression compilation", expression);

        assertEquals(new Long(3), expression.evaluate(new ExpressionContext()));
    }

    public void testFactoryJXPath() throws ExpressionException {
        ExpressionFactory factory = (ExpressionFactory) this.getBeanFactory().getBean(ExpressionFactory.ROLE);
        assertNotNull("Test lookup of expression factory", factory);

        Expression expression = factory.getExpression("jxpath", "1+2");
        assertNotNull("Test expression compilation", expression);

        assertEquals(new Double(3), expression.evaluate(new ExpressionContext()));
    }

    public void testFactoryPluggable() throws ExpressionException {
        ExpressionFactory factory = (ExpressionFactory) this.getBeanFactory().getBean(ExpressionFactory.ROLE);
        assertNotNull("Test lookup of expression factory", factory);

        Expression expression = factory.getExpression("1+2");
        assertNotNull("Test expression compilation", expression);
        assertEquals(new Double(3), expression.evaluate(new ExpressionContext()));

        expression = factory.getExpression("jexl:1+2");
        assertNotNull("Test expression compilation", expression);
        assertEquals(new Long(3), expression.evaluate(new ExpressionContext()));

        expression = factory.getExpression("jxpath:1+2");
        assertNotNull("Test expression compilation", expression);
        assertEquals(new Double(3), expression.evaluate(new ExpressionContext()));
    }
}

