/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: HistoryAspect.java,v 1.1 2003/12/12 10:13:34 cziegeler Exp $
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
