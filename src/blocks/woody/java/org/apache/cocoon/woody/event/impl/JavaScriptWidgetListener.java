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
package org.apache.cocoon.woody.event.impl;

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.woody.event.ActionEvent;
import org.apache.cocoon.woody.event.ActionListener;
import org.apache.cocoon.woody.event.ValueChangedEvent;
import org.apache.cocoon.woody.event.ValueChangedListener;
import org.apache.cocoon.woody.event.WidgetEvent;
import org.apache.cocoon.woody.util.JavaScriptHelper;
import org.mozilla.javascript.Script;

/**
 * Listeners built by {@link org.apache.cocoon.woody.event.impl.JavaScriptWidgetListenerBuilder}
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: JavaScriptWidgetListener.java,v 1.10 2004/05/25 07:28:26 cziegeler Exp $
 */
public abstract class JavaScriptWidgetListener {
    
    private Script script;
    
    public JavaScriptWidgetListener(Script script) {
        this.script = script;
    }
    
    /**
     * Call the script that implements the event handler
     */
    protected void callScript(WidgetEvent event) {
        try {
            
            HashMap values = new HashMap(2);
            values.put("event", event);
            
            // FIXME: remove this ugly hack and get the request from the Avalon context once
            // listener builder are real components
            Map objectModel = EnvironmentHelper.getCurrentEnvironment().getObjectModel();
            
            // Add the biz data that was passed to showForm()
            Object viewData = FlowHelper.getContextObject(objectModel);
            if (viewData != null) {
                values.put("viewData", viewData);
            }
            
            JavaScriptHelper.execScript(this.script, values, objectModel);
            
        } catch(RuntimeException re) {
            // rethrow
            throw re;
        } catch(Exception e) {
            throw new CascadingRuntimeException("Error invoking JavaScript event handler", e);
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
