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

import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.forms.event.WidgetListener;
import org.apache.cocoon.forms.event.WidgetListenerBuilder;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.util.ClassUtils;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.w3c.dom.Element;

/**
 * A {@link WidgetListenerBuilder} that creates java classes.
 * <p>
 * The syntax for this listener is as follows :<br/>
 * <pre>
 *   &lt;java class="com.my.SuperListener"/&gt;
 * </pre>
 *
 * @version $Id$
 */
public class JavaClassWidgetListenerBuilder implements WidgetListenerBuilder, ThreadSafe, Serviceable {
	ServiceManager manager;

	public void service(ServiceManager manager) throws ServiceException {
		this.manager = manager;
	}

	public WidgetListener buildListener(Element element, Class listenerClass) throws Exception {

        String name = DomHelper.getAttribute(element, "class");

        Object listener = ClassUtils.newInstance(name);
        if (listenerClass.isAssignableFrom(listener.getClass())) {
            LifecycleHelper.setupComponent(listener, null, null, manager, null);
            return (WidgetListener)listener;
        } else {
            throw new Exception("Class " + listener.getClass() + " is not a " + listenerClass);
        }
    }
}
