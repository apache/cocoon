/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.cocoon.el.jexl;

import org.apache.avalon.framework.logger.ConsoleLogger;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.core.container.ContainerTestCase;
import org.apache.cocoon.el.Context;
import org.apache.cocoon.el.DefaultContext;
import org.apache.cocoon.el.Expression;

public class JexlExpressionTestCase extends ContainerTestCase {
    Logger logger = new ConsoleLogger(ConsoleLogger.LEVEL_WARN);

    public Logger getLogger() {
        return logger;
    }

    JexlExpressionCompiler compiler;
    Context context;

    public void setUp() throws Exception {
        super.setUp();
        compiler = new JexlExpressionCompiler();
        context = new DefaultContext();
    }

    public void testEvaluate() {
        Expression expression = compiler.compile("1 + 1");
        assertEquals(2, expression.toInt(context));
        assertEquals("2", expression.toString(context));
    }

    public void testEvaluateVariables() {
        context.getVariables().put("a", new Integer(1));
        context.getVariables().put("b", new Integer(2));
        Expression expression = compiler.compile("a + b");
        assertEquals(3, expression.toInt(context));
    }

    public void testToCharArrayNull() {
        context.getVariables().put("a", null);
        Expression expression = compiler.compile("a");
        assertEquals(0, expression.toCharArray(context).length);
    }
}

