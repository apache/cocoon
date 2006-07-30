/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.impl.FrameLayout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
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
 *  <li>{@link org.apache.cocoon.portal.layout.impl.FrameLayout}</li>
 * </ul>
 *
 * @version $Id$
 */
public class FrameAspect extends AbstractCIncludeAspect {

    /**
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
     */
    public void toSAX(RendererAspectContext rendererContext, Layout layout, PortalService service, ContentHandler handler)
    throws SAXException {
        if (!(layout instanceof FrameLayout)) {
            throw new SAXException("Wrong layout type, FrameLayout expected: " + layout.getClass().getName());
        }

        String source = (String)layout.getTemporaryAttribute("frame");
        if (source == null) {
            source = ((FrameLayout) layout).getSource();
        }

        this.createCInclude(source, handler);
    }
}
