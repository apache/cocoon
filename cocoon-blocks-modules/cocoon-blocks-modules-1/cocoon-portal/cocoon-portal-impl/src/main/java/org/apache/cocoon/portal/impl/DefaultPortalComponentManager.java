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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.portal.LinkService;
import org.apache.cocoon.portal.PortalComponentManager;
import org.apache.cocoon.portal.PortalManager;
import org.apache.cocoon.portal.PortalRuntimeException;
import org.apache.cocoon.portal.coplet.CopletFactory;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.layout.LayoutFactory;
import org.apache.cocoon.portal.layout.renderer.Renderer;
import org.apache.cocoon.portal.profile.ProfileManager;

/**
 * Default {@link PortalComponentManager} implementation.
 *
 * @version $Id$
 */
public class DefaultPortalComponentManager
    implements PortalComponentManager {

    /** The avalon component manager. */
    protected ServiceManager manager;

    /** The profile manager. */
    protected ProfileManager profileManager;

    /** The link service. */
    protected LinkService linkService;

    /** The used renderers. */
    protected Map renderers = new HashMap();

    /** The used coplet adapters. */
    protected Map copletAdapters = new HashMap();

    /** The coplet factory. */
    protected CopletFactory copletFactory;

    /** The layout factory. */
    protected LayoutFactory layoutFactory;

    /** The event manager. */
    protected EventManager eventManager;

    /** The portal manager. */
    protected PortalManager portalManager;

    /**
     * Create a new portal component manager. Each portal has a own
     * component manager that manages all central components for this
     * portal.
     */
    public DefaultPortalComponentManager(ServiceManager manager) {
        this.manager = manager;
    }

    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getLinkService()
     */
    public LinkService getLinkService() {
        if ( null == this.linkService ) {
            try {
                this.linkService = (LinkService)this.manager.lookup( LinkService.ROLE );
            } catch (ServiceException e) {
                throw new PortalRuntimeException("Unable to lookup link service.", e);
            }
        }
        return this.linkService;
    }

    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getProfileManager()
     */
    public ProfileManager getProfileManager() {
        if ( null == this.profileManager ) {
            try {
                this.profileManager = (ProfileManager)this.manager.lookup( ProfileManager.ROLE );
            } catch (ServiceException e) {
                throw new PortalRuntimeException("Unable to lookup profile manager.", e);
            }
        }
        return this.profileManager;
    }

    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getEventManager()
     */
    public EventManager getEventManager() {
        if ( null == this.eventManager ) {
            try {
                this.eventManager = (EventManager)this.manager.lookup( EventManager.ROLE );
            } catch (ServiceException e) {
                throw new PortalRuntimeException("Unable to lookup event manager.", e);
            }
        }
        return this.eventManager;
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if (this.manager != null) {
            Iterator i = this.renderers.values().iterator();
            while (i.hasNext()) {
                this.manager.release(i.next());
            }
            this.renderers.clear();
            i = this.copletAdapters.values().iterator();
            while (i.hasNext()) {
                this.manager.release(i.next());
            }
            this.copletAdapters.clear();
            this.manager.release(this.profileManager);
            this.profileManager = null;
            this.manager.release(this.linkService);
            this.linkService = null;
            this.manager.release(this.copletFactory);
            this.copletFactory = null;
            this.manager.release(this.layoutFactory);
            this.layoutFactory = null;
            this.manager.release(this.eventManager);
            this.eventManager = null;
            this.manager.release(this.portalManager);
            this.portalManager = null;
            this.manager = null;
        }
    }

    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getRenderer(java.lang.String)
     */
    public Renderer getRenderer(String name) {
        Renderer o = (Renderer) this.renderers.get( name );
        if ( o == null ) {
            try {
                o = (Renderer) this.manager.lookup( Renderer.ROLE + '/' + name );
                this.renderers.put( name, o );
            } catch (ServiceException e) {
                throw new PortalRuntimeException("Unable to lookup renderer with name " + name, e);
            }
        }
        return o;
    }

    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getCopletAdapter(java.lang.String)
     */
    public CopletAdapter getCopletAdapter(String name) {
        CopletAdapter o = (CopletAdapter) this.copletAdapters.get( name );
        if ( o == null ) {
            try {
                o = (CopletAdapter) this.manager.lookup( CopletAdapter.ROLE + '/' + name );
                this.copletAdapters.put( name, o );
            } catch (ServiceException e) {
                throw new PortalRuntimeException("Unable to lookup coplet adapter with name " + name, e);
            }
        }
        return o;
    }

    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getCopletFactory()
     */
    public CopletFactory getCopletFactory() {
        if ( null == this.copletFactory ) {
            try {
                this.copletFactory = (CopletFactory)this.manager.lookup( CopletFactory.ROLE );
            } catch (ServiceException e) {
                throw new PortalRuntimeException("Unable to lookup coplet factory.", e);
            }
        }
        return this.copletFactory;
    }

    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getLayoutFactory()
     */
    public LayoutFactory getLayoutFactory() {
        if ( null == this.layoutFactory ) {
            try {
                this.layoutFactory = (LayoutFactory)this.manager.lookup( LayoutFactory.ROLE );
            } catch (ServiceException e) {
                throw new PortalRuntimeException("Unable to lookup layout factory.", e);
            }
        }
        return this.layoutFactory;
    }

    /**
     * @see org.apache.cocoon.portal.PortalComponentManager#getPortalManager()
     */
    public PortalManager getPortalManager() {
        if ( null == this.portalManager ) {
            try {
                this.portalManager = (PortalManager)this.manager.lookup( PortalManager.ROLE );
            } catch (ServiceException e) {
                throw new PortalRuntimeException("Unable to lookup portal manager.", e);
            }
        }
        return this.portalManager;
    }
}
