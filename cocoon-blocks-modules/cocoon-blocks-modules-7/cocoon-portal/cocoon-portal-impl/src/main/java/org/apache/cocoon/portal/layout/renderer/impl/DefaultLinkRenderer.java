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
package org.apache.cocoon.portal.layout.renderer.impl;

import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutException;
import org.apache.cocoon.portal.om.LayoutFeatures;
import org.apache.cocoon.portal.om.LayoutInstance;
import org.apache.cocoon.portal.om.LinkLayout;
import org.apache.cocoon.portal.om.Renderer;
import org.apache.cocoon.portal.util.AbstractBean;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Include a linked layout in the generated XML.
 *
 * <h2>Applicable to:</h2>
 * <ul>
 *  <li>{@link org.apache.cocoon.portal.om.LinkLayout}</li>
 * </ul>
 *
 * @version $Id$
 */
public class DefaultLinkRenderer
    extends AbstractBean
    implements Renderer {

    /**
     * @see org.apache.cocoon.portal.om.Renderer#toSAX(org.apache.cocoon.portal.om.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
     */
    public void toSAX(Layout layout, PortalService service, ContentHandler handler)
    throws SAXException, LayoutException {
        LayoutFeatures.checkLayoutClass(layout, LinkLayout.class, true);
        String layoutId = null;
        final LayoutInstance instance = LayoutFeatures.getLayoutInstance(service, layout, false);
        if ( instance != null ) {
            layoutId = (String)instance.getTemporaryAttribute(LinkLayout.ATTRIBUTE_LAYOUT_ID);
        }
        if ( layoutId == null){
            // get default values
            layoutId = ((LinkLayout)layout).getLayoutId();
        }
        final Layout linkedLayout = service.getProfileManager().getLayout(layoutId);
        linkedLayout.getRenderer().toSAX(linkedLayout, service, handler);
    }
}
