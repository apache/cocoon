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
package org.apache.cocoon.portal.pluto.services.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortalContext;
import javax.portlet.PortletConfig;
import javax.portlet.PortletContext;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.ServletConfig;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.portal.pluto.factory.ControllerFactoryImpl;
import org.apache.cocoon.portal.pluto.factory.ObjectIDFactoryImpl;
import org.apache.cocoon.portal.pluto.factory.PortletPreferencesFactoryImpl;
import org.apache.cocoon.servlet.CocoonServlet;
import org.apache.pluto.factory.Factory;
import org.apache.pluto.factory.ObjectIDFactory;
import org.apache.pluto.factory.impl.ActionRequestFactoryImpl;
import org.apache.pluto.factory.impl.ActionResponseFactoryImpl;
import org.apache.pluto.factory.impl.PortalContextFactoryImpl;
import org.apache.pluto.factory.impl.PortletConfigFactoryImpl;
import org.apache.pluto.factory.impl.PortletContextFactoryImpl;
import org.apache.pluto.factory.impl.PortletSessionFactoryImpl;
import org.apache.pluto.factory.impl.PortletURLFactoryImpl;
import org.apache.pluto.factory.impl.RenderRequestFactoryImpl;
import org.apache.pluto.factory.impl.RenderResponseFactoryImpl;
import org.apache.pluto.invoker.PortletInvoker;
import org.apache.pluto.invoker.impl.PortletInvokerFactoryImpl;
import org.apache.pluto.om.ControllerFactory;
import org.apache.pluto.services.factory.FactoryManagerService;
import org.apache.pluto.util.NamespaceMapper;
import org.apache.pluto.util.impl.NamespaceMapperFactoryImpl;

/**
 * Manages the life-time of factories registered during container startup.
 * A service has to derive from {@link Factory} and implement the
 * <CODE>init()</CODE> and <CODE>destroy()</CODE> methods as appropriate.
 * 
 * TODO This is a very lazy implementation, we need to improve it
 * 
 * @see Factory
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: FactoryManagerServiceImpl.java,v 1.2 2004/01/23 12:34:31 joerg Exp $
 */
public class FactoryManagerServiceImpl 
implements FactoryManagerService, Initializable, Contextualizable, Disposable {

    protected ServletConfig servletConfig;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.servletConfig = (ServletConfig) context.get(CocoonServlet.CONTEXT_SERVLET_CONFIG);
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize()
    throws Exception {
        final Map factories = new HashMap();

        factories.put(ActionRequest.class.getName(), ActionRequestFactoryImpl.class.getName());
        factories.put(RenderRequest.class.getName(), RenderRequestFactoryImpl.class.getName());
        factories.put(RenderResponse.class.getName(), RenderResponseFactoryImpl.class.getName());
        factories.put(PortletSession.class.getName(), PortletSessionFactoryImpl.class.getName());
        factories.put(PortletConfig.class.getName(), PortletConfigFactoryImpl.class.getName());
        factories.put(PortletContext.class.getName(), PortletContextFactoryImpl.class.getName());
        factories.put(PortletPreferences.class.getName(), PortletPreferencesFactoryImpl.class.getName());
        factories.put(PortalContext.class.getName(), PortalContextFactoryImpl.class.getName());
        factories.put(ActionResponse.class.getName(), ActionResponseFactoryImpl.class.getName());
        factories.put(PortletURL.class.getName(), PortletURLFactoryImpl.class.getName());
        factories.put(PortletPreferences.class.getName(), PortletPreferencesFactoryImpl.class.getName());

        factories.put(PortletInvoker.class.getName(), PortletInvokerFactoryImpl.class.getName());

        factories.put(NamespaceMapper.class.getName(), NamespaceMapperFactoryImpl.class.getName());

        factories.put(ObjectIDFactory.class.getName(), ObjectIDFactoryImpl.class.getName());

        factories.put(ControllerFactory.class.getName(), ControllerFactoryImpl.class.getName());
    
        for (Iterator iter = factories.keySet().iterator(); iter.hasNext (); ) {
            String factoryInterfaceName = (String) iter.next ();

            // try to get hold of the factory
            Class factoryInterface;

            factoryInterface = Class.forName (factoryInterfaceName);

            String factoryImplName = (String)factories.get(factoryInterfaceName);
            Class factoryImpl = Class.forName (factoryImplName);
            Factory factory = (Factory) factoryImpl.newInstance ();

            factory.init(this.servletConfig, new HashMap());

            factoryMap.put (factoryInterface, factory);

            // build up list in reverse order for later destruction
            factoryList.add (0, factory);

        }

    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {

        // destroy the services in reverse order
        for (Iterator iterator = factoryList.iterator (); iterator.hasNext (); ) {
            Factory factory = (Factory) iterator.next ();

            try {
                factory.destroy ();
            } catch (Exception exc) {
            }
        }

        factoryList.clear();
        factoryMap.clear();

    }

    /**
     ** Returns the service implementation for the given service class, or
     ** <CODE>null</CODE> if no such service is registered.
     **
     ** @param theClass  the service class
     **
     ** @return   the service implementation
     **/
    public Factory getFactory (Class theClass) {
        // at this state the services map is read-only,
        // therefore we can go without synchronization
        return ((Factory) factoryMap.get (theClass));
    }

    private Map  factoryMap  = new HashMap();
    private List factoryList = new ArrayList();

}
