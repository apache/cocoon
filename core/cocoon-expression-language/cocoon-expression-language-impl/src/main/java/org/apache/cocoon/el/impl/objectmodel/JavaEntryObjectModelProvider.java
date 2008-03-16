/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.el.impl.objectmodel;

import org.apache.cocoon.el.objectmodel.ObjectModelProvider;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * Adds java packages to {@link org.apache.cocoon.el.objectmodel.ObjectModel ObjectModel}. Allows to construct java objects.
 *
 * @see PackagesEntryObjectModelProvider
 */
public class JavaEntryObjectModelProvider implements ObjectModelProvider {

    private Scriptable rootScope;

    //FIXME: This method is duplicated in PackagesEntryObjectModelProvider
    private Scriptable getScope() {
        Context ctx = Context.enter();
        try {
            // Create it if never used up to now
            if (rootScope == null) {
                rootScope = ctx.initStandardObjects(null);
            }
            try {
                Scriptable scope = ctx.newObject(rootScope);
                scope.setPrototype(rootScope);
                scope.setParentScope(null);
                return scope;
            } catch (Exception e) {
                throw new RuntimeException("Exception", e);
            }
        } finally {
            Context.exit();
        }
    }

    public Object getObject() {
        Object javaPackage;
        Context.enter();
        try {
           javaPackage = ScriptableObject.getProperty( getScope(), "java" );
        } finally {
            Context.exit();
        }
        return javaPackage;
    }

    public Scriptable getRootScope() {
        return rootScope;
    }

    public void setRootScope(Scriptable rootScope) {
        this.rootScope = rootScope;
    }

}
