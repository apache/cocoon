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

import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.portal.pluto.om.PortletDefinitionImpl;
import org.apache.pluto.factory.PortletInvokerFactory;
import org.apache.pluto.invoker.PortletInvoker;
import org.apache.pluto.invoker.impl.PortletInvokerImpl;
import org.apache.pluto.om.portlet.PortletDefinition;

/**
 * The implementation of the invoker factory
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletInvokerFactoryImpl.java,v 1.2 2004/01/27 09:56:37 cziegeler Exp $
 */
public class PortletInvokerFactoryImpl 
extends AbstractFactory
implements PortletInvokerFactory, Serviceable, Contextualizable {

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
     * @see org.apache.pluto.factory.PortletInvokerFactory#getPortletInvoker(org.apache.pluto.om.portlet.PortletDefinition)
     */
    public PortletInvoker getPortletInvoker(PortletDefinition portletDefinition) {
        // test, if this is a local portlet
        boolean local = false;
        PortletInvoker invoker;
        if ( portletDefinition instanceof PortletDefinitionImpl ) {
            local = ((PortletDefinitionImpl)portletDefinition).isLocalPortlet();
        }
        if (local) {
            invoker = ((PortletDefinitionImpl)portletDefinition).getLocalPortletInvoker();
            if ( invoker == null ) {
                invoker = new LocalPortletInvokerImpl(portletDefinition, this.servletConfig);
                ((PortletDefinitionImpl)portletDefinition).setLocalPortletInvoker(invoker);
                try {
                    ContainerUtil.enableLogging(invoker, this.getLogger());
                    ContainerUtil.contextualize(invoker, this.context);
                    ContainerUtil.service(invoker, this.manager);
                    ContainerUtil.initialize(invoker);
                } catch (Exception ignore) {
                    this.getLogger().warn("Unable to initialize local portlet invoker.", ignore);
                }
            }
        } else {
            invoker = new PortletInvokerImpl(portletDefinition, this.servletConfig);
        }
        
        return invoker;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.factory.PortletInvokerFactory#releasePortletInvoker(org.apache.pluto.invoker.PortletInvoker)
     */
    public void releasePortletInvoker(PortletInvoker invoker) {
        // nothing to do here
    }

}
