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
package org.apache.cocoon.portal.layout.renderer.aspect.impl;

import org.apache.cocoon.portal.event.layout.RemoveLayoutEvent;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.portal.om.CopletDefinitionFeatures;
import org.apache.cocoon.portal.om.CopletInstance;
import org.apache.cocoon.portal.om.CopletLayout;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutException;
import org.apache.cocoon.portal.util.XMLUtils;
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
 *  <li>{@link org.apache.cocoon.portal.om.Layout}</li>
 * </ul>
 *
 * @version $Id$
 */
public class RemovableAspect
    extends AbstractAspect {

	/**
	 * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.om.Layout, org.xml.sax.ContentHandler)
	 */
	public void toSAX(RendererAspectContext rendererContext,
                      Layout layout,
                      ContentHandler handler)
	throws SAXException, LayoutException {
        if ( layout instanceof CopletLayout ) {
            final CopletInstance cid = this.getCopletInstance(((CopletLayout)layout).getCopletInstanceId());

            boolean mandatory = CopletDefinitionFeatures.isMandatory(cid.getCopletDefinition());
            if ( !mandatory ) {
                RemoveLayoutEvent lre = new RemoveLayoutEvent(layout);
                XMLUtils.startElement(handler, "remove-uri");
                XMLUtils.data(handler, rendererContext.getPortalService().getLinkService().getLinkURI(lre));
                XMLUtils.endElement(handler, "remove-uri");
            }
        } else {
            // for any other layout just create the event
            RemoveLayoutEvent lre = new RemoveLayoutEvent(layout);
            XMLUtils.startElement(handler, "remove-uri");
            XMLUtils.data(handler, rendererContext.getPortalService().getLinkService().getLinkURI(lre));
            XMLUtils.endElement(handler, "remove-uri");
        }
        rendererContext.invokeNext(layout, handler);
	}
}
