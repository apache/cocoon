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

import org.outerj.expression.AbstractExpression;
import org.outerj.expression.ExpressionContext;
import org.outerj.expression.ExpressionException;
import org.outerj.expression.Expression;

/**
 * Converts argument to the string. Useful when concatenating non-string
 * values, such as numbers.
 *
 * @author <a href="http://cocoon.apache.org/">The Apache Cocoon Team</a>
 * @version CVS $Id: StringFunction.java,v 1.4 2004/03/09 13:54:24 reinhard Exp $
 */
public class StringFunction extends AbstractExpression {

    public Object evaluate(ExpressionContext context) throws ExpressionException {
        return String.valueOf(((Expression)arguments.get(0)).evaluate(context));
    }

    public Class getResultType() {
        return String.class;
    }

    public String getDescription() {
        return "String function";
    }
}
