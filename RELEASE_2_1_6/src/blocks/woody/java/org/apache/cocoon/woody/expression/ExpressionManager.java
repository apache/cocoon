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

import org.outerj.expression.Expression;
import org.outerj.expression.ParseException;
import org.outerj.expression.ExpressionException;

/**
 * Work interface for the component that creates Expression objects.
 * The reason for centralising the creation of expressions is so that
 * new functions can be registered in one place.
 * 
 * @version $Id: ExpressionManager.java,v 1.6 2004/03/09 13:54:24 reinhard Exp $
 */
public interface ExpressionManager {
    
    String ROLE = ExpressionManager.class.getName();
    
    Expression parse(String expression) throws ParseException, ExpressionException;
}
