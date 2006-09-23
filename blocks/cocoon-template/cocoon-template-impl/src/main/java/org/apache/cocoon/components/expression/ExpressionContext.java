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

import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.xml.NamespacesTable;

/**
 * @version $Id$
 */
public class ExpressionContext extends HashMap {

    private ExpressionContext closure;
    private Object contextBean;
    private NamespacesTable namespaces;

    public ExpressionContext() {
        this(null);
    }

    public ExpressionContext(ExpressionContext closure) {
        this.closure = closure;
        if (closure == null) {
            this.namespaces = new NamespacesTable();
        } else {
            // Reuse the parent one. Users of the context should correctly enter and leave namespace scopes.
            this.namespaces = closure.namespaces;
        }
    }
    
    /**
     * Get the namespace table that tracks the applicable namespace prefix mappings
     * for the expression context.
     * 
     * @return the namespaces table
     */
    public NamespacesTable getNamespaces() {
        return this.namespaces;
    }

    public Object getContextBean() {
        if (contextBean != null)
            return contextBean;
        else if (closure != null)
            return closure.getContextBean();
        else
            return null;
    }

    public void setContextBean(Object contextBean) {
        this.contextBean = contextBean;
    }

    public Map getVars() {
        return this;
    }

    public void setVars(Map map) {
        clear();
        putAll(map);
    }

    public boolean containsKey(Object key) {
        return this.get(key) != null;
    }

    public Object get(Object key) {
        if (key.equals("this")) {
            return this;
        }
        Object result = super.get(key);
        if (result == null && closure != null) {
            result = closure.get(key);
        }
        return result;
    }
}
