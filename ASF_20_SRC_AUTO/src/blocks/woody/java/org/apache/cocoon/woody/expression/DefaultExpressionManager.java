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

package org.apache.cocoon.woody.expression;

import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.outerj.expression.DefaultFunctionFactory;
import org.outerj.expression.Expression;
import org.outerj.expression.ExpressionException;
import org.outerj.expression.FormulaParser;
import org.outerj.expression.ParseException;

/**
 * Implementation of the {@link ExpressionManager} role.
 *
 * Custom functions can be added using configuration elements:
 * <pre>
 *   &lt;function name="MyFunction" class="net.foo.MyFunction"/&gt;
 * </pre>
 * 
 * @version CVS $Id: DefaultExpressionManager.java,v 1.4 2004/03/05 13:02:31 bdelacretaz Exp $
 */
public class DefaultExpressionManager
        implements ExpressionManager, Component, Configurable, ThreadSafe {
    
    private DefaultFunctionFactory factory;
    
    public void configure(Configuration config) throws ConfigurationException {
        factory = new DefaultFunctionFactory();
        
        Configuration[] functions = config.getChildren("function");
        for (int i = 0; i < functions.length; i++) {
            String name = functions[i].getAttribute("name");
            String clazz = functions[i].getAttribute("class");
            try {
                factory.registerFunction(name, Class.forName(clazz));
            } catch (ClassNotFoundException e) {
                throw new ConfigurationException("Can not find class " + clazz + " for function " + name + ": " + e);
            }
        }
    }
    
    public Expression parse(String expressionString) throws ParseException, ExpressionException {
        
        FormulaParser parser = new FormulaParser(new java.io.StringReader(expressionString), factory);
        parser.sum();

        Expression expression = parser.getExpression();
        expression.check();

        return expression;
    }
}
