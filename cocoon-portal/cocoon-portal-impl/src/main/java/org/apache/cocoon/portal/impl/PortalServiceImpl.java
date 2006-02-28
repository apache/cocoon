/*
 * Copyright 1999-2002,2004-2005 The Apache Software Foundation.
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
package org.apache.cocoon.portal.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
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
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.PortalComponentManager;
import org.apache.cocoon.portal.PortalManager;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.layout.LayoutFactory;
import org.apache.cocoon.portal.layout.SkinDescription;
import org.apache.cocoon.portal.layout.renderer.Renderer;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.cocoon.servlet.CocoonServlet;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

/**
 * Default implementation of a portal service using a session to store
 * custom information.
 *
 * @version $Id$
 */
public class PortalServiceImpl
    extends AbstractLogEnabled
    implements Serviceable,
                ThreadSafe, 
                PortalService, 
                Contextualizable,
                Disposable,
                Configurable {

    /** The component context. */
    protected Context context;

    /** The service locator. */
    protected ServiceManager manager;

    /** The manager for some core portal components. */
    protected PortalComponentManager portalComponentManager;

    /** The list of skins. */
    protected List skinList = new ArrayList();

    /** The name of the portal. */
    protected String portalName;

    /** The default layout key. */
    protected String defaultLayoutKey;

    /** The attribute prefix used to prefix attributes in the session and request. */
    protected String attributePrefix;

    final protected static String KEY = PortalServiceImpl.class.getName();

    /** The portal configuration. */
    protected Configuration config;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager serviceManager) throws ServiceException {
        this.manager = serviceManager;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getPortalName()
     */
    public String getPortalName() {
        return this.portalName;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getAttribute(java.lang.String)
     */
    public Object getAttribute(String key) {
        final Session session = ContextHelper.getRequest(this.context).getSession(false);
        if (session == null) {
            return null;
        }
        return session.getAttribute( this.attributePrefix + key);
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String key, Object value) {
        final Session session = ContextHelper.getRequest(this.context).getSession();
        session.setAttribute( this.attributePrefix + key, value);
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#removeAttribute(java.lang.String)
     */
    public Object removeAttribute(String key) {
        final Session session = ContextHelper.getRequest(this.context).getSession(false);
        if ( session != null ) {
            Object value = session.getAttribute(this.attributePrefix + key);
            if ( value != null ) {
                session.removeAttribute( this.attributePrefix + key );
            }
            return value;
        }
        return null;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getAttributeNames()
     */
    public Iterator getAttributeNames() {
        final Session session = ContextHelper.getRequest(this.context).getSession(false);
        if ( session != null ) {
            List names = new ArrayList();
            Enumeration e = session.getAttributeNames();
            final int pos = this.attributePrefix.length() + 1;
            if ( e != null ) {
                while ( e.hasMoreElements() ) {
                    final String name = (String)e.nextElement();
                    if ( name.startsWith( this.attributePrefix )) {
                        names.add( name.substring( pos ) );
                    }
                }
            }
            return names.iterator();
        }
        return Collections.EMPTY_MAP.keySet().iterator();
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getTemporaryAttribute(java.lang.String)
     */
    public Object getTemporaryAttribute(String key) {
        final Request request = ContextHelper.getRequest(this.context);
        return request.getAttribute(this.attributePrefix + key);
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#setTemporaryAttribute(java.lang.String, java.lang.Object)
     */
    public void setTemporaryAttribute(String key, Object value) {
        final Request request = ContextHelper.getRequest(this.context);
        request.setAttribute( this.attributePrefix + key, value );
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#removeTemporaryAttribute(java.lang.String)
     */
    public Object removeTemporaryAttribute(String key) {
        final Request request = ContextHelper.getRequest(this.context);
        final Object oldValue = request.getAttribute(this.attributePrefix + key);
        if ( oldValue != null ) {
            request.removeAttribute( this.attributePrefix + key );
        }
        return oldValue;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getTemporaryAttributeNames()
     */
    public Iterator getTemporaryAttributeNames() {
        final Request request = ContextHelper.getRequest(this.context);
        List names = new ArrayList();
        Enumeration e = request.getAttributeNames();
        final int pos = this.attributePrefix.length() + 1;
        if ( e != null ) {
            while ( e.hasMoreElements() ) {
                final String name = (String)e.nextElement();
                if ( name.startsWith( this.attributePrefix )) {
                    names.add( name.substring( pos ) );
                }
            }
        }
        return names.iterator();
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getComponentManager()
     */
    public PortalComponentManager getComponentManager() {
        return this.portalComponentManager;
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
        ContainerUtil.dispose( this.portalComponentManager );
        // remove the portal service from the servlet context - if available
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
        final Configuration portal = config.getChild("portal", false);
        if ( portal == null ) {
            throw new ConfigurationException("No portal configured.", config);
        }
        this.portalName = portal.getAttribute("name");
        this.defaultLayoutKey = portal.getAttribute("default-layout-key", "portal");
        this.attributePrefix = this.getClass().getName() + '/' + this.portalName + '/';
        SourceResolver resolver = null;
        try {
            resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
            PortalComponentManager c = new DefaultPortalComponentManager(this, this.context);
            this.portalComponentManager = c;
            ContainerUtil.enableLogging( c, this.getLogger() );
            ContainerUtil.contextualize( c, this.context );
            ContainerUtil.service( c, this.manager );
            ContainerUtil.configure( c, portal );
            ContainerUtil.initialize( c );

            // scan for skins
            final Configuration[] skinConfs = portal.getChild("skins").getChildren("skin");
            if ( skinConfs != null ) {
                for(int s=0;s<skinConfs.length;s++) {
                    final Configuration currentSkin = skinConfs[s];
                    final String skinName = currentSkin.getAttribute("name");
                    final SkinDescription desc = new SkinDescription();
                    desc.setName(skinName);
                    Source source = null;
                    try {
                        source = resolver.resolveURI(currentSkin.getAttribute("base-path"));
                        desc.setBasePath(source.getURI());
                    } finally {
                        resolver.release(source);
                    }
                    desc.setThumbnailPath(currentSkin.getChild("thumbnail-path").getValue(null));
                    this.skinList.add(desc);
                }
            }
        } catch (ConfigurationException ce) {
            throw ce;
        } catch (Exception e) {
            throw new ConfigurationException("Unable to setup new portal component manager for portal " + this.portalName, e);
        } finally {
            this.manager.release(resolver);
        }
        this.config = portal.getChild("configuration");
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

    /**
     * @see org.apache.cocoon.portal.PortalService#getDefaultLayoutKey()
     */
    public String getDefaultLayoutKey() {
        String key = (String)this.getAttribute("default-layout-key");
        if ( key == null ) {
            key = this.defaultLayoutKey;
            this.setDefaultLayoutKey(key);
        }
        return key;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getSkinDescriptions()
     */
    public List getSkinDescriptions() {
        return this.skinList;
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getObjectModel()
     */
    public Map getObjectModel() {
        return ContextHelper.getObjectModel(this.context);
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getConfiguration(java.lang.String, java.lang.String)
     */
    public String getConfiguration(String key, String defaultValue) {
        return this.config.getChild(key).getValue(defaultValue);
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getConfiguration(java.lang.String)
     */
    public String getConfiguration(String key) {
        return this.getConfiguration(key, null);
    }

    /**
     * @see org.apache.cocoon.portal.PortalService#getConfigurationAsBoolean(java.lang.String, boolean)
     */
    public boolean getConfigurationAsBoolean(String key, boolean defaultValue) {
        return this.config.getChild(key).getValueAsBoolean(defaultValue);
    }

    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getComponentContext()
     */
    public Context getComponentContext() {
        return this.context;
    }

    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getCopletAdapter(java.lang.String)
     */
    public CopletAdapter getCopletAdapter(String name) {
        return this.getComponentManager().getCopletAdapter(name);
    }

    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getCopletFactory()
     */
    public CopletFactory getCopletFactory() {
        return this.getComponentManager().getCopletFactory();
    }

    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getEventManager()
     */
    public EventManager getEventManager() {
        return this.getComponentManager().getEventManager();
    }

    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getLayoutFactory()
     */
    public LayoutFactory getLayoutFactory() {
        return this.getComponentManager().getLayoutFactory();
    }

    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getLinkService()
     */
    public LinkService getLinkService() {
        return this.getComponentManager().getLinkService();
    }

    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getPortalManager()
     */
    public PortalManager getPortalManager() {
        return this.getComponentManager().getPortalManager();
    }

    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getProfileManager()
     */
    public ProfileManager getProfileManager() {
        return this.getComponentManager().getProfileManager();
    }

    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getRenderer(java.lang.String)
     */
    public Renderer getRenderer(String name) {
        return this.getComponentManager().getRenderer(name);
    }
}
