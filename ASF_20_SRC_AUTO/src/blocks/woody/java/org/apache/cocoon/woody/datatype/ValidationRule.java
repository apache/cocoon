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
package org.apache.cocoon.woody.datatype;

import org.outerj.expression.ExpressionContext;

/**
 * Interface for validation rules. Most {@link Datatype} implementations will
 * perform their validation by checking a number of these validation rules
 * (though strictly spoken this is not required).
 * 
 * @version $Id: ValidationRule.java,v 1.5 2004/03/05 13:02:28 bdelacretaz Exp $
 */
public interface ValidationRule {
    /**
     *
     * @param value a value of a class supported by the ValidationRule implementation
     * @param expressionContext many validation rules use the xReporter expression interpreter,
     * the expressionContext allows to resolve variables used in these expressions.
     */
    ValidationError validate(Object value, ExpressionContext expressionContext);

    /**
     * Returns true if this ValidationRule supports validating objects of the same class
     * as the one specified. If the flag 'arrayType' is true, this method will return true
     * if this validation rule can validate arrays of these objects (i.e. the object passed
     * to the validate method will then be an array).
     */
    boolean supportsType(Class clazz, boolean arrayType);
}
