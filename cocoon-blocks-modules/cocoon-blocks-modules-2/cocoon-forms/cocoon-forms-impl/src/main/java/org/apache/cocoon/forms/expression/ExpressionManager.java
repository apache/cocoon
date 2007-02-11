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

import java.util.List;

import org.outerj.expression.Expression;
import org.outerj.expression.ParseException;
import org.outerj.expression.ExpressionException;

/**
 * Work interface for the component that creates Expression objects.
 * The reason for centralising the creation of expressions is so that
 * new functions can be registered in one place.
 * 
 * @version $Id$
 */
public interface ExpressionManager {
    
    String ROLE = ExpressionManager.class.getName();
    
    /**
     * Parse the given expression.
     * @param expression The string containing the expression to parse.
     * @return The Expression object resulting from parse.
     * @throws ParseException If something goes wrong while parsing.
     * @throws ExpressionException If the expression has been parsed successfully but is invalid.
     */
    Expression parse(String expression) throws ParseException, ExpressionException;
    
    /**
     * Parse the given expression to extract variables.
     * @param expressionString The string containing the expression to parse.
     * @return A {@link List} of {@link org.outerj.expression.VariableFunction}, one for each variable used in the expression.
     * @see org.outerj.expression.VariableFunction#getVariableName()
     * @throws ParseException If something goes wrong while parsing.
     * @throws ExpressionException If the expression has been parsed successfully but is invalid.
     */
    List parseVariables(String expressionString) throws ParseException, ExpressionException;
}
