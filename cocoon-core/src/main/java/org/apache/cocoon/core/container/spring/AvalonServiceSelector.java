/*
 * Copyright 2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * This bean acts like a Avalon {@link ServiceSelector}.
 *
 * @since 2.2
 * @version $Id$
 */
public class AvalonServiceSelector implements BeanFactoryAware, ServiceSelector {

    protected final String role;
    protected String defaultKey;
    protected BeanFactory beanFactory;

    public AvalonServiceSelector(String r) {
        this.role = r + '/';
    }

    /**
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory factory) throws BeansException {
        this.beanFactory = factory;
    }

    public void setDefault(String value) {
        this.defaultKey = value;
    }

    /**
     * @see org.apache.avalon.framework.service.ServiceSelector#select(java.lang.Object)
     */
    public Object select(Object key) throws ServiceException {
        if ( key == null || key.toString().length() == 0 ) {
            key = this.defaultKey;
        }
        return this.beanFactory.getBean(this.role + key);
    }

    /**
     * @see org.apache.avalon.framework.service.ServiceSelector#isSelectable(java.lang.Object)
     */
    public boolean isSelectable(Object key) {
        return this.beanFactory.containsBean(this.role + key);
    }

    /**
     * @see org.apache.avalon.framework.service.ServiceSelector#release(java.lang.Object)
     */
    public void release(Object component) {
        // nothing to do
    }
}
