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
package org.apache.cocoon.el;

import org.apache.cocoon.el.jexl.JexlExpressionCompiler;

public class GenericExpressionCompiler implements ExpressionCompiler {
    public static final String JEXL_PREFIX = "jexl:";

    static GenericExpressionCompiler instance;

    ExpressionCompiler jexlCompiler = new JexlExpressionCompiler();

    ExpressionCompiler defaultCompiler = jexlCompiler;

    public static GenericExpressionCompiler getInstance() {
        if (instance == null)
            instance = new GenericExpressionCompiler();
        return instance;
    }

    public Expression compile(String expression) {
        // Should an exception be thrown here if prefix is invalid?
        // The check might be costly.

        if (expression.startsWith(JEXL_PREFIX))
            return jexlCompiler.compile(expression.substring(JEXL_PREFIX
                    .length()));
        else
            return defaultCompiler.compile(expression);
    }
}