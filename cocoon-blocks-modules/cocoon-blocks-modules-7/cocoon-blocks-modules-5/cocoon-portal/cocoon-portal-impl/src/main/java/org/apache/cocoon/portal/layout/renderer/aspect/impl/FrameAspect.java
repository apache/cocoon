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

import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.portal.om.FrameLayout;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutException;
import org.apache.cocoon.portal.om.LayoutFeatures;
import org.apache.cocoon.portal.om.LayoutInstance;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Adds a cinclude tag for a FrameLayout's source to the resulting stream.
 *
 * <h2>Example XML:</h2>
 * <pre>
 *  &lt;xy:z src="coplet://copletID"/&gt;
 * </pre>
 *
 * <h2>Applicable to:</h2>
 * <ul>
 *  <li>{@link org.apache.cocoon.portal.om.FrameLayout}</li>
 * </ul>
 *
 * @version $Id$
 */
public class FrameAspect extends AbstractCIncludeAspect {

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.om.Layout, org.xml.sax.ContentHandler)
     */
    public void toSAX(RendererAspectContext rendererContext, Layout layout,ContentHandler handler)
    throws SAXException, LayoutException {
        LayoutFeatures.checkLayoutClass(layout, FrameLayout.class, true);
        String source = null;
        final LayoutInstance instance = LayoutFeatures.getLayoutInstance(rendererContext.getPortalService(), layout, false);
        if ( instance != null ) {
            source = (String)instance.getTemporaryAttribute(FrameLayout.ATTRIBUTE_SOURCE_ID);
        }
        if (source == null) {
            source = ((FrameLayout) layout).getSource();
        }

        this.createCInclude(source, handler);
        rendererContext.invokeNext(layout, handler);
    }
}
