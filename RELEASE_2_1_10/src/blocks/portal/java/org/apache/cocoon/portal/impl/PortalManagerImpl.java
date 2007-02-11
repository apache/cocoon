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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.portal.PortalManager;
import org.apache.cocoon.portal.PortalManagerAspect;
import org.apache.cocoon.portal.PortalManagerAspectPrepareContext;
import org.apache.cocoon.portal.PortalManagerAspectRenderContext;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.renderer.Renderer;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id$
 */
public class PortalManagerImpl
	extends AbstractLogEnabled
	implements PortalManager, Serviceable, Disposable, ThreadSafe, Contextualizable, PortalManagerAspect, Configurable {

    /** The service manager */
    protected ServiceManager manager;

    /** The portal service */
    protected PortalService portalService;

    protected PortalManagerAspectChain chain;

    protected ServiceSelector aspectSelector;
    protected ServiceSelector adapterSelector;

    /** The component context. */
    protected Context context;

    /** Indicates whether navigation appears on full screen portlets */
    private boolean fullScreenNav;
    public static final String FULLSCREEN = "fullScreenNav";

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(ServiceManager)
     */
    public void service(ServiceManager serviceManager)
    throws ServiceException {
        this.manager = serviceManager;
        this.portalService = (PortalService)this.manager.lookup(PortalService.ROLE);
        if ( this.manager.hasService(PortalManagerAspect.ROLE+"Selector") ) {
            this.aspectSelector = (ServiceSelector) this.manager.lookup( PortalManagerAspect.ROLE+"Selector");
        }
        this.adapterSelector = (ServiceSelector)this.manager.lookup(CopletAdapter.ROLE+"Selector");
    }

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            this.manager.release(this.portalService);
            this.portalService = null;
            if ( this.chain != null) {
                this.chain.dispose( this.aspectSelector, this.adapterSelector );
            }
            this.manager.release( this.aspectSelector );
            this.aspectSelector = null;
            this.manager.release( this.adapterSelector );
            this.adapterSelector = null;
            this.manager = null;
        }
    }

    /**
     * @see org.apache.cocoon.portal.PortalManager#process()
     */
    public void process()
    throws ProcessingException {
        DefaultPortalManagerAspectContext aspectContext =
            new DefaultPortalManagerAspectContext(this.chain,
                                                  this.portalService,
                                                  ContextHelper.getObjectModel(this.context));
        aspectContext.invokeNext();
    }

	/**
	 * @see PortalManager#showPortal(ContentHandler, Parameters)
	 */
	public void showPortal(ContentHandler contentHandler, Parameters parameters) 
    throws SAXException {
        DefaultPortalManagerAspectContext aspectContext =
            new DefaultPortalManagerAspectContext(this.chain,
                                                  this.portalService,
                                                  ContextHelper.getObjectModel(this.context));
        aspectContext.invokeNext(contentHandler, parameters);
	}

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration conf) throws ConfigurationException {
        this.chain = new PortalManagerAspectChain();
        this.chain.configure(this.aspectSelector, 
                             this.adapterSelector, 
                             conf.getChild("aspects"), 
                             this, 
                             new Parameters());
        this.fullScreenNav = conf.getChild(FULLSCREEN, true).getValueAsBoolean(false);
    }

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /**
     * @see org.apache.cocoon.portal.PortalManagerAspect#prepare(org.apache.cocoon.portal.PortalManagerAspectPrepareContext, org.apache.cocoon.portal.PortalService)
     */
    public void prepare(PortalManagerAspectPrepareContext context, PortalService service) throws ProcessingException {
        EventManager eventManager = this.portalService.getComponentManager().getEventManager();
        eventManager.processEvents();
    }

    /**
     * @see org.apache.cocoon.portal.PortalManagerAspect#render(org.apache.cocoon.portal.PortalManagerAspectRenderContext, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler, org.apache.avalon.framework.parameters.Parameters)
     */
    public void render(PortalManagerAspectRenderContext context, PortalService service, ContentHandler ch, Parameters parameters) throws SAXException {
        // first check for a full screen layout

        Layout portalLayout = null;
        Boolean renderable = (service.getEntryLayout(null) == null) ?
                Boolean.TRUE : Boolean.FALSE;
        if (!this.fullScreenNav) {
            // If fullscreen mode - otherwise the aspects will deal with the layout
            portalLayout = service.getEntryLayout(null);
            renderable = Boolean.TRUE;
        }
        if ( portalLayout == null ) {
            portalLayout = service.getComponentManager().getProfileManager().getPortalLayout(null, null);
        }
        service.setRenderable(renderable);

        Renderer portalLayoutRenderer = this.portalService.getComponentManager().getRenderer( portalLayout.getRendererName());       

        ch.startDocument();
        portalLayoutRenderer.toSAX(portalLayout, this.portalService, ch);
        ch.endDocument();
        service.setRenderable(null);
    }
}
