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

import java.util.Collections;
import java.util.Iterator;

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.aspect.impl.DefaultAspectDescription;
import org.apache.cocoon.portal.event.impl.ChangeAspectDataEvent;
import org.apache.cocoon.portal.layout.CompositeLayout;
import org.apache.cocoon.portal.layout.Item;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.NamedItem;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.xml.AttributesImpl;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id: TabContentAspect.java,v 1.12 2003/12/12 08:17:27 cziegeler Exp $
 */
public class TabContentAspect 
    extends CompositeContentAspect {

    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.renderer.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
     */
    public void toSAX(RendererAspectContext context,
                        Layout layout,
                        PortalService service,
                        ContentHandler handler)
    throws SAXException {
        if (layout instanceof CompositeLayout) {
            TabPreparedConfiguration config = (TabPreparedConfiguration)context.getAspectConfiguration();

            if ( config.rootTag ) {
                XMLUtils.startElement(handler, config.tagName);
            }

            AttributesImpl attributes = new AttributesImpl();
            CompositeLayout tabLayout = (CompositeLayout) layout;

            // selected tab
            Integer data = (Integer) layout.getAspectData(config.aspectName);
            int selected = data.intValue();
            
            // loop over all tabs
            for (int j = 0; j < tabLayout.getSize(); j++) {
                Item tab = tabLayout.getItem(j);

                // open named-item tag
                attributes.clear();
                if ( tab instanceof NamedItem ) {
                    attributes.addCDATAAttribute("name", String.valueOf(((NamedItem)tab).getName()));
                }
                if (j == selected) {
                    attributes.addCDATAAttribute("selected", "true");
                } else {
                    ChangeAspectDataEvent event = new ChangeAspectDataEvent(tabLayout, "tab", new Integer(j));
                    attributes.addCDATAAttribute("parameter", service.getComponentManager().getLinkService().getLinkURI(event));
                }
                XMLUtils.startElement(handler, "named-item", attributes);
                if (j == selected) {
                    this.processLayout(tab.getLayout(), service, handler);
                }
                // close named-item tag
                XMLUtils.endElement(handler, "named-item");
            }

            if ( config.rootTag ) {
                XMLUtils.endElement(handler, config.tagName);
            }
        } else {
            throw new SAXException("Wrong layout type, TabLayout expected: " + layout.getClass().getName());
        }


    }

    /**
     * Return the aspects required for this renderer
     * @return An iterator for the aspect descriptions or null.
     */
    public Iterator getAspectDescriptions(Object configuration) {
        TabPreparedConfiguration pc = (TabPreparedConfiguration)configuration;
        
        DefaultAspectDescription desc = new DefaultAspectDescription();
        desc.setName(pc.aspectName);
        desc.setClassName("java.lang.Integer");
        desc.setPersistence(pc.store);
        desc.setAutoCreate(true);
        
        return Collections.singletonList(desc).iterator();
    }

    protected class TabPreparedConfiguration extends PreparedConfiguration {
        public String aspectName;
        public String store;
        
        public void takeValues(TabPreparedConfiguration from) {
            super.takeValues(from);
            this.aspectName = from.aspectName;
            this.store = from.store;
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#prepareConfiguration(org.apache.avalon.framework.parameters.Parameters)
     */
    public Object prepareConfiguration(Parameters configuration) 
    throws ParameterException {
        TabPreparedConfiguration pc = new TabPreparedConfiguration();
        pc.takeValues((PreparedConfiguration)super.prepareConfiguration(configuration));
        pc.aspectName = configuration.getParameter("aspect-name", "tab");
        pc.store = configuration.getParameter("store");
        return pc;
    }

}
