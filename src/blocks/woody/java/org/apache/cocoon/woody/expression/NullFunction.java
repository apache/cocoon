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

/**
 * Returns null constant.
 *  
 * @author <a href="mailto:vadim.gritsenko@dc.gov">Vadim Gritsenko</a>
 * @version CVS $Id: NullFunction.java,v 1.5 2004/03/09 13:54:24 reinhard Exp $
 */
public class NullFunction extends AbstractExpression {

    public Object evaluate(ExpressionContext context) throws ExpressionException {
        return null;
    }

    public Class getResultType() {
        return Object.class;
    }

    public String getDescription() {
        return "Null function";
    }
}
