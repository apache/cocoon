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
package org.apache.cocoon.forms.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.javascript.fom.FOM_JavaScriptFlowHelper;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.Element;

/**
 * Helper methods to use JavaScript in various locations of the Cocoon Forms configuration files
 * such as event listeners and bindings.
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: JavaScriptHelper.java,v 1.4 2004/04/29 08:46:19 cziegeler Exp $
 */
public class JavaScriptHelper {

    /**
     * A shared root scope, avoiding to recreate a new one each time.
     */
    private static Scriptable _rootScope = null;

    /**
     * Build a script with the content of a DOM element.
     * 
     * @param element the element containing the script
     * @return the compiled script
     * @throws IOException
     */
    public static Script buildScript(Element element) throws IOException {
        String jsText = DomHelper.getElementText(element);
        String sourceName = DomHelper.getSystemIdLocation(element);

        Context ctx = Context.enter();
        Script script;
        try {
            script = ctx.compileReader(
                getRootScope(), //scope
                new StringReader(jsText), // in
                sourceName == null ? "<unknown>" : sourceName, // sourceName
                DomHelper.getLineLocation(element), // lineNo
                null // securityDomain
             );
        } finally {
            Context.exit();
        }
        return script;
    }

    /**
     * Build a function with the content of a DOM element.
     * 
     * @param element the element containing the function body
     * @param argumentNames names of the function arguments
     * @return the compiled function
     * @throws IOException
     */
    public static Function buildFunction(Element element, String[] argumentNames) throws IOException {
        // Enclose the script text with a function declaration
        StringBuffer buffer = new StringBuffer("function foo(");
        for (int i = 0; i < argumentNames.length; i++) {
            if (i > 0) {
                buffer.append(',');
            }
            buffer.append(argumentNames[i]);
        }
        buffer.append(") {\n").append(DomHelper.getElementText(element)).append("\n}");
        
        String jsText = buffer.toString();
        String sourceName = DomHelper.getSystemIdLocation(element);

        Context ctx = Context.enter();
        Function func;
        try {
            func = ctx.compileFunction(
                getRootScope(), //scope
                jsText, // in
                sourceName == null ? "<unknown>" : sourceName, // sourceName
                DomHelper.getLineLocation(element) - 1, // lineNo, "-1" because we added "function..."
                null // securityDomain
             );
        } finally {
            Context.exit();
        }
        return func;
    }

    /**
     * Get a root scope for building child scopes.
     * 
     * @return an appropriate root scope
     */
    public static Scriptable getRootScope() {
        if (_rootScope == null) {
            // Create it if never used up to now
            Context ctx = Context.enter();
            try {
                _rootScope = ctx.initStandardObjects(null);
            } finally {
                Context.exit();
            }
        }
        return _rootScope;
    }

    /**
     * Get a parent scope for building a child scope. The request is searched for an existing scope
     * that can be provided by a flowscript higher in the call stack, giving visibility to flowscript
     * functions and global (session) variables.
     * 
     * @param objectModel the object model where the flowscript scope will be searched (can be <code>null</code>).
     * @return an appropriate parent scope.
     */
    public static Scriptable getParentScope(Map objectModel) {
        // Try to get the flowscript scope
        Scriptable parentScope = null;
        if (objectModel != null) {
            parentScope = FOM_JavaScriptFlowHelper.getFOM_FlowScope(objectModel);
        }

        if (parentScope != null) {
            return parentScope;
        } else {
            return getRootScope();
        }
    }

    public static Object execScript(Script script, Map values, Map objectModel) throws JavaScriptException {
        Context ctx = Context.enter();
        try {
            Scriptable parentScope = getParentScope(objectModel);

            // Create a new local scope
            Scriptable scope;
            try {
                scope = ctx.newObject(parentScope);
            } catch (Exception e) {
                // Should normally not happen
                throw new CascadingRuntimeException("Cannont create script scope", e);
            }
            scope.setParentScope(parentScope);

            // Populate the scope
            Iterator iter = values.entrySet().iterator();
            while(iter.hasNext()) {
                Map.Entry entry = (Map.Entry)iter.next();
                String key = (String)entry.getKey();
                Object value = entry.getValue();
                scope.put(key, scope, Context.toObject(value, scope));
            }
            
            if (objectModel != null) {
                Object viewData = FlowHelper.getContextObject(objectModel);
                if (viewData != null) {
                    scope.put("viewData", scope, Context.toObject(viewData, scope));
                }
            }

            Object result = script.exec(ctx, scope);
            return FlowHelper.unwrap(result);
        } finally {
            Context.exit();
        }
    }
    
    public static Object callFunction(Function func, Object thisObject, Object[] arguments, Map objectModel) throws JavaScriptException {
        Context ctx = Context.enter();
        try {
            Scriptable scope = getParentScope(objectModel);

            if (objectModel != null) {
                Object viewData = FlowHelper.getContextObject(objectModel);
                if (viewData != null) {
                    // Create a new local scope to hold the view data
                    Scriptable newScope;
                    try {
                        newScope = ctx.newObject(scope);
                    } catch (Exception e) {
                        // Should normally not happen
                        throw new CascadingRuntimeException("Cannont create function scope", e);
                    }
                    newScope.setParentScope(scope);
                    scope = newScope;
            
                    scope.put("viewData", scope, Context.toObject(viewData, scope));
                }
            }
            Object result = func.call(ctx, scope, thisObject == null? null: Context.toObject(thisObject, scope), arguments);
            return FlowHelper.unwrap(result);
        } finally {
            Context.exit();
        }
    }
}
