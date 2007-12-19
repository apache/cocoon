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

import javax.servlet.ServletContext;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.event.EventConverter;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.impl.PortalServiceImpl;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.cocoon.portal.services.CopletFactory;
import org.apache.cocoon.portal.services.LayoutFactory;
import org.apache.cocoon.portal.services.LinkService;
import org.apache.cocoon.portal.services.PortalManager;
import org.apache.cocoon.portal.services.UserService;
import org.apache.cocoon.portal.spi.RequestContextProvider;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * This extensions of the {@link PortalServiceImpl} uses Spring
 * to resolve the dependencies.
 *
 * @version $Id$
 */
public class SpringPortalService
    extends PortalServiceImpl
    implements ApplicationContextAware {

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext appContext)
    throws BeansException {
        this.requestContextProvider = (RequestContextProvider) appContext.getBean(RequestContextProvider.class.getName());
        this.servletContext = (ServletContext)appContext.getBean(ServletContext.class.getName());
        // add the portal service to the servlet context
        this.servletContext.setAttribute(PortalService.class.getName(), this);
        this.profileManager = (ProfileManager)appContext.getBean(ProfileManager.class.getName());
        this.linkService = (LinkService)appContext.getBean( LinkService.class.getName() );
        this.eventManager = (EventManager)appContext.getBean( EventManager.class.getName() );
        this.copletFactory = (CopletFactory)appContext.getBean( CopletFactory.class.getName() );
        this.layoutFactory = (LayoutFactory)appContext.getBean( LayoutFactory.class.getName() );
        this.portalManager = (PortalManager)appContext.getBean( PortalManager.class.getName() );
        this.userService = (UserService)appContext.getBean(UserService.class.getName());
        this.eventConverter = (EventConverter)appContext.getBean(EventConverter.class.getName());
    }
}
