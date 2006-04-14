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
package org.apache.cocoon.environment;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.SitemapComponentTestCase;
import org.apache.cocoon.components.expression.Expression;
import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.components.expression.ExpressionException;
import org.apache.cocoon.components.expression.ExpressionFactory;
import org.apache.cocoon.template.environment.FlowObjectModelHelper;

public class FOMTestCase extends SitemapComponentTestCase {

    public void testFOMJexl() throws ExpressionException, ServiceException {
        ExpressionFactory factory = (ExpressionFactory)this.lookup(ExpressionFactory.ROLE);
        Parameters parameters = new Parameters();
        parameters.setParameter("test", "foo");
        ExpressionContext fomContext =
            FlowObjectModelHelper.getFOMExpressionContext(getObjectModel(), parameters);

        Expression expression = factory.getExpression("jexl", "cocoon.parameters.test");
        assertEquals("foo", expression.evaluate(fomContext));

        expression = factory.getExpression("jexl", "cocoon.request.protocol");
        assertEquals("HTTP/1.1", expression.evaluate(fomContext));
        this.release(factory);
    }

    public void testFOMJXPath() throws ExpressionException, ServiceException {
        ExpressionFactory factory = (ExpressionFactory)this.lookup(ExpressionFactory.ROLE);
        Parameters parameters = new Parameters();
        parameters.setParameter("test", "foo");
        ExpressionContext fomContext =
            FlowObjectModelHelper.getFOMExpressionContext(getObjectModel(), parameters);

        Expression expression = factory.getExpression("jxpath", "$cocoon/parameters/test");
        assertEquals("foo", expression.evaluate(fomContext));

        expression = factory.getExpression("jxpath", "$cocoon/request/protocol");
        assertEquals("HTTP/1.1", expression.evaluate(fomContext));
        this.release(factory);
    }
}

