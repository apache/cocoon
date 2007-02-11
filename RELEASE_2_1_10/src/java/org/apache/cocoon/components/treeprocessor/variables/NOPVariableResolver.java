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
package org.apache.cocoon.components.treeprocessor.variables;

import org.apache.cocoon.components.treeprocessor.InvokeContext;

import java.util.Map;

/**
 * No-op implementation of {@link VariableResolver} for constant expressions
 *
 * @version CVS $Id$
 */
public class NOPVariableResolver extends VariableResolver {

    private String expression = null;

    public NOPVariableResolver(String expression) {
        super(expression);
        if (expression != null) {
            this.expression = VariableResolverFactory.unescape(expression);
        }
    }

    public final String resolve(InvokeContext context, Map objectModel) {
        return this.expression;
    }
    
    public final void release() {
        // Nothing to do
    }
}
