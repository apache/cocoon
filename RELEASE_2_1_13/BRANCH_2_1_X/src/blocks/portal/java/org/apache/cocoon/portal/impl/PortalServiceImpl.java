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
package org.apache.cocoon.portal.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;

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
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.portal.PortalComponentManager;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.SkinDescription;
import org.apache.cocoon.servlet.CocoonServlet;

/**
 * Default implementation of a portal service using a session to store
 * custom information.
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id$
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
    
    protected Map portalConfigurations = new HashMap();
    
    protected Map skins = new HashMap();
    
    final protected static String KEY = PortalServiceImpl.class.getName();
    
    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager serviceManager) throws ServiceException {
        this.manager = serviceManager;
    }

    protected PortalServiceInfo getInfo() {
        final Request request = ContextHelper.getRequest( this.context );
        PortalServiceInfo info = (PortalServiceInfo) request.getAttribute(KEY);
        if ( info == null ) {
            info = new PortalServiceInfo();
            info.setup(ContextHelper.getObjectModel(this.context), this.portalComponentManagers);
            request.setAttribute(KEY, info);
        }
        return info;
    }
    
    /**
     * @see org.apache.cocoon.portal.PortalService#getPortalName()
     */
    public String getPortalName() {
        return this.getInfo().getPortalName();
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#setPortalName(java.lang.String)
     */
    public void setPortalName(String value) {
        this.getInfo().setPortalName(value);
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getAttribute(java.lang.String)
     */
    public Object getAttribute(String key) {
        return this.getInfo().getAttribute(key);
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String key, Object value) {
        this.getInfo().setAttribute(key, value);
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String key) {
        this.getInfo().removeAttribute(key);
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getAttributeNames()
     */
    public Iterator getAttributeNames() {
        return this.getInfo().getAttributeNames();
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getTemporaryAttribute(java.lang.String)
     */
    public Object getTemporaryAttribute(String key) {
        return this.getInfo().getTemporaryAttribute(key);
    }
    
    /**
     * @see org.apache.cocoon.portal.PortalService#setTemporaryAttribute(java.lang.String, java.lang.Object)
     */
    public void setTemporaryAttribute(String key, Object value) {
        this.getInfo().setTemporaryAttribute(key, value);
    }
    
    /**
     * @see org.apache.cocoon.portal.PortalService#removeTemporaryAttribute(java.lang.String)
     */
    public void removeTemporaryAttribute(String key) {
        this.getInfo().removeTemporaryAttribute(key);
    }
    
    /**
     * @see org.apache.cocoon.portal.PortalService#getTemporaryAttributeNames()
     */
    public Iterator getTemporaryAttributeNames() {
        return this.getInfo().getTemporaryAttributeNames();
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getComponentManager()
     */
    public PortalComponentManager getComponentManager() {
        return this.getInfo().getComponentManager();
    }

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
        // add the portal service to the servlet context - if available
        try {
            final ServletConfig servletConfig = (ServletConfig) context.get(CocoonServlet.CONTEXT_SERVLET_CONFIG);
            servletConfig.getServletContext().setAttribute(PortalService.ROLE, this);
        } catch (ContextException ignore) {
            // we ignore the context exception
            // this avoids startup errors if the portal is configured for the CLI
            // environment
            this.getLogger().warn("The portal service is not stored in the servlet config.", ignore);
        }
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        final Iterator i = this.portalComponentManagers.values().iterator();
        while ( i.hasNext() ) {
            ContainerUtil.dispose( i.next() );
        }
        this.portalComponentManagers.clear();       
        this.portalConfigurations.clear();
        // remove the portal service to the servlet context - if available
        try {
            final ServletConfig servletConfig = (ServletConfig) context.get(CocoonServlet.CONTEXT_SERVLET_CONFIG);
            servletConfig.getServletContext().removeAttribute(PortalService.ROLE);
        } catch (ContextException ignore) {
            // we ignore the context exception
        }
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        final Configuration[] portals = config.getChild("portals").getChildren("portal");
        for(int i=0; i < portals.length; i++ ) {
            final Configuration current = portals[i];
            final String name = current.getAttribute("name");
            try {
                PortalComponentManager c = new DefaultPortalComponentManager(this, this.context);
                this.portalComponentManagers.put( name, c );
                ContainerUtil.enableLogging( c, this.getLogger() );
                ContainerUtil.contextualize( c, this.context );
                ContainerUtil.service( c, this.manager );
                ContainerUtil.configure( c, current );
                ContainerUtil.initialize( c );
                
                this.portalConfigurations.put( name, current );
                
                // scan for skins
                final List skinList = new ArrayList();
                this.skins.put(name, skinList);
                final Configuration[] skinConfs = current.getChild("skins").getChildren("skin");
                if ( skinConfs != null ) {
                    for(int s=0;s<skinConfs.length;s++) {
                        final Configuration currentSkin = skinConfs[s];
                        final String skinName = currentSkin.getAttribute("name");
                        final SkinDescription desc = new SkinDescription();
                        desc.setName(skinName);
                        desc.setBasePath(currentSkin.getAttribute("base-path"));
                        desc.setThumbnailPath(currentSkin.getChild("thumbnail-path").getValue(null));
                        skinList.add(desc);
                    }
                }
            } catch (Exception e) {
                throw new ConfigurationException("Unable to setup new portal component manager for portal " + name, e);
            }
            
        }
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#setEntryLayout(java.lang.String, org.apache.cocoon.portal.layout.Layout)
     */
    public void setEntryLayout(String layoutKey, Layout object) {
        if ( layoutKey == null ) {
            layoutKey = this.getDefaultLayoutKey();
        }
        if ( object == null ) {
            this.removeTemporaryAttribute("DEFAULT_LAYOUT:" + layoutKey);
        } else {
            this.setTemporaryAttribute("DEFAULT_LAYOUT:" + layoutKey, object);
        }
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getEntryLayout(java.lang.String)
     */
    public Layout getEntryLayout(String layoutKey) {
        if ( layoutKey == null ) {
            layoutKey = this.getDefaultLayoutKey();
        }
        return (Layout)this.getTemporaryAttribute("DEFAULT_LAYOUT:" + layoutKey);
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#setDefaultLayoutKey(java.lang.String)
     */
    public void setDefaultLayoutKey(String layoutKey) {
        if ( layoutKey == null ) {
            this.removeAttribute("default-layout-key");
        } else {
            this.setAttribute("default-layout-key", layoutKey);
        }
    }

    public void setRenderable(Boolean renderable) {
        String key = getDefaultLayoutKey();
        if (renderable == null) {
            removeTemporaryAttribute("RENDER:" + key);
        } else {
            setTemporaryAttribute("RENDER:" + key, renderable);
        }
    }

    public Boolean isRenderable() {
        String key = getDefaultLayoutKey();
        Boolean bool = (Boolean)getTemporaryAttribute("RENDER:" + key);
        if (bool != null) {
            return bool;
        }
        return Boolean.TRUE;
    }
    
    /**
     * @see org.apache.cocoon.portal.PortalService#getDefaultLayoutKey()
     */
    public String getDefaultLayoutKey() {
        String key = (String)this.getAttribute("default-layout-key");
        if ( key == null ) {
            Configuration config = (Configuration)this.portalConfigurations.get(this.getPortalName());
            key = config.getAttribute("default-layout-key", "portal");
            if ( key != null ) {
                this.setDefaultLayoutKey(key);
            }
        }
        return key;
    }
   
    /**
     * @see org.apache.cocoon.portal.PortalService#getSkinDescriptions()
     */
    public List getSkinDescriptions() {
        return (List)this.skins.get(this.getPortalName());
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getObjectModel()
     */
    public Map getObjectModel() {
        return ContextHelper.getObjectModel(this.context);
    }
}
