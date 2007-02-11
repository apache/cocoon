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

import org.apache.cocoon.woody.event.ActionListener;
import org.apache.cocoon.woody.event.ValueChangedListener;
import org.apache.cocoon.woody.event.WidgetListener;
import org.apache.cocoon.woody.event.WidgetListenerBuilder;
import org.apache.cocoon.woody.util.JavaScriptHelper;
import org.mozilla.javascript.Script;
import org.w3c.dom.Element;

/**
 * Builds a {@link WidgetListener} based on a JavaScript snippet.
 * <p>
 * The syntax for this listener is as follows :
 * <pre>
 *   &lt;javascript&gt;
 *     var widget = event.sourceWidget;
 *     sourceWidget.setValue("Yeah");
 *   &lt;/javascript&gt;
 * </pre>
 * As shown above, the event that fired this listener is published as the <code>event</code>
 * variable.
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: JavaScriptWidgetListenerBuilder.java,v 1.8 2004/03/09 13:54:26 reinhard Exp $
 */
public class JavaScriptWidgetListenerBuilder implements WidgetListenerBuilder {

    public static final JavaScriptWidgetListenerBuilder INSTANCE = new JavaScriptWidgetListenerBuilder();

    public WidgetListener buildListener(Element element, Class listenerClass) throws Exception {

        Script script = JavaScriptHelper.buildScript(element);

        if (listenerClass == ActionListener.class) {
            return new JavaScriptWidgetListener.JSActionListener(script);
        } else if (listenerClass == ValueChangedListener.class) {
            return new JavaScriptWidgetListener.JSValueChangedListener(script);
        } else {
            throw new Exception("Unkonwn event class: " + listenerClass);
        }
    }
}
