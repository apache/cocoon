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

import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.commons.jxpath.Variables;

public class MyVariables implements Variables {
    MyVariables closure;
    Map localVariables = new HashMap();

    static final String[] VARIABLES = new String[] { "cocoon", "continuation",
            "flowContext", "request", "response", "context", "session",
            "parameters" };

    Object cocoon;

    // backward compatibility
    Object bean, kont, request, response, session, context, parameters;

    public MyVariables(Object cocoon, Object bean, WebContinuation kont,
            Object request, Object session, Object context, Object parameters) {
        this.cocoon = cocoon;
        this.bean = bean;
        this.kont = kont;
        this.request = request;
        this.session = session;
        this.context = context;
        this.parameters = parameters;
    }

    public MyVariables(MyVariables parent) {
        this.closure = parent;
    }

    public boolean isDeclaredVariable(String varName) {
        int len = VARIABLES.length;
        for (int i = 0; i < len; i++) {
            if (varName.equals(VARIABLES[i])) {
                return true;
            }
        }
        if (localVariables.containsKey(varName)) {
            return true;
        }
        if (closure != null) {
            return closure.isDeclaredVariable(varName);
        }
        return false;
    }

    public Object getVariable(String varName) {
        Object result = localVariables.get(varName);
        if (result != null) {
            return result;
        }
        if (closure != null) {
            return closure.getVariable(varName);
        }
        if (varName.equals("cocoon")) {
            return cocoon;
        }
        // backward compatibility
        if (varName.equals("continuation")) {
            return kont;
        } else if (varName.equals("flowContext")) {
            return bean;
        } else if (varName.equals("request")) {
            return request;
        } else if (varName.equals("session")) {
            return session;
        } else if (varName.equals("context")) {
            return context;
        } else if (varName.equals("parameters")) {
            return parameters;
        }
        return null;
    }

    public void declareVariable(String varName, Object value) {
        localVariables.put(varName, value);
    }

    public void undeclareVariable(String varName) {
        localVariables.remove(varName);
    }
}