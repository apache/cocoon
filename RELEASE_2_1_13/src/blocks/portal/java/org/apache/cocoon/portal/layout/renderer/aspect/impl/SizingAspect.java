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

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.coplet.status.SizingStatus;
import org.apache.cocoon.portal.event.impl.ChangeCopletInstanceAspectDataEvent;
import org.apache.cocoon.portal.layout.Layout;
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
 * TODO: make the names of the aspects to test configurable
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id$
 */
public class SizingAspect extends AbstractAspect {

	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.layout.renderer.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
	 */
	public void toSAX(RendererAspectContext context,
                		Layout layout,
                		PortalService service,
                		ContentHandler handler)
	throws SAXException {
        
        CopletInstanceData cid = ((CopletLayout)layout).getCopletInstanceData();

        boolean showContent = true;
        
        boolean sizable = ((Boolean)cid.getCopletData().getAspectData("sizable")).booleanValue();
        Integer size = null;
        
        if ( sizable ) {
            size = (Integer)cid.getAspectData("size");
            if ( size == null ) {
                size = SizingStatus.STATUS_MAXIMIZED;
            }

            ChangeCopletInstanceAspectDataEvent event;    

            if ( size.equals(SizingStatus.STATUS_MAXIMIZED) ) {
                event = new ChangeCopletInstanceAspectDataEvent(cid, "size", SizingStatus.STATUS_MINIMIZED);
                XMLUtils.createElement(handler, "minimize-uri", service.getComponentManager().getLinkService().getLinkURI(event));
            }

            if ( size.equals(SizingStatus.STATUS_MINIMIZED)) {
                event = new ChangeCopletInstanceAspectDataEvent(cid, "size", SizingStatus.STATUS_MAXIMIZED);
                XMLUtils.createElement(handler, "maximize-uri", service.getComponentManager().getLinkService().getLinkURI(event));
            }
            
            if (size.equals(SizingStatus.STATUS_MINIMIZED)) {
                showContent = false;
            }
        } 
/*        boolean maxPageable = ((Boolean)cid.getCopletData().getAspectData("maxpageable")).booleanValue();
        if ( maxPageable ) {
            if ( size == null ) {
                size = (Integer)cid.getAspectData("size");
                if ( size == null ) {
                    size = SizingStatus.STATUS_MAXIMIZED;
                }
            }
            ChangeCopletInstanceAspectDataEvent event;    

            if ( size == SizingStatus.STATUS_MAXIMIZED) {
                event = new ChangeCopletInstanceAspectDataEvent(cid, "size", SizingStatus.STATUS_MAXPAGED);
                XMLUtils.createElement(handler, "maxpage-uri", service.getComponentManager().getLinkService().getLinkURI(event));
            }
            if ( size == SizingStatus.STATUS_MAXPAGED) {
                event = new ChangeCopletInstanceAspectDataEvent(cid, "size", SizingStatus.STATUS_MAXIMIZED);
                XMLUtils.createElement(handler, "minpage-uri", service.getComponentManager().getLinkService().getLinkURI(event));
            }
        }
*/               
        if ( showContent ) {
            context.invokeNext(layout, service, handler);
        }
	}

}
