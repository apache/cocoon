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

import java.util.Iterator;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.aspect.CompositeLayoutStatus;
import org.apache.cocoon.portal.layout.impl.CompositeLayout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.portal.profile.ProfileManager;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: MaxPageAspect.java,v 1.2 2003/05/08 13:38:11 cziegeler Exp $
 */
public class MaxPageAspect 
    extends AbstractAspect {

	/* (non-Javadoc)
	 * @see org.apache.cocoon.portal.layout.renderer.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
	 */
	public void toSAX(RendererAspectContext context,
                        Layout layout,
                        PortalService service,
                        ContentHandler handler)
    throws SAXException {
        if (layout instanceof CompositeLayout) {

            CompositeLayout compositeLayout = (CompositeLayout) layout;
            Layout maxPageLayout = this.getMaxpageLayout( compositeLayout );

            if (maxPageLayout != null) {
                for (Iterator iter = compositeLayout.getItems().iterator(); iter.hasNext();) {
                    Item item = (Item) iter.next();
                    Layout itemLayout = item.getLayout();
                    if (itemLayout.isStatic() || itemLayout == maxPageLayout) {
                        // FIXME MaxPage
                        //processItem(item, handler, service);
                        continue;
                    }
                    if (itemLayout instanceof CompositeLayout) {
                        Layout maxLayout = this.getMaxpageLayout( (CompositeLayout)itemLayout);
                        if (maxLayout == null) {
                            continue;
                        }
                        // FIXME MaxPage
                        //processItem(item, maxLayout, handler, service);
                    }
                }
            } else {
                context.invokeNext( layout, service, handler );
            }
        } else {
            throw new SAXException("Expecting composite layout, received: " + layout);
        }
    }

    /**
     * @return Layout
     */
    public Layout getMaxpageLayout(CompositeLayout layout) {
        CompositeLayoutStatus status = (CompositeLayoutStatus)this.getStatus( null, ProfileManager.REQUEST_STATUS, layout.getId());
        Layout maxpageLayout = (status == null ? null : status.getMaxpageLayout());
        
        if (maxpageLayout != null)
            return maxpageLayout;
        for (Iterator iter = layout.getItems().iterator(); iter.hasNext();) {
            Item item = (Item) iter.next();
            Layout current = item.getLayout();
            if (current.isStatic() == false && current instanceof CompositeLayout) {
                maxpageLayout = this.getMaxpageLayout((CompositeLayout)current );
                if (maxpageLayout != null) {
                    return maxpageLayout;
                }
            }
        }
        return null;
    
	}

}
