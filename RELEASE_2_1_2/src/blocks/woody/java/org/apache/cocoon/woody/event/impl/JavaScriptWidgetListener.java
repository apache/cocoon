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
package org.apache.cocoon.woody.event.impl;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.javascript.fom.FOM_JavaScriptFlowHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.woody.event.ActionEvent;
import org.apache.cocoon.woody.event.ActionListener;
import org.apache.cocoon.woody.event.ValueChangedEvent;
import org.apache.cocoon.woody.event.ValueChangedListener;
import org.apache.cocoon.woody.event.WidgetEvent;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

/**
 * Listeners built by {@link org.apache.cocoon.woody.event.impl.JavaScriptWidgetListenerBuilder}
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 */
public abstract class JavaScriptWidgetListener {
    
    private static Scriptable _rootScope = null;
    
    private Script script;
    
    public JavaScriptWidgetListener(Script script) {
        this.script = script;
    }
    
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
    
    public static Scriptable getParentScope(Request request) {
        // Try to get the flowscript scope
        Scriptable parentScope = (Scriptable)request.getAttribute(FOM_JavaScriptFlowHelper.FOM_SCOPE);

        if (parentScope != null) {
            return parentScope;
        } else {
            return getRootScope();
        }
    }

    /**
     * Call the script that implements the event handler
     */
    protected void callScript(WidgetEvent event) {
        Context ctx = Context.enter();
        try {
            
            // FIXME: remove this ugly hack and get the request from the Avalon context once
            // listener builder are real components
            Request request = ObjectModelHelper.getRequest(CocoonComponentManager.getCurrentEnvironment().getObjectModel());

            Scriptable parentScope = getParentScope(request);
            
            // Create a new local scope for the event listener variables
            Scriptable scope = ctx.newObject(parentScope);
            scope.setPrototype(parentScope);
            scope.put("event", scope, Context.toObject(event, scope));
            
            // Add the biz data that was passed to showForm()
            Object viewData = request.getAttribute(FlowHelper.CONTEXT_OBJECT);
            if (viewData != null) {
                scope.put("viewData", scope, viewData);
            }
            
            script.exec(ctx, scope);
            
        } catch(RuntimeException re) {
            // rethrow
            throw re;
        } catch(Exception e) {
            throw new CascadingRuntimeException("Error invoking JavaScript event handler", e);
        } finally {
            Context.exit();
        }
    }
    
    public static class JSActionListener extends JavaScriptWidgetListener implements ActionListener {

        public JSActionListener(Script script) {
            super(script);
        }

        public void actionPerformed(ActionEvent event) {
            super.callScript(event);
        }
    }
    
    public static class JSValueChangedListener extends JavaScriptWidgetListener implements ValueChangedListener {

        public JSValueChangedListener(Script script) {
            super(script);
        }

        public void valueChanged(ValueChangedEvent event) {
            super.callScript(event);
        }
    }
}
