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
import org.apache.cocoon.portal.event.layout.LayoutRemoveEvent;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Add a tag holding a URI used to remove a layout from the profile.
 * If the layout holds a coplet then the event is only created if
 * the coplet is not mandatory.
 *
 * <h2>Example XML:</h2>
 * <pre>
 *   &lt;remove-uri&gt;layout-remove-event&lt;/remove-uri&gt;
 *   &lt;!-- output from following renderers --&gt;
 * </pre>
 *
 * <h2>Applicable to:</h2>
 * <ul>
 *  <li>{@link org.apache.cocoon.portal.layout.Layout}</li>
 * </ul>
 *
 * @version $Id$
 */
public class RemovableAspect 
    extends AbstractAspect {

	/**
	 * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
	 */
	public void toSAX(RendererAspectContext context,
                      Layout layout,
                      PortalService service,
                      ContentHandler handler)
	throws SAXException {
        if ( layout instanceof CopletLayout ) {
            final CopletInstanceData cid = ((CopletLayout)layout).getCopletInstanceData();
    
            boolean mandatory = CopletDataFeatures.isMandatory(cid.getCopletData());
            if ( !mandatory ) {
                LayoutRemoveEvent lre = new LayoutRemoveEvent(layout);
                XMLUtils.createElement(handler, "remove-uri", service.getLinkService().getLinkURI(lre));
            }
        } else {
            // for any other layout just create the event
            LayoutRemoveEvent lre = new LayoutRemoveEvent(layout);
            XMLUtils.createElement(handler, "remove-uri", 
                                   service.getLinkService().getLinkURI(lre));
        }
        context.invokeNext(layout, service, handler);
	}
}
