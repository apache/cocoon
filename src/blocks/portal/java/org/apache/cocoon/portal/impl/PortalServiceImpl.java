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
package org.apache.cocoon.portal.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.persistence.RequestDataStore;
import org.apache.cocoon.portal.PortalComponentManager;
import org.apache.cocoon.portal.PortalService;

/**
 * Default implementation of a portal service using a session to store
 * custom information.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: PortalServiceImpl.java,v 1.9 2003/10/20 13:36:56 cziegeler Exp $
 */
public class PortalServiceImpl
    extends AbstractLogEnabled
    implements Serviceable,
                ThreadSafe, 
                PortalService, 
                Contextualizable,
                Disposable,
                Configurable {

    protected Context context;
    
    protected ServiceManager manager;

    protected Map portalComponentManagers = new HashMap();
    
    protected RequestDataStore dataStore;
    
    final protected String key = this.getClass().getName();
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager serviceManager) throws ServiceException {
        this.manager = serviceManager;
        this.dataStore = (RequestDataStore) this.manager.lookup(RequestDataStore.ROLE);
    }

    protected PortalServiceInfo getInfo() {
        PortalServiceInfo info = (PortalServiceInfo) this.dataStore.getRequestData(this.key);
        if ( info == null ) {
            info = new PortalServiceInfo();
            info.setup(ContextHelper.getObjectModel(this.context), this.portalComponentManagers);
            this.dataStore.setRequestData(this.key, info);
        }
        return info;
    }
    
    public String getPortalName() {
        return this.getInfo().getPortalName();
    }

    public void setPortalName(String value) {
        this.getInfo().setPortalName(value);
    }

    public Object getAttribute(String key) {
        return this.getInfo().getAttribute(key);
    }

    public void setAttribute(String key, Object value) {
        this.getInfo().setAttribute(key, value);
    }

    public void removeAttribute(String key) {
        this.getInfo().removeAttribute(key);
    }

    public Iterator getAttributeNames() {
        return this.getInfo().getAttributeNames();
    }

    public Object getTemporaryAttribute(String key) {
        return this.getInfo().getTemporaryAttribute(key);
    }
    
    public void setTemporaryAttribute(String key, Object value) {
        this.getInfo().setTemporaryAttribute(key, value);
    }
    
    public void removeTemporaryAttribute(String key) {
        this.getInfo().removeTemporaryAttribute(key);
    }
    
    public Iterator getTemporaryAttributeNames() {
        return this.getInfo().getTemporaryAttributeNames();
    }

    /**
     * Return the component manager for the current portal
     */
    public PortalComponentManager getComponentManager() {
        return this.getInfo().getComponentManager();
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release( this.dataStore );
            this.manager = null;
            this.dataStore = null;
        }
        final Iterator i = this.portalComponentManagers.values().iterator();
        while ( i.hasNext() ) {
            ContainerUtil.dispose( i.next() );
        }
        this.portalComponentManagers.clear();       
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        final Configuration[] portals = config.getChild("portals").getChildren("portal");
        for(int i=0; i < portals.length; i++ ) {
            final Configuration current = portals[i];
            final String name = current.getAttribute("name");
            try {
                PortalComponentManager c = new DefaultPortalComponentManager();
                this.portalComponentManagers.put( name, c );
                ContainerUtil.enableLogging( c, this.getLogger() );
                ContainerUtil.contextualize( c, this.context );
                ContainerUtil.service( c, this.manager );
                ContainerUtil.configure( c, current );
                ContainerUtil.initialize( c );
            } catch (Exception e) {
                throw new ConfigurationException("Unable to setup new portal component manager for portal " + name, e);
            }
            
        }
    }

}
