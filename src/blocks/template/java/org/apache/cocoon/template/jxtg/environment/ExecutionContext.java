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
package org.apache.cocoon.template.jxtg.environment;

import java.util.Map;

import org.apache.cocoon.template.jxtg.expression.MyJexlContext;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Variables;

public class ExecutionContext {
    private MyJexlContext jexlContext;
    private JXPathContext jxpathContext;
    private Variables variables;
    private Map definitions;

    public ExecutionContext(MyJexlContext jexlContext,
            JXPathContext jxpathContext, Variables variables, Map definitions) {
        this.jexlContext = jexlContext;
        this.jxpathContext = jxpathContext;
        this.variables = variables;
        this.definitions = definitions;
    }

    public MyJexlContext getJexlContext() {
        return this.jexlContext;
    }

    public JXPathContext getJXPathContext() {
        return this.jxpathContext;
    }

    public Variables getVariables() {
        return this.variables;
    }

    public Map getDefinitions() {
        return this.definitions;
    }

    public ExecutionContext getChildContext(MyJexlContext jexlContext,
            JXPathContext jxpathContext) {
        return new ExecutionContext(jexlContext, jxpathContext, this.variables,
                this.definitions);
    }
}