/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.woody.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.javascript.fom.FOM_JavaScriptFlowHelper;
import org.apache.cocoon.environment.Request;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.w3c.dom.Element;

/**
 * Helper methods to use JavaScript in various locations of the Woody configuration files
 * such as event listeners and bindings.
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: JavaScriptHelper.java,v 1.4 2004/02/04 17:25:58 sylvain Exp $
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
     * @param request a request where the flowscript scope will be searched (can be <code>null</code>).
     * @return an appropriate parent scope.
     */
    public static Scriptable getParentScope(Request request) {
        // Try to get the flowscript scope
        Scriptable parentScope = null;
        if (request != null) {
            parentScope = (Scriptable)request.getAttribute(FOM_JavaScriptFlowHelper.FOM_SCOPE);
        }

        if (parentScope != null) {
            return parentScope;
        } else {
            return getRootScope();
        }
    }

    public static Object execScript(Script script, Map values, Request request) throws JavaScriptException {
        Context ctx = Context.enter();
        try {
            Scriptable parentScope = getParentScope(request);

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
            
            if (request != null) {
                Object viewData = request.getAttribute(FlowHelper.CONTEXT_OBJECT);
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
    
    public static Object callFunction(Function func, Object thisObject, Object[] arguments, Request request) throws JavaScriptException {
        Context ctx = Context.enter();
        try {
            Scriptable scope = getParentScope(request);

            if (request != null) {
                Object viewData = request.getAttribute(FlowHelper.CONTEXT_OBJECT);
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
