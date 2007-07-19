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
import org.apache.cocoon.objectmodel.ObjectModelImpl;

public class ExpressionTestCase extends CocoonTestCase {

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

    public void testFactoryJexl() throws ExpressionException {
        ExpressionFactory factory = (ExpressionFactory) this.getBeanFactory().getBean(ExpressionFactory.ROLE);
        assertNotNull("Test lookup of expression factory", factory);

        Expression expression = factory.getExpression("jexl", "1+2");
        assertNotNull("Test expression compilation", expression);

        assertEquals(new Long(3), expression.evaluate(new ObjectModelImpl()));
    }

    public void testFactoryJXPath() throws ExpressionException {
        ExpressionFactory factory = (ExpressionFactory) this.getBeanFactory().getBean(ExpressionFactory.ROLE);
        assertNotNull("Test lookup of expression factory", factory);

        Expression expression = factory.getExpression("jxpath", "1+2");
        assertNotNull("Test expression compilation", expression);

        assertEquals(new Double(3), expression.evaluate(new ObjectModelImpl()));
    }

    public void testFactoryPluggable() throws ExpressionException {
        ExpressionFactory factory = (ExpressionFactory) this.getBeanFactory().getBean(ExpressionFactory.ROLE);
        assertNotNull("Test lookup of expression factory", factory);

        Expression expression = factory.getExpression("1+2");
        assertNotNull("Test expression compilation", expression);
        assertEquals(new Double(3), expression.evaluate(new ObjectModelImpl()));

        expression = factory.getExpression("jexl:1+2");
        assertNotNull("Test expression compilation", expression);
        assertEquals(new Long(3), expression.evaluate(new ObjectModelImpl()));

        expression = factory.getExpression("jxpath:1+2");
        assertNotNull("Test expression compilation", expression);
        assertEquals(new Double(3), expression.evaluate(new ObjectModelImpl()));
    }
}

