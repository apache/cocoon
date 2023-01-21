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
package org.apache.cocoon.portal.spring;

import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.services.aspects.DynamicAspect;
import org.apache.cocoon.portal.services.aspects.ProfileManagerAspect;
import org.apache.cocoon.portal.services.aspects.RequestProcessorAspect;
import org.apache.cocoon.portal.services.aspects.ResponseProcessorAspect;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * This is a Spring bean post processor for registering portal
 * components to the {@link PortalService}.
 *
 * @version $Id$
 */
public class RegistrationBeanPostProcessor
    implements BeanPostProcessor, BeanFactoryAware {

    protected BeanFactory beanFactory;

    /**
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    /**
     * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object, java.lang.String)
     */
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if ( bean instanceof DynamicAspect ) {
            final PortalService portalService = (PortalService) this.beanFactory.getBean(PortalService.class.getName());
            try {
                if ( bean instanceof ResponseProcessorAspect ) {
                    portalService.getPortalManager().getResponseProcessorAspectChain().addAspect(bean, null, 0);
                }
                if ( bean instanceof RequestProcessorAspect ) {
                    portalService.getPortalManager().getRequestProcessorAspectChain().addAspect(bean, null, -1);
                }
                if ( bean instanceof ProfileManagerAspect ) {
                    portalService.getProfileManager().getProfileManagerAspectChain().addAspect(bean, null, -1);
                }
            } catch (PortalException pe) {
                throw new BeanInitializationException("Unable to register dynamic aspect of bean " + beanName, pe);
            }
        }
        return bean;
    }

    /**
     * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessBeforeInitialization(java.lang.Object, java.lang.String)
     */
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // nothing to do
        return bean;
    }
}
