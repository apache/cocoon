/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.components.expression;

import java.util.Iterator;

/**
 * @version SVN $Id$
 */
public interface Expression {
    public Object evaluate(ExpressionContext context)
            throws ExpressionException;

    public Iterator iterate(ExpressionContext context)
            throws ExpressionException;

    public void assign(ExpressionContext context, Object value)
            throws ExpressionException;

    public String getExpression();

    public String getLanguage();

    /*
     * This method is added to handle that JXPath have two access methods
     * getValue and getNode, where getNode gives direct access to the object
     * while getValue might do some conversion of the object. I would prefer to
     * get rid of the getNode method, but have not yet figured out how to get
     * work in JXTG
     */
    public Object getNode(ExpressionContext context) throws ExpressionException;

    public void setProperty(String property, Object value);
}
