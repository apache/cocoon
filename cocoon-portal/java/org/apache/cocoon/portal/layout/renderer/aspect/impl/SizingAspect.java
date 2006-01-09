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
package org.apache.cocoon.portal.layout.renderer.aspect.impl;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletDataFeatures;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.CopletInstanceDataFeatures;
import org.apache.cocoon.portal.event.Event;
import org.apache.cocoon.portal.event.coplet.CopletInstanceSizingEvent;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.LayoutFeatures;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This renderer aspect tests, if a coplet is sizable and/or maxpageable and adds
 * tags holding URIs for switching to currently inactive modes (i.e. maximize or
 * minimize).
 *
 * <h2>Example XML:</h2>
 * <pre>
 *   &lt;minimize-uri&gt;minimize-event&lt;/minimize-uri&gt;
 *   &lt;!-- output from following renderers --&gt;
 *
 * or
 *
 *   &lt;maximize-uri&gt;maximize-event&lt;/maximize-uri&gt;
 *   &lt;!-- processing stops here --&gt;
 *
 * </pre>
 *
 * <h2>Applicable to:</h2>
 * <ul>
 *  <li>{@link org.apache.cocoon.portal.layout.impl.CopletLayout}</li>
 * </ul>
 *
 * @version $Id$
 */
public class SizingAspect extends AbstractAspect {

    /** Is full-screen enabled? */
    protected boolean enableFullScreen;

    /** Is maximized enabled? */
    protected boolean enableMaximized;

	/**
	 * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
	 */
	public void toSAX(RendererAspectContext context,
              	      Layout layout,
                      PortalService service,
                      ContentHandler handler)
	throws SAXException {
        CopletInstanceData cid = ((CopletLayout)layout).getCopletInstanceData();

        boolean showContent = true;

        boolean sizable = CopletDataFeatures.isSizable(cid.getCopletData());

        if ( sizable ) {
            int size = cid.getSize();

            XMLUtils.createElement(handler, "coplet-size", CopletInstanceDataFeatures.sizeToString(size));
            Event event;

            if ( size != CopletInstanceData.SIZE_MINIMIZED ) {
                event = new CopletInstanceSizingEvent(cid, CopletInstanceData.SIZE_MINIMIZED);
                XMLUtils.createElement(handler, "minimize-uri", service.getLinkService().getLinkURI(event));
            }
            if ( size != CopletInstanceData.SIZE_NORMAL) {
                event = new CopletInstanceSizingEvent(cid, CopletInstanceData.SIZE_NORMAL);
                XMLUtils.createElement(handler, "normal-uri", service.getLinkService().getLinkURI(event));
            }
            if ( this.enableMaximized ) {
                if ( size != CopletInstanceData.SIZE_MAXIMIZED ) {
                    event = new CopletInstanceSizingEvent(cid, CopletInstanceData.SIZE_MAXIMIZED);
                    XMLUtils.createElement(handler, "maximize-uri", service.getLinkService().getLinkURI(event));
                }
            }

            if ( this.enableFullScreen ) {
                boolean supportsFullScreen = CopletDataFeatures.supportsFullScreenMode(cid.getCopletData());
                if ( supportsFullScreen ) {
                    final Layout rootLayout = service.getProfileManager().getPortalLayout(null, null);
                    final Layout fullScreenLayout = LayoutFeatures.getFullScreenInfo(rootLayout);
                    if ( fullScreenLayout != null && fullScreenLayout.equals( layout )) {
                        event = new CopletInstanceSizingEvent( cid, CopletInstanceData.SIZE_NORMAL );
                        XMLUtils.createElement(handler, "normal-uri", service.getLinkService().getLinkURI(event));
                    } else {
                        event = new CopletInstanceSizingEvent( cid, CopletInstanceData.SIZE_FULLSCREEN );
                        XMLUtils.createElement(handler, "fullscreen-uri", service.getLinkService().getLinkURI(event));
                    }
                }
            }

            if (!CopletDataFeatures.handlesSizing(cid.getCopletData())
                && size == CopletInstanceData.SIZE_MINIMIZED) {
                showContent = false;
            }
        } 
        if ( showContent ) {
            context.invokeNext(layout, service, handler);
        }
	}

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        super.initialize();
        this.enableFullScreen = this.portalService.getConfigurationAsBoolean(PortalService.CONFIGURATION_FULL_SCREEN_ENABLED, true);
        this.enableMaximized = this.portalService.getConfigurationAsBoolean(PortalService.CONFIGURATION_MAXIMIZED_ENABLED, true);
    }
}
