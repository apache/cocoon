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
package org.apache.cocoon.portal.services.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.cocoon.portal.PortalException;
import org.apache.cocoon.portal.PortalRuntimeException;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.CopletInstanceFeatures;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutException;
import org.apache.cocoon.portal.om.LayoutFeatures;
import org.apache.cocoon.portal.om.Renderer;
import org.apache.cocoon.portal.services.PortalManager;
import org.apache.cocoon.portal.services.ProfileManager;
import org.apache.cocoon.portal.services.aspects.AspectChain;
import org.apache.cocoon.portal.services.aspects.RequestProcessorAspect;
import org.apache.cocoon.portal.services.aspects.RequestProcessorAspectContext;
import org.apache.cocoon.portal.services.aspects.ResponseProcessorAspect;
import org.apache.cocoon.portal.services.aspects.ResponseProcessorAspectContext;
import org.apache.cocoon.portal.services.aspects.impl.support.RequestProcessorAspectContextImpl;
import org.apache.cocoon.portal.services.aspects.impl.support.ResponseProcessorAspectContextImpl;
import org.apache.cocoon.portal.services.aspects.support.AspectChainImpl;
import org.apache.cocoon.portal.util.AbstractBean;
import org.apache.cocoon.portal.util.XMLUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 *
 * @version $Id$
 */
public class DefaultPortalManager
	extends AbstractBean
	implements PortalManager, RequestProcessorAspect, ResponseProcessorAspect {

    public static final String BU_NSURI = "http://apache.org/cocoon/browser-update/1.0";

    /** The aspect chain for additional request processing. */
    protected AspectChainImpl requestChain;

    /** The aspect chain for additional response processing. */
    protected AspectChainImpl responseChain;

    public DefaultPortalManager() {
        this.setRequestAspectChain(new AspectChainImpl(RequestProcessorAspect.class));
        this.setResponseAspectChain(new AspectChainImpl(ResponseProcessorAspect.class));
    }

    /**
     * Set the request aspect chain.
     * @param a A chain.
     */
    public void setRequestAspectChain(AspectChainImpl a) {
        this.requestChain = a;
        try {
            this.requestChain.addAspect(this, null);
        } catch (PortalException e) {
            throw new PortalRuntimeException("Unable to add DefaultPortalManager to request aspect chain.", e);
        }
    }

    /**
     * Set the response aspect chain.
     * @param a A chain.
     */
    public void setResponseAspectChain(AspectChainImpl a) {
        this.responseChain = a;
        try {
            this.responseChain.addAspect(this, null);
        } catch (PortalException e) {
            throw new PortalRuntimeException("Unable to add DefaultPortalManager to response aspect chain.", e);
        }
    }

    /**
     * @see org.apache.cocoon.portal.services.PortalManager#process()
     */
    public void process()
    throws PortalException {
        RequestProcessorAspectContextImpl aspectContext =
            new RequestProcessorAspectContextImpl(this.portalService, this.requestChain);
        aspectContext.invokeNext();
    }

	/**
	 * @see PortalManager#render(ContentHandler, Properties)
	 */
	public void render(ContentHandler contentHandler, Properties properties)
    throws SAXException {
        ResponseProcessorAspectContextImpl aspectContext =
            new ResponseProcessorAspectContextImpl(this.portalService, this.responseChain);
        aspectContext.invokeNext(contentHandler, properties);
	}

    /**
     * @see org.apache.cocoon.portal.services.aspects.RequestProcessorAspect#process(org.apache.cocoon.portal.services.aspects.RequestProcessorAspectContext)
     */
    public void process(RequestProcessorAspectContext rpContext) {
        // by defaut, we have nothing to do, except invoking the next
        rpContext.invokeNext();
    }

    /**
     * @see org.apache.cocoon.portal.services.aspects.ResponseProcessorAspect#render(org.apache.cocoon.portal.services.aspects.ResponseProcessorAspectContext, org.xml.sax.ContentHandler, java.util.Properties)
     */
    public void render(ResponseProcessorAspectContext renderContext,
                       ContentHandler                 ch,
                       Properties                     properties)
    throws SAXException {
        final ProfileManager profileManager = this.portalService.getProfileManager();

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
        if ( portalLayout == null && portalService.getRequestContext().isAjaxRequest() ) {
            Layout rootLayout = profileManager.getLayout(null);
            ch.startPrefixMapping("bu", BU_NSURI);
            ch.startElement(BU_NSURI, "document", "bu:document", XMLUtils.EMPTY_ATTRIBUTES);

            final List changed = CopletInstanceFeatures.getChangedCopletInstanceDataObjects(this.portalService);
            final Iterator i = changed.iterator();
            while ( i.hasNext() ) {
                final CopletInstance current = (CopletInstance)i.next();
                final AttributesImpl a = new AttributesImpl();
                XMLUtils.addCDATAAttribute(a, "id", current.getId());
                XMLUtils.startElement(ch, "coplet", a);
                final Layout l = LayoutFeatures.searchLayout(this.portalService, current.getId(), rootLayout);
                Renderer portalLayoutRenderer = l.getRenderer();
                try {
                    portalLayoutRenderer.toSAX(l, this.portalService, ch);
                } catch (LayoutException e) {
                    throw new SAXException(e);
                }
                XMLUtils.endElement(ch, "coplet");
            }
            ch.endElement(BU_NSURI, "document", "bu:document");
            ch.endPrefixMapping("bu");
        } else {
            if ( StringUtils.isNotEmpty(copletId) ) {
                ch.startPrefixMapping("bu", BU_NSURI);
                ch.startElement(BU_NSURI, "document", "bu:document", XMLUtils.EMPTY_ATTRIBUTES);

                final AttributesImpl a = new AttributesImpl();
                XMLUtils.addCDATAAttribute(a, "id", copletId);
                XMLUtils.startElement(ch, "coplet", a);
            } else if ( StringUtils.isNotEmpty(layoutId) ) {
                ch.startPrefixMapping("bu", BU_NSURI);
                ch.startElement(BU_NSURI, "document", "bu:document", XMLUtils.EMPTY_ATTRIBUTES);

                final AttributesImpl a = new AttributesImpl();
                XMLUtils.addCDATAAttribute(a, "id", layoutId);
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
                final Renderer portalLayoutRenderer = portalLayout.getRenderer();

                portalLayoutRenderer.toSAX(portalLayout, this.portalService, ch);
            } catch (LayoutException e) {
                throw new SAXException(e);
            }
            if ( StringUtils.isNotEmpty(copletId) ) {
                XMLUtils.endElement(ch, "coplet");
                ch.endElement(BU_NSURI, "document", "bu:document");
                ch.endPrefixMapping("bu");
            } else if ( StringUtils.isNotEmpty(layoutId) ) {
                XMLUtils.endElement(ch, "layout");
                ch.endElement(BU_NSURI, "document", "bu:document");
                ch.endPrefixMapping("bu");
            }
        }

        ch.endDocument();
        // although we should be the last in the queue,
        // let's invoke the next
        renderContext.invokeNext(ch, properties);
    }

    /**
     * @see org.apache.cocoon.portal.services.PortalManager#getRequestProcessorAspectChain()
     */
    public AspectChain getRequestProcessorAspectChain() {
        return this.requestChain;
    }

    /**
     * @see org.apache.cocoon.portal.services.PortalManager#getResponseProcessorAspectChain()
     */
    public AspectChain getResponseProcessorAspectChain() {
        return this.responseChain;
    }
}
