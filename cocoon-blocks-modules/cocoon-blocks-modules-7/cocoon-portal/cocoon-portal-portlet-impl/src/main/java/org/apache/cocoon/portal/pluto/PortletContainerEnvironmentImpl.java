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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.portal.pluto.om.PortletDefinitionRegistry;
import org.apache.cocoon.portal.pluto.om.PortletDefinitionRegistryImpl;
import org.apache.cocoon.portal.pluto.service.log.LogServiceImpl;
import org.apache.cocoon.portal.pluto.services.PropertyManagerServiceImpl;
import org.apache.cocoon.portal.pluto.services.factory.FactoryManagerServiceImpl;
import org.apache.cocoon.portal.PortalRuntimeException;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.pluto.services.ContainerService;
import org.apache.pluto.services.PortletContainerEnvironment;
import org.apache.pluto.services.factory.FactoryManagerService;
import org.apache.pluto.services.information.InformationProviderService;
import org.apache.pluto.services.log.LogService;
import org.apache.pluto.services.property.PropertyManagerService;
import org.apache.pluto.services.title.DynamicTitleService;

/**
 *
 *
 * @version $Id$
 */
public class PortletContainerEnvironmentImpl 
    extends AbstractLogEnabled
    implements PortletContainerEnvironment,
               Serviceable,
               Disposable,
               Initializable,
               Parameterizable {

    /** The service manager. */
    protected ServiceManager manager;
    
    /** Services. */
    protected Map services = new HashMap();
    
    /** Static services. */
    protected Map staticServices = new HashMap();
    
    /** Configuration. */
    protected Parameters parameters;

    /**
     * @see org.apache.avalon.framework.parameters.Parameterizable#parameterize(org.apache.avalon.framework.parameters.Parameters)
     */
    public void parameterize(Parameters params)
    throws ParameterException {
        this.parameters = params;
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager serviceManager) {
        this.manager = serviceManager;
    }
    
    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        this.staticServices.put(LogService.class.getName(), 
                                this.init(new LogServiceImpl(this.getLogger())));
        this.staticServices.put(PortletDefinitionRegistry.class.getName(),
                                this.init(new PortletDefinitionRegistryImpl()));
        this.staticServices.put(InformationProviderService.class.getName(),
                this.init(new InformationProviderServiceImpl()));
        this.staticServices.put(FactoryManagerService.class.getName(),
                this.init(new FactoryManagerServiceImpl()));
        this.staticServices.put(DynamicTitleService.class.getName(), 
                this.init(new DynamicTitleServiceImpl()));
        this.staticServices.put(PropertyManagerService.class.getName(),
        		this.init(new PropertyManagerServiceImpl()));
        this.staticServices.put(PortletPreferencesProvider.class.getName(),
                this.init(new PortletPreferencesProviderImpl()));
    }

    /**
     * Initialize a service
     */
    protected Object init(Object o) 
    throws Exception {
        if ( o instanceof AbstractLogEnabled ) {
            ((AbstractLogEnabled)o).setLogger(this.getLogger());
        }
        ContainerUtil.parameterize(o, this.parameters);
        ContainerUtil.service(o, this.manager);
        if ( o instanceof PortletContainerEnabled ) {
            ((PortletContainerEnabled)o).setPortletContainerEnvironment(this);
        }
        ContainerUtil.initialize(o);
        return o;
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            Iterator i = this.services.entrySet().iterator();
            while ( i.hasNext() ) {
                this.manager.release(i.next());
            }
            this.services.clear();
            this.manager = null;
        }
    }
    
    /**
     * @see org.apache.pluto.services.PortletContainerEnvironment#getContainerService(java.lang.Class)
     */
    public ContainerService getContainerService(Class serviceClazz) {
        final String key = serviceClazz.getName();
        ContainerService service = (ContainerService) this.staticServices.get(key);
        if ( service == null ) {
            service = (ContainerService) this.services.get(key);
            if ( service == null ) {
                try {
                    service = (ContainerService)this.manager.lookup(key);
                    this.services.put(key, service);
                } catch (ServiceException se) {
                    throw new PortalRuntimeException("Unable to lookup service " + key, se);
                }
            }
        }
        return service;
    }
}
