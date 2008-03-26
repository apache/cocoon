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
package org.apache.cocoon.portal.pluto;

import javax.servlet.http.HttpServletRequest;

import org.apache.cocoon.portal.avalon.AbstractComponent;
import org.apache.cocoon.portal.pluto.om.PortletDefinitionRegistry;
import org.apache.pluto.services.PortletContainerEnvironment;
import org.apache.pluto.services.information.DynamicInformationProvider;
import org.apache.pluto.services.information.InformationProviderService;
import org.apache.pluto.services.information.StaticInformationProvider;

/**
 * Our own information provider service.
 *
 * @version $Id$
 */
public class InformationProviderServiceImpl
    extends AbstractComponent
    implements InformationProviderService,
               PortletContainerEnabled {

    /** The portlet container environment */
    protected PortletContainerEnvironmentImpl portletContainerEnvironment;

    /** The static information provider (thread safe) */
    protected StaticInformationProvider staticProvider;

    /** The portal context provider (thread safe) */
    protected PortalContextProviderImpl provider;

    final static protected String dynamicProviderRole= InformationProviderServiceImpl.class.getName();

    /**
     * @see org.apache.cocoon.portal.pluto.PortletContainerEnabled#setPortletContainerEnvironment(org.apache.pluto.services.PortletContainerEnvironment)
     */
    public void setPortletContainerEnvironment(PortletContainerEnvironment env) {
        this.portletContainerEnvironment = (PortletContainerEnvironmentImpl)env;
    }

    /**
     * @see org.apache.pluto.services.information.InformationProviderService#getStaticProvider()
     */
    public StaticInformationProvider getStaticProvider() {
        if ( this.staticProvider == null ) {
            this.staticProvider = new StaticInformationProviderImpl(this.getPortalContextProvider(),
                    (PortletDefinitionRegistry)this.portletContainerEnvironment.getContainerService(PortletDefinitionRegistry.class));
        }
        return this.staticProvider;
    }

    /**
     * @see org.apache.pluto.services.information.InformationProviderService#getDynamicProvider(javax.servlet.http.HttpServletRequest)
     */
    public DynamicInformationProvider getDynamicProvider(HttpServletRequest request) {
        DynamicInformationProvider dynProvider = (DynamicInformationProvider)request.getAttribute(dynamicProviderRole);

        if (dynProvider == null) {
            dynProvider = new DynamicInformationProviderImpl(this.manager,
                                                             this.getPortalContextProvider(),
                                                             this.portalService);
            request.setAttribute(dynamicProviderRole, dynProvider);
        }

        return dynProvider;
    }

    /**
     * Get the portal context provider
     * We have to do a lazy initialization, as the provider needs the object model
     */
    protected PortalContextProviderImpl getPortalContextProvider() {
        if ( this.provider == null ) {
            this.provider = new PortalContextProviderImpl(this.portalService.getRequestContext());
        }
        return this.provider;
    }
}
