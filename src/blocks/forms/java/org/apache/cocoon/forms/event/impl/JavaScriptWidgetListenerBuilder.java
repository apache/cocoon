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
package org.apache.cocoon.forms.event.impl;

import org.apache.cocoon.forms.event.ActionListener;
import org.apache.cocoon.forms.event.CreateListener;
import org.apache.cocoon.forms.event.ValueChangedListener;
import org.apache.cocoon.forms.event.WidgetListener;
import org.apache.cocoon.forms.event.WidgetListenerBuilder;
import org.apache.cocoon.forms.util.JavaScriptHelper;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.mozilla.javascript.Script;
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
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: JavaScriptWidgetListenerBuilder.java,v 1.3 2004/06/15 07:33:43 sylvain Exp $
 */
public class JavaScriptWidgetListenerBuilder implements WidgetListenerBuilder, ThreadSafe, Contextualizable {

    private Context context;

    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public WidgetListener buildListener(Element element, Class listenerClass) throws Exception {

        Script script = JavaScriptHelper.buildScript(element);

        if (listenerClass == ActionListener.class) {
            return new JavaScriptWidgetListener.JSActionListener(script, context);
        } else if (listenerClass == CreateListener.class) {
            return new JavaScriptWidgetListener.JSCreateListener(script, context);
        } else if (listenerClass == ValueChangedListener.class) {
            return new JavaScriptWidgetListener.JSValueChangedListener(script, context);
        } else {
            throw new Exception("Unkonwn event class: " + listenerClass);
        }
    }
}
