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
package org.apache.cocoon.portal.pluto.services.factory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
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
import javax.servlet.ServletContext;

import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.portal.avalon.AbstractComponent;
import org.apache.cocoon.portal.pluto.factory.ActionRequestFactoryImpl;
import org.apache.cocoon.portal.pluto.factory.ControllerFactoryImpl;
import org.apache.cocoon.portal.pluto.factory.ObjectIDFactoryImpl;
import org.apache.cocoon.portal.pluto.factory.PortletInvokerFactoryImpl;
import org.apache.cocoon.portal.pluto.factory.PortletPreferencesFactoryImpl;
import org.apache.cocoon.portal.pluto.factory.RenderRequestFactoryImpl;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.util.ClassUtils;
import org.apache.pluto.factory.Factory;
import org.apache.pluto.factory.ObjectIDFactory;
import org.apache.pluto.factory.impl.ActionResponseFactoryImpl;
import org.apache.pluto.factory.impl.PortalContextFactoryImpl;
import org.apache.pluto.factory.impl.PortletConfigFactoryImpl;
import org.apache.pluto.factory.impl.PortletContextFactoryImpl;
import org.apache.pluto.factory.impl.PortletSessionFactoryImpl;
import org.apache.pluto.factory.impl.PortletURLFactoryImpl;
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
 * @see Factory
 * @version $Id$
 */
public class FactoryManagerServiceImpl
    extends AbstractComponent
    implements FactoryManagerService, Parameterizable {

    /** The servlet configuration */
    protected ServletConfig servletConfig;

    /** All factories mapped by factory class name. */
    protected Map  factoryMap  = new HashMap();

    /** All factories. */
    protected List factoryList = new ArrayList();

    /** The configuration. */
    protected Parameters parameters;

    protected static final class PortalServletConfig implements ServletConfig {

        private final ServletContext servletContext;

        public PortalServletConfig(ServletContext sContext) {
            this.servletContext = sContext;
        }

        /**
         * @see javax.servlet.ServletConfig#getInitParameter(java.lang.String)
         */
        public String getInitParameter(String arg0) {
            return this.servletContext.getInitParameter(arg0);
        }

        /**
         * @see javax.servlet.ServletConfig#getInitParameterNames()
         */
        public Enumeration getInitParameterNames() {
            return this.servletContext.getInitParameterNames();
        }

        /**
         * @see javax.servlet.ServletConfig#getServletContext()
         */
        public ServletContext getServletContext() {
            return this.servletContext;
        }

        /**
         * @see javax.servlet.ServletConfig#getServletName()
         */
        public String getServletName() {
            return this.servletContext.getServletContextName();
        }

    }
    /**
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters params) throws ParameterException {
        this.parameters = params;
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize()
    throws Exception {
        super.initialize();
        final ServletContext servletContext = this.portalService.getRequestContext().getServletContext();
        this.servletConfig = new PortalServletConfig(servletContext);
        final Map factories = new HashMap();

        factories.put(ActionRequest.class.getName(),
                      this.parameters.getParameter("action-request-factory", ActionRequestFactoryImpl.class.getName()));
        factories.put(RenderRequest.class.getName(),
                      this.parameters.getParameter("render-request-factory", RenderRequestFactoryImpl.class.getName()));
        factories.put(ActionResponse.class.getName(),
                      this.parameters.getParameter("action-response-factory", ActionResponseFactoryImpl.class.getName()));
        factories.put(RenderResponse.class.getName(),
                      this.parameters.getParameter("render-response-factory", RenderResponseFactoryImpl.class.getName()));
        factories.put(PortletSession.class.getName(),
                      this.parameters.getParameter("portlet-session-factory", PortletSessionFactoryImpl.class.getName()));
        factories.put(PortletConfig.class.getName(),
                      this.parameters.getParameter("portlet-config-factory", PortletConfigFactoryImpl.class.getName()));
        factories.put(PortletContext.class.getName(),
                      this.parameters.getParameter("portlet-context-factory", PortletContextFactoryImpl.class.getName()));
        factories.put(PortalContext.class.getName(),
                      this.parameters.getParameter("portal-context-factory", PortalContextFactoryImpl.class.getName()));
        factories.put(PortletURL.class.getName(),
                      this.parameters.getParameter("portlet-url-factory", PortletURLFactoryImpl.class.getName()));
        factories.put(PortletPreferences.class.getName(),
                      this.parameters.getParameter("portlet-preferences-factory", PortletPreferencesFactoryImpl.class.getName()));

        factories.put(PortletInvoker.class.getName(),
                      this.parameters.getParameter("portlet-invoker-factory", PortletInvokerFactoryImpl.class.getName()));

        factories.put(NamespaceMapper.class.getName(),
                      this.parameters.getParameter("namespace-mapper-factory", NamespaceMapperFactoryImpl.class.getName()));

        factories.put(ObjectIDFactory.class.getName(),
                      this.parameters.getParameter("objectid-factory", ObjectIDFactoryImpl.class.getName()));

        factories.put(ControllerFactory.class.getName(),
                      this.parameters.getParameter("controller-factory", ControllerFactoryImpl.class.getName()));

        for (Iterator iter = factories.entrySet().iterator(); iter.hasNext (); ) {
            Map.Entry me = (Map.Entry)iter.next();
            // try to get hold of the factory
            Factory factory = (Factory) ClassUtils.newInstance((String)me.getValue());

            if ( factory instanceof AbstractLogEnabled ) {
                ((AbstractLogEnabled)factory).setLogger(this.getLogger());
            }
            ContainerUtil.parameterize(factory, this.parameters);
            ContainerUtil.service(factory, this.manager);
            ContainerUtil.initialize(factory);

            factory.init(this.servletConfig, Collections.EMPTY_MAP);

            this.factoryMap.put(me.getKey(), factory);

            // build up list in reverse order for later destruction
            factoryList.add (0, factory);
        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        // destroy the services in reverse order
        for (Iterator iterator = factoryList.iterator (); iterator.hasNext (); ) {
            Factory factory = (Factory) iterator.next ();

            try {
                factory.destroy ();
            } catch (Exception exc) {
                // ignore it
            }
        }

        this.factoryList.clear();
        this.factoryMap.clear();
        super.dispose();
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
        return ((Factory) this.factoryMap.get (theClass.getName()));
    }
}
