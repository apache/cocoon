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
package org.apache.cocoon.template.jxtg.expression;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.jexl.JexlContext;

public class MyJexlContext extends HashMap implements JexlContext {
    private MyJexlContext closure;

    public MyJexlContext() {
        this(null);
    }

    public MyJexlContext(MyJexlContext closure) {
        this.closure = closure;
    }

    public Map getVars() {
        return this;
    }

    public void setVars(Map map) {
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