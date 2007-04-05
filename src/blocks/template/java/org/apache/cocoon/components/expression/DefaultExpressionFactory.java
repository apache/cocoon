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
package org.apache.cocoon.components.expression;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * @version $Id$
 */
public class DefaultExpressionFactory
    extends AbstractLogEnabled
    implements Disposable, Serviceable, ThreadSafe, ExpressionFactory {

    public static final String DEFAULT_EXPRESSION_LANGUAGE = "default";

    /** The component manager */
    protected ServiceManager manager;

    /** The Expression compiler selector */
    protected ServiceSelector compilerSelector;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(final ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.compilerSelector = (ServiceSelector) this.manager.lookup(ExpressionCompiler.ROLE + "Selector");
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (null != this.manager) {
            this.manager.release(this.compilerSelector);
            this.compilerSelector = null;
            this.manager = null;
        }
    }

    public Expression getExpression(String language, String expression) throws ExpressionException {

        Expression expressionImpl = null;
        ExpressionCompiler compiler = null;
        try {
            compiler = (ExpressionCompiler) this.compilerSelector.select(language);
            expressionImpl = compiler.compile(language, expression);
        } catch (final ServiceException ce) {
            throw new ExpressionException("Can't find a compiler for " + language);
        } finally {
            this.compilerSelector.release(compiler);
        }
        return expressionImpl;
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
}
