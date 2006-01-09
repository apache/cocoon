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

import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.ajax.AjaxHelper;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.portal.PortalManager;
import org.apache.cocoon.portal.PortalManagerAspect;
import org.apache.cocoon.portal.PortalManagerAspectPrepareContext;
import org.apache.cocoon.portal.PortalManagerAspectRenderContext;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.CopletInstanceDataFeatures;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutFeatures;
import org.apache.cocoon.portal.layout.renderer.Renderer;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @version $Id$
 */
public class PortalManagerImpl
	extends AbstractComponent
	implements PortalManager, PortalManagerAspect, Configurable {

    protected PortalManagerAspectChain chain;

    protected ServiceSelector aspectSelector;
    protected ServiceSelector adapterSelector;

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(ServiceManager)
     */
    public void service(ServiceManager serviceManager)
    throws ServiceException {
        super.service(serviceManager);
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
            if ( this.chain != null) {
                this.chain.dispose( this.aspectSelector, this.adapterSelector );
            }
            this.manager.release( this.aspectSelector );
            this.aspectSelector = null;
            this.manager.release( this.adapterSelector );
            this.adapterSelector = null;
        }
        super.dispose();
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
        EventManager eventManager = this.portalService.getEventManager();
        eventManager.processEvents();
    }

    /**
     * @see org.apache.cocoon.portal.PortalManagerAspect#render(org.apache.cocoon.portal.PortalManagerAspectRenderContext, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler, org.apache.avalon.framework.parameters.Parameters)
     */
    public void render(PortalManagerAspectRenderContext context,
                       PortalService                    service,
                       ContentHandler                   ch,
                       Parameters                       parameters)
    throws SAXException {
        final ProfileManager profileManager = this.portalService.getProfileManager();

        // test for ajax request
        final Request req = ObjectModelHelper.getRequest(context.getObjectModel());
        if ( AjaxHelper.isAjaxRequest(req) ) {
            Layout rootLayout = profileManager.getPortalLayout(null, null);
            ch.startDocument();
            XMLUtils.startElement(ch, "coplets");
            final List changed = CopletInstanceDataFeatures.getChangedCopletInstanceDataObjects(service);
            final Iterator i = changed.iterator();
            while ( i.hasNext() ) {
                final CopletInstanceData current = (CopletInstanceData)i.next();
                AttributesImpl a = new AttributesImpl();
                a.addCDATAAttribute("id", current.getId());
                XMLUtils.startElement(ch, "coplet", a);
                final Layout l = CopletInstanceDataFeatures.searchLayout(current.getId(), rootLayout);
                Renderer portalLayoutRenderer = this.portalService.getRenderer( l.getRendererName());
                portalLayoutRenderer.toSAX(l, this.portalService, ch);
                XMLUtils.endElement(ch, "coplet");
            }
            XMLUtils.endElement(ch, "coplets");
            ch.endDocument();
        } else {
            Layout portalLayout = null;

            // check for parameters
            final String copletId = parameters.getParameter(PortalManager.PARAMETER_RENDER_COPLET, null);
            final String layoutId = parameters.getParameter(PortalManager.PARAMETER_RENDER_LAYOUT, null);
            if ( StringUtils.isNotEmpty(copletId) && StringUtils.isNotEmpty(layoutId) ) {
                throw new SAXException("Only one of the paramteters can be specified for rendering: coplet or layout.");
            }
            if ( StringUtils.isNotEmpty(copletId) ) {
                final CopletInstanceData cid = profileManager.getCopletInstanceData(copletId);
                if ( cid != null ) {
                    portalLayout = CopletInstanceDataFeatures.searchLayout(cid.getId(), profileManager.getPortalLayout(null, null));
                }
            } else if ( StringUtils.isNotEmpty(layoutId) ) {
                portalLayout = profileManager.getPortalLayout(null, layoutId);
            } else {
                // first check for a full screen layout
                Layout rootLayout = profileManager.getPortalLayout(null, layoutId);
                portalLayout = LayoutFeatures.getFullScreenInfo(rootLayout);
                if ( portalLayout == null ) {
                    portalLayout = rootLayout;
                }
            }
            if ( portalLayout == null) {
                getLogger().error("No Layout to render");
                return;
            }

            Renderer portalLayoutRenderer = this.portalService.getRenderer( portalLayout.getRendererName());

            ch.startDocument();
            portalLayoutRenderer.toSAX(portalLayout, this.portalService, ch);
            ch.endDocument();            
        }
        // although we should be the last in the queue,
        // let's invoke the next
        context.invokeNext(ch, parameters);
    }
}
