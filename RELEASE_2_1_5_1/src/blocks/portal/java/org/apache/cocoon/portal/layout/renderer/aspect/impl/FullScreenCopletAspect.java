/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.event.impl.FullScreenCopletEvent;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Includes a tag containing a URI that is connected with a fullscreen
 * display of a coplet. If fullscreen is explicitly unsupported, no tag
 * will be created. Otherwise, it depends on the current layout being the
 * fullscreen layout or not whether the URI contains an event that switches
 * to this layout or not.
 * 
 * <h2>Example XML:</h2>
 * <pre>
 *   &lt;fullscreen-uri&gt;fullscreen-event-if-supported&lt;/fullscreen-uri&gt;
 *   &lt;!-- output from following renderers --&gt;
 * </pre>
 * 
 * <h2>Applicable to:</h2>
 * <ul>
 *  <li>{@link org.apache.cocoon.portal.layout.impl.CopletLayout}</li>
 * </ul>
 * 
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: FullScreenCopletAspect.java,v 1.8 2004/04/25 20:09:34 haul Exp $
 */
public class FullScreenCopletAspect extends AbstractAspect {

	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.layout.renderer.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
	 */
	public void toSAX(RendererAspectContext context,
                		Layout layout,
                		PortalService service,
                		ContentHandler handler)
	throws SAXException {
        
        CopletInstanceData cid = ((CopletLayout)layout).getCopletInstanceData();

        Boolean supportsFullScreen = (Boolean)cid.getCopletData().getAspectData("full-screen");
        if ( supportsFullScreen == null || supportsFullScreen.equals(Boolean.TRUE) ) {
            final Layout fullScreenLayout = service.getComponentManager().getProfileManager().getEntryLayout();
            if ( fullScreenLayout != null && fullScreenLayout.equals( layout )) {
                FullScreenCopletEvent event = new FullScreenCopletEvent( cid, null );
                XMLUtils.createElement(handler, "fullscreen-uri", service.getComponentManager().getLinkService().getLinkURI(event));
            } else {
                FullScreenCopletEvent event = new FullScreenCopletEvent( cid, layout );
                XMLUtils.createElement(handler, "fullscreen-uri", service.getComponentManager().getLinkService().getLinkURI(event));
            }
        }
        context.invokeNext(layout, service, handler);
	}

}
