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
package org.apache.cocoon.portal.pluto.factory;

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.Portlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.ServletConfig;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.util.ClassUtils;
import org.apache.pluto.factory.PortletObjectAccess;
import org.apache.pluto.invoker.PortletInvoker;
import org.apache.pluto.om.portlet.PortletDefinition;

/**
 * This is an invoker for a "local" portlet, which is a portlet running inside Cocoon.
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: LocalPortletInvokerImpl.java,v 1.4 2004/03/05 13:02:14 bdelacretaz Exp $
 */
public class LocalPortletInvokerImpl
extends AbstractLogEnabled
implements PortletInvoker, Contextualizable, Serviceable, Initializable {
    
    /** servlet configuration */
    protected final ServletConfig servletConfig;

    /** The portlet definition */
    protected final PortletDefinition portletDefinition;

    /** The portlet */
    protected Portlet portlet;
    
    /** The avalon context */
    protected Context context;
    
    /** The service manager */
    protected ServiceManager manager;
    
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

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        if (this.portlet != null) {
            try {
                ContainerUtil.enableLogging(this.portlet, this.getLogger());
                ContainerUtil.contextualize(this.portlet, this.context);
                ContainerUtil.service(this.portlet, this.manager);
                ContainerUtil.initialize(this.portlet);
            } catch (Exception ignore) {
                // we ignore the exception here and throw later on a portlet exception
                this.getLogger().warn("Unable to initialize local portlet invoker.", ignore);
                this.portlet = null;
            }
        }
    }

    /**
     * Constructor
     */
    public LocalPortletInvokerImpl(PortletDefinition portletDefinition, 
                                   ServletConfig     servletConfig) {
        this.portletDefinition = portletDefinition;
        this.servletConfig = servletConfig;
        
        try {
            final String clazzName = portletDefinition.getClassName();
            this.portlet = (Portlet)ClassUtils.newInstance(clazzName);
        } catch (Exception ignore) {
            // we ignore the exception here and throw later on a portlet exception
            this.getLogger().warn("Unable to initialize local portlet invoker.", ignore);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.invoker.PortletInvoker#action(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
     */
    public void action(ActionRequest request, 
                       ActionResponse response) 
    throws PortletException, IOException {
        if ( this.portlet == null ) {
            throw new PortletException("Unable to instantiate portlet from class " + this.portletDefinition.getClassName());
        }
        this.portlet.processAction(request, response);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.invoker.PortletInvoker#render(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
     */
    public void render(RenderRequest request, RenderResponse response) 
    throws PortletException, IOException {
        if ( this.portlet == null ) {
            throw new PortletException("Unable to instantiate portlet from class " + this.portletDefinition.getClassName());
        }
        this.portlet.render(request, response);
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.invoker.PortletInvoker#load(javax.portlet.PortletRequest, javax.portlet.RenderResponse)
     */
    public void load(PortletRequest request, RenderResponse response) 
    throws PortletException {
        if ( this.portlet == null ) {
            throw new PortletException("Unable to instantiate portlet from class " + this.portletDefinition.getClassName());
        }
        PortletContext portletContext;
        PortletConfig portletConfig;
        portletContext = PortletObjectAccess.getPortletContext(this.servletConfig.getServletContext(),
                portletDefinition.getPortletApplicationDefinition());
        portletConfig = PortletObjectAccess.getPortletConfig(this.servletConfig, 
                portletContext,
                portletDefinition);
        this.portlet.init(portletConfig);
    }

    /**
     * Destroy the associated portlet
     */
    public void destroy() {
        if (this.portlet != null ) {
            this.portlet.destroy();
            this.portlet = null;
        }
    }
}
