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

package org.apache.cocoon.forms.expression;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.iterators.EntrySetMapIterator;
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
 * @version $Id$
 */
public class DefaultExpressionManager
        implements ExpressionManager {
// FIXME: Component is there to allow this block to also run in the 2.1 branch

    private DefaultFunctionFactory factory = new DefaultFunctionFactory();

    public Expression parse(String expressionString) throws ParseException, ExpressionException {
        FormulaParser parser = new FormulaParser(new java.io.StringReader(expressionString), factory);
        parser.parse();

        Expression expression = parser.getExpression();
        expression.check();

        return expression;
    }

    public List parseVariables(String expressionString) throws ParseException, ExpressionException {
        FormulaParser parser = new FormulaParser(new java.io.StringReader(expressionString), factory);
        parser.parse();
        return parser.getVariables();
    }

    public void setFunctions( Map functions )
    {
        for (final Iterator i = functions.entrySet().iterator(); i.hasNext();) {
            final Map.Entry entry = (Map.Entry)i.next();
            String name = (String)entry.getKey();
            Class clazz = ((Expression)entry.getValue()).getClass();
            factory.registerFunction(name, clazz);
        }
    }

}
