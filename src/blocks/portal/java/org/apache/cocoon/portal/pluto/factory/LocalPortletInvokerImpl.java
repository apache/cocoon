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
 * @version CVS $Id: LocalPortletInvokerImpl.java,v 1.2 2004/01/27 09:56:38 cziegeler Exp $
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

}
