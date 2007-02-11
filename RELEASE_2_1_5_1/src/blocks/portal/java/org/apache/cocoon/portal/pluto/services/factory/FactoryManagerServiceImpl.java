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
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.portal.pluto.factory.ControllerFactoryImpl;
import org.apache.cocoon.portal.pluto.factory.ObjectIDFactoryImpl;
import org.apache.cocoon.portal.pluto.factory.PortletInvokerFactoryImpl;
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
 * @version CVS $Id: FactoryManagerServiceImpl.java,v 1.5 2004/03/05 13:02:16 bdelacretaz Exp $
 */
public class FactoryManagerServiceImpl 
extends AbstractLogEnabled
implements FactoryManagerService, Initializable, Contextualizable, Serviceable, Disposable {

    /** The servlet configuration */
    protected ServletConfig servletConfig;
    
    /** The avalon context */
    protected Context context;
    
    /** The service manager */
    protected ServiceManager manager;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
        this.servletConfig = (ServletConfig) context.get(CocoonServlet.CONTEXT_SERVLET_CONFIG);
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

            ContainerUtil.enableLogging(factory, this.getLogger());
            ContainerUtil.contextualize(factory, this.context);
            ContainerUtil.service(factory, this.manager);
            ContainerUtil.initialize(factory);
            
            factory.init(this.servletConfig, new HashMap());

            factoryMap.put (factoryInterface.getName(), factory);

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
        return ((Factory) factoryMap.get (theClass.getName()));
    }

    private Map  factoryMap  = new HashMap();
    private List factoryList = new ArrayList();

}
