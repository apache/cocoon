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
 * @version CVS $Id: PortletContainerEnvironmentImpl.java,v 1.3 2004/02/02 10:26:43 cziegeler Exp $
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
