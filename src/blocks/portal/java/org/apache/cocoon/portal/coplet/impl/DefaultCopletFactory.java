/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
package org.apache.cocoon.portal.coplet.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.aspect.AspectDataHandler;
import org.apache.cocoon.portal.aspect.AspectDataStore;
import org.apache.cocoon.portal.aspect.AspectDescription;
import org.apache.cocoon.portal.aspect.impl.DefaultAspectDataHandler;
import org.apache.cocoon.portal.aspect.impl.DefaultAspectDescription;
import org.apache.cocoon.portal.coplet.CopletData;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;

/**
 * This factory is for creating and managing coplet objects
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: DefaultCopletFactory.java,v 1.9 2004/03/05 13:02:11 bdelacretaz Exp $
 */
public class DefaultCopletFactory  
    extends AbstractLogEnabled 
    implements Component, ThreadSafe, CopletFactory, Serviceable, Disposable, Configurable {
    
    protected ServiceManager manager;
    
    protected Map coplets = new HashMap();
    
    protected List descriptions = new ArrayList();
    
    protected ServiceSelector storeSelector;

    public void prepare(CopletData copletData)
    throws ProcessingException {
        if ( copletData != null ) {
     
            final String copletName = copletData.getName();
            if ( copletName == null ) {
                throw new ProcessingException("CopletData "+copletData.getId()+" has no associated name.");
            }
            Object[] o = (Object[]) this.coplets.get( copletName );
            
            if ( o == null ) {
                throw new ProcessingException("CopletDescription with name " + copletName + " not found.");
            }
            DefaultCopletDescription copletDescription = (DefaultCopletDescription)o[0];

            copletData.setDescription( copletDescription );
            copletData.setAspectDataHandler((AspectDataHandler)o[1]);
            
        }
    }
    
    public void prepare(CopletInstanceData copletInstanceData)
    throws ProcessingException {
        if ( copletInstanceData != null ) {
     
            final String copletName = copletInstanceData.getName();
            if ( copletName == null ) {
                throw new ProcessingException("CopletInstanceData "+copletInstanceData.getId()+" has no associated name.");
            }
            Object[] o = (Object[]) this.coplets.get( copletName );
            
            if ( o == null ) {
                throw new ProcessingException("CopletDescription with name " + copletName + " not found.");
            }
            DefaultCopletDescription copletDescription = (DefaultCopletDescription)o[0];

            copletInstanceData.setDescription( copletDescription );
            copletInstanceData.setAspectDataHandler((AspectDataHandler)o[2]);
            
        }
    }

    
    public CopletInstanceData newInstance(CopletData copletData)
    throws ProcessingException {
        String name = copletData.getName();
        Object[] o = (Object[]) this.coplets.get( name );
            
        if ( o == null ) {
            throw new ProcessingException("CopletDescription with name " + name + " not found.");
        }
        DefaultCopletDescription copletDescription = (DefaultCopletDescription)o[0];
        
        CopletInstanceData instance = new CopletInstanceData();
        
        String id = null;
        if ( copletDescription.createId() ) {
            // TODO - create unique id
            id = name + '-' + System.currentTimeMillis();
        }
        instance.initialize( name, id );
        
        instance.setDescription( copletDescription );
        instance.setAspectDataHandler((AspectDataHandler)o[2]);
        instance.setCopletData(copletData);
        
        // now lookup the adapter
        final String adapterName = copletData.getCopletBaseData().getCopletAdapterName();
        CopletAdapter adapter = null;
        ServiceSelector adapterSelector = null;
        try {
            adapterSelector = (ServiceSelector) this.manager.lookup( CopletAdapter.ROLE + "Selector");
            adapter = (CopletAdapter)adapterSelector.select( adapterName );
            adapter.init( instance );
            adapter.login( instance );
        } catch (ServiceException ce) {
            throw new ProcessingException("Unable to lookup coplet adapter selector or adaptor.", ce);
        } finally {
            if ( adapterSelector != null) {
                adapterSelector.release( adapter );
            }
            this.manager.release( adapterSelector );
        }
        
        PortalService service = null;
        try {
            service = (PortalService)this.manager.lookup(PortalService.ROLE);
            service.getComponentManager().getProfileManager().register(instance);
        } catch (ServiceException ce) {
            throw new ProcessingException("Unable to lookup profile manager.", ce);
        } finally {
            this.manager.release( service );
        }
        return instance;
    }
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.storeSelector = (ServiceSelector)this.manager.lookup( AspectDataStore.ROLE+"Selector" );
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( this.storeSelector );
            this.storeSelector = null;
            this.manager = null;
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration) 
    throws ConfigurationException {
        final Configuration[] copletsConf = configuration.getChild("coplets").getChildren("coplet");
        if ( copletsConf != null ) {
            for(int i=0; i < copletsConf.length; i++ ) {
                DefaultCopletDescription desc = new DefaultCopletDescription();
                DefaultCopletDescription instanceDesc = new DefaultCopletDescription();
                final String name = copletsConf[i].getAttribute("name");
                
                // unique test
                if ( this.coplets.get(name) != null) {
                    throw new ConfigurationException("Coplet name must be unique. Double definition for " + name);
                }
                desc.setName(copletsConf[i].getAttribute("name"));
                instanceDesc.setName(copletsConf[i].getAttribute("name"));
                instanceDesc.setCreateId(copletsConf[i].getAttributeAsBoolean("create-id", true));
                
                // and now the aspects
                Configuration[] aspectsConf = copletsConf[i].getChild("coplet-data-aspects").getChildren("aspect");
                if (aspectsConf != null) {
                    for(int m=0; m < aspectsConf.length; m++) {
                        AspectDescription adesc = DefaultAspectDescription.newInstance(aspectsConf[m]);
                        desc.addAspectDescription( adesc );
                    }
                }

                // and now the aspects of the instances
                aspectsConf = copletsConf[i].getChild("coplet-instance-data-aspects").getChildren("aspect");
                if (aspectsConf != null) {
                    for(int m=0; m < aspectsConf.length; m++) {
                        AspectDescription adesc = DefaultAspectDescription.newInstance(aspectsConf[m]);
                        instanceDesc.addAspectDescription( adesc );
                    }
                }

                DefaultAspectDataHandler handler = new DefaultAspectDataHandler(desc, this.storeSelector);
                DefaultAspectDataHandler instanceHandler = new DefaultAspectDataHandler(instanceDesc, this.storeSelector);
                this.coplets.put(desc.getName(), new Object[] {desc, handler, instanceHandler});
                this.descriptions.add(desc);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.coplet.CopletFactory#remove(org.apache.cocoon.portal.coplet.CopletInstanceData)
     */
    public void remove(CopletInstanceData copletInstanceData) 
    throws ProcessingException {
        if ( copletInstanceData != null ) {
            // now lookup the adapter
            final String adapterName = copletInstanceData.getCopletData().getCopletBaseData().getCopletAdapterName();
            CopletAdapter adapter = null;
            ServiceSelector adapterSelector = null;
            try {
                adapterSelector = (ServiceSelector) this.manager.lookup( CopletAdapter.ROLE + "Selector");
                adapter = (CopletAdapter)adapterSelector.select( adapterName );
                adapter.logout( copletInstanceData );
                adapter.destroy( copletInstanceData );
            } catch (ServiceException ce) {
                throw new ProcessingException("Unable to lookup coplet adapter selector or adaptor.", ce);
            } finally {
                if ( adapterSelector != null) {
                    adapterSelector.release( adapter );
                }
                this.manager.release( adapterSelector );
            }
            
            PortalService service = null;
            try {
                service = (PortalService)this.manager.lookup(PortalService.ROLE);
                service.getComponentManager().getProfileManager().unregister(copletInstanceData);
            } catch (ServiceException ce) {
                throw new ProcessingException("Unable to lookup portal service.", ce);
            } finally {
                this.manager.release( service );
            }
        }
    }

}
