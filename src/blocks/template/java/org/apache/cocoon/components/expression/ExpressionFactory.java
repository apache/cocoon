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
package org.apache.cocoon.components.expression;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

public class ExpressionFactory
    extends AbstractLogEnabled
    implements Disposable, Serviceable, ThreadSafe {
    public static String ROLE = ExpressionFactory.class.getName();
    public static String DEFAULT ="default";

    /** The component manager */
    protected ServiceManager manager;

    /** The Expression compiler selector */
    protected ServiceSelector compilerSelector;

    public void service(final ServiceManager manager)
        throws ServiceException {
        this.manager = manager;

        this.compilerSelector =
            (ServiceSelector)this.manager.lookup(ExpressionCompiler.ROLE + "Selector");
    }

    public void dispose() {
        if(null != this.manager) {
            this.manager.release(this.compilerSelector);
            this.compilerSelector = null;
        }
    }

    public Expression getExpression(String language, String expression)
        throws ExpressionException {

        Expression expressionImpl = null;
        ExpressionCompiler compiler = null;
        try {
            compiler = (ExpressionCompiler)this.compilerSelector.select(language);
            expressionImpl = compiler.compile(language, expression);
        } catch(final ServiceException ce) {
            throw new ExpressionException("Can't find a compiler for " + language);
        } finally {
            this.compilerSelector.release(compiler);
        }
        return expressionImpl;
    }

    public Expression getExpression(String expression) throws ExpressionException {
        String language = DEFAULT;
        int end = expression.indexOf(':');
        if (end != -1) {
            language = expression.substring(0, end);
            expression = expression.substring(end+1);
        }
        return getExpression(language, expression);
    }
}
