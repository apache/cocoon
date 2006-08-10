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
import org.apache.cocoon.portal.coplet.CopletInstance;
import org.apache.cocoon.portal.coplet.adapter.CopletAdapter;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This aspect directly invokes the coplet adapter to stream out the coplet content.
 * An alternative solution is to generate only cinclude tags by using the
 * {@link org.apache.cocoon.portal.layout.renderer.aspect.impl.CIncludeCopletAspect}
 * and include the coplet contents later. That would allow caching up to the point 
 * of the cinclude transformer.
 *
 * <h2>Example XML:</h2>
 * <pre>
 * &lt;content&gt;
 *   &lt;!-- content streamed from coplet --&gt;
 * &lt;/content&gt;
 * </pre>
 *
 * <h2>Applicable to:</h2>
 * <ul>
 *  <li>{@link org.apache.cocoon.portal.layout.impl.CopletLayout}</li>
 * </ul>
 *
 * @version $Id$
 */
public class DefaultCopletAspect extends AbstractAspect {

	/**
	 * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
	 */
	public void toSAX(RendererAspectContext rendererContext,
                      Layout layout,
                      PortalService service,
                      ContentHandler handler)
	throws SAXException {
        XMLUtils.startElement(handler, "content");
        CopletInstance cid = this.getCopletInstance(((CopletLayout)layout).getCopletInstanceId());

        final String adapterName = cid.getCopletDefinition().getCopletType().getCopletAdapterName();
        CopletAdapter copletAdapter = service.getCopletAdapter(adapterName);
        copletAdapter.toSAX(cid, new IncludeXMLConsumer(handler));

        XMLUtils.endElement(handler, "content");
        rendererContext.invokeNext(layout, service, handler);
	}
}
