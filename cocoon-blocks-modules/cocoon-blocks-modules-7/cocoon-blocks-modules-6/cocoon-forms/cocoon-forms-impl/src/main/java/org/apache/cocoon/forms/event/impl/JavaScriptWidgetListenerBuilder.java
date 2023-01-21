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

import org.apache.cocoon.forms.event.ActionListener;
import org.apache.cocoon.forms.event.CreateListener;
import org.apache.cocoon.forms.event.ProcessingPhaseListener;
import org.apache.cocoon.forms.event.RepeaterListener;
import org.apache.cocoon.forms.event.ValueChangedListener;
import org.apache.cocoon.forms.event.WidgetListener;
import org.apache.cocoon.forms.event.WidgetListenerBuilder;
import org.apache.cocoon.forms.formmodel.tree.TreeSelectionListener;
import org.apache.cocoon.forms.util.JavaScriptHelper;
import org.apache.cocoon.processing.ProcessInfoProvider;
import org.mozilla.javascript.Function;
import org.w3c.dom.Element;

/**
 * Builds a {@link WidgetListener} based on a JavaScript snippet.
 * <p>
 * The syntax for this listener is as follows :
 * <pre>
 *   &lt;fd:javascript&gt;
 *     var widget = event.sourceWidget;
 *     sourceWidget.setValue("Yeah");
 *   &lt;/fd:javascript&gt;
 * </pre>
 * As shown above, the event that fired this listener is published as the <code>event</code>
 * variable.
 *
 * @version $Id$
 */
public class JavaScriptWidgetListenerBuilder implements WidgetListenerBuilder {

    private ProcessInfoProvider processInfoProvider;

    public WidgetListener buildListener(Element element, Class listenerClass) throws Exception {

        Function func = JavaScriptHelper.buildFunction(element, "handleEvent", new String[]{"widget", "event"});

        if (listenerClass == ActionListener.class) {
            return new JavaScriptWidgetListener.JSActionListener(func, processInfoProvider);
        } else if (listenerClass == CreateListener.class) {
            return new JavaScriptWidgetListener.JSCreateListener(func, processInfoProvider);
        } else if (listenerClass == ValueChangedListener.class) {
            return new JavaScriptWidgetListener.JSValueChangedListener(func, processInfoProvider);
        } else if (listenerClass == TreeSelectionListener.class) {
            return new JavaScriptWidgetListener.JSTreeSelectionListener(func, processInfoProvider);
        } else if (listenerClass == ProcessingPhaseListener.class) {
            return new JavaScriptWidgetListener.JSProcessingPhaseListener(func, processInfoProvider);
        } else if (listenerClass == RepeaterListener.class) {
            return new JavaScriptWidgetListener.JSRepeaterListener(func, processInfoProvider);
        } else {
            throw new Exception("Unkonwn event class: " + listenerClass);
        }
    }

    public void setProcessInfoProvider( ProcessInfoProvider processInfoProvider )
    {
        this.processInfoProvider = processInfoProvider;
    }
}
