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
import org.apache.cocoon.portal.layout.renderer.aspect.impl.support.RendererAspectChain;
import org.apache.cocoon.portal.layout.renderer.aspect.impl.support.RendererContextImpl;
import org.apache.cocoon.portal.om.Layout;
import org.apache.cocoon.portal.om.LayoutException;
import org.apache.cocoon.portal.om.Renderer;
import org.apache.cocoon.portal.util.AbstractBean;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Container for chain of aspect renderers. All aspect renderers are applied in order
 * of appearance.
 *
 * <h2>Configuration</h2>
 * <table><tbody>
 * <tr><th>aspects</th><td>List of aspect renderers to apply. See 
 *      {@link org.apache.cocoon.portal.layout.renderer.aspect.impl.support.RendererAspectChain}</td>
 *      <td></td><td>Configuration</td><td><code>EmptyConfiguration</code></td></tr>
 * </tbody></table>
 *
 * @version $Id$
 */
public class AspectRenderer
    extends AbstractBean
    implements Renderer {

    /** The aspect chain for rendering. */
    protected RendererAspectChain chain;

    /**
     * Set the event chain.
     * @param a A chain.
     */
    public void setAspectChain(RendererAspectChain a) {
        this.chain = a;
    }

    /**
     * Stream out raw layout 
     */
    public void toSAX(Layout layout, PortalService service, ContentHandler handler)
    throws SAXException, LayoutException {
        final RendererContextImpl renderContext = new RendererContextImpl(service, this.chain);
        renderContext.invokeNext(layout, handler);
    }
}
