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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.portal.pluto.om.PortletDefinitionRegistry;
import org.apache.cocoon.portal.pluto.om.PortletDefinitionRegistryImpl;
import org.apache.cocoon.portal.pluto.service.log.LogServiceImpl;
import org.apache.cocoon.portal.pluto.services.factory.FactoryManagerServiceImpl;
import org.apache.pluto.services.ContainerService;
import org.apache.pluto.services.PortletContainerEnvironment;
import org.apache.pluto.services.factory.FactoryManagerService;
import org.apache.pluto.services.information.InformationProviderService;
import org.apache.pluto.services.log.LogService;
import org.apache.pluto.services.title.DynamicTitleService;

/**
 * 
 *
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: PortletContainerEnvironmentImpl.java,v 1.4 2004/03/05 13:02:14 bdelacretaz Exp $
 */
public class PortletContainerEnvironmentImpl 
extends AbstractLogEnabled
implements PortletContainerEnvironment, Serviceable, Disposable, Initializable, Contextualizable {

    /** The service manager */
    protected ServiceManager manager;
    
    /** Services */
    protected Map services = new HashMap();
    
    /** Static services */
    protected Map staticServices = new HashMap();
    
    /** Context */
    protected Context context;
    
    /**
     * Serviceable
     */
    public void service(ServiceManager manager) {
        this.manager = manager;
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) {
        this.context = context;        
    }
    
    /* (non-Javadoc)
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
    }

    /**
     * Initialize a service
     */
    protected Object init(Object o) 
    throws Exception {
        ContainerUtil.enableLogging(o, this.getLogger());
        ContainerUtil.contextualize(o, this.context);
        ContainerUtil.service(o, this.manager);
        if ( o instanceof PortletContainerEnabled ) {
            ((PortletContainerEnabled)o).setPortletContainerEnvironment(this);
        }
        ContainerUtil.initialize(o);
        return o;
    }

    /* (non-Javadoc)
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
    
    /* (non-Javadoc)
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
                    throw new CascadingRuntimeException("Unable to lookup service " + key, se);
                }
            }
        }
        return service;
    }

}
