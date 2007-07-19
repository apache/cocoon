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
package org.apache.cocoon.environment;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.expression.Expression;
import org.apache.cocoon.components.expression.ExpressionException;
import org.apache.cocoon.components.expression.ExpressionFactory;
import org.apache.cocoon.core.container.ContainerTestCase;
import org.apache.cocoon.objectmodel.ObjectModel;
import org.apache.cocoon.template.environment.FlowObjectModelHelper;

public class FOMTestCase extends ContainerTestCase {

    public void testFOMJexl() throws ExpressionException {
        ExpressionFactory factory = (ExpressionFactory) this.getBeanFactory().getBean(ExpressionFactory.ROLE);
        Parameters parameters = new Parameters();
        parameters.setParameter("test", "foo");
        ObjectModel objectModel =
            FlowObjectModelHelper.getNewObjectModelWithFOM(getObjectModel(), parameters);

        Expression expression = factory.getExpression("jexl", "cocoon.parameters.test");
        assertEquals("foo", expression.evaluate(objectModel));

        expression = factory.getExpression("jexl", "cocoon.request.protocol");
        assertEquals("HTTP/1.1", expression.evaluate(objectModel));
    }

    public void testFOMJXPath() throws ExpressionException {
        ExpressionFactory factory = (ExpressionFactory) this.getBeanFactory().getBean(ExpressionFactory.ROLE);
        Parameters parameters = new Parameters();
        parameters.setParameter("test", "foo");
        ObjectModel objectModel =
            FlowObjectModelHelper.getNewObjectModelWithFOM(getObjectModel(), parameters);

        Expression expression = factory.getExpression("jxpath", "$cocoon/parameters/test");
        assertEquals("foo", expression.evaluate(objectModel));

        expression = factory.getExpression("jxpath", "$cocoon/request/protocol");
        assertEquals("HTTP/1.1", expression.evaluate(objectModel));
    }
    
}

