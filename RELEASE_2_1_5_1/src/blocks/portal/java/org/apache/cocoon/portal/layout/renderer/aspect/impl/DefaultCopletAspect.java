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

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
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
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: DefaultCopletAspect.java,v 1.5 2004/04/25 20:09:34 haul Exp $
 */
public class DefaultCopletAspect extends AbstractAspect {

	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.layout.renderer.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
	 */
	public void toSAX(RendererAspectContext context,
                		Layout layout,
                		PortalService service,
                		ContentHandler handler)
	throws SAXException {
        
        XMLUtils.startElement(handler, "content");
        CopletInstanceData cid = ((CopletLayout)layout).getCopletInstanceData();

        final String adapterName = cid.getCopletData().getCopletBaseData().getCopletAdapterName();
        CopletAdapter copletAdapter = null;
        ServiceSelector adapterSelector = null;
        try {
            adapterSelector = (ServiceSelector) this.manager.lookup(CopletAdapter.ROLE + "Selector");
            copletAdapter = (CopletAdapter) adapterSelector.select(adapterName);
            copletAdapter.toSAX(cid, new IncludeXMLConsumer(handler));
        } catch (ServiceException ce) {
            throw new SAXException("Unable to lookup component.", ce);
        } finally {
            if (null != copletAdapter) {
                adapterSelector.release(copletAdapter);
            }
            this.manager.release(adapterSelector);
        }

        XMLUtils.endElement(handler, "content");
        context.invokeNext(layout, service, handler);
	}

}
