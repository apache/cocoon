/*
 * Copyright 2004,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.pluto;

import javax.servlet.http.HttpServletRequest;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.portal.pluto.om.PortletDefinitionRegistry;
import org.apache.pluto.services.PortletContainerEnvironment;
import org.apache.pluto.services.information.DynamicInformationProvider;
import org.apache.pluto.services.information.InformationProviderService;
import org.apache.pluto.services.information.StaticInformationProvider;

/**
 * Our own information provider service
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: InformationProviderServiceImpl.java,v 1.2 2004/03/05 13:02:14 bdelacretaz Exp $
 */
public class InformationProviderServiceImpl 
implements InformationProviderService, PortletContainerEnabled, Serviceable, Contextualizable {

    /** The service manager */
    protected ServiceManager manager;
    
    /** The portlet container environment */
    protected PortletContainerEnvironmentImpl portletContainerEnvironment;
    
    /** The static information provider (thread safe) */
    protected StaticInformationProvider staticProvider;

    /** The portal context provider (thread safe) */
    protected PortalContextProviderImpl provider;
    
    /** The component context */
    protected Context context;

    final static protected String dynamicProviderRole= InformationProviderServiceImpl.class.getName();
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.pluto.PortletContainerEnabled#setPortletContainerEnvironment(org.apache.pluto.services.PortletContainerEnvironment)
     */
    public void setPortletContainerEnvironment(PortletContainerEnvironment env) {
        this.portletContainerEnvironment = (PortletContainerEnvironmentImpl)env;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.InformationProviderService#getStaticProvider()
     */
    public StaticInformationProvider getStaticProvider() {
        if ( this.staticProvider == null ) {
            this.staticProvider = new StaticInformationProviderImpl(this.getPortalContextProvider(), 
                    (PortletDefinitionRegistry)this.portletContainerEnvironment.getContainerService(PortletDefinitionRegistry.class));            
        }
        return this.staticProvider;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.services.information.InformationProviderService#getDynamicProvider(javax.servlet.http.HttpServletRequest)
     */
    public DynamicInformationProvider getDynamicProvider(HttpServletRequest request) {
        DynamicInformationProvider dynProvider = (DynamicInformationProvider)request.getAttribute(dynamicProviderRole);

        if (dynProvider == null) {
            dynProvider = new DynamicInformationProviderImpl(this.manager,
                                                          this.getPortalContextProvider());
            request.setAttribute(dynamicProviderRole, dynProvider);
        }

        return dynProvider;
    }


    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * Get the portal context provider
     * We have to do a lazy initialization, as the provider needs the object model
     */
    protected PortalContextProviderImpl getPortalContextProvider() {
        if ( this.provider == null ) {
            this.provider = new PortalContextProviderImpl(ContextHelper.getObjectModel(context));
        }
        return this.provider;        
    }
    
}
