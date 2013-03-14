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

import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.portal.PortalService;
import org.apache.cocoon.portal.coplet.CopletInstanceData;
import org.apache.cocoon.portal.layout.Layout;
import org.apache.cocoon.portal.layout.impl.CopletLayout;
import org.apache.cocoon.portal.layout.renderer.aspect.RendererAspectContext;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * This aspect streams a cinclude statement into the stream that
 * will include the coplet using the coplet protocol.
 * 
 * <h2>Resulting XML:</h2>
 * <pre>
 * &lt;content&gt;
 *  &lt;xy:z src="coplet://copletID"/&gt;
 * &lt;/content&gt;
 * </pre>
 * where <code>xy</code> is the CInclude namespace and <code>z</code> is
 * the CInclude tagname.
 * By default, the cinclude statetement is surrounded by a "content" element;
 * this can be further controlled by parameters (see below).
 * 
 * <h2>Applicable to:</h2>
 * <ul>
 *  <li>{@link org.apache.cocoon.portal.layout.impl.CopletLayout}</li>
 * </ul>
 *
 * <h2>Parameters</h2>
 * <table><tbody>
 *   <tr><th>root-tag</th><td>Should a tag enclosing the cinclude be generated? (Default is true)</td>
 *      <td></td><td>boolean</td><td><code>true</code></td></tr>
 *   <tr><th>tag-name</th><td>Name of enclosing tag.</td>
 *      <td></td><td>String</td><td><code>"content"</code></td></tr>
 * </tbody></table>
 * 
 * @author <a href="mailto:cziegeler@s-und-n.de">Carsten Ziegeler</a>
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * 
 * @version CVS $Id$
 */
public class CIncludeCopletAspect 
    extends AbstractCIncludeAspect {

    /* (non-Javadoc)
	 * @see org.apache.cocoon.portal.layout.renderer.RendererAspect#toSAX(org.apache.cocoon.portal.layout.renderer.RendererAspectContext, org.apache.cocoon.portal.layout.Layout, org.apache.cocoon.portal.PortalService, org.xml.sax.ContentHandler)
	 */
	public void toSAX(RendererAspectContext context,
                		Layout layout,
                		PortalService service,
                		ContentHandler handler)
	throws SAXException {
        final PreparedConfiguration config = (PreparedConfiguration)context.getAspectConfiguration();
        final CopletInstanceData cid = ((CopletLayout)layout).getCopletInstanceData();
        if (!(context.isRendering())) {
            context.invokeNext(layout, service, handler);
        }
        if ( config.rootTag) {
            XMLUtils.startElement(handler, config.tagName);
        }

        this.createCInclude("coplet://" + cid.getId(), handler);

        if ( config.rootTag) {
            XMLUtils.endElement(handler, config.tagName);
        }

        context.invokeNext(layout, service, handler);
	}

    protected static class PreparedConfiguration {

        public String tagName;
        public boolean rootTag;
        
        public void takeValues(PreparedConfiguration from) {
            this.tagName = from.tagName;
            this.rootTag = from.rootTag;
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.portal.layout.renderer.aspect.RendererAspect#prepareConfiguration(org.apache.avalon.framework.parameters.Parameters)
     */
    public Object prepareConfiguration(Parameters configuration) 
    throws ParameterException {
        PreparedConfiguration pc = new PreparedConfiguration();
        pc.tagName = configuration.getParameter("tag-name", "content");
        pc.rootTag = configuration.getParameterAsBoolean("root-tag", true);
        return pc;
    }
    
}
