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
import org.apache.avalon.framework.service.ServiceManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * This bean acts like a Avalon {@link ServiceManager}.
 * It just delegates to the underlying bean factory.
 *
 * @since 2.2
 * @version $Id$
 */
final public class AvalonServiceManager
    implements ServiceManager, BeanFactoryAware {

    /** The bean factory this service manager is defined in. */
    protected BeanFactory beanFactory;

    /**
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * @see org.apache.avalon.framework.service.ServiceManager#hasService(java.lang.String)
     */
    public boolean hasService(String role) {
        return this.beanFactory.containsBean(role);
    }

    /**
     * @see org.apache.avalon.framework.service.ServiceManager#lookup(java.lang.String)
     */
    public Object lookup(String role) throws ServiceException {
        if ( !this.hasService(role) ) {
            throw new ServiceException("AvalonServiceManager",
                                       "Component with '" + role + "' is not defined in this service manager.");
        }
        try {
            return this.beanFactory.getBean(role);
        } catch (BeansException be) {
            throw new ServiceException("AvalonServiceManager",
                                       "Exception during lookup of component with '" + role + "'.", be);
        }
    }

    /**
     * @see org.apache.avalon.framework.service.ServiceManager#release(java.lang.Object)
     */
    public void release(Object component) {
        if ( component instanceof AvalonPoolable ) {
            ((AvalonPoolable)component).putBackIntoAvalonPool();
        }
    }
}