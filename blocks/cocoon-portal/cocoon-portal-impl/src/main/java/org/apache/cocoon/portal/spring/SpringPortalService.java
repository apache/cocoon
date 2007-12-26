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

import org.apache.cocoon.portal.services.impl.AbstractPortalService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * This extension of the {@link AbstractPortalService} uses Spring
 * to resolve the dependencies.
 *
 * @version $Id$
 */
public class SpringPortalService
    extends AbstractPortalService
    implements ApplicationContextAware {

    /** The application context. */
    protected ApplicationContext appContext;

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext appContext)
    throws BeansException {
        this.appContext = appContext;
    }

    /**
     * @see org.apache.cocoon.portal.services.impl.AbstractPortalService#getService(java.lang.String)
     */
    protected Object getService(String name) {
        return this.appContext.getBean(name);
    }
}
