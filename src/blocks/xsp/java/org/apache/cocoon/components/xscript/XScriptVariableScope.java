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
package org.apache.cocoon.components.xscript;

import java.util.HashMap;

/**
 * <code>XScriptVariableScope</code> maintains variables in a given
 * scope. A variable has a unique name within a scope, but multiple
 * variables with the same name can exist within different scopes.
 *
 * @author <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
 * @version CVS $Id: XScriptVariableScope.java,v 1.1 2004/03/10 12:58:08 stephan Exp $
 * @since August 4, 2001
 */
public class XScriptVariableScope {
    /**
     * The variables store; each entry is <code>String</code>
     * representing the name of the variable, with the corresponding
     * value an {@link XScriptObject}.
     */
    HashMap variables = new HashMap();

    /**
     * Define a new variable or overwrite the value of an existing
     * variable in this scope.
     *
     * @param name a <code>String</code> value
     * @param value a <code>{@link XScriptObject}</code> value
     */
    public synchronized void put(String name, XScriptObject value) {
        variables.put(name, value);
    }

    /**
     * Obtains the value of the XScript <code>name</code> variable.
     *
     * @param name a <code>String</code> value
     * @return a <code>{@link XScriptObject}</code> value
     */
    public synchronized XScriptObject get(String name) {
        return (XScriptObject) variables.get(name);
    }

    /**
     * Removes the XScript variable that's accessible via
     * <code>name</code>.
     *
     * @param name a <code>String</code> value
     */
    public synchronized XScriptObject remove(String name) {
        return (XScriptObject) variables.remove(name);
    }

    public synchronized boolean defines(String name) {
        return variables.containsKey(name);
    }
}
