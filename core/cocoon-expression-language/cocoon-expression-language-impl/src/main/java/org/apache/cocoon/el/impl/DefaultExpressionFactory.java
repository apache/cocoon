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

import java.util.Map;

import org.apache.cocoon.el.Expression;
import org.apache.cocoon.el.ExpressionCompiler;
import org.apache.cocoon.el.ExpressionException;
import org.apache.cocoon.el.ExpressionFactory;

/**
 * @version $Id$
 */
public class DefaultExpressionFactory implements ExpressionFactory {

    public static final String DEFAULT_EXPRESSION_LANGUAGE = "default";
    
    protected Map expressionCompilers;

    public Expression getExpression(String language, String expression) throws ExpressionException {
        if (!this.expressionCompilers.containsKey(language))
            throw new ExpressionException("Can't find a compiler for " + language);
        ExpressionCompiler compiler = (ExpressionCompiler) this.expressionCompilers.get(language);
        return compiler.compile(language, expression);
    }

    public Expression getExpression(String expression) throws ExpressionException {
        String language = DEFAULT_EXPRESSION_LANGUAGE;
        int end = expression.indexOf(':');
        if (end != -1) {
            language = expression.substring(0, end);
            expression = expression.substring(end + 1);
        }
        return getExpression(language, expression);
    }

    public Map getExpressionCompilers() {
        return expressionCompilers;
    }

    public void setExpressionCompilers(Map expressionCompilers) {
        this.expressionCompilers = expressionCompilers;
    }
}
