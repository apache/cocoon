/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 2004 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

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
 * @version CVS $Id: InformationProviderServiceImpl.java,v 1.1 2004/01/22 14:01:21 cziegeler Exp $
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
