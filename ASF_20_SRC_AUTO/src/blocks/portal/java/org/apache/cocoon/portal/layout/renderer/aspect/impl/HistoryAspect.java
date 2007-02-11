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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.acting.helpers.LayoutEventDescription;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/**
 * Save the current state of the layout into the session
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * 
 * @version CVS $Id: HistoryAspect.java,v 1.3 2004/03/05 13:02:13 bdelacretaz Exp $
 */
public class HistoryAspect 
    extends AbstractAspect {

    /**
     * Add the values to the state
     * @param id
     * @param state
     * @param values
     */
    protected void addValues(String id, List state, Map values, String prefix) {
        final Iterator iter = values.entrySet().iterator();
        while ( iter.hasNext() ) {
            final Map.Entry entry = (Map.Entry)iter.next();
            final String path = prefix + entry.getKey();
            LayoutEventDescription led = new LayoutEventDescription();
            led.path = path;
            led.layoutId = id;
            led.data = entry.getValue();
            state.add(led);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
     */
    public void toSAX(RendererAspectContext context,
                    Layout layout,
                    PortalService service,
                    ContentHandler handler)
    throws SAXException {
        if ( layout.getId() != null ) {
            final Request request = ObjectModelHelper.getRequest(context.getObjectModel());
            final Session session = request.getSession(false);
            if ( session != null ) {
                List history = (List)session.getAttribute("portal-history");
                if ( history == null ) {
       
                    history = new ArrayList();
                }
                List state = (List)request.getAttribute("portal-history");
                if ( state == null ) {
                    state = new ArrayList();
                    request.setAttribute("portal-history", state);
                    history.add(state);
                }
                
                this.addValues(layout.getId(), state, layout.getAspectDatas(), "aspectDatas/");
                this.addValues(layout.getId(), state, layout.getParameters(), "parameters/");
                
                // are we a coplet layout
                if ( layout instanceof CopletLayout ) {
                    CopletInstanceData cid = ((CopletLayout)layout).getCopletInstanceData();
                    this.addValues(cid.getId(), state, cid.getAspectDatas(), "aspectDatas/");
                    this.addValues(cid.getId(), state, cid.getAttributes(), "attributes/");
                }
                session.setAttribute("portal-history", history);
            }
        }
        context.invokeNext(layout, service, handler);
    }

}
