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

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.ajax.AjaxHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.portal.LayoutException;
import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.PortalManager;
import org.apache.cocoon.portal.event.EventManager;
import org.apache.cocoon.portal.layout.renderer.Renderer;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.CopletInstanceFeatures;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutFeatures;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.apache.cocoon.portal.services.aspects.PortalManagerAspect;
import org.apache.cocoon.portal.services.aspects.PortalManagerAspectPrepareContext;
import org.apache.cocoon.portal.services.aspects.PortalManagerAspectRenderContext;
import org.apache.cocoon.portal.services.aspects.impl.support.PortalManagerAspectContextImpl;
import org.apache.cocoon.portal.services.aspects.support.AspectChain;
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

    protected AspectChain chain;

    /**
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        if ( this.manager != null ) {
            if ( this.chain != null) {
                this.chain.dispose( this.manager );
            }
        }
        super.dispose();
    }

    /**
     * @see org.apache.cocoon.portal.PortalManager#process()
     */
    public void process()
    throws PortalException {
        PortalManagerAspectContextImpl aspectContext =
            new PortalManagerAspectContextImpl(this.portalService, this.chain);
        aspectContext.invokeNext();
    }

	/**
	 * @see PortalManager#showPortal(ContentHandler, Properties)
	 */
	public void showPortal(ContentHandler contentHandler, Properties properties) 
    throws SAXException {
        PortalManagerAspectContextImpl aspectContext =
            new PortalManagerAspectContextImpl(this.portalService, this.chain);
        aspectContext.invokeNext(contentHandler, properties);
	}

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration conf) throws ConfigurationException {
        try {
            this.chain = new AspectChain(PortalManagerAspect.class);
            this.chain.configure(this.manager, conf);
            this.chain.addAspect(this, null);
        } catch (PortalException pe) {
            throw new ConfigurationException("Unable to configure portal manager aspects.", pe);
        }
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.PortalManagerAspect#prepare(org.apache.cocoon.portal.services.aspects.PortalManagerAspectPrepareContext)
     */
    public void prepare(PortalManagerAspectPrepareContext renderContext) throws PortalException {
        EventManager eventManager = this.portalService.getEventManager();
        eventManager.processEvents();
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.PortalManagerAspect#render(org.apache.cocoon.portal.services.aspects.PortalManagerAspectRenderContext, org.xml.sax.ContentHandler, java.util.Properties)
     */
    public void render(PortalManagerAspectRenderContext renderContext,
                       ContentHandler                   ch,
                       Properties                       properties)
    throws SAXException {
        final ProfileManager profileManager = this.portalService.getProfileManager();

        final Request req = ObjectModelHelper.getRequest(this.portalService.getProcessInfoProvider().getObjectModel());
        // check for render parameters
        // if a parameter for a layout or a coplet is defined
        // then only this coplet or layout object is rendered
        final String copletId = (properties == null ? null : properties.getProperty(PortalManager.PROPERTY_RENDER_COPLET, null));
        final String layoutId = (properties == null ? null : properties.getProperty(PortalManager.PROPERTY_RENDER_LAYOUT, null));
        if ( StringUtils.isNotEmpty(copletId) && StringUtils.isNotEmpty(layoutId) ) {
            throw new SAXException("Only one of the paramteters can be specified for rendering: coplet or layout.");
        }
        Layout portalLayout = null;

        if ( StringUtils.isNotEmpty(copletId) ) {
            final CopletInstance cid = profileManager.getCopletInstance(copletId);
            if ( cid != null ) {
                portalLayout = LayoutFeatures.searchLayout(this.portalService, cid.getId(), profileManager.getLayout(null));
                if ( portalLayout == null) {
                    getLogger().error("No Layout to render for coplet instance with id: " + copletId);
                    return;
                }
            }
        } else if ( StringUtils.isNotEmpty(layoutId) ) {
            portalLayout = profileManager.getLayout(layoutId);
            if ( portalLayout == null) {
                getLogger().error("No Layout to render for layout instance with id: " + layoutId);
                return;
            }
        }

        ch.startDocument();

        // If no parameter is specified test for ajax request which will
        // only render the changed coplets
        if ( portalLayout == null && AjaxHelper.isAjaxRequest(req) ) {
            Layout rootLayout = profileManager.getLayout(null);
            XMLUtils.startElement(ch, "coplets");
            final List changed = CopletInstanceFeatures.getChangedCopletInstanceDataObjects(this.portalService);
            final Iterator i = changed.iterator();
            while ( i.hasNext() ) {
                final CopletInstance current = (CopletInstance)i.next();
                final AttributesImpl a = new AttributesImpl();
                a.addCDATAAttribute("id", current.getId());
                XMLUtils.startElement(ch, "coplet", a);
                final Layout l = LayoutFeatures.searchLayout(this.portalService, current.getId(), rootLayout);
                Renderer portalLayoutRenderer = this.portalService.getRenderer( this.portalService.getLayoutFactory().getRendererName(l));
                try {
                    portalLayoutRenderer.toSAX(l, this.portalService, ch);
                } catch (LayoutException e) {
                    throw new SAXException(e);
                }
                XMLUtils.endElement(ch, "coplet");
            }
            XMLUtils.endElement(ch, "coplets");
        } else {
            if ( StringUtils.isNotEmpty(copletId) ) {
                XMLUtils.startElement(ch, "coplets");
                final AttributesImpl a = new AttributesImpl();
                a.addCDATAAttribute("id", copletId);
                XMLUtils.startElement(ch, "coplet", a);
            } else if ( StringUtils.isNotEmpty(layoutId) ) {
                final AttributesImpl a = new AttributesImpl();
                a.addCDATAAttribute("id", layoutId);
                XMLUtils.startElement(ch, "layout", a);
            }

            // if no render parameter is specified we render the whole page or just the full screen coplet
            if ( portalLayout == null ) {
                // first check for a full screen layout
                portalLayout = LayoutFeatures.getFullScreenInfo(this.portalService);
                if ( portalLayout == null ) {
                    portalLayout = profileManager.getLayout(null);
                }
            }

            try {
                final Renderer portalLayoutRenderer = this.portalService.getRenderer( this.portalService.getLayoutFactory().getRendererName(portalLayout));

                portalLayoutRenderer.toSAX(portalLayout, this.portalService, ch);
            } catch (LayoutException e) {
                throw new SAXException(e);
            }
            if ( StringUtils.isNotEmpty(copletId) ) {
                XMLUtils.endElement(ch, "coplet");
                XMLUtils.endElement(ch, "coplets");
            } else if ( StringUtils.isNotEmpty(layoutId) ) {
                XMLUtils.endElement(ch, "layout");
            }
        }

        ch.endDocument();
        // although we should be the last in the queue,
        // let's invoke the next
        renderContext.invokeNext(ch, properties);
    }

    /**
     * @see org.apache.cocoon.portal.PortalManager#register(org.apache.cocoon.portal.services.aspects.PortalManagerAspect)
     */
    public void register(PortalManagerAspect aspect) {
        try {
            this.chain.addAspect(aspect, null, 0);
        } catch (PortalException pe) {
            final IllegalArgumentException e = new IllegalArgumentException("Unable to add portal manager aspects.");
            e.initCause(pe);
            throw e;
        }
    }
}
