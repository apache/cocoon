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
 * @version CVS $Id: TabContentAspect.java,v 1.13 2004/03/05 13:02:13 bdelacretaz Exp $
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
