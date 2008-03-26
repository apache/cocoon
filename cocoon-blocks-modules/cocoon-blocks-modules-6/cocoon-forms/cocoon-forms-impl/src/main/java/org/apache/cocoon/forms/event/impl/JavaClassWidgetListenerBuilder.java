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

import org.apache.cocoon.forms.FormsException;
import org.apache.cocoon.forms.event.ConfigurableWidgetListener;
import org.apache.cocoon.forms.event.WidgetListener;
import org.apache.cocoon.forms.event.WidgetListenerBuilder;
import org.apache.cocoon.forms.util.DomHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.w3c.dom.Element;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A {@link WidgetListenerBuilder} that uses Spring beans.
 * <p>
 * The syntax for this listener is as follows :<br/>
 * <pre>
 *   &lt;java ref="spring-bean-id"&gt;
 *     ...
 *   &lt;/java&gt;
 * </pre>
 * 
 * The {@link Element} node denoted by upper snippet is passed to the bean at a 
 * method configure(Element e) if it exists.
 *
 * @version $Id$
 */
public class JavaClassWidgetListenerBuilder
    implements WidgetListenerBuilder, BeanFactoryAware {

    private static Log LOG = LogFactory.getLog( JavaClassWidgetListenerBuilder.class );
    
    private BeanFactory beanFactory;

    public void setBeanFactory( BeanFactory beanFactory)
                                                  throws BeansException
    {
        this.beanFactory = beanFactory;
    }

	/**
	 * @see org.apache.cocoon.forms.event.WidgetListenerBuilder#buildListener(org.w3c.dom.Element, java.lang.Class)
	 */
	public WidgetListener buildListener(Element element, Class listenerClass) throws Exception {
	    
	    // hard way deprecation
	    if (DomHelper.getAttribute(element, "class", null) != null) {
                throw new RuntimeException("The 'class' attribute is not supported anymore at "
                                           + DomHelper.getLocationObject( element )
                                           + ". Use a 'ref' attribute to address a Spring bean");
	    }
	    
        String name = DomHelper.getAttribute(element, "ref");

        try {
            Object listener = beanFactory.getBean( name );
            if (listener != null && listenerClass.isAssignableFrom(listener.getClass())) {
                if (listener instanceof ConfigurableWidgetListener) {
                    ((ConfigurableWidgetListener)listener).setConfiguration( element );
                }
                return (WidgetListener)listener;
            } else {
                throw new FormsException("Bean referenced by " + name + " is not a " + listenerClass.getName(), DomHelper.getLocationObject( element ));
            }
        } catch(BeansException be) {
            throw new FormsException("Bean referenced by " + name + " doesn't exist in Spring context", be, DomHelper.getLocationObject( element ));
        }
    }
}
