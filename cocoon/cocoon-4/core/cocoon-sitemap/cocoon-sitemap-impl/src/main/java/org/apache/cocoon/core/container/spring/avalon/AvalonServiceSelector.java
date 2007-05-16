/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring.avalon;

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
public class AvalonServiceSelector
    implements BeanFactoryAware, ServiceSelector {

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
    public Object select(Object hint) throws ServiceException {
        Object key = hint;
        if ( key == null || key.toString().length() == 0 ) {
            key = this.defaultKey;
        }
        if ( !this.isSelectable(key) ) {
            throw new ServiceException("AvalonServiceSelector",
                                       "Component with role '" + this.role + "' and key '" + key + "' is not defined in this service selector.");
        }
        try {
            return this.beanFactory.getBean(this.role + key);
        } catch (BeansException be) {
            throw new ServiceException("AvalonServiceSelector",
                                       "Exception during lookup of component with role '" + this.role + "' and key '" + key + "'.", be);
        }
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
        if ( component instanceof AvalonPoolable ) {
            ((AvalonPoolable)component).putBackIntoAvalonPool();
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "AvalonServiceSelector[" + super.toString() + "]: role=" + this.role + ", factory=" + this.beanFactory;
    }
}
