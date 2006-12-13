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
package org.apache.cocoon.forms.event.impl;

import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.context.Context;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.forms.event.ActionEvent;
import org.apache.cocoon.forms.event.ActionListener;
import org.apache.cocoon.forms.event.CreateEvent;
import org.apache.cocoon.forms.event.CreateListener;
import org.apache.cocoon.forms.event.ProcessingPhaseEvent;
import org.apache.cocoon.forms.event.ProcessingPhaseListener;
import org.apache.cocoon.forms.event.RepeaterEvent;
import org.apache.cocoon.forms.event.RepeaterListener;
import org.apache.cocoon.forms.event.ValueChangedEvent;
import org.apache.cocoon.forms.event.ValueChangedListener;
import org.apache.cocoon.forms.event.WidgetEvent;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.formmodel.tree.TreeSelectionEvent;
import org.apache.cocoon.forms.formmodel.tree.TreeSelectionListener;
import org.apache.cocoon.forms.util.JavaScriptHelper;
import org.mozilla.javascript.Function;

/**
 * Listeners built by {@link org.apache.cocoon.forms.event.impl.JavaScriptWidgetListenerBuilder}
 * 
 * @version $Id$
 */
public abstract class JavaScriptWidgetListener {
    
    private Function func;
    private Context context;

    public JavaScriptWidgetListener(Function func, Context context) {
        this.func = func;
        this.context = context;
    }
    
    /**
     * Call the script that implements the event handler
     */
    protected void callScript(WidgetEvent event) {
        try {
            //FIXME(SW) it would be nice to have "this" be the widget, but I don't know how to define
            //the "this" object for a script (this is easy for a function)
            Map objectModel = ContextHelper.getObjectModel(context);
            Widget w = event.getSourceWidget();
            JavaScriptHelper.callFunction(this.func, w, new Object[]{w, event}, objectModel);
        } catch(RuntimeException re) {
            // rethrow
            throw re;
        } catch(Exception e) {
            throw new CascadingRuntimeException("Error invoking JavaScript event handler", e);
        }
    }
    
    public static class JSActionListener extends JavaScriptWidgetListener implements ActionListener {

        public JSActionListener(Function func, Context context) {
            super(func, context);
        }

        public void actionPerformed(ActionEvent event) {
            super.callScript(event);
        }
    }
    
    public static class JSValueChangedListener extends JavaScriptWidgetListener implements ValueChangedListener {

        public JSValueChangedListener(Function func, Context context) {
            super(func, context);
        }

        public void valueChanged(ValueChangedEvent event) {
            super.callScript(event);
        }
    }
    
    public static class JSCreateListener extends JavaScriptWidgetListener implements CreateListener {

        public JSCreateListener(Function func, Context context) {
            super(func, context);
        }

        public void widgetCreated(CreateEvent event) {
            super.callScript(event);
        }
    }
    
    public static class JSTreeSelectionListener extends JavaScriptWidgetListener implements TreeSelectionListener {

        public JSTreeSelectionListener(Function func, Context context) {
            super(func, context);
        }

        public void selectionChanged(TreeSelectionEvent event) {
            super.callScript(event);
        }
    }

    public static class JSProcessingPhaseListener extends JavaScriptWidgetListener implements ProcessingPhaseListener {

        public JSProcessingPhaseListener(Function func, Context context) {
            super(func, context);
        }

        public void phaseEnded(ProcessingPhaseEvent event) {
            super.callScript(event);
        }
    }

    
    public static class JSRepeaterListener extends JavaScriptWidgetListener implements RepeaterListener {

        public JSRepeaterListener(Function func, Context context) {
            super(func, context);
        }

        public void repeaterModified(RepeaterEvent event) {
            super.callScript(event);
        }
    }
    
}

