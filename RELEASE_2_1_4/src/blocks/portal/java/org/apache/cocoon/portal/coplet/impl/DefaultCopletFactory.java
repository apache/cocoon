/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: DefaultCopletFactory.java,v 1.8 2003/10/20 13:36:42 cziegeler Exp $
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
