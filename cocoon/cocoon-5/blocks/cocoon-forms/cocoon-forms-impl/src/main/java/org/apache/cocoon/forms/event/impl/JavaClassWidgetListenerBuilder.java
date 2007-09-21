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

import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.forms.event.WidgetListener;
import org.apache.cocoon.forms.event.WidgetListenerBuilder;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.ConfigurationUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
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
public class JavaClassWidgetListenerBuilder
    extends AbstractLogEnabled
    implements WidgetListenerBuilder, ThreadSafe, Contextualizable, Serviceable {

    protected ServiceManager manager;

    protected Context context;

	/**
	 * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
	 */
	public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /**
	 * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
	 */
	public void service(ServiceManager manager) throws ServiceException {
		this.manager = manager;
	}

	/**
	 * @see org.apache.cocoon.forms.event.WidgetListenerBuilder#buildListener(org.w3c.dom.Element, java.lang.Class)
	 */
	public WidgetListener buildListener(Element element, Class listenerClass) throws Exception {
        String name = DomHelper.getAttribute(element, "class");

        Object listener = ClassUtils.newInstance(name);
        if (listenerClass.isAssignableFrom(listener.getClass())) {
            LifecycleHelper.setupComponent(listener, this.getLogger(), this.context, manager, ConfigurationUtil.toConfiguration(element));
            return (WidgetListener)listener;
        } else {
            throw new Exception("Class " + listener.getClass() + " is not a " + listenerClass);
        }
    }
}
